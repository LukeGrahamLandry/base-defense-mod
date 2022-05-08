package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.util.MaterialCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenMaterialsGuiPacket {
    MaterialCollection storage;
    MaterialCollection production;

    public OpenMaterialsGuiPacket(FriendlyByteBuf buf) {
        storage = new MaterialCollection(buf);
        production = new MaterialCollection(buf);
    }

    public void encode(FriendlyByteBuf buf){
        storage.toBytes(buf);
        production.toBytes(buf);
    }

    public OpenMaterialsGuiPacket(ServerPlayer player){
        // todo read from player capability
        storage = new MaterialCollection();
        storage.add(new ResourceLocation(ModMain.MOD_ID, "hydrogen"), 50);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "helium"), 500);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "lithium"), 10);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "berilyum"), 9999);
        production = new MaterialCollection();
        production.add(new ResourceLocation(ModMain.MOD_ID, "hydrogen"), 5);
        production.add(new ResourceLocation(ModMain.MOD_ID, "helium"), 50);
        production.add(new ResourceLocation(ModMain.MOD_ID, "lithium"), 1);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(this::openScreen);
        ctx.get().setPacketHandled(true);
    }

    private void openScreen() {
        Minecraft.getInstance().setScreen(new PlayerMaterialsScreen(storage, production));
    }
}
