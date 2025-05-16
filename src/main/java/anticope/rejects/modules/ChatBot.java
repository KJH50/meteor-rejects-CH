package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.settings.StringMapSetting;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import meteordevelopment.starscript.compiler.Compiler;
import meteordevelopment.starscript.compiler.Parser;
import meteordevelopment.starscript.utils.StarscriptError;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatBot extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> prefix = sgGeneral.add(new StringSetting.Builder()
            .name("前缀")
            .description("机器人的命令前缀。")
            .defaultValue("!")
            .build()
    );

    private final Setting<Boolean> help = sgGeneral.add(new BoolSetting.Builder()
            .name("帮助")
            .description("是否添加帮助命令。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Map<String, String>> commands = sgGeneral.add(new StringMapSetting.Builder()
            .name("命令")
            .description("命令列表。")
            .renderer(StarscriptTextBoxRenderer.class)
            .defaultValue(new LinkedHashMap<>() {{
                put("ping", "Pong!");
                put("tps", "当前TPS: {server.tps}");
                put("time", "当前时间: {server.time}");
                put("pos", "我的位置: {player.pos}");
            }})
            .build()
    );

    public ChatBot() {
        super(MeteorRejectsAddon.CATEGORY, "聊天机器人", "自动响应聊天消息的机器人。");
    }

    @EventHandler
    private void onMessageRecieve(ReceiveMessageEvent event) {
        String msg = event.getMessage().getString();
        if (help.get() && msg.endsWith(prefix.get() + "help")) {
            ChatUtils.sendPlayerMsg("Available commands: " + String.join(", ", commands.get().keySet()));
            return;
        }
        for (String cmd : commands.get().keySet()) {
            if (msg.endsWith(prefix.get() + cmd)) {
                Script script = compile(commands.get().get(cmd));
                if (script == null) ChatUtils.sendPlayerMsg("An error occurred");
                try {
                    var section = MeteorStarscript.ss.run(script);
                    ChatUtils.sendPlayerMsg(section.text);
                } catch (StarscriptError e) {
                    MeteorStarscript.printChatError(e);
                    ChatUtils.sendPlayerMsg("An error occurred");
                }
                return;
            }
        }
    }

    private static Script compile(String script) {
        if (script == null) return null;
        Parser.Result result = Parser.parse(script);
        if (result.hasErrors()) {
            MeteorStarscript.printChatError(result.errors.get(0));
            return null;
        }
        return Compiler.compile(result);
    }
}
