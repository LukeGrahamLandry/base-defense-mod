package ca.lukegrahamlandry.basedefense.game.block;

import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.tile.AttackableTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class AttackableBlock extends Block {
    public AttackableBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        if (!entity.level.isClientSide() && entity instanceof Player player && level.getBlockEntity(pos) instanceof AttackableTile tile){
            var team = TeamManager.get(player);
            return Objects.equals(team, tile.getOwnerTeam());
        }
        return super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        super.attack(pState, pLevel, pPos, pPlayer);
        if (pLevel.isClientSide()) return;
        if (pLevel.getBlockEntity(pPos) instanceof AttackableTile tile){
            var team = TeamManager.get(pPlayer);
            if (Objects.equals(team, tile.getOwnerTeam())) return;

            pPlayer.attack(tile.getAvatar());
        }
    }

    @Override
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        super.onProjectileHit(pLevel, pState, pHit, pProjectile);
        if (!pLevel.isClientSide()){
            if (pLevel.getBlockEntity(pHit.getBlockPos()) instanceof AttackableTile tile){
                if (!tile.isStillAlive()) return;
                if (pProjectile.getOwner() instanceof Player player){
                    var team = TeamManager.get(player);
                    if (Objects.equals(team, tile.getOwnerTeam())) return;
                }

                try {
                    Method onHitEntity = Projectile.class.getDeclaredMethod("onHitEntity", EntityHitResult.class);
                    onHitEntity.setAccessible(true);
                    EntityHitResult fakeHit = new EntityHitResult(tile.getAvatar());
                    onHitEntity.invoke(pProjectile, fakeHit);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
