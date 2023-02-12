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

public class OpenMaterialGeneratorGui implements ClientSideHandler {
    MaterialCollection currentProduction;
    MaterialCollection nextProduction;
    MaterialCollection upgradeCost;
    MaterialCollection playerMaterials;
    int tier;
    ResourceLocation type;
    BlockPos pos;

    public OpenMaterialGeneratorGui(ServerPlayer player, LeveledMaterialGenerator generator, BlockPos pos){
        playerMaterials = MaterialsUtil.getTeamMaterials(player);
        currentProduction = generator.getStats().getProduction();
        if (!generator.getStats().isMaxTier()){
            nextProduction = generator.getStats().getNextTier().getProduction();
            upgradeCost = generator.getUpgradeCost();
        }
        type = generator.getStats().type;
        tier = generator.getTier();
        this.pos = pos;
    }


    @Override
    public void handle() {
        ClientPacketHandlers.openGeneratorScreen(this);
    }
}
