package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;


public class Lavacast extends Module {

    private enum Stage {
        None,
        LavaDown,
        LavaUp,
        WaterDown,
        WaterUp
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgShape = settings.createGroup("Shape", false);
    private final Setting<Integer> tickInterval = sgGeneral.add(new IntSetting.Builder()
            .name("tick间隔")
            .description("Tick间隔")
            .defaultValue(2)
            .min(0)
            .sliderMax(20)
            .build()
    );

    private final Setting<Integer> distMin = sgShape.add(new IntSetting.Builder()
            .name("最小距离")
            .description("顶部平面截断距离")
            .defaultValue(5)
            .min(0)
            .sliderMax(10)
            .build()
    );
    private final Setting<Integer> lavaDownMult = sgShape.add(new IntSetting.Builder()
            .name("岩浆下降乘数")
            .description("控制岩浆下降的形状")
            .defaultValue(40)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> lavaUpMult = sgShape.add(new IntSetting.Builder()
            .name("岩浆上升乘数")
            .description("控制岩浆上升的形状")
            .defaultValue(8)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> waterDownMult = sgShape.add(new IntSetting.Builder()
            .name("水下降乘数")
            .description("控制水下降的形状")
            .defaultValue(4)
            .min(1)
            .sliderMax(100)
            .build()
    );
    private final Setting<Integer> waterUpMult = sgShape.add(new IntSetting.Builder()
            .name("水上升乘数")
            .description("控制水上升的形状")
            .defaultValue(1)
            .min(1)
            .sliderMax(100)
            .build()
    );
    
    private int dist;
    private BlockPos placeFluidPos;
    private int tick;
    private Stage stage = Stage.None;

    public Lavacast() {
        super(MeteorRejectsAddon.CATEGORY, "岩浆铸形", "自动进行岩浆铸形");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) toggle();
        tick = 0;
        stage = Stage.None;
        placeFluidPos = getTargetBlockPos();
        if (placeFluidPos == null) {
            placeFluidPos = mc.player.getBlockPos().down(2);
        } else {
            placeFluidPos = placeFluidPos.up();
        }
        dist=-1;
        getDistance(new Vec3i(1,0,0));
        getDistance(new Vec3i(-1,0,0));
        getDistance(new Vec3i(0,0,1));
        getDistance(new Vec3i(1,0,-1));
        if (dist<1) {
            error("无法定位底部。");
            toggle();
            return;
        }
        info("距离: (highlight)%d(default).", dist);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        tick++;
        if (shouldBreakOnTick()) return;
        if (dist < distMin.get()) toggle();
        tick = 0;
        if (checkMineBlock()) return;
        switch (stage) {
            case None: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeLava);
                stage = Stage.LavaDown;
                break;
            }
            case LavaDown: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::pickupLiquid);
                stage = Stage.LavaUp;
                break;
            }
            case LavaUp: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeWater);
                stage = Stage.WaterDown;
                break;
            }
            case WaterDown: {
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::pickupLiquid);
                stage = Stage.WaterUp;
                break;
            }
            case WaterUp: {
                dist--;
                Rotations.rotate(Rotations.getYaw(placeFluidPos),Rotations.getPitch(placeFluidPos),100, this::placeLava);
                stage = Stage.LavaDown;
                break;
            }
            default:
                break;
        }
    }

    private boolean shouldBreakOnTick() {
        if (stage == Stage.LavaDown && tick < dist*lavaDownMult.get()) return true;
        if (stage == Stage.LavaUp && tick < dist*lavaUpMult.get()) return true;
        if (stage == Stage.WaterDown && tick < dist*waterDownMult.get()) return true;
        if (stage == Stage.WaterUp && tick < dist*waterUpMult.get()) return true;
        if (tick < tickInterval.get()) return true;
        return false;
    }

    private boolean checkMineBlock() {
        if (stage == Stage.None && mc.world.getBlockState(placeFluidPos).getBlock() != Blocks.AIR) {
            Rotations.rotate(Rotations.getYaw(placeFluidPos), Rotations.getPitch(placeFluidPos), 100, this::updateBlockBreakingProgress);
            return true;
        }
        return false;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (placeFluidPos == null) return;
        double x1 = placeFluidPos.getX();
        double y1 = placeFluidPos.getY();
        double z1 = placeFluidPos.getZ();
        double x2 = x1+1;
        double y2 = y1+1;
        double z2 = z1+1;

        SettingColor color = new SettingColor(128, 128, 128);
        if (stage == Stage.LavaDown) color = new SettingColor(255, 180, 10);
        if (stage == Stage.LavaUp) color = new SettingColor(255, 180, 128);
        if (stage == Stage.WaterDown) color = new SettingColor(10, 10, 255);
        if (stage == Stage.WaterUp) color = new SettingColor(128, 128, 255);
        SettingColor color1 = color;
        color1.a = 75;

        event.renderer.box(x1, y1, z1, x2, y2, z2, color1, color, ShapeMode.Both, 0);
    }

    private void placeLava() {
        FindItemResult findItemResult = InvUtils.findInHotbar(Items.LAVA_BUCKET);
        if (!findItemResult.found()) {
            error("未找到岩浆桶。");
            toggle();
            return;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = findItemResult.slot();
        mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private void placeWater() {
        FindItemResult findItemResult = InvUtils.findInHotbar(Items.WATER_BUCKET);
        if (!findItemResult.found()) {
            error("未找到水桶。");
            toggle();
            return;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = findItemResult.slot();
        mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private void pickupLiquid() {
        FindItemResult findItemResult = InvUtils.findInHotbar(Items.BUCKET);
        if (!findItemResult.found()) {
            error("未找到桶。");
            toggle();
            return;
        }
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = findItemResult.slot();
        mc.interactionManager.interactItem(mc.player,Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private void updateBlockBreakingProgress() {
        mc.interactionManager.updateBlockBreakingProgress(placeFluidPos,Direction.UP);
    }

    private BlockPos getTargetBlockPos() {
        HitResult blockHit = mc.crosshairTarget;
        if (blockHit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return ((BlockHitResult) blockHit).getBlockPos();
    }

    private void getDistance(Vec3i offset) {
        BlockPos pos = placeFluidPos.down().add(offset);
        int new_dist;
        final BlockHitResult result = mc.world.raycast(new RaycastContext(
                Vec3d.ofCenter(pos), Vec3d.ofCenter(pos.down(250)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, mc.player
        ));
        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return;
        }
        new_dist = placeFluidPos.getY() - result.getBlockPos().getY();
        if (new_dist>dist) dist = new_dist;
    }

    @Override
    public String getInfoString() {
        return stage.toString();
    }
}
