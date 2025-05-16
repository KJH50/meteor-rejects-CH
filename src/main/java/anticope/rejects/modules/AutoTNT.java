package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.TntBlock;
import net.minecraft.item.FireChargeItem;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoTNT extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<Boolean> ignite = sgGeneral.add(new BoolSetting.Builder()
            .name("点燃")
            .description("是否点燃TNT。")
            .defaultValue(true)
            .build()
    );

    private final Setting<Integer> igniteDelay = sgGeneral.add(new IntSetting.Builder()
            .name("点燃延迟")
            .description("两次点燃之间的延迟（刻）")
            .defaultValue(1)
            .visible(ignite::get)
            .build()
    );

    private final Setting<Integer> horizontalRange = sgGeneral.add(new IntSetting.Builder()
            .name("水平范围")
            .description("点燃的水平范围")
            .defaultValue(4)
            .build()
    );

    private final Setting<Integer> verticalRange = sgGeneral.add(new IntSetting.Builder()
            .name("垂直范围")
            .description("点燃的垂直范围")
            .defaultValue(4)
            .build()
    );

    private final Setting<Boolean> antiBreak = sgGeneral.add(new BoolSetting.Builder()
            .name("防损坏")
            .description("是否防止打火石损坏。")
            .defaultValue(true)
            .visible(ignite::get)
            .build()
    );

    private final Setting<Boolean> fireCharge = sgGeneral.add(new BoolSetting.Builder()
            .name("火焰弹")
            .description("是否也使用火焰弹。")
            .defaultValue(true)
            .visible(ignite::get)
            .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("旋转")
            .description("是否朝向动作方向旋转。")
            .defaultValue(true)
            .build()
    );

    private final List<BlockPos.Mutable> blocksToIgnite = new ArrayList<>();
    private final Pool<BlockPos.Mutable> ignitePool = new Pool<>(BlockPos.Mutable::new);
    private int igniteTick = 0;

    public AutoTNT() {
        super(MeteorRejectsAddon.CATEGORY, "自动TNT", "自动点燃TNT。适用于破坏。");
    }

    @Override
    public void onDeactivate() {
        igniteTick = 0;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (ignite.get() && igniteTick > igniteDelay.get()) {
            // Clear blocks
            for (BlockPos.Mutable blockPos : blocksToIgnite) ignitePool.free(blockPos);
            blocksToIgnite.clear();

            // Register
            BlockIterator.register(horizontalRange.get(), verticalRange.get(), (blockPos, blockState) -> {
                if (blockState.getBlock() instanceof TntBlock) blocksToIgnite.add(ignitePool.get().set(blockPos));
            });
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        // Ignition
        if (ignite.get() && !blocksToIgnite.isEmpty()) {
            if (igniteTick > igniteDelay.get()) {
                // Sort based on closest tnt
                blocksToIgnite.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));

                // Ignition
                FindItemResult itemResult = InvUtils.findInHotbar(item -> {
                    if (item.getItem() instanceof FlintAndSteelItem) {
                        return (antiBreak.get() && (item.getMaxDamage() - item.getDamage()) > 10);
                    }
                    else if (item.getItem() instanceof FireChargeItem) {
                        return fireCharge.get();
                    }
                    return false;
                });
                if (!itemResult.found()) {
                    error("No flint and steel in hotbar");
                    toggle();
                    return;
                }
                ignite(blocksToIgnite.get(0), itemResult);

                // Reset ticks
                igniteTick = 0;
            }
        }
        igniteTick++;        
    }

    private void ignite(BlockPos pos, FindItemResult item) {
        InvUtils.swap(item.slot(), true);

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, true));

        InvUtils.swapBack();
    }
}
