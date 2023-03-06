package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AttackableTile extends BlockEntity implements AttackTargetable {
    protected int maxHealth;
    protected UUID uuid;
    protected UUID teamUUID;
    long nextRegenTime = 0;

    public AttackableTile(int maxHealth, BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.uuid = UUID.randomUUID();
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains(ModMain.MOD_ID + ":health")){
            this.health = pTag.getFloat(ModMain.MOD_ID + ":health");
        } else {
            this.health = this.maxHealth;
        }
        if (pTag.contains(ModMain.MOD_ID + ":uuid")){
            this.uuid = pTag.getUUID(ModMain.MOD_ID + ":uuid");
        }
        if (pTag.contains(ModMain.MOD_ID + ":team")){
            this.teamUUID = pTag.getUUID(ModMain.MOD_ID + ":team");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putFloat(ModMain.MOD_ID + ":health", health);
        pTag.putUUID(ModMain.MOD_ID + ":uuid", uuid);
        if (teamUUID != null){
            pTag.putUUID(ModMain.MOD_ID + ":team", teamUUID);
        }
    }

    public void onTeamBaseDie() {
        this.teamUUID = null;
        this.uuid = UUID.randomUUID();
        this.setChanged();
    }

    float health;
    AttackTargetAvatar avatar;
    @Override
    public List<BlockPos> getSpawnLocations() {
        return Arrays.asList(this.getBlockPos().east(10), this.getBlockPos().west(10), this.getBlockPos().north(10), this.getBlockPos().south(10));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        amount = (int) amount;
        if (amount <= 0) return false;

        this.health -= amount;
        this.nextRegenTime = this.level.getGameTime() + BaseDefense.CONFIG.get().blockHealthRegenRateTicks;
        if (!isStillAlive()) {
            this.onDie();
        } else if (getOwnerTeam() != null ){
            getOwnerTeam().message(Component.literal("Your block at (" + getBlockPos().toShortString() + ") took " + ((int) amount) + " damage! Now at " + ((int) health) + " health."));
        }
        return true;
    }

    @Override
    public float health() {
        return this.health;
    }

    @Override
    public float maxHealth() {
        return this.maxHealth;
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
        AttackTargetable.super.onDie();
        getOwnerTeam().message(Component.literal("Your block at (" + getBlockPos().toShortString() + ") was destroyed."));
        ModMain.LOGGER.debug("AttackableTile at " + this.getBlockPos() + " died.");
        this.level.removeBlock(this.getBlockPos(), false);
    }

    public void serverTick(){
        if (this.hasLevel() && this.level.getGameTime() > this.nextRegenTime){
            if (this.health() >= this.maxHealth() || !this.isStillAlive()) return;

            this.health = Math.min(this.health + 1, this.maxHealth());
            this.nextRegenTime = this.level.getGameTime() + BaseDefense.CONFIG.get().blockHealthRegenRateTicks;
            for (int i=0;i<15;i++){
                double x = this.getBlockPos().getX() + (2 * (this.level.getRandom().nextFloat() - 0.5)) + 0.5;
                double y = this.getBlockPos().getY() + (1 * (this.level.getRandom().nextFloat() - 0.5)) + 0.5;
                double z = this.getBlockPos().getZ() + (2 * (this.level.getRandom().nextFloat() - 0.5)) + 0.5;
                ((ServerLevel)this.level).sendParticles(ParticleTypes.HEART, x, y, z, 1, 0, 0.2, 0, 1);
            }
        }
    }
}
