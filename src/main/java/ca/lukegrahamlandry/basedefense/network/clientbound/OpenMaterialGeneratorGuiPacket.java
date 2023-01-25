package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import ca.lukegrahamlandry.basedefense.client.gui.GeneratorUpgradeScreen;
import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class OpenMaterialGeneratorGuiPacket implements ClientSideHandler {
    MaterialCollection currentProduction;
    MaterialCollection nextProduction;
    MaterialCollection upgradeCost;
    MaterialCollection playerMaterials;
    int tier;
    ResourceLocation type;
    BlockPos pos;

    public OpenMaterialGeneratorGuiPacket(ServerPlayer player, LeveledMaterialGenerator generator, BlockPos pos){
        playerMaterials = MaterialsUtil.getTeamMaterials(player);
        currentProduction = generator.getProduction();
        nextProduction = generator.getNextProduction();
        upgradeCost = generator.getUpgradeCost();
        type = generator.getGenType();
        tier = generator.getTier();
        this.pos = pos;
    }


    @Override
    public void handle() {
        Minecraft.getInstance().setScreen(new GeneratorUpgradeScreen(tier, type, currentProduction, nextProduction, upgradeCost, playerMaterials, pos));
    }
}
