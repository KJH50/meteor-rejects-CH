package anticope.rejects.utils;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.List;

public class ConfigModifier {

    private static ConfigModifier INSTANCE;

    public final SettingGroup sgRejects = Config.get().settings.createGroup("Rejects");

    public final Setting<RejectsConfig.HttpAllowed> httpAllowed = sgRejects.add(new EnumSetting.Builder<RejectsConfig.HttpAllowed>()
            .name("HTTP允许")
            .description("更改可以访问的API端点。")
            .defaultValue(RejectsConfig.get().httpAllowed)
            .onChanged(v -> RejectsConfig.get().httpAllowed = v)
            .build()
    );

    public final Setting<String> httpUserAgent = sgRejects.add(new StringSetting.Builder()
            .name("HTTP用户代理")
            .description("更改HTTP用户代理。留空则不设置。")
            .defaultValue(RejectsConfig.get().httpUserAgent)
            .onChanged(v -> RejectsConfig.get().httpUserAgent = v)
            .build()
    );

    public final Setting<List<Module>> hiddenModules = sgRejects.add(new ModuleListSetting.Builder()
            .name("隐藏模块")
            .description("要隐藏的模块。")
            .defaultValue(List.of())
            .defaultValue(RejectsConfig.get().getHiddenModules())
            .onChanged(v -> RejectsConfig.get().setHiddenModules(v))
            .build()
    );

    public final Setting<Boolean> loadSystemFonts = sgRejects.add(new BoolSetting.Builder()
            .name("加载系统字体")
            .description("禁用此选项以加快启动速度。您可以将字体放入 meteor-client/fonts 文件夹中。重启后生效。")
            .defaultValue(true)
            .defaultValue(RejectsConfig.get().loadSystemFonts)
            .onChanged(v -> RejectsConfig.get().loadSystemFonts = v)
            .build()
    );

    public final Setting<Boolean> duplicateModuleNames = sgRejects.add(new BoolSetting.Builder()
            .name("允许重复模块名称")
            .description("允许重复的模块名称。最佳用于插件兼容性。")
            .defaultValue(false)
            .defaultValue(RejectsConfig.get().duplicateModuleNames)
            .onChanged(v -> RejectsConfig.get().duplicateModuleNames = v)
            .build()
    );

    public static ConfigModifier get() {
        if (INSTANCE == null) INSTANCE = new ConfigModifier();
        return INSTANCE;
    }
}