package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class Painter extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Block> block = sgGeneral.add(new BlockSetting.Builder()
            .name("方块")
            .description("用于绘画的方块")
            .defaultValue(Blocks.STONE_BUTTON)
            .build()
    );
    
    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
            .name("范围")
            .description("放置范围")
            .min(0)
            .defaultValue(0)
            .build()
    );
    
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
            .name("延迟")
            .description("方块放置之间的延迟（刻）")
            .min(0)
            .defaultValue(0)
            .build()
    );

    private final Setting<Integer> bpt = sgGeneral.add(new IntSetting.Builder()
            .name("每刻方块数")
            .description("每刻可以放置的方块数量")
            .min(1)
            .defaultValue(1)
            .build()
    );
    
    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
            .name("旋转")
            .description("放置时是否朝向方块")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> topSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("顶部表面")
            .description("是否覆盖顶部表面")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> sideSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("侧面表面")
            .description("是否覆盖侧面表面")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> bottomSurfaces = sgGeneral.add(new BoolSetting.Builder()
            .name("底部表面")
            .description("是否覆盖底部表面")
            .defaultValue(true)
            .build()
    );
    
    private final Setting<Boolean> oneBlockHeight = sgGeneral.add(new BoolSetting.Builder()
            .name("一格高度")
            .description("是否覆盖一格高的间隙")
            .defaultValue(true)
            .build()
    );

    private int ticksWaited;
    
    public Painter() {
        super(MeteorRejectsAddon.CATEGORY, "上色器", "自动绘画/覆盖表面（适合恶作剧）");
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        // Tick delay
        if (delay.get() != 0 && ticksWaited < delay.get() - 1) {
            ticksWaited++;
            return;
        }
        else ticksWaited = 0;
        
        // Get slot
        FindItemResult findItemResult = InvUtils.findInHotbar(itemStack -> block.get() == Block.getBlockFromItem(itemStack.getItem()));
        if (!findItemResult.found()) {
            error("No selected blocks in hotbar");
            toggle();
            return;
        }
        
        // Find spots
        int placed = 0;
        for (BlockPos blockPos : WorldUtils.getSphere(mc.player.getBlockPos(), range.get(), range.get())) {
            if (shouldPlace(blockPos, block.get())) {
                BlockUtils.place(blockPos, findItemResult, rotate.get(), -100, false);
                placed++;

                // Delay 0
                if (delay.get() != 0 && placed >= bpt.get()) break;
            }
        }
    }
    
    private boolean shouldPlace(BlockPos blockPos, Block useBlock) {
        // Self
        if (!mc.world.getBlockState(blockPos).isReplaceable()) return false;
    
        // One block height
        if (!oneBlockHeight.get() &&
                !mc.world.getBlockState(blockPos.up()).isReplaceable() &&
                !mc.world.getBlockState(blockPos.down()).isReplaceable()) return false;
    
    
        boolean north = true;
        boolean south = true;
        boolean east = true;
        boolean west = true;
        boolean up = true;
        boolean bottom = true;
        BlockState northState = mc.world.getBlockState(blockPos.north());
        BlockState southState = mc.world.getBlockState(blockPos.south());
        BlockState eastState = mc.world.getBlockState(blockPos.east());
        BlockState westState = mc.world.getBlockState(blockPos.west());
        BlockState upState = mc.world.getBlockState(blockPos.up());
        BlockState bottomState = mc.world.getBlockState(blockPos.down());
    
        // Top surface
        if (topSurfaces.get()) {
            if (upState.isReplaceable() || upState.getBlock() == useBlock) up = false;
        }
        
        // Side surfaces
        if (sideSurfaces.get()) {
            
            if (northState.isReplaceable() || northState.getBlock() == useBlock) north = false;
            if (southState.isReplaceable() || southState.getBlock() == useBlock) south = false;
            if (eastState.isReplaceable() || eastState.getBlock() == useBlock) east = false;
            if (westState.isReplaceable() || westState.getBlock() == useBlock) west = false;
        }
        
        // Bottom surface
        if (bottomSurfaces.get()) {
            if (bottomState.isReplaceable() || bottomState.getBlock() == useBlock) bottom = false;
        }
    
        return north || south || east || west || up || bottom;
    }
}
