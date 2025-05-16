package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ExtraElytra extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> instantFly = sgGeneral.add(new BoolSetting.Builder()
            .name("即时飞行")
            .description("跳跃即可飞行，无需奇怪的二段跳！")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> speedCtrl = sgGeneral.add(new BoolSetting.Builder()
            .name("速度控制")
            .description("使用前进和后退键控制速度。\n（默认：W 和 S）\n无需烟花！")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> heightCtrl = sgGeneral.add(new BoolSetting.Builder()
            .name("高度控制")
            .description("使用跳跃和潜行键控制高度。\n（默认：空格和 Shift）\n无需烟花！")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> stopInWater = sgGeneral.add(new BoolSetting.Builder()
            .name("水中停止")
            .description("在水中停止飞行")
            .defaultValue(true)
            .build()
    );

    private int jumpTimer;

    @Override
    public void onActivate() {
        jumpTimer = 0;
    }

    public ExtraElytra() {
        super(MeteorRejectsAddon.CATEGORY, "额外鞘翅", "更简单的鞘翅飞行");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (jumpTimer > 0)
            jumpTimer--;

        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.getItem() != Items.ELYTRA)
            return;

        if (mc.player.isGliding()) {
            if (stopInWater.get() && mc.player.isTouchingWater()) {
                sendStartStopPacket();
                return;
            }
            controlSpeed();
            controlHeight();
            return;
        }

        if (chest.getDamage() < chest.getMaxDamage() - 1 && mc.options.jumpKey.isPressed())
            doInstantFly();
    }

    private void sendStartStopPacket() {
        ClientCommandC2SPacket packet = new ClientCommandC2SPacket(mc.player,
                ClientCommandC2SPacket.Mode.START_FALL_FLYING);
        mc.player.networkHandler.sendPacket(packet);
    }

    private void controlHeight() {
        if (!heightCtrl.get())
            return;

        Vec3d v = mc.player.getVelocity();

        if (mc.options.jumpKey.isPressed())
            mc.player.setVelocity(v.x, v.y + 0.08, v.z);
        else if (mc.options.sneakKey.isPressed())
            mc.player.setVelocity(v.x, v.y - 0.04, v.z);
    }

    private void controlSpeed() {
        if (!speedCtrl.get())
            return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        Vec3d forward = new Vec3d(-MathHelper.sin(yaw) * 0.05, 0,
                MathHelper.cos(yaw) * 0.05);

        Vec3d v = mc.player.getVelocity();

        if (mc.options.forwardKey.isPressed())
            mc.player.setVelocity(v.add(forward));
        else if (mc.options.backKey.isPressed())
            mc.player.setVelocity(v.subtract(forward));
    }

    private void doInstantFly() {
        if (!instantFly.get())
            return;

        if (jumpTimer <= 0) {
            jumpTimer = 20;
            mc.player.setJumping(false);
            mc.player.setSprinting(true);
            mc.player.jump();
        }

        sendStartStopPacket();
    }
}
