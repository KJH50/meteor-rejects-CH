package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomPackets extends Module {
    private static final Gson GSON_NON_PRETTY = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
    private static final Type BADLION_MODS_TYPE = new TypeToken<Map<String, BadlionMod>>() {
    }.getType();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBadlion = settings.createGroup("Badlion");

    private final Setting<Boolean> unknownPackets = sgGeneral.add(new BoolSetting.Builder()
            .name("未知数据包")
            .description("是否打印未知数据包。")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> mods = sgBadlion.add(new BoolSetting.Builder()
            .name("禁用模组")
            .description("是否打印 Badlion 禁用的模组。")
            .defaultValue(true)
            .build()
    );

    public CustomPackets() {
        super(MeteorRejectsAddon.CATEGORY, "自定义数据包", "处理不同的非原版协议。");
    }

    @EventHandler
    private void onCustomPayloadPacket(PacketEvent.Receive event) {
        if (event.packet instanceof CustomPayloadS2CPacket packet) {
            if (packet.payload().getId().toString().equals("badlion:mods")) {
                event.setCancelled(onBadlionModsPacket(packet));
            } else {
                onUnknownPacket(packet);
            }
        }
    }

    PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());

    private void onUnknownPacket(CustomPayloadS2CPacket packet) {
        if (!unknownPackets.get()) return;
        MutableText text = Text.literal(packet.payload().getId().toString());
        buffer.clear();
        text.setStyle(text.getStyle()
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.literal(readString(buffer)
                ))));
        info(text);
    }

    private boolean onBadlionModsPacket(CustomPayloadS2CPacket packet) {
        if (!mods.get()) return false;
        buffer.clear();
        String json = readString(buffer);
        Map<String, BadlionMod> mods = GSON_NON_PRETTY.fromJson(json, BADLION_MODS_TYPE);
        ChatUtils.sendMsg("Badlion", format("Mods", formatMods(mods)));
        return true;
    }

    private MutableText format(String type, MutableText message) {
        MutableText text = Text.literal(String.format("[%s%s%s]",
                Formatting.AQUA,
                type,
                Formatting.GRAY
        ));
        text.append(" ");
        text.append(message);
        return text;
    }

    private String readString(PacketByteBuf data) {
        return data.readCharSequence(
                data.readableBytes(),
                StandardCharsets.UTF_8
        ).toString();
    }

    private MutableText formatMods(Map<String, BadlionMod> mods) {
        MutableText text = Text.literal("Disallowed mods: \n");

        mods.forEach((name, data) -> {
            MutableText modLine = Text.literal(String.format("- %s%s%s ", Formatting.YELLOW, name, Formatting.GRAY));
            modLine.append(data.disabled ? "disabled" : "enabled");
            modLine.append("\n");
            if (data.extra_data != null) {
                modLine.setStyle(modLine.getStyle()
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Text.literal(data.extra_data.toString())
                        )));
            }
            text.append(modLine);
        });

        return text;
    }

    private static class BadlionMod {
        private boolean disabled;
        private JsonObject extra_data;
        private JsonObject settings;
    }
}
