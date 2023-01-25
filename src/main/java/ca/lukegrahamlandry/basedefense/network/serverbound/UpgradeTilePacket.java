package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.material.old.Upgradable;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialGeneratorGuiPacket;
import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public class UpgradeTilePacket implements ServerSideHandler {
    private final BlockPos tilePosition;

    public UpgradeTilePacket(BlockPos pos){
        this.tilePosition = pos;
    }

    @Override
    public void handle(ServerPlayer player) {
        BlockEntity tile = player.getLevel().getBlockEntity(this.tilePosition);

        // do the update
        if (tile instanceof Upgradable){
            ((Upgradable) tile).tryUpgrade(player);
        }

        // reopen gui with the updated data
        if (tile instanceof LeveledMaterialGenerator){
            new OpenMaterialGeneratorGuiPacket(player, (LeveledMaterialGenerator) tile, this.tilePosition).sendToClient(player);
        }
    }
}
