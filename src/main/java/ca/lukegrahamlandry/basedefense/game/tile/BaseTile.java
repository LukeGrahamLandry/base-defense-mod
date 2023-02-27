package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseTile extends BlockEntity implements GeoBlockEntity, AttackTargetable {
    int tier = 0;
    Integer rfGenerated;
    UUID uuid = UUID.randomUUID();
    UUID teamUUID;
    int openAnimTick = 20;
    public BaseTile(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistry.BASE_TILE.get(), pPos, pBlockState);
    }

    public static void setTeam(ServerLevel level, BlockPos pos, Team team){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BaseTile base){
            base.tier = team.getBaseTier();
            base.rfGenerated = null;
            base.teamUUID = team.getId();
            AttackLocation.targets.put(base.uuid, base);
            team.addAttackLocation(new AttackLocation(level, base.getBlockPos(), base.uuid));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("teamTier", tier);
        pTag.putUUID("targetuuid", uuid);
        if (teamUUID != null){
            pTag.putUUID("teamuuid", teamUUID);
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("teamTier")){
            this.tier = pTag.getInt("teamTier");
        }
        if (pTag.contains("targetuuid")){
            this.uuid = pTag.getUUID("targetuuid");
            AttackLocation.targets.put(this.uuid, this);
        }
        if (pTag.contains("teamuuid")){
            this.teamUUID = pTag.getUUID("teamuuid");
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


    float health = maxHealth();
    AttackTargetAvatar avatar;
    @Override
    public List<BlockPos> getSpawnLocations() {
        return Arrays.asList(this.getBlockPos().east(10), this.getBlockPos().west(10), this.getBlockPos().north(10), this.getBlockPos().south(10));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        this.health -= amount;
        Team team = TeamManager.getTeamById(this.teamUUID);
        if (team != null) team.message(Component.literal("Your base block took " + amount + " damage! Now at " + health + " health."));
        if (!isStillAlive()) this.onDie();
        return true;
    }

    @Override
    public float health() {
        return this.health;
    }

    @Override
    public float maxHealth() {
        return 40;
    }

    @Override
    public LivingEntity getAvatar() {
        if (this.avatar == null) this.avatar = new AttackTargetAvatar(this.level, this, this.getBlockPos());
        return this.avatar;
    }

    @Override
    public Team getOwnerTeam() {
        return TeamManager.getTeamById(this.teamUUID);
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void onDie() {
        Team team = getOwnerTeam();
        if (team == null){
            ModMain.LOGGER.error("MaterialGeneratorTile#onDie team null");
            return;
        }
        getOwnerTeam().getAttackOptions().removeIf(location -> Objects.equals(this.uuid, location.id));
        TeamManager.getTeamById(this.teamUUID).message(Component.literal("Your base block was destroyed!"));
        AttackLocation.destroyed.add(this);

        AttackTargetable.super.onDie();
        this.level.removeBlock(this.getBlockPos(), false);
        this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 4.0F, Level.ExplosionInteraction.TNT);
    }
}
