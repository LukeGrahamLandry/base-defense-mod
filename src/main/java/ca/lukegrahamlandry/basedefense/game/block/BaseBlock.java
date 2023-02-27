package ca.lukegrahamlandry.basedefense.game.block;

import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenBaseUpgradeGui;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BaseBlock extends Block implements EntityBlock {
    public BaseBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BaseTile.setTeam((ServerLevel) pLevel, pPos, TeamManager.get(pPlayer));
            new OpenBaseUpgradeGui((ServerPlayer) pPlayer, pPos).sendToClient((ServerPlayer) pPlayer);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pPlacer instanceof ServerPlayer p){
            BaseTile.setTeam((ServerLevel) pLevel, pPos, TeamManager.get(p));
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModRegistry.BASE_TILE.get().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (level, pos, state, tile) -> {
            if (tile instanceof BaseTile) ((BaseTile) tile).tick();
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        if (!pLevel.isClientSide()){
            if (pLevel.getBlockEntity(pPos) instanceof BaseTile base){
                base.onDie();
            }
        }
    }

    boolean isExploding = false;  // Stops infinite recursion since onDie causes an explosion as well
    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide() && !isExploding){
            isExploding = true;
            if (level.getBlockEntity(pos) instanceof BaseTile base){
                base.onDie();
            }
            isExploding = false;
        }
        super.onBlockExploded(state, level, pos, explosion);
    }
}
