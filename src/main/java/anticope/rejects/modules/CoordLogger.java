package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class CoordLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTeleports = settings.createGroup("传送");
    private final SettingGroup sgWorldEvents = settings.createGroup("世界事件");

    // General
    
    private final Setting<Double> minDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("最小距离")
            .description("记录事件的最小距离。")
            .min(5)
            .max(100)
            .sliderMin(5)
            .sliderMax(100)
            .defaultValue(10)
            .build()
    );
    
    // Teleports
    
    private final Setting<Boolean> players = sgTeleports.add(new BoolSetting.Builder()
            .name("玩家")
            .description("记录玩家的传送。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> wolves = sgTeleports.add(new BoolSetting.Builder()
            .name("狼")
            .description("记录狼的传送。")
            .defaultValue(false)
            .build()
    );

    // World events
    
    private final Setting<Boolean> enderDragons = sgWorldEvents.add(new BoolSetting.Builder()
            .name("末影龙")
            .description("记录被击杀的末影龙。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> endPortals = sgWorldEvents.add(new BoolSetting.Builder()
            .name("末地传送门")
            .description("记录被打开的末地传送门。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> withers = sgWorldEvents.add(new BoolSetting.Builder()
            .name("凋灵")
            .description("记录凋灵的生成。")
            .defaultValue(false)
            .build()
    );
    

    private final Setting<Boolean> otherEvents = sgWorldEvents.add(new BoolSetting.Builder()
            .name("其他全局事件")
            .description("记录其他全局事件。")
            .defaultValue(false)
            .build()
    );
    
    public CoordLogger() {
        super(MeteorRejectsAddon.CATEGORY,"坐标记录器", "记录各种事件的坐标。可能无法在 Spigot/Paper 服务器上工作。");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        // Teleports
        if (event.packet instanceof EntityPositionS2CPacket) {
            EntityPositionS2CPacket packet = (EntityPositionS2CPacket) event.packet;
            
            try {
                Entity entity = mc.world.getEntityById(packet.entityId());
                
                // Player teleport
                if (entity.getType().equals(EntityType.PLAYER) && players.get()) {
                    Vec3d packetPosition = packet.change().position();
                    Vec3d playerPosition = entity.getPos();
                    
                    if (playerPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Player '" + entity.getNameForScoreboard() + "' has teleported to ", packetPosition));
                    }
                }

                // World teleport
                else if (entity.getType().equals(EntityType.WOLF) && wolves.get()) {
                    Vec3d packetPosition = packet.change().position();
                    Vec3d wolfPosition = entity.getPos();
                    
                    UUID ownerUuid = ((TameableEntity) entity).getOwnerUuid();
                    
                    if (ownerUuid != null && wolfPosition.distanceTo(packetPosition) >= minDistance.get()) {
                        info(formatMessage("Wolf has teleported to ", packetPosition));
                    }
                }
            } catch(NullPointerException ignored) {}
            
        // World events
        } else if (event.packet instanceof WorldEventS2CPacket) {
            WorldEventS2CPacket worldEventS2CPacket = (WorldEventS2CPacket) event.packet;
            
            if (worldEventS2CPacket.isGlobal()) {
                // Min distance
                if (PlayerUtils.distanceTo(worldEventS2CPacket.getPos()) <= minDistance.get()) return;
                
                switch (worldEventS2CPacket.getEventId()) {
                    case 1023:
                        if (withers.get()) info(formatMessage("Wither spawned at ", worldEventS2CPacket.getPos()));
                        break;
                    case 1038:
                        if (endPortals.get()) info(formatMessage("End portal opened at ", worldEventS2CPacket.getPos()));
                        break;
                    case 1028:
                        if (enderDragons.get()) info(formatMessage("Ender dragon killed at ", worldEventS2CPacket.getPos()));
                        break;
                    default:
                        if (otherEvents.get()) info(formatMessage("Unknown global event at ", worldEventS2CPacket.getPos()));
                }
            }
        }
    }

    public MutableText formatMessage(String message, Vec3d coords) {
        MutableText text = Text.literal(message);
        text.append(ChatUtils.formatCoords(coords));
        text.append(Formatting.GRAY +".");
        return text;
    }

    public MutableText formatMessage(String message, BlockPos coords) {
        return formatMessage(message, new Vec3d(coords.getX(), coords.getY(), coords.getZ()));
    }
}
