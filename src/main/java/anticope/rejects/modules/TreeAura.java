package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.utils.WorldUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class TreeAura extends Module {
    // 添加 SortMode 枚举定义
    public enum SortMode {
        Farthest,
        Nearest
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotation = sgGeneral.add(new BoolSetting.Builder()
        .name("旋转")
        .description("为方块交互旋转")
        .defaultValue(false)
        .build()
    );
    private final Setting<Integer> plantDelay = sgGeneral.add(new IntSetting.Builder()
        .name("种植延迟")
        .description("种植树木之间的延迟")
        .defaultValue(6)
        .min(0)
        .sliderMax(25)
        .build()
    );
    private final Setting<Integer> bonemealDelay = sgGeneral.add(new IntSetting.Builder()
        .name("骨粉延迟")
        .description("在树木上放置骨粉之间的延迟")
        .defaultValue(3)
        .min(0)
        .sliderMax(25)
        .build()
    );
    private final Setting<Integer> rRange = sgGeneral.add(new IntSetting.Builder()
        .name("半径")
        .description("可以水平放置的距离")
        .defaultValue(4)
        .min(1)
        .sliderMax(5)
        .build()
    );
    private final Setting<Integer> yRange = sgGeneral.add(new IntSetting.Builder()
        .name("垂直范围")
        .description("可以垂直放置的距离")
        .defaultValue(3)
        .min(1)
        .sliderMax(5)
        .build()
    );
    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
        .name("排序模式")
        .description("如何对附近的树木/放置进行排序。")
        .defaultValue(SortMode.Farthest)
        .build()
    );

    private int bonemealTimer, plantTimer;


    public TreeAura() {
        super(MeteorRejectsAddon.CATEGORY, "树木光环", "在你周围种植树木");
    }

    @Override
    public void onActivate() {
        bonemealTimer = 0;
        plantTimer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {

        plantTimer--;
        bonemealTimer--;

        if (plantTimer <= 0) {
            BlockPos plantPos = findPlantLocation();
            if (plantPos == null) return;
            doPlant(plantPos);
            plantTimer = plantDelay.get();
        }

        if (bonemealTimer <= 0) {
            BlockPos p = findPlantedSapling();
            if (p == null) return;
            doBonemeal(p);
            bonemealTimer = bonemealDelay.get();
        }
    }


    private FindItemResult findBonemeal() {
        return InvUtils.findInHotbar(Items.BONE_MEAL);
    }

    private FindItemResult findSapling() {
        return InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof SaplingBlock);
    }

    private boolean isSapling(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() instanceof SaplingBlock;
    }

    private void doPlant(BlockPos plantPos) {
        FindItemResult sapling = findSapling();
        if (!sapling.found()) {
            error("No saplings in hotbar");
            toggle();
            return;
        }
        InvUtils.swap(sapling.slot(), false);
        if (rotation.get())
            Rotations.rotate(Rotations.getYaw(plantPos), Rotations.getPitch(plantPos), () -> mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(plantPos), Direction.UP, plantPos, false), 0)));
        else
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(plantPos), Direction.UP, plantPos, false), 0));
    }

    private void doBonemeal(BlockPos sapling) {
        FindItemResult bonemeal = findBonemeal();
        if (!bonemeal.found()) {
            error("No bonemeal in hotbar");
            toggle();
            return;
        }
        InvUtils.swap(bonemeal.slot(), false);
        if (rotation.get())
            Rotations.rotate(Rotations.getYaw(sapling), Rotations.getPitch(sapling), () -> mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(sapling), Direction.UP, sapling, false), 0)));
        else
            mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(Utils.vec3d(sapling), Direction.UP, sapling, false), 0));
    }

    private boolean canPlant(BlockPos pos) {
        Block b = mc.world.getBlockState(pos).getBlock();
        if (b.equals(Blocks.SHORT_GRASS) || b.equals(Blocks.GRASS_BLOCK) || b.equals(Blocks.DIRT) || b.equals(Blocks.COARSE_DIRT)) {
            final AtomicBoolean plant = new AtomicBoolean(true);
            IntStream.rangeClosed(1, 5).forEach(i -> {
                // Check above
                BlockPos check = pos.up(i);
                if (!mc.world.getBlockState(check).getBlock().equals(Blocks.AIR)) {
                    plant.set(false);
                    return;
                }
                // Check around
                for (CardinalDirection dir : CardinalDirection.values()) {
                    if (!mc.world.getBlockState(check.offset(dir.toDirection(), i)).getBlock().equals(Blocks.AIR)) {
                        plant.set(false);
                        return;
                    }
                }
            });
            return plant.get();
        }
        return false;
    }

    private List<BlockPos> findSaplings(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocc = new ArrayList<>();
        List<BlockPos> blocks = WorldUtils.getSphere(centerPos, radius, height);
        for (BlockPos b : blocks) if (isSapling(b)) blocc.add(b);
        return blocc;
    }

    private BlockPos findPlantedSapling() {
        List<BlockPos> saplings = findSaplings(mc.player.getBlockPos(), rRange.get(), yRange.get());
        if (saplings.isEmpty()) return null;
        saplings.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        if (sortMode.get().equals(SortMode.Farthest)) Collections.reverse(saplings);
        return saplings.get(0);
    }

    private List<BlockPos> getPlantLocations(BlockPos centerPos, int radius, int height) {
        ArrayList<BlockPos> blocc = new ArrayList<>();
        List<BlockPos> blocks = WorldUtils.getSphere(centerPos, radius, height);
        for (BlockPos b : blocks) if (canPlant(b)) blocc.add(b);
        return blocc;
    }

    private BlockPos findPlantLocation() {
        List<BlockPos> nearby = getPlantLocations(mc.player.getBlockPos(), rRange.get(), yRange.get());
        if (nearby.isEmpty()) return null;
        nearby.sort(Comparator.comparingDouble(PlayerUtils::distanceTo));
        if (sortMode.get().equals(SortMode.Farthest)) Collections.reverse(nearby);
        return nearby.get(0);
    }

    private double distanceBetween(BlockPos pos1, BlockPos pos2) {
        double d = pos1.getX() - pos2.getX();
        double e = pos1.getY() - pos2.getY();
        double f = pos1.getZ() - pos2.getZ();
        return MathHelper.sqrt((float) (d * d + e * e + f * f));
    }
}