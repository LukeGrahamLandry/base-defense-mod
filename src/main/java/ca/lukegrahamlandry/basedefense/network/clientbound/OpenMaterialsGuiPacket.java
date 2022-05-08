package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
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
        storage.add(new ResourceLocation(ModMain.MOD_ID, "copper"), 50);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "lead"), 500);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "platinum"), 10);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "silver"), 9999);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "plutonium"), 5);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "nickle"), 70);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "cobalt"), 10);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "plastic"), 23480934);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "lithium"), 800);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "aluminum"), 75);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "steel"), 15);
        storage.add(new ResourceLocation(ModMain.MOD_ID, "gallium"), 59);
        production = new MaterialCollection();
        production.add(new ResourceLocation(ModMain.MOD_ID, "copper"), 5);
        production.add(new ResourceLocation(ModMain.MOD_ID, "lead"), 50);
        production.add(new ResourceLocation(ModMain.MOD_ID, "silver"), 1);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(this::openScreen);
        ctx.get().setPacketHandled(true);
    }

    private void openScreen() {
        Minecraft.getInstance().setScreen(new PlayerMaterialsScreen(storage, production));
    }
}
