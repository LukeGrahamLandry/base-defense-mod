package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

public class OpenMaterialsGui implements ClientSideHandler {
    MaterialCollection storage;
    MaterialCollection production;

    public OpenMaterialsGui(ServerPlayer player){
        storage = MaterialsUtil.getTeamMaterials(player);
        production = MaterialsUtil.getTeamProduction(player);
    }

    @Override
    public void handle() {
        Minecraft.getInstance().setScreen(new PlayerMaterialsScreen(storage, production));
    }
}
