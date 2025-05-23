package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.StringMapSetting;
import anticope.rejects.utils.RejectsUtils;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("执行命令前的延迟（毫秒）。")
            .defaultValue(1000)
            .min(0)
            .sliderMax(10000)
            .build()
    );

    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
            .name("智能")
            .description("自动添加条目。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Map<String, String>> commands = sgGeneral.add(new StringMapSetting.Builder()
            .name("命令")
            .description("服务器和命令。（* 表示通用）")
            .defaultValue(new LinkedHashMap<>() {{
                put("localhost", "/login 123456");
            }})
            .build()
    );

    private final Timer timer = new Timer();

    public AutoLogin() {
        super(MeteorRejectsAddon.CATEGORY, "自动登录", "在加入指定服务器时运行命令。");
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WHorizontalList l = theme.horizontalList();
        WButton btn = l.add(theme.button("随机生成密码")).widget();
        btn.action = () -> {
            String password = RejectsUtils.getRandomPassword(16);
            MutableText text = Text.literal(Formatting.BOLD + "Click here to register securely.");
            text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/register %s %s", password, password))));
            info(text);
        };
        return l;
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!isActive()) return;
        String command = commands.get().getOrDefault("*", commands.get().get(Utils.getWorldName()));
        if (command != null) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mc.player != null) ChatUtils.sendPlayerMsg(command);
                }
            }, delay.get());
        }
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Send event) {
        if (!smart.get()) return;

        if (event.packet instanceof CommandExecutionC2SPacket packet) {
            String command = packet.command();
            List<String> hint = Arrays.asList("reg", "register", "l", "login", "log");
            String[] cmds = command.split(" ");
            if (cmds.length >= 2 && hint.contains(cmds[0])) {
                commands.get().put(Utils.getWorldName(), "/login " + cmds[1]);
            }
        }
    }
}
