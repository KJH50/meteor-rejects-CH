package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AntiBot extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgFilters = settings.createGroup("过滤器");

    private final Setting<Boolean> removeInvisible = sgGeneral.add(new BoolSetting.Builder()
            .name("移除隐身玩家")
            .description("仅在玩家隐身时移除机器人。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> gameMode = sgFilters.add(new BoolSetting.Builder()
            .name("无游戏模式")
            .description("移除没有游戏模式的玩家。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> api = sgFilters.add(new BoolSetting.Builder()
            .name("无玩家条目")
            .description("移除没有玩家条目的玩家。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> profile = sgFilters.add(new BoolSetting.Builder()
            .name("无游戏档案")
            .description("移除没有游戏档案的玩家。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> latency = sgFilters.add(new BoolSetting.Builder()
            .name("延迟检查")
            .description("使用延迟检查移除玩家。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> nullException = sgFilters.add(new BoolSetting.Builder()
            .name("空指针异常")
            .description("如果发生空指针异常，则移除玩家。")
            .defaultValue(false)
            .build()
    );

    public AntiBot() {
        super(MeteorRejectsAddon.CATEGORY, "反机器人", "检测并移除机器人。");
    }

    @EventHandler
    public void onTick(TickEvent.Post tickEvent) {
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity playerEntity)) continue;
            if (removeInvisible.get() && !entity.isInvisible()) continue;

            if (isBot(playerEntity)) entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    private boolean isBot(PlayerEntity entity) {
        try {
            if (gameMode.get() && EntityUtils.getGameMode(entity) == null) return true;
            if (api.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(entity.getUuid()) == null) return true;
            if (profile.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(entity.getUuid()).getProfile() == null) return true;
            if (latency.get() &&
                    mc.getNetworkHandler().getPlayerListEntry(entity.getUuid()).getLatency() > 1) return true;
        } catch (NullPointerException e) {
            if (nullException.get()) return true;
        }

        return false;
    }
}