package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.mixin.HandshakeC2SPacketAccessor;
import com.google.gson.Gson;
import com.mojang.authlib.properties.PropertyMap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;

import java.util.List;

public class BungeeCordSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private static final Gson GSON = new Gson();

    private final Setting<Boolean> whitelist = sgGeneral.add(new BoolSetting.Builder()
            .name("白名单")
            .description("是否使用白名单。")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<String>> whitelistedServers = sgGeneral.add(new StringListSetting.Builder()
            .name("白名单服务器")
            .description("只有在加入以上服务器时才会生效。")
            .visible(whitelist::get)
            .build()
    );

    private final Setting<Boolean> spoofProfile = sgGeneral.add(new BoolSetting.Builder()
            .name("伪造资料")
            .description("是否伪造账户资料。")
            .defaultValue(false)
            .build()
    );

    private final Setting<String> forwardedIP = sgGeneral.add(new StringSetting.Builder()
            .name("转发IP")
            .description("转发的IP地址。")
            .defaultValue("127.0.0.1")
            .build()
    );

    public BungeeCordSpoof() {
        super(MeteorRejectsAddon.CATEGORY, "BungeeCord伪造", "让你加入BungeeCord服务器，在绕过代理时很有用。");
        runInMainMenu = true;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof HandshakeC2SPacket packet && packet.intendedState() == ConnectionIntent.LOGIN) {
            if (whitelist.get() && !whitelistedServers.get().contains(Utils.getWorldName())) return;
            String address = packet.address() + "\0" + forwardedIP + "\0" + mc.getSession().getUuidOrNull().toString().replace("-", "")
                    + (spoofProfile.get() ? getProperty() : "");
            ((HandshakeC2SPacketAccessor) (Object) packet).setAddress(address);
        }
    }

    private String getProperty() {
        PropertyMap propertyMap = mc.getGameProfile().getProperties();
        return "\0" + GSON.toJson(propertyMap.values().toArray());
    }
}
