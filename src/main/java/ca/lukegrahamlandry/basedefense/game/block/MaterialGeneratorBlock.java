package ca.lukegrahamlandry.basedefense.game.block;

import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialGeneratorGui;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MaterialGeneratorBlock extends Block implements EntityBlock {
    private boolean isTerrainGenerated;

    public MaterialGeneratorBlock(boolean isTerrainGenerated, Properties p_49795_) {
        super(p_49795_);
        this.isTerrainGenerated = isTerrainGenerated;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModRegistry.MATERIAL_GENERATOR_TILE.get().create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide()){
            return InteractionResult.CONSUME;
        } else {
            if (this.isTerrainGenerated){
                MaterialGeneratorTile.getAndDo(pLevel, pPos, MaterialGeneratorTile::setIsTerrain);
            }

            MaterialGeneratorTile.getAndDo(pLevel, pPos, (t) -> t.tryBind((ServerPlayer) pPlayer));
            BlockEntity tile = pLevel.getBlockEntity(pPos);
            if (tile instanceof LeveledMaterialGenerator){
                new OpenMaterialGeneratorGui((ServerPlayer) pPlayer, (LeveledMaterialGenerator) tile, pPos).sendToClient((ServerPlayer) pPlayer);
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        MaterialGeneratorTile.getAndDo(pLevel, pPos, MaterialGeneratorTile::unBind);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pPlacer instanceof ServerPlayer p){
            if (this.isTerrainGenerated){
                MaterialGeneratorTile.getAndDo(pLevel, pPos, MaterialGeneratorTile::setIsTerrain);
            }

            MaterialGeneratorTile.getAndDo(pLevel, pPos, (t) -> t.tryBind(p));
        }
    }
}
