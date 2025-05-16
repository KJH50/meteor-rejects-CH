package anticope.rejects.gui.servers;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.MultiplayerScreenAccessor;
import anticope.rejects.mixin.ServerListAccessor;
import anticope.rejects.utils.server.IPAddress;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.utils.misc.IGetter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ServerManagerScreen extends WindowScreen {

    private static final PointerBuffer saveFileFilters;

    static {
        saveFileFilters = BufferUtils.createPointerBuffer(1);
        saveFileFilters.put(MemoryUtil.memASCII("*.txt"));
        saveFileFilters.rewind();
    }

    private final MultiplayerScreen multiplayerScreen;

    public ServerManagerScreen(GuiTheme theme, MultiplayerScreen multiplayerScreen) {
        super(theme, "服务器管理");
        this.parent = multiplayerScreen;
        this.multiplayerScreen = multiplayerScreen;
    }

    public static Runnable tryHandle(ThrowingRunnable<?> tr, Consumer<Throwable> handler) {
        return Objects.requireNonNull(tr).addHandler(handler);
    }

    @Override
    public void initWidgets() {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();
        addButton(l, "查找服务器 (新)", () -> new ServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "查找服务器 (旧)", () -> new LegacyServerFinderScreen(theme, multiplayerScreen, this));
        addButton(l, "清理", () -> new CleanUpScreen(theme, multiplayerScreen, this));
        l = add(theme.horizontalList()).expandX().widget();
        l.add(theme.button("保存IP地址")).expandX().widget().action = tryHandle(() -> {
            String targetPath = TinyFileDialogs.tinyfd_saveFileDialog("保存IP地址", null, saveFileFilters, null);
            if (targetPath == null) return;
            if (!targetPath.endsWith(".txt")) targetPath += ".txt";
            Path filePath = Path.of(targetPath);

            int newIPs = 0;

            Set<IPAddress> hashedIPs = new HashSet<>();
            if (Files.exists(filePath)) {
                try {
                    List<String> ips = Files.readAllLines(filePath);
                    for (String ip : ips) {
                        IPAddress parsedIP = IPAddress.fromText(ip);
                        if (parsedIP != null)
                            hashedIPs.add(parsedIP);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ServerList servers = multiplayerScreen.getServerList();
            for (int i = 0; i < servers.size(); i++) {
                ServerInfo info = servers.get(i);
                IPAddress addr = IPAddress.fromText(info.address);
                if (addr != null && hashedIPs.add(addr))
                    newIPs++;
            }

            StringBuilder fileOutput = new StringBuilder();
            for (IPAddress ip : hashedIPs) {
                String stringIP = ip.toString();
                if (stringIP != null)
                    fileOutput.append(stringIP).append("\n");
            }

            try {
                Files.writeString(filePath, fileOutput.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            toast("成功！", newIPs == 1 ? "保存了 %s 个新IP" : "保存了 %s 个新IP", newIPs);
        }, e -> {
            MeteorRejectsAddon.LOG.error("无法保存IP地址");
            toast("出错了", "IP地址无法保存，请查看日志获取详细信息");
        });
        l.add(theme.button("加载IP地址")).expandX().widget().action = tryHandle(() -> {
            String targetPath = TinyFileDialogs.tinyfd_openFileDialog("加载IP地址", null, saveFileFilters, "", false);
            if (targetPath == null) return;
            Path filePath = Path.of(targetPath);
            if (!Files.exists(filePath)) return;

            List<ServerInfo> servers = ((ServerListAccessor) multiplayerScreen.getServerList()).getServers();
            Set<String> presentAddresses = new HashSet<>();
            int newIPs = 0;
            for (ServerInfo server : servers) presentAddresses.add(server.address);
            for (String addr : MinecraftClient.getInstance().keyboard.getClipboard().split("[\r\n]+")) {
                if (presentAddresses.add(addr = addr.split(" ")[0])) {
                    servers.add(new ServerInfo("服务器发现 #" + presentAddresses.size(), addr, ServerInfo.ServerType.OTHER));
                    newIPs++;
                }
            }
            multiplayerScreen.getServerList().saveFile();
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setSelected(null);
            ((MultiplayerScreenAccessor) multiplayerScreen).getServerListWidget().setServers(multiplayerScreen.getServerList());
            toast("成功！", newIPs == 1 ? "加载了 %s 个新IP" : "加载了 %s 个新IP", newIPs);
        }, e -> {
            MeteorRejectsAddon.LOG.error("无法加载IP地址");
            toast("出错了", "IP地址无法加载，请查看日志获取详细信息");
        });
    }

    private void toast(String titleKey, String descriptionKey, Object... params) {
        SystemToast.add(client.getToastManager(), SystemToast.Type.WORLD_BACKUP, Text.literal(titleKey), Text.translatable(descriptionKey, params));
    }

    private void addButton(WContainer c, String text, IGetter<Screen> action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = () -> client.setScreen(action.get());
    }

    public interface ThrowingRunnable<TEx extends Throwable> {
        void run() throws TEx;

        default Runnable addHandler(Consumer<Throwable> handler) {
            Objects.requireNonNull(handler);
            return () -> {
                try {
                    this.run();
                } catch (Throwable var3) {
                    handler.accept(var3);
                }
            };
        }
    }

}
