package anticope.rejects.modules;


import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;

public class VehicleOneHit extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
            .name("数量")
            .description("要发送的数据包数量。")
            .defaultValue(16)
            .range(1, 100)
            .sliderRange(1, 20)
            .build()
    );

    public VehicleOneHit() {
        super(MeteorRejectsAddon.CATEGORY, "车辆一击", "一击摧毁车辆。");
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerInteractEntityC2SPacket)
            || !(mc.crosshairTarget instanceof EntityHitResult ehr)
            || (!(ehr.getEntity() instanceof AbstractMinecartEntity) && !(ehr.getEntity() instanceof BoatEntity))
        ) return;

        for (int i = 0; i < amount.get() - 1; i++) {
            mc.player.networkHandler.getConnection().send(event.packet, null);
        }
    }
}