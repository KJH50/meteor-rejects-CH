package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;

public class ItemGenerator extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> speed = sgGeneral.add(new IntSetting.Builder()
            .name("生成速度")
            .description("每刻生成的物品堆叠数。高速度可能导致卡顿。")
            .defaultValue(1)
            .min(1)
            .max(36)
            .sliderMax(36)
            .build()
    );

    private final Setting<Integer> stackSize = sgGeneral.add(new IntSetting.Builder()
            .name("堆叠大小")
            .description("每堆物品的数量。")
            .defaultValue(1)
            .min(1)
            .max(64)
            .sliderMax(64)
            .build()
    );

    private final Random random = Random.create();

    public ItemGenerator() {
        super(MeteorRejectsAddon.CATEGORY, "物品生成器", "生成随机物品并将其丢在地上。仅限创造模式。");
    }

    @Override
    public void onActivate() {
        if(!mc.player.getAbilities().creativeMode) {
            error("仅限创造模式。");
            this.toggle();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        int stacks = speed.get();
        int size = stackSize.get();
        for(int i = 9; i < 9 + stacks; i++) {
            mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(i, new ItemStack(Registries.ITEM.getRandom(random).map(RegistryEntry::value).orElse(Items.DIRT), size)));
        }

        for(int i = 9; i < 9 + stacks; i++) {
            InvUtils.drop().slot(i);
        }
    }
}
