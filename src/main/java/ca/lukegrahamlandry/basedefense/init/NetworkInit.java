package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGuiPacket;
import ca.lukegrahamlandry.basedefense.network.serverbound.RequestMaterialsGuiPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkInit {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;

    public static int nextID() {
        return ID++;
    }

    public static void registerPackets() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModMain.MOD_ID, "packets"), () -> "1.0", s -> true, s -> true);

        INSTANCE.registerMessage(nextID(), OpenMaterialsGuiPacket.class, OpenMaterialsGuiPacket::encode, OpenMaterialsGuiPacket::new, OpenMaterialsGuiPacket::handle);
        INSTANCE.registerMessage(nextID(), RequestMaterialsGuiPacket.class, RequestMaterialsGuiPacket::encode, RequestMaterialsGuiPacket::new, RequestMaterialsGuiPacket::handle);
    }
}
