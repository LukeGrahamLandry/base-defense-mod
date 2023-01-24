package ca.lukegrahamlandry.basedefense.block;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.init.TileTypeInit;
import ca.lukegrahamlandry.basedefense.material.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialGeneratorGuiPacket;
import ca.lukegrahamlandry.basedefense.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;

public class MaterialGeneratorBlock extends Block implements EntityBlock {
    public MaterialGeneratorBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TileTypeInit.MATERIAL_GENERATOR.get().create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()){
            return InteractionResult.CONSUME;
        } else {
            // RequestMaterialGeneratorGuiPacket.send(pPos);
            MaterialGeneratorTile.getAndDo(pLevel, pPos, (t) -> t.tryBind((ServerPlayer) pPlayer));
            BlockEntity tile = pLevel.getBlockEntity(pPos);
            if (tile instanceof LeveledMaterialGenerator){
                NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) pPlayer), new OpenMaterialGeneratorGuiPacket((ServerPlayer) pPlayer, (LeveledMaterialGenerator) tile, pPos));
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        MaterialGeneratorTile.getAndDo(pLevel, pPos, MaterialGeneratorTile::unBind);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
