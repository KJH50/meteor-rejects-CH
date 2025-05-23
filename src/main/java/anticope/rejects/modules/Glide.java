package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.OffGroundSpeedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


public class Glide extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> fallSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("下落速度")
            .description("下落速度。")
            .defaultValue(0.125)
            .min(0.005)
            .sliderRange(0.005, 0.25)
            .build()
    );

    public final Setting<Double> moveSpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("移动速度")
            .description("水平移动速度因子。")
            .defaultValue(1.2)
            .min(1)
            .sliderRange(1, 5)
            .build()
    );

    public final Setting<Double> minHeight = sgGeneral.add(new DoubleSetting.Builder()
            .name("最小高度")
            .description("当离地面太近时不会滑翔。")
            .defaultValue(0)
            .min(0)
            .sliderRange(0, 2)
            .build()
    );

    public Glide() {
        super(MeteorRejectsAddon.CATEGORY, "滑翔", "Yeehaw!");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ClientPlayerEntity player = mc.player;
        Vec3d v = player.getVelocity();

        if (player.isOnGround() || player.isTouchingWater() || player.isInLava() || player.isClimbing() || v.y >= 0)
            return;

        if (minHeight.get() > 0) {
            Box box = player.getBoundingBox();
            box = box.union(box.offset(0, -minHeight.get(), 0));
            if (!mc.world.isSpaceEmpty(box)) return;
        }

        player.setVelocity(v.x, Math.max(v.y, -fallSpeed.get()), v.z);
    }

    @EventHandler
    private void onOffGroundSpeed(OffGroundSpeedEvent event) {
        event.speed *= moveSpeed.get().floatValue();
    }
}