package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class OpenBaseUpgradeGui implements ClientSideHandler {
    final BlockPos pos;
    MaterialCollection storage;
    MaterialCollection upgradeCost;
    int nextLevel;
    List<String> itemsUnlockedNextLevel;
    int rfPerTick;

    public OpenBaseUpgradeGui(ServerPlayer player, BlockPos pos) {
        this.pos = pos;
        Team team = TeamManager.get(player);
        this.storage = team.getMaterials();
        this.nextLevel = team.getBaseTier() + 1;
        this.upgradeCost = BaseTier.get(team.getBaseTier()).getNextUpgradeCost();
        if (upgradeCost != null){
            this.itemsUnlockedNextLevel = BaseTier.get(team.getBaseTier() + 1).getNewItems();
        }
        this.rfPerTick = BaseTier.get(team.getBaseTier()).rfPerTick;
    }

    @Override
    public void handle() {
        ClientPacketHandlers.openBaseUpgradeScreen(this);
    }
}
