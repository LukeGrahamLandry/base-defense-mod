package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.client.gui.GeneratorUpgradeScreen;
import ca.lukegrahamlandry.basedefense.material.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.material.MaterialsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenMaterialGeneratorGuiPacket {
    MaterialCollection currentProduction;
    MaterialCollection nextProduction;
    MaterialCollection upgradeCost;
    MaterialCollection playerMaterials;
    int tier;
    ResourceLocation type;

    public OpenMaterialGeneratorGuiPacket(FriendlyByteBuf buf) {
        tier = buf.readInt();
        type = buf.readResourceLocation();
        currentProduction = new MaterialCollection(buf);
        nextProduction = new MaterialCollection(buf);
        upgradeCost = new MaterialCollection(buf);
        playerMaterials = new MaterialCollection(buf);
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeInt(this.tier);
        buf.writeResourceLocation(this.type);
        currentProduction.toBytes(buf);
        nextProduction.toBytes(buf);
        upgradeCost.toBytes(buf);
        playerMaterials.toBytes(buf);
    }

    public OpenMaterialGeneratorGuiPacket(ServerPlayer player, LeveledMaterialGenerator generator){
        playerMaterials = MaterialsUtil.getMaterials(player);
        currentProduction = generator.getProduction();
        nextProduction = generator.getNextProduction();
        upgradeCost = generator.getUpgradeCost();
        type = generator.getGenType();
        tier = generator.getTier();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(this::openScreen);
        ctx.get().setPacketHandled(true);
    }

    private void openScreen() {
        Minecraft.getInstance().setScreen(new GeneratorUpgradeScreen(tier, type, currentProduction, nextProduction, upgradeCost, playerMaterials));
    }
}
