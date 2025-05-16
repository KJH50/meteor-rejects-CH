package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class KnockbackPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ka = sgGeneral.add(new BoolSetting.Builder()
            .name("仅在击杀光环时")
            .description("仅在使用击杀光环时增加击退效果。")
            .defaultValue(false)
            .build()
    );

    public KnockbackPlus() {
        super(MeteorRejectsAddon.CATEGORY, "增强击退", "在击中目标时增加击退效果。");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof IPlayerInteractEntityC2SPacket packet && packet.meteor$getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            Entity entity = packet.meteor$getEntity();

            if (!(entity instanceof LivingEntity) || (entity != Modules.get().get(KillAura.class).getTarget() && ka.get()))
                return;

            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }
    }
}
