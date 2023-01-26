package ca.lukegrahamlandry.basedefense.game.block;

import ca.lukegrahamlandry.basedefense.base.MaterialShop;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialShopGui;
import ca.lukegrahamlandry.lib.resources.DataPackSyncMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MaterialShopBlock extends Block {
    public MaterialShopBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            new DataPackSyncMessage(MaterialShop.SHOP_ENTRIES).sendToClient((ServerPlayer) pPlayer);  // TODO: temp fix
            new OpenMaterialShopGui(pPlayer).sendToClient((ServerPlayer) pPlayer);
        }
        return InteractionResult.CONSUME;
    }
}