package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.systems.modules.Module;

public class SilentDisconnect extends Module {
    public SilentDisconnect() {
        super(MeteorRejectsAddon.CATEGORY, "静默断开连接", "断开连接时不会显示断开连接屏幕。");
    }
}
