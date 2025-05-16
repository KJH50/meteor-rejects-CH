package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import anticope.rejects.gui.screens.InteractionScreen;
import anticope.rejects.settings.StringMapSetting;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.value.Value;
import meteordevelopment.starscript.value.ValueMap;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class InteractionMenu extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgStyle = settings.createGroup("样式");

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
            .name("实体类型")
            .description("可交互的实体类型。")
            .defaultValue(EntityType.PLAYER)
            .build()
    );
    public final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
            .name("快捷键")
            .description("打开交互菜单的快捷键。")
            .action(this::onKey)
            .build()
    );
    public final Setting<Boolean> useCrosshairTarget = sgGeneral.add(new BoolSetting.Builder()
            .name("使用准星目标")
            .description("是否使用准星所指的目标。")
            .defaultValue(false)
            .build()
    );

    // 样式
    public final Setting<SettingColor> selectedDotColor = sgStyle.add(new ColorSetting.Builder()
            .name("选中点颜色")
            .description("选中时点的颜色。")
            .defaultValue(new SettingColor(76, 255, 0))
            .build()
    );
    public final Setting<SettingColor> dotColor = sgStyle.add(new ColorSetting.Builder()
            .name("点颜色")
            .description("未选中时点的颜色。")
            .defaultValue(new SettingColor(0, 148, 255))
            .build()
    );
    public final Setting<SettingColor> backgroundColor = sgStyle.add(new ColorSetting.Builder()
            .name("背景颜色")
            .description("背景颜色。")
            .defaultValue(new SettingColor(128, 128, 128, 128))
            .build()
    );
    public final Setting<SettingColor> borderColor = sgStyle.add(new ColorSetting.Builder()
            .name("边框颜色")
            .description("边框颜色。")
            .defaultValue(new SettingColor(0, 0, 0))
            .build()
    );
    public final Setting<SettingColor> textColor = sgStyle.add(new ColorSetting.Builder()
            .name("文本颜色")
            .description("文本颜色。")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    public final Setting<Map<String, String>> messages = sgGeneral.add(new StringMapSetting.Builder()
            .name("消息")
            .description("交互消息。")
            .renderer(StarscriptTextBoxRenderer.class)
            .build()
    );

    public InteractionMenu() {
        super(MeteorRejectsAddon.CATEGORY, "交互菜单", "当看向实体时显示的交互屏幕。");
        MeteorStarscript.ss.set("entity", () -> wrap(InteractionScreen.interactionMenuEntity));
    }

    public void onKey() {
        if (mc.player == null || mc.currentScreen != null) return;
        Entity e = null;
        if (useCrosshairTarget.get()) {
            e = mc.targetedEntity;
        } else {
            Optional<Entity> lookingAt = DebugRenderer.getTargetedEntity(mc.player, 20);
            if (lookingAt.isPresent()) {
                e = lookingAt.get();
            }
        }

        if (e == null) return;
        if (entities.get().contains(e.getType())) {
            mc.setScreen(new InteractionScreen(e, this));
        }
    }

    private static Value wrap(Entity entity) {
        if (entity == null) {
            return Value.map(new ValueMap()
                    .set("_toString", Value.null_())
                    .set("health", Value.null_())
                    .set("pos", Value.map(new ValueMap()
                            .set("_toString", Value.null_())
                            .set("x", Value.null_())
                            .set("y", Value.null_())
                            .set("z", Value.null_())
                    ))
                    .set("uuid", Value.null_())
            );
        }
        return Value.map(new ValueMap()
                .set("_toString", Value.string(entity.getName().getString()))
                .set("health", Value.number(entity instanceof LivingEntity e ? e.getHealth() : 0))
                .set("pos", Value.map(new ValueMap()
                        .set("_toString", posString(entity.getX(), entity.getY(), entity.getZ()))
                        .set("x", Value.number(entity.getX()))
                        .set("y", Value.number(entity.getY()))
                        .set("z", Value.number(entity.getZ()))
                ))
                .set("uuid", Value.string(entity.getUuidAsString()))
        );
    }

    private static Value posString(double x, double y, double z) {
        return Value.string(String.format("X: %.0f Y: %.0f Z: %.0f", x, y, z));
    }
}
