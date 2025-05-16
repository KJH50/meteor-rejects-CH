package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.brigadier.suggestion.Suggestion;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AntiVanish extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("检测间隔")
            .description("检测消失的间隔时间。")
            .defaultValue(100)
            .min(0)
            .sliderMax(300)
            .build()
    );

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
            .name("检测模式")
            .description("检测模式。")
            .defaultValue(Mode.LeaveMessage)
            .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
            .name("命令")
            .description("用于检测玩家名称的补全命令。")
            .defaultValue("minecraft:msg")
            .visible(() -> mode.get() == Mode.RealJoinMessage)
            .build()
    );

    private Map<UUID, String> playerCache = new HashMap<>();
    private final List<String> messageCache = new ArrayList<>();

    private final Random random = new Random();
    private final List<Integer> completionIDs = new ArrayList<>();
    private List<String> completionPlayerCache = new ArrayList<>();

    private int timer = 0;

    public AntiVanish() {
        super(MeteorRejectsAddon.CATEGORY, "反消失", "当管理员使用 /vanish 时通知用户。");
        }

    @Override
    public void onActivate() {
        timer = 0;
        completionIDs.clear();
        messageCache.clear();
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList l = theme.verticalList();
        l.add(theme.label("LeaveMessage: 如果客户端没有收到退出游戏的消息（如 essentials）。"));
        l.add(theme.label("RealJoinMessage: 通过玩家名称补全来判断玩家是否真的离开。"));
        return l;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (mode.get() == Mode.RealJoinMessage && event.packet instanceof CommandSuggestionsS2CPacket packet) {
            if (completionIDs.contains(packet.id())) {
                var lastUsernames = completionPlayerCache.stream().toList();

                completionPlayerCache = packet.getSuggestions().getList().stream()
                        .map(Suggestion::getText)
                        .toList();

                if (lastUsernames.isEmpty()) return;

                Predicate<String> joinedOrQuit = playerName -> lastUsernames.contains(playerName) != completionPlayerCache.contains(playerName);

                for (String playerName : completionPlayerCache) {
                    if (Objects.equals(playerName, mc.player.getName().getString())) continue;
                    if (playerName.contains(" ")) continue;
                    if (playerName.length() < 3 || playerName.length() > 16) continue;
                    if (joinedOrQuit.test(playerName)) {
                        info("玩家加入: " + playerName);
                    }
                }

                for (String playerName : lastUsernames) {
                    if (Objects.equals(playerName, mc.player.getName().getString())) continue;
                    if (playerName.contains(" ")) continue;
                    if (playerName.length() < 3 || playerName.length() > 16) continue;
                    if (joinedOrQuit.test(playerName)) {
                        info("玩家离开: " + playerName);
                    }
                }

                completionIDs.remove(Integer.valueOf(packet.id()));
                event.cancel();
            }
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        messageCache.add(event.getMessage().getString());
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        timer++;
        if (timer < interval.get()) return;

        switch (mode.get()) {
            case LeaveMessage -> {
                Map<UUID, String> oldPlayers = Map.copyOf(playerCache);
                playerCache = mc.getNetworkHandler().getPlayerList().stream().collect(Collectors.toMap(e -> e.getProfile().getId(), e -> e.getProfile().getName()));

                for (UUID uuid : oldPlayers.keySet()) {
                    if (playerCache.containsKey(uuid)) continue;
                    String name = oldPlayers.get(uuid);
                    if (name.contains(" ")) continue;
                    if (name.length() < 3 || name.length() > 16) continue;
                    if (messageCache.stream().noneMatch(s -> s.contains(name))) {
                        warning(name + " 已进入消失状态。");
                    }
                }
            }
            case RealJoinMessage -> {
                int id = random.nextInt(200);
                completionIDs.add(id);
                mc.getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(id, command.get() + " "));
            }
        }
        timer = 0;
        messageCache.clear();
    }

    public enum Mode {
        LeaveMessage("离开消息"),
        RealJoinMessage("真实加入消息");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
