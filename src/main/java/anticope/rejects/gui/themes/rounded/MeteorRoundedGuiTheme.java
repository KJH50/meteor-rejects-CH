package anticope.rejects.gui.themes.rounded;

import anticope.rejects.gui.themes.rounded.widgets.*;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorDropdown;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorSlider;
import anticope.rejects.gui.themes.rounded.widgets.input.WMeteorTextBox;
import anticope.rejects.gui.themes.rounded.widgets.pressable.*;
import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.gui.utils.CharFilter;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WView;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WDropdown;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.*;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MeteorRoundedGuiTheme extends GuiTheme {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("颜色");
    private final SettingGroup sgTextColors = settings.createGroup("文本");
    private final SettingGroup sgBackgroundColors = settings.createGroup("背景");
    private final SettingGroup sgOutline = settings.createGroup("轮廓");
    private final SettingGroup sgSeparator = settings.createGroup("分隔符");
    private final SettingGroup sgScrollbar = settings.createGroup("滚动条");
    private final SettingGroup sgSlider = settings.createGroup("滑块");
    private final SettingGroup sgStarscript = settings.createGroup("Starscript");

    // General

    public final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("缩放比例")
            .description("GUI 的缩放比例。")
            .defaultValue(1)
            .min(0.75)
            .sliderMin(0.75)
            .sliderMax(4)
            .onSliderRelease()
            .onChanged(aDouble -> {
                if (mc.currentScreen instanceof WidgetScreen) ((WidgetScreen) mc.currentScreen).invalidate();
            })
            .build()
    );

    public final Setting<AlignmentX> moduleAlignment = sgGeneral.add(new EnumSetting.Builder<AlignmentX>()
            .name("模块对齐方式")
            .description("模块标题的对齐方式。")
            .defaultValue(AlignmentX.Center)
            .build()
    );

    public final Setting<Boolean> categoryIcons = sgGeneral.add(new BoolSetting.Builder()
            .name("类别图标")
            .description("在模块类别中添加物品图标。")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> hideHUD = sgGeneral.add(new BoolSetting.Builder()
            .name("隐藏HUD")
            .description("在 GUI 中隐藏 HUD。")
            .defaultValue(false)
            .onChanged(v -> {
                if (mc.currentScreen instanceof WidgetScreen) mc.options.hudHidden = v;
            })
            .build()
    );

    public final Setting<Integer> round = sgGeneral.add(new IntSetting.Builder()
            .name("圆角程度")
            .description("窗口的圆角程度。")
            .defaultValue(0)
            .min(0)
            .max(20)
            .sliderMin(0)
            .sliderMax(15)
            .build()
    );

    // Colors

    public final Setting<SettingColor> accentColor = color("主色调", "GUI 的主色调。", new SettingColor(135, 0, 255));
    public final Setting<SettingColor> checkboxColor = color("复选框颜色", "复选框的颜色。", new SettingColor(135, 0, 255));
    public final Setting<SettingColor> plusColor = color("加号按钮颜色", "加号按钮的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> minusColor = color("减号按钮颜色", "减号按钮的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> favoriteColor = color("收藏按钮颜色", "收藏按钮被选中时的颜色。", new SettingColor(255, 255, 0));

    // Text

    public final Setting<SettingColor> textColor = color(sgTextColors, "文本颜色", "文本的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> textSecondaryColor = color(sgTextColors, "次要文本颜色", "次要文本的颜色。", new SettingColor(150, 150, 150));
    public final Setting<SettingColor> textHighlightColor = color(sgTextColors, "文本高亮颜色", "文本高亮的颜色。", new SettingColor(45, 125, 245, 100));
    public final Setting<SettingColor> titleTextColor = color(sgTextColors, "标题文本颜色", "标题文本的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> loggedInColor = color(sgTextColors, "已登录账户颜色", "已登录账户名称的颜色。", new SettingColor(45, 225, 45));
    public final Setting<SettingColor> placeholderColor = color(sgTextColors, "占位符颜色", "占位符文本的颜色。", new SettingColor(255, 255, 255, 20));

    // Background

    public final ThreeStateColorSetting backgroundColor = new ThreeStateColorSetting(
            sgBackgroundColors,
            "背景颜色",
            new SettingColor(20, 20, 20, 200),
            new SettingColor(30, 30, 30, 200),
            new SettingColor(40, 40, 40, 200)
    );

    public final Setting<SettingColor> moduleBackground = color(sgBackgroundColors, "模块背景颜色", "模块激活时的背景颜色。", new SettingColor(50, 50, 50));

    // Outline

    public final ThreeStateColorSetting outlineColor = new ThreeStateColorSetting(
            sgOutline,
            "轮廓颜色",
            new SettingColor(0, 0, 0),
            new SettingColor(10, 10, 10),
            new SettingColor(20, 20, 20)
    );

    // Separator

    public final Setting<SettingColor> separatorText = color(sgSeparator, "分隔符文本颜色", "分隔符文本的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorCenter = color(sgSeparator, "分隔符中心颜色", "分隔符中心的颜色。", new SettingColor(255, 255, 255));
    public final Setting<SettingColor> separatorEdges = color(sgSeparator, "分隔符边缘颜色", "分隔符边缘的颜色。", new SettingColor(225, 225, 225, 150));

    // Scrollbar

    public final ThreeStateColorSetting scrollbarColor = new ThreeStateColorSetting(
            sgScrollbar,
            "滚动条颜色",
            new SettingColor(30, 30, 30, 200),
            new SettingColor(40, 40, 40, 200),
            new SettingColor(50, 50, 50, 200)
    );

    // Slider

    public final ThreeStateColorSetting sliderHandle = new ThreeStateColorSetting(
            sgSlider,
            "滑块手柄颜色",
            new SettingColor(0, 255, 180),
            new SettingColor(0, 240, 165),
            new SettingColor(0, 225, 150)
    );

    public final Setting<SettingColor> sliderLeft = color(sgSlider, "滑块左侧颜色", "滑块左侧部分的颜色。", new SettingColor(0, 150, 80));
    public final Setting<SettingColor> sliderRight = color(sgSlider, "滑块右侧颜色", "滑块右侧部分的颜色。", new SettingColor(50, 50, 50));

    // Starscript

    private final Setting<SettingColor> starscriptText = color(sgStarscript, "Starscript文本颜色", "Starscript 代码中文本的颜色。", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptBraces = color(sgStarscript, "Starscript大括号颜色", "Starscript 代码中大括号的颜色。", new SettingColor(150, 150, 150));
    private final Setting<SettingColor> starscriptParenthesis = color(sgStarscript, "Starscript括号颜色", "Starscript 代码中括号的颜色。", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptDots = color(sgStarscript, "Starscript点颜色", "Starscript 代码中点的颜色。", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptCommas = color(sgStarscript, "Starscript逗号颜色", "Starscript 代码中逗号的颜色。", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptOperators = color(sgStarscript, "Starscript操作符颜色", "Starscript 代码中操作符的颜色。", new SettingColor(169, 183, 198));
    private final Setting<SettingColor> starscriptStrings = color(sgStarscript, "Starscript字符串颜色", "Starscript 代码中字符串的颜色。", new SettingColor(106, 135, 89));
    private final Setting<SettingColor> starscriptNumbers = color(sgStarscript, "Starscript数字颜色", "Starscript 代码中数字的颜色。", new SettingColor(104, 141, 187));
    private final Setting<SettingColor> starscriptKeywords = color(sgStarscript, "Starscript关键字颜色", "Starscript 代码中关键字的颜色。", new SettingColor(204, 120, 50));
    private final Setting<SettingColor> starscriptAccessedObjects = color(sgStarscript, "Starscript访问对象颜色", "Starscript 代码中访问对象的颜色。", new SettingColor(152, 118, 170));

    public MeteorRoundedGuiTheme() {
        super("Meteor 圆角主题");

        settingsFactory = new DefaultSettingsWidgetFactory(this);
    }

    private Setting<SettingColor> color(SettingGroup group, String name, String description, SettingColor color) {
        return group.add(new ColorSetting.Builder()
                .name(name + "-颜色")
                .description(description)
                .defaultValue(color)
                .build());
    }

    private Setting<SettingColor> color(String name, String description, SettingColor color) {
        return color(sgColors, name, description, color);
    }

    // Widgets

    @Override
    public WWindow window(WWidget icon, String title) {
        return w(new WMeteorWindow(icon, title));
    }

    @Override
    public WLabel label(String text, boolean title, double maxWidth) {
        if (maxWidth == 0) return w(new WMeteorLabel(text, title));
        return w(new WMeteorMultiLabel(text, title, maxWidth));
    }

    @Override
    public WHorizontalSeparator horizontalSeparator(String text) {
        return w(new WMeteorHorizontalSeparator(text));
    }

    @Override
    public WVerticalSeparator verticalSeparator() {
        return w(new WMeteorVerticalSeparator());
    }

    @Override
    protected WButton button(String text, GuiTexture texture) {
        return w(new WMeteorButton(text, texture));
    }

    @Override
    public WMinus minus() {
        return w(new WMeteorMinus());
    }

    @Override
    public WPlus plus() {
        return w(new WMeteorPlus());
    }

    @Override
    public WCheckbox checkbox(boolean checked) {
        return w(new WMeteorCheckbox(checked));
    }

    @Override
    public WSlider slider(double value, double min, double max) {
        return w(new WMeteorSlider(value, min, max));
    }

    @Override
    public WTextBox textBox(String text, String placeholder, CharFilter filter, Class<? extends WTextBox.Renderer> renderer) {
        return w(new WMeteorTextBox(text, placeholder, filter, renderer));
    }

    @Override
    public <T> WDropdown<T> dropdown(T[] values, T value) {
        return w(new WMeteorDropdown<>(values, value));
    }

    @Override
    public WTriangle triangle() {
        return w(new WMeteorTriangle());
    }

    @Override
    public WTooltip tooltip(String text) {
        return w(new WMeteorTooltip(text));
    }

    @Override
    public WView view() {
        return w(new WMeteorView());
    }

    @Override
    public WSection section(String title, boolean expanded, WWidget headerWidget) {
        return w(new WMeteorSection(title, expanded, headerWidget));
    }

    @Override
    public WAccount account(WidgetScreen screen, Account<?> account) {
        return w(new WMeteorAccount(screen, account));
    }

    @Override
    public WWidget module(Module module) {
        return w(new WMeteorModule(module));
    }

    @Override
    public WQuad quad(Color color) {
        return w(new WMeteorQuad(color));
    }

    @Override
    public WTopBar topBar() {
        return w(new WMeteorTopBar());
    }

    @Override
    public WFavorite favorite(boolean checked) {
        return w(new WMeteorFavorite(checked));
    }

    // Colors

    @Override
    public Color textColor() {
        return textColor.get();
    }

    @Override
    public Color textSecondaryColor() {
        return textSecondaryColor.get();
    }

    //     Starscript

    @Override
    public Color starscriptTextColor() {
        return starscriptText.get();
    }

    @Override
    public Color starscriptBraceColor() {
        return starscriptBraces.get();
    }

    @Override
    public Color starscriptParenthesisColor() {
        return starscriptParenthesis.get();
    }

    @Override
    public Color starscriptDotColor() {
        return starscriptDots.get();
    }

    @Override
    public Color starscriptCommaColor() {
        return starscriptCommas.get();
    }

    @Override
    public Color starscriptOperatorColor() {
        return starscriptOperators.get();
    }

    @Override
    public Color starscriptStringColor() {
        return starscriptStrings.get();
    }

    @Override
    public Color starscriptNumberColor() {
        return starscriptNumbers.get();
    }

    @Override
    public Color starscriptKeywordColor() {
        return starscriptKeywords.get();
    }

    @Override
    public Color starscriptAccessedObjectColor() {
        return starscriptAccessedObjects.get();
    }

    // Other

    @Override
    public TextRenderer textRenderer() {
        return TextRenderer.get();
    }

    @Override
    public double scale(double value) {
        return value * scale.get();
    }

    @Override
    public boolean categoryIcons() {
        return categoryIcons.get();
    }

    @Override
    public boolean hideHUD() {
        return hideHUD.get();
    }

    public int roundAmount() {
        return round.get();
    }

    public class ThreeStateColorSetting {
        private final Setting<SettingColor> normal, hovered, pressed;

        public ThreeStateColorSetting(SettingGroup group, String name, SettingColor c1, SettingColor c2, SettingColor c3) {
            normal = color(group, name, "Color of " + name + ".", c1);
            hovered = color(group, "hovered-" + name, "Color of " + name + " when hovered.", c2);
            pressed = color(group, "pressed-" + name, "Color of " + name + " when pressed.", c3);
        }

        public SettingColor get() {
            return normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered, boolean bypassDisableHoverColor) {
            if (pressed) return this.pressed.get();
            return (hovered && (bypassDisableHoverColor || !disableHoverColor)) ? this.hovered.get() : this.normal.get();
        }

        public SettingColor get(boolean pressed, boolean hovered) {
            return get(pressed, hovered, false);
        }
    }
}
