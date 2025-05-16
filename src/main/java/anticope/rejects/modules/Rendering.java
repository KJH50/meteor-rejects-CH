package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;

public class Rendering extends Module {

    public enum Shader {
        None,
        Blur,
        Creeper,
        Invert,
        Spider,
    }

    private final SettingGroup sgInvisible = settings.createGroup("隐形");
    private final SettingGroup sgFun = settings.createGroup("趣味");

    private final Setting<Boolean> structureVoid = sgInvisible.add(new BoolSetting.Builder()
			.name("结构空位")
			.description("渲染结构空位方块。")
			.defaultValue(true)
            .onChanged(onChanged -> {
                if(this.isActive()) {
                    mc.worldRenderer.reload();
                }
            })
			.build()
	);

    private final Setting<Shader> shaderEnum = sgFun.add(new EnumSetting.Builder<Shader>()
        .name("着色器")
        .description("选择要使用的着色器")
        .defaultValue(Shader.None)
        .onChanged(this::onChanged)
        .build()
    );

    private final Setting<Boolean> dinnerbone = sgFun.add(new BoolSetting.Builder()
			.name("倒置")
			.description("对所有实体应用倒置效果")
			.defaultValue(false)
			.build()
	);

    private final Setting<Boolean> deadmau5Ears = sgFun.add(new BoolSetting.Builder()
			.name("deadmau5耳朵")
			.description("为所有玩家添加deadmau5耳朵")
			.defaultValue(false)
			.build()
	);

    private final Setting<Boolean> christmas = sgFun.add(new BoolSetting.Builder()
			.name("圣诞节")
			.description("随时显示圣诞节箱子")
			.defaultValue(false)
			.build()
	);
    
    private PostEffectProcessor shader = null;
    
    public Rendering() {
        super(MeteorRejectsAddon.CATEGORY, "渲染", "各种渲染调整");
    }

    @Override
    public void onActivate() {
        mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        mc.worldRenderer.reload();
    }

    public void onChanged(Shader s) {
        if (mc.world == null) return;
        String name = s.toString().toLowerCase();

        if (name.equals("none")) {
            this.shader = null;
            return;
        }

        Identifier shaderID = Identifier.ofVanilla(name);
        this.shader = mc.getShaderLoader().loadPostEffect(shaderID, DefaultFramebufferSet.MAIN_ONLY);
    }

    public boolean renderStructureVoid() {
        return this.isActive() && structureVoid.get();
    }

    public PostEffectProcessor getShaderEffect() {
        if (!this.isActive()) return null;
        return shader;
    }

    public boolean dinnerboneEnabled() {
        if (!this.isActive()) return false;
        return dinnerbone.get();
    }

    public boolean deadmau5EarsEnabled() {
        if (!this.isActive()) return false;
        return deadmau5Ears.get();
    }

    public boolean chistmas() {
        if (!this.isActive()) return false;
        return christmas.get();
    }
}
