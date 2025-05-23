package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoBedTrap extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
            .name("每刻放置方块数")
            .description("每刻放置的方块数量")
            .defaultValue(2)
            .min(1)
            .sliderMax(8)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("旋转")
            .description("放置时是否旋转")
            .defaultValue(true)
            .build()
    );

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
            .name("范围")
            .description("破坏范围")
            .defaultValue(4)
            .min(0)
            .build()
    );

    private final Setting<List<Block>> blockTypes = sgGeneral.add(new BlockListSetting.Builder()
        .name("方块类型")
        .description("用于床陷阱的方块类型")
        .defaultValue(Arrays.asList(Blocks.OBSIDIAN))
        .build()
    );

    private final Pool<BlockPos.Mutable> blockPosPool = new Pool<>(BlockPos.Mutable::new);
    private final List<BlockPos.Mutable> blocks = new ArrayList<>();

    int cap = 0;

    public AutoBedTrap() {
        super(MeteorRejectsAddon.CATEGORY, "自动床陷阱", "自动在床周围放置黑曜石");
    }

    @Override
    public void onDeactivate() {
        for (BlockPos.Mutable blockPos : blocks) blockPosPool.free(blockPos);
        blocks.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {

        BlockIterator.register((int) Math.ceil(range.get()), (int) Math.ceil(range.get()), (blockPos, blockState) -> {
            if (!BlockUtils.canBreak(blockPos, blockState)) return;

            if (!(blockState.getBlock() instanceof BedBlock)) return;

            blocks.add(blockPosPool.get().set(blockPos));
        });

    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        boolean noBlocks = false;
        for (BlockPos blockPos : blocks) {
            if (!placeTickAround(blockPos)) {
                noBlocks = true;
                break;
            }
        }
        if (noBlocks && isActive()) toggle(); 
    }

    public boolean placeTickAround(BlockPos block) {
        for (BlockPos b : new BlockPos[]{
                block.up(), block.west(),
                block.north(), block.south(),
                block.east(), block.down()}) {

            if (cap >= bpt.get()) {
                cap = 0;
                return true;
            }

            if (blockTypes.get().contains(mc.world.getBlockState(b).getBlock())) return true;

            FindItemResult findBlock = InvUtils.findInHotbar((item) -> {
                if (!(item.getItem() instanceof BlockItem)) return false;
                BlockItem bitem = (BlockItem)item.getItem();
                return blockTypes.get().contains(bitem.getBlock());
            });
            if (!findBlock.found()) {
                error("No specified blocks found. Disabling.");
                return false;
            }


            if (BlockUtils.place(b, findBlock, rotate.get(), 10, false)) {
                cap++;
                if (cap >= bpt.get()) {
                    return true;
                }
            }
        }
        cap = 0;
        return true;
    }
}
