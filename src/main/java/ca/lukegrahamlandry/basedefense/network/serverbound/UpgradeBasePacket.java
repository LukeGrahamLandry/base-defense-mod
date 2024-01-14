package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.material.old.Upgradable;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenBaseUpgradeGui;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialGeneratorGui;
import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpgradeBasePacket implements ServerSideHandler {
    private BlockPos pos;

    public UpgradeBasePacket(BlockPos pos){
        this.pos = pos;
    }

    @Override
    public void handle(ServerPlayer player) {
        Team team = TeamManager.get(player);
        MaterialCollection cost = BaseTier.get(team.getBaseTier()).getNextUpgradeCost();

        if (cost == null){
            player.displayClientMessage(Component.literal("You are at the max base tier"), false);
            return;
        }

        if (!team.getMaterials().canAfford(cost)){
            player.displayClientMessage(Component.literal("You can't afford to upgrade the base right now."), false);
            return;
        }

        team.getMaterials().subtract(cost);
        team.upgradeBaseTier();
        team.setDirty();

        BaseTile.setTeam((ServerLevel) player.level(), pos, team);

        new OpenBaseUpgradeGui(player, pos).sendToClient(player);
    }
}
