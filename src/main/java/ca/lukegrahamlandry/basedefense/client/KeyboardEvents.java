package ca.lukegrahamlandry.basedefense.client;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.network.serverbound.RequestMaterialsGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyboardEvents {
    @SubscribeEvent
    public static void onPress(InputEvent.Key event){
        if (Minecraft.getInstance().player == null) return;

        if (ClientSetup.OPEN.consumeClick()) {
            RequestMaterialsGuiPacket.send();
            System.out.println("open");
        }
    }
}
