package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.LivingEntityAccessor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super(MeteorRejectsAddon.CATEGORY, "无跳跃冷却", "移除跳跃冷却时间。");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        ((LivingEntityAccessor) mc.player).setJumpCooldown(0);
    }
}
