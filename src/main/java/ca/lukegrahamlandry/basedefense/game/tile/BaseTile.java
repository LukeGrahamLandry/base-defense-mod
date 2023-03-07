package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseTile extends AttackableTile implements GeoBlockEntity {
    int tier = 0;
    Integer rfGenerated;
    int openAnimTick = 20;
    public BaseTile(BlockPos pPos, BlockState pBlockState) {
        super(40, ModRegistry.BASE_TILE.get(), pPos, pBlockState);
    }

    public static void setTeam(ServerLevel level, BlockPos pos, Team team){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BaseTile base){
            // If a team is already set, and it's not the same team, and the set team exists...
            if (base.teamUUID != null && !base.teamUUID.equals(team.getId()) && TeamManager.getTeamById(base.teamUUID) != null){
                team = TeamManager.getTeamById(base.teamUUID);
            }

            base.tier = team.getBaseTier();
            base.rfGenerated = null;
            base.teamUUID = team.getId();
            AttackLocation.targets.put(base.uuid, base);
            team.updateBaseLocation(new AttackLocation(level, base.getBlockPos(), base.uuid));
        }
    }

    public void invalidate(){
        if (this.getOwnerTeam() != null){
            this.getOwnerTeam().removeAttackLocation(this.uuid);
            AttackLocation.destroyed.add(this);
            AttackLocation.targets.remove(this.uuid);
            this.teamUUID = null;
        }

        this.level.removeBlock(this.getBlockPos(), false);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("teamTier", tier);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("teamTier")){
            this.tier = pTag.getInt("teamTier");
        }
        this.rfGenerated = null;
    }

    public void serverTick() {
        if (this.rfGenerated == null){
            this.rfGenerated = BaseTier.get(this.tier).rfPerTick;
        }

        if (this.rfGenerated == 0) {
            return;
        }

        AtomicInteger capacity = new AtomicInteger(this.rfGenerated);

        for (Direction direction : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be != null) {
                boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                            if (handler.canReceive()) {
                                int received = handler.receiveEnergy(capacity.get(), false);
                                capacity.addAndGet(-received);
                                setChanged();
                                return capacity.get() > 0;
                            } else {
                                return true;
                            }
                        }
                ).orElse(true);
                if (!doContinue) {
                    return;
                }
            }
        }
    }

    // Animation

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation OPENING_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");

    private PlayState animation(AnimationState<BaseTile> state){
        state.setAnimation(this.openAnimTick > 0 ? OPENING_ANIM : IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", this::animation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void tick() {
        if (!this.hasLevel()) return;  // Can't happen but im afraid

        if (this.level.isClientSide()){
            if (openAnimTick > 0) openAnimTick--;
        } else {
            this.serverTick();
        }
    }

    // Attacks

    @Override
    public void onDie() {
        super.onDie();
        Team team = getOwnerTeam();
        if (team == null){
            ModMain.LOGGER.error("MaterialGeneratorTile#onDie team null");
            return;
        }
        team.onBaseDie();
        this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 4.0F, Level.ExplosionInteraction.TNT);
    }
}
