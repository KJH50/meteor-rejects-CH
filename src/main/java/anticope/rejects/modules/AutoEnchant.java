package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;

import java.util.List;
import java.util.Objects;

public class AutoEnchant extends meteordevelopment.meteorclient.systems.modules.Module {

    public final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("附魔物品之间的刻延迟。")
            .defaultValue(50)
            .sliderMax(500)
            .min(0)
            .build()
    );

    private final Setting<Integer> level = sgGeneral.add(new IntSetting.Builder()
            .name("等级")
            .description("选择附魔等级 1-3")
            .defaultValue(3)
            .max(3)
            .min(1)
            .build()
    );

    private final Setting<Boolean> drop = sgGeneral.add(new BoolSetting.Builder()
            .name("丢弃")
            .description("自动丢弃附魔后的物品（当物品栏空间不足时有用）")
            .defaultValue(false)
            .build()
    );

    private final Setting<List<Item>> itemWhitelist = sgGeneral.add(new ItemListSetting.Builder()
            .name("物品白名单")
            .description("需要附魔的物品。")
            .defaultValue()
            .filter(item -> item.equals(Items.BOOK) || new ItemStack(item).isDamageable())
            .build()
    );

    public AutoEnchant() {
        super(MeteorRejectsAddon.CATEGORY, "自动附魔", "自动附魔物品。");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(Objects.requireNonNull(mc.player).currentScreenHandler instanceof EnchantmentScreenHandler))
            return;
        MeteorExecutor.execute(this::autoEnchant);
    }

    private void autoEnchant() {
        if (!(Objects.requireNonNull(mc.player).currentScreenHandler instanceof EnchantmentScreenHandler handler))
            return;
        if (mc.player.experienceLevel < 30) {
            info("你的经验等级不足");
            return;
        }
        while (getEmptySlotCount(handler) > 2 || drop.get()) {
            if (!(mc.player.currentScreenHandler instanceof EnchantmentScreenHandler)) {
                info("附魔台已关闭。");
                break;
            }
            if (handler.getLapisCount() < level.get() && !fillLapisItem()) {
                info("未找到青金石。");
                break;
            }
            if (!fillCanEnchantItem()) {
                info("未找到可附魔的物品。");
                break;
            }
            Objects.requireNonNull(mc.interactionManager).clickButton(handler.syncId, level.get() - 1);
            if (getEmptySlotCount(handler) > 2) {
                InvUtils.shiftClick().slotId(0);
            } else if (drop.get() && handler.getSlot(0).hasStack()) {
                // 我不知道为什么这里会抛出 LegacyRandomSource 异常，
                // 所以我使用主线程来丢弃物品。
                mc.execute(() -> InvUtils.drop().slotId(0));
            }

            /*
            虽然这里的描述表示延迟的单位是刻，
            但实际的延迟并不是刻单位，
            但这并不影响游戏中的正常操作。
            也许我们可以忽略它
            */
            try {
                Thread.sleep(delay.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean fillCanEnchantItem() {
        FindItemResult res = InvUtils.find(stack -> itemWhitelist.get().contains(stack.getItem()) && EnchantmentHelper.canHaveEnchantments(stack));
        if (!res.found()) return false;
        InvUtils.shiftClick().slot(res.slot());
        return true;
    }

    private boolean fillLapisItem() {
        FindItemResult res = InvUtils.find(Items.LAPIS_LAZULI);
        if (!res.found()) return false;
        InvUtils.shiftClick().slot(res.slot());
        return true;
    }

    private int getEmptySlotCount(ScreenHandler handler) {
        int emptySlotCount = 0;
        for (int i = 0; i < handler.slots.size(); i++) {
            if (!handler.slots.get(i).getStack().getItem().equals(Items.AIR))
                continue;
            emptySlotCount++;
        }
        return emptySlotCount;
    }

}
