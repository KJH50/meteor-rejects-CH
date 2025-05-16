package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class GhostMode extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> fullFood = sgGeneral.add(new BoolSetting.Builder()
        .name("满饱食度")
        .description("将饱食度客户端设置为最大值。")
        .defaultValue(true)
        .build()
    );

    public GhostMode() {
        super(MeteorRejectsAddon.CATEGORY, "幽灵模式", "允许你在死亡后继续游戏。适用于 Forge、Fabric 和 Vanilla 服务器。");
    }

    private boolean active = false;

    @Override
    public void onDeactivate() {
        super.onDeactivate();
        active = false;
        warning("你已退出幽灵模式！");
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.requestRespawn();
            info("已向服务器发送重生请求。");
        }
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        active = false;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!active) return;
        if (mc.player.getHealth() < 1f) mc.player.setHealth(20f);
        if (fullFood.get() && mc.player.getHungerManager().getFoodLevel() < 20) {
            mc.player.getHungerManager().setFoodLevel(20);
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof DeathScreen) {
            event.cancel();
            if (!active) {
                active = true;
                info("你已进入幽灵模式。");
            }
        }
    }
}
