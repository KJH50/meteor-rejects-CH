package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.events.StopUsingItemEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class ArrowDmg extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> packets = sgGeneral.add(new IntSetting.Builder()
            .name("数据包数量")
            .description("发送的数据包数量。更多的数据包 = 更高的伤害。")
            .defaultValue(200)
            .min(2)
            .sliderMax(2000)
            .build()
    );

    public final Setting<Boolean> tridents = sgGeneral.add(new BoolSetting.Builder()
            .name("三叉戟")
            .description("启用时，三叉戟会飞得更远。似乎不会影响伤害或激流。警告：启用此选项后，您可能会很容易丢失三叉戟！")
            .defaultValue(false)
            .build()
    );

    public ArrowDmg() {
        super(MeteorRejectsAddon.CATEGORY, "箭矢伤害", "大幅增加箭矢伤害，但也会消耗大量饥饿值并降低准确性。不适用于弩，并且在 Paper 服务器上似乎已被修复。");
    }

    @EventHandler
    private void onStopUsingItem(StopUsingItemEvent event) {
        if (!isValidItem(event.itemStack.getItem()))
            return;

        ClientPlayerEntity p = mc.player;

        p.networkHandler.sendPacket(
                new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.START_SPRINTING));

        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        for (int i = 0; i < packets.get() / 2; i++) {
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x,
                    y - 1e-10, z, true, mc.player.horizontalCollision));
            p.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x,
                    y + 1e-10, z, false, mc.player.horizontalCollision));
        }
    }

    private boolean isValidItem(Item item) {
        return tridents.get() && item == Items.TRIDENT || item == Items.BOW;
    }
}