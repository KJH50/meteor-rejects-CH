package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;

public class Boost extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> strength = sgGeneral.add(new DoubleSetting.Builder()
            .name("强度")
            .description("将你向前推进的强度。")
            .defaultValue(4.0)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> autoBoost = sgGeneral.add(new BoolSetting.Builder()
            .name("自动推进")
            .description("自动进行推进。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("间隔")
            .description("推进间隔时间（刻）。")
            .visible(autoBoost::get)
            .defaultValue(20)
            .sliderMax(120)
            .build()
    );

    private int timer = 0;

    public Boost() {
        super(MeteorRejectsAddon.CATEGORY, "推进", "像冲刺一样向前移动。");
    }

    @Override
    public void onActivate() {
        timer = interval.get();
        if (!autoBoost.get()) {
            if (mc.player != null) boost();
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!autoBoost.get()) return;
        if (timer < 1) {
            boost();
            timer = interval.get();
        } else {
            timer--;
        }
    }

    private void boost() {
        Vec3d v = mc.player.getRotationVecClient().multiply(strength.get());
        mc.player.addVelocity(v.getX(), v.getY(), v.getZ());
    }
}
