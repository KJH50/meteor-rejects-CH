package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.GameModeListSetting;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.world.GameMode;

import java.util.List;

public class GamemodeNotifier extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<GameMode>> gamemodes = sgGeneral.add(new GameModeListSetting.Builder()
            .name("游戏模式")
            .description("要通知的游戏模式。")
            .build()
    );

    public GamemodeNotifier() {
        super(MeteorRejectsAddon.CATEGORY, "游戏模式通知器", "当玩家的游戏模式发生变化时通知用户。");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet) {
            for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                if (!packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_GAME_MODE)) continue;
                PlayerListEntry entry1 = mc.getNetworkHandler().getPlayerListEntry(entry.profileId());
                if (entry1 == null) continue;
                GameMode gameMode = entry.gameMode();
                if (entry1.getGameMode() != gameMode) {
                    if (!gamemodes.get().contains(gameMode)) continue;
                    info("Player %s changed gamemode to %s", entry1.getProfile().getName(), entry.gameMode());
                }
            }
        }
    }
}


