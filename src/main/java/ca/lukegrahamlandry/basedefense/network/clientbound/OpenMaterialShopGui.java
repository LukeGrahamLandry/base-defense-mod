package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.client.gui.ShopScreen;
import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class OpenMaterialShopGui implements ClientSideHandler {
    int baseTier;
    MaterialCollection storage;

    public OpenMaterialShopGui(Player player){
        System.out.println(player.level.isClientSide);
        Team team = TeamManager.get(player);
        storage = team.getMaterials();
        baseTier = team.getBaseTier();
    }

    @Override
    public void handle() {
        ClientPacketHandlers.openShipScreen(this);
    }
}
