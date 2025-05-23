package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3d;

import java.util.Set;

public class AimAssist extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("瞄准速度");

    // 通用设置

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("实体类型")
            .description("要瞄准的实体类型。")
            .defaultValue(EntityType.PLAYER)
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("范围")
            .description("可以瞄准实体的范围。")
            .defaultValue(5)
            .min(0)
            .build()
    );

    private final Setting<Double> fov = sgGeneral.add(new DoubleSetting.Builder()
            .name("视野")
            .description("只会在视野范围内瞄准实体。")
            .defaultValue(360)
            .min(0)
            .max(360)
            .build()
    );

    private final Setting<Boolean> ignoreWalls = sgGeneral.add(new BoolSetting.Builder()
            .name("忽略墙壁")
            .description("是否忽略墙壁进行瞄准。")
            .defaultValue(false)
            .build()
    );

    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
            .name("优先级")
            .description("如何筛选范围内的目标。")
            .defaultValue(SortPriority.LowestHealth)
            .build()
    );

    private final Setting<Target> bodyTarget = sgGeneral.add(new EnumSetting.Builder<Target>()
            .name("瞄准目标")
            .description("瞄准实体身体的哪个部分。")
            .defaultValue(Target.Body)
            .build()
    );

    // 瞄准速度

    private final Setting<Boolean> instant = sgSpeed.add(new BoolSetting.Builder()
            .name("瞬间瞄准")
            .description("瞬间看向实体。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
            .name("速度")
            .description("瞄准实体的速度。")
            .defaultValue(5)
            .min(0)
            .visible(() -> !instant.get())
            .build()
    );

    private final Vector3d vec3d1 = new Vector3d();
    private Entity target;

    public AimAssist() {
        super(MeteorRejectsAddon.CATEGORY, "自动瞄准", "自动瞄准实体。");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        target = TargetUtils.get(entity -> {
            if (!entity.isAlive()) return false;
            if (!PlayerUtils.isWithin(entity, range.get())) return false;
            if (!ignoreWalls.get() && !PlayerUtils.canSeeEntity(entity)) return false;
            if (entity == mc.player || !entities.get().contains(entity.getType())) return false;
            if (entity instanceof PlayerEntity && !Friends.get().shouldAttack((PlayerEntity) entity)) return false;
            return RejectsUtils.inFov(entity, fov.get());
        }, priority.get());
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target != null) aim(target, event.tickDelta, instant.get());
    }

    private void aim(Entity target, double delta, boolean instant) {
        Utils.set(vec3d1, target, delta);

        switch (bodyTarget.get()) {
            case Head -> vec3d1.add(0, target.getEyeHeight(target.getPose()), 0);
            case Body -> vec3d1.add(0, target.getEyeHeight(target.getPose()) / 2, 0);
        }

        double deltaX = vec3d1.x - mc.player.getX();
        double deltaZ = vec3d1.z - mc.player.getZ();
        double deltaY = vec3d1.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));

        // Yaw
        double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;
        double deltaAngle;
        double toRotate;

        if (instant) {
            mc.player.setYaw((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getYaw());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle))
                toRotate = deltaAngle;
            mc.player.setYaw(mc.player.getYaw() + (float) toRotate);
        }

        // Pitch
        double idk = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        angle = -Math.toDegrees(Math.atan2(deltaY, idk));

        if (instant) {
            mc.player.setPitch((float) angle);
        } else {
            deltaAngle = MathHelper.wrapDegrees(angle - mc.player.getPitch());
            toRotate = speed.get() * (deltaAngle >= 0 ? 1 : -1) * delta;
            if ((toRotate >= 0 && toRotate > deltaAngle) || (toRotate < 0 && toRotate < deltaAngle))
                toRotate = deltaAngle;
            mc.player.setPitch(mc.player.getPitch() + (float) toRotate);
        }
    }

    @Override
    public String getInfoString() {
        return EntityUtils.getName(target);
    }
}