package ca.lukegrahamlandry.basedefense.client;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    public static final KeyMapping OPEN = new KeyMapping("key.materials_open", GLFW.GLFW_KEY_M, "key.categories." + ModMain.MOD_ID);

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event){
        // NO-OP
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event){
        event.register(OPEN);
    }
}
