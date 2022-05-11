package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.material.MaterialGenerationHandler;
import ca.lukegrahamlandry.basedefense.material.MaterialStorageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
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
        storage = MaterialStorageHandler.get(player);
        production = MaterialGenerationHandler.get(player.getLevel()).getProduction(player.getUUID());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(this::openScreen);
        ctx.get().setPacketHandled(true);
    }

    private void openScreen() {
        Minecraft.getInstance().setScreen(new PlayerMaterialsScreen(storage, production));
    }
}
