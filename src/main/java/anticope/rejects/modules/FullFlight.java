package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import com.google.common.collect.Streams;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.stream.Stream;

public class FullFlight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAntiKick = settings.createGroup("防踢");

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("速度")
            .description("飞行时的速度。")
            .defaultValue(0.3)
            .min(0.0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> verticalSpeedMatch = sgGeneral.add(new BoolSetting.Builder()
            .name("垂直速度匹配")
            .description("将垂直速度与水平速度匹配，否则使用原版比例。")
            .defaultValue(false)
            .build()
    );

    private final Setting<AntiKickMode> antiKickMode = sgAntiKick.add(new EnumSetting.Builder<AntiKickMode>()
            .name("模式")
            .description("防踢模式。")
            .defaultValue(AntiKickMode.PaperNew)
            .build()
    );

    public FullFlight() {
        super(MeteorRejectsAddon.CATEGORY, "全速飞行", "全速飞行。");
    }

    private double calculateGround() {
        for (double ground = mc.player.getY(); ground > 0D; ground -= 0.05) {
            Box box = mc.player.getBoundingBox();
            Box adjustedBox = box.offset(0, ground - mc.player.getY(), 0);

            Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

            if (blockCollisions.findAny().isPresent()) return ground + 0.05;
        }

        return 0F;
    }

    // Copied from ServerPlayNetworkHandler#isEntityOnAir
    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    private int delayLeft = 20;
    private double lastPacketY = Double.MAX_VALUE;

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        } else return lastY - currentY < 0.03130D;
    }

    private void antiKickPacket(PlayerMoveC2SPacket packet, double currentY) {
        // maximum time we can be "floating" is 80 ticks, so 4 seconds max
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE &&
                shouldFlyDown(currentY, this.lastPacketY) && isEntityOnAir(mc.player)) {
            // actual check is for >= -0.03125D, but we have to do a bit more than that
            // due to the fact that it's a bigger or *equal* to, and not just a bigger than
            ((PlayerMoveC2SPacketAccessor) packet).setY(lastPacketY - 0.03130D);
            lastPacketY -= 0.03130D;
            delayLeft = 20;
        } else {
            lastPacketY = currentY;
            if (!isEntityOnAir(mc.player))
                delayLeft = 20;
        }
        if (delayLeft > 0) delayLeft--;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player.getVehicle() == null || !(event.packet instanceof PlayerMoveC2SPacket packet) || antiKickMode.get() != AntiKickMode.PaperNew)
            return;

        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            antiKickPacket(packet, currentY);
        } else {
            // if the packet is a LookAndOnGround packet or an OnGroundOnly packet then we need to
            // make it a Full packet or a PositionAndOnGround packet respectively, so it has a Y value
            PlayerMoveC2SPacket fullPacket;
            if (packet.changesLook()) {
                fullPacket = new PlayerMoveC2SPacket.Full(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        packet.getYaw(0),
                        packet.getPitch(0),
                        packet.isOnGround(),
                        packet.horizontalCollision()
                );
            } else {
                fullPacket = new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        packet.isOnGround(),
                        packet.horizontalCollision()
                );
            }
            event.cancel();
            antiKickPacket(fullPacket, mc.player.getY());
            mc.getNetworkHandler().sendPacket(fullPacket);
        }
    }

    private int floatingTicks = 0;

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (antiKickMode.get() == AntiKickMode.PaperNew) {
            // Resend movement packets
            ((ClientPlayerEntityAccessor) mc.player).setTicksSinceLastPositionPacketSent(20);
        }
        if (floatingTicks >= 20) {
            switch (antiKickMode.get()) {
                case New -> {
                    Box box = mc.player.getBoundingBox();
                    Box adjustedBox = box.offset(0, -0.4, 0);

                    Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

                    if (blockCollisions.findAny().isPresent()) break;

                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.4, mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));
                }
                case Old -> {
                    Box box = mc.player.getBoundingBox();
                    Box adjustedBox = box.offset(0, -0.4, 0);

                    Stream<VoxelShape> blockCollisions = Streams.stream(mc.world.getBlockCollisions(mc.player, adjustedBox));

                    if (blockCollisions.findAny().isPresent()) break;

                    double ground = calculateGround();
                    double groundExtra = ground + 0.1D;

                    for (double posY = mc.player.getY(); posY > groundExtra; posY -= 4D) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), posY, mc.player.getZ(), true, mc.player.horizontalCollision));

                        if (posY - 4D < groundExtra) break; // Prevent next step
                    }

                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), groundExtra, mc.player.getZ(), true, mc.player.horizontalCollision));

                    for (double posY = groundExtra; posY < mc.player.getY(); posY += 4D) {
                        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), posY, mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));

                        if (posY + 4D > mc.player.getY()) break; // Prevent next step
                    }

                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround(), mc.player.horizontalCollision));

                }
            }
            floatingTicks = 0;
        }

        float ySpeed = RejectsUtils.fullFlightMove(event, speed.get(), verticalSpeedMatch.get());

        if (floatingTicks < 20)
            if (ySpeed >= -0.1)
                floatingTicks++;
            else if (antiKickMode.get() == AntiKickMode.New)
                floatingTicks = 0;
    }

    public enum AntiKickMode {
        Old,
        New,
        PaperNew,
        None
    }
}