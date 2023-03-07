package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.attacks.OngoingAttack;
import ca.lukegrahamlandry.basedefense.base.attacks.OngoingCaptureAttack;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MaterialGeneratorTile extends AttackableTile implements LeveledMaterialGenerator, AttackTargetable, GeoBlockEntity {
    private static class SaveData {
        private MaterialGeneratorType.Instance genInfo;
        private boolean isTerrainGenerated = false;
        OngoingAttack.State attackState = null;
        UUID attackingTeam = null;
        int attackTimer = 0;
    }

    private SaveData data = new SaveData();

    public MaterialGeneratorTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(40, ModRegistry.MATERIAL_GENERATOR_TILE.get(), pWorldPosition, pBlockState);
    }

    public static void getAndDo(Level level, BlockPos pos, Consumer<MaterialGeneratorTile> action){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof MaterialGeneratorTile && !level.isClientSide()){
            action.accept((MaterialGeneratorTile) tile);
        }
    }

    // Returns true to allow the player to open the gui.
    public boolean tryBind(ServerPlayer player){
        if (this.teamUUID != null) return this.getOwnerTeam().contains(player);

        if (this.data.genInfo.allowInstantCapture()){
            Team team = TeamManager.get(player);
            this.doBind(team);
            return true;
        } else {
            this.startCaptureAttack(player);
            return false;
        }
    }

    private void doBind(Team team){
        this.teamUUID = team.getId();
        AttackLocation.targets.put(this.uuid, this);
        team.addAttackLocation(new AttackLocation(level, this.getBlockPos(), this.uuid));

        this.setChanged();  // adds to team generator list
    }

    public void unBind(){
        if (this.hasOngoingCaptureAttack()){
            this.onDie();
        }
        if (this.getOwnerTeam() != null){
            TeamManager.getTeamById(this.teamUUID).removeGenerator(this.uuid);
            this.teamUUID = null;
        }
    }

    public void setIsTerrain() {
        this.data.isTerrainGenerated = true;
        if (this.data.genInfo == null){
            this.data.genInfo = MaterialGeneratorType.EMPTY.createInst(0);
        }
        if (this.hasLevel()){
            Holder<Biome> biome = this.level.getBiome(this.worldPosition);
            this.data.genInfo.type = BaseDefense.CONFIG.get().getGeneratorTypeFor(this.level, biome);
            if (this.teamUUID != null){
                AttackLocation.targets.put(this.uuid, this);
                this.getOwnerTeam().addAttackLocation(new AttackLocation(level, this.getBlockPos(), this.uuid));
            }
        } else {
            ModMain.LOGGER.error("MaterialGeneratorTile#setIsTerrain called before in level so can't read biome type.");
        }
    }

    public boolean isTerrain() {
        return this.data.isTerrainGenerated;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("wlData", JsonHelper.get().toJson(this.data));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("wlData")){
            try {
                this.data = JsonHelper.get().fromJson(tag.getString("wlData"), SaveData.class);
            } catch (JsonSyntaxException e){
                ModMain.LOGGER.error("Failed to load MaterialGeneratorTile data.");
                ModMain.LOGGER.error(tag.getString("wlData"));
                e.printStackTrace();
            }
        }

        if (this.data.genInfo == null) this.data.genInfo = MaterialGeneratorType.EMPTY.createInst(0);
        if (this.hasLevel() && this.data.isTerrainGenerated) this.setIsTerrain();
    }

    @Override
    public int getTier() {
        return this.data.genInfo.tier;
    }

    @Override
    public int getMaxTier() {
        return 0;
    }

    @Override
    public boolean canAccess(Player player) {
        return TeamManager.get(player).getId().equals(this.teamUUID);
    }

    @Override
    public boolean upgrade(MaterialCollection inventory) {
        if (!this.level.isClientSide()){
            if (inventory.getDifference(this.getUpgradeCost()).isEmpty()){
                inventory.subtract(this.getUpgradeCost());
                this.data.genInfo.tier++;
                this.setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public MaterialCollection getUpgradeCost() {
        return this.data.genInfo.getUpgradeCost();
    }

    public void setType(ResourceLocation prodType, int prodTier){
        this.data.genInfo = new MaterialGeneratorType.Instance(prodType, prodTier);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.teamUUID != null) this.getOwnerTeam().addGenerator(this.uuid, this.data.genInfo);
    }

    @Override
    public MaterialGeneratorType.Instance getStats() {
        return this.data.genInfo;
    }

    @Override
    public void onDie() {
        if (this.hasOngoingCaptureAttack()){
            this.onCaptureAttackFail("The monsters destroyed the generator.");
            return;
        }

        super.onDie();
        if (getOwnerTeam() != null){
            getOwnerTeam().message(Component.literal("Your material generator at " + this.getBlockPos() + " was destroyed!"));
            AttackLocation.destroyed.add(this);
            getOwnerTeam().removeAttackLocation(this.uuid);

            this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 4.0F, Level.ExplosionInteraction.TNT);
        }
   }



    // animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ACTIVATED_ANIM = RawAnimation.begin().thenPlay("activated");
    private static final RawAnimation FAN_ANIM = RawAnimation.begin().thenPlay("fan");

    private PlayState animation(AnimationState<MaterialGeneratorTile> state){
        state.setAnimation(FAN_ANIM);
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

    ///// Capture System /////

    OngoingCaptureAttack activeAttack;

    public void tick() {
        if (!this.hasLevel() || this.level.isClientSide()) return;
        if (!this.hasOngoingCaptureAttack()) return;
        this.activeAttack.tick();

        if (!this.activeAttack.isInProgress()){
            this.onCaptureAttackSuccess("You defeated all the monsters.");
            return;
        }

        this.data.attackTimer--;
        this.activeAttack.setTimer(this.data.attackTimer, this.data.genInfo.getCaptureRequirements().durationTicks);
        if (this.data.genInfo.getCaptureRequirements().durationTicks > 0){
            if (this.data.attackTimer <= 0){
                if (this.data.genInfo.getCaptureRequirements().failOnTimeout){
                    this.onCaptureAttackFail("You didn't defeat all the monsters in time.");
                } else {
                    this.onCaptureAttackSuccess("You survived long enough.");
                }
                return;
            }
        }

        if (this.data.genInfo.getCaptureRequirements().radiusBlocks > 0){
            AABB box = new AABB(this.getBlockPos()).inflate(this.data.genInfo.getCaptureRequirements().radiusBlocks);
            Team team = TeamManager.getTeamById(this.data.attackingTeam);
            List<Player> onCapturingTeam = level.getEntitiesOfClass(Player.class, box, team::contains);
            if (onCapturingTeam.isEmpty()){
                this.onCaptureAttackFail("No team members were close enough to the generator.");
                return;
            }
        }
    }

    private void startCaptureAttack(ServerPlayer player) {
        Team team = TeamManager.get(player);
        if (this.hasOngoingCaptureAttack()){
            if (team.contains(player)){
                player.displayClientMessage(Component.literal("Your team is already trying to capture the generator at " + this.getBlockPos().toShortString()), false);
            } else {
                player.displayClientMessage(Component.literal("Another team is trying to capture the generator at " + this.getBlockPos().toShortString()), false);
            }
            return;
        }

        AttackLocation.targets.put(this.uuid, this);
        this.data.attackingTeam = team.getId();
        this.data.attackTimer = this.data.genInfo.getCaptureRequirements().durationTicks;
        this.data.attackState = new OngoingAttack.State(new AttackLocation(level, this.getBlockPos(), this.uuid), this.data.genInfo.getCaptureRequirements().waves);
        this.activeAttack = new OngoingCaptureAttack(this.data.attackState, team, this.data.genInfo.getCaptureRequirements().failOnTimeout);
        this.activeAttack.startWave(this.data.attackState.getWave());
        team.message(Component.literal("Your team is trying to capture the generator at (" + this.getBlockPos().toShortString() + ")."));
        if (this.data.attackTimer > 0){
            if (this.data.genInfo.getCaptureRequirements().failOnTimeout){
                team.message(Component.literal("You must defend it and defeat all attacking monsters within " + ((int) (this.data.attackTimer / 20)) + " seconds."));
            } else {
                team.message(Component.literal("You must defend it from attacking monsters for " + ((int) (this.data.attackTimer / 20)) + " seconds."));
            }
        }
        if (this.data.genInfo.getCaptureRequirements().radiusBlocks > 0){
            team.message(Component.literal("You must also have a team member within " + this.data.genInfo.getCaptureRequirements().radiusBlocks + " blocks of the generator at all times."));
        }
        this.activeAttack.setTimer(this.data.attackTimer, this.data.attackTimer);
        this.setChanged();
    }

    private void onCaptureAttackSuccess(String msg) {
        if (!this.hasOngoingCaptureAttack()){
            ModMain.LOGGER.error("Generator onCaptureAttackSuccess but no ongoing attack. " + this.getBlockPos());
            return;
        }

        Team team = TeamManager.getTeamById(this.data.attackingTeam);
        messageTeam(Component.literal(msg + " Your team now owns the generator at (" + this.getBlockPos().toShortString() + ")"));
        this.doParticles(ParticleTypes.TOTEM_OF_UNDYING);
        AttackLocation.targets.remove(this.uuid);
        this.doBind(team);
        clearAttack();
    }

    private void onCaptureAttackFail(String msg) {
        if (!this.hasOngoingCaptureAttack()){
            ModMain.LOGGER.error("Generator onCaptureAttackFail but no ongoing attack. " + this.getBlockPos());
            return;
        }
        AttackLocation.targets.remove(this.uuid);
        messageTeam(Component.literal(msg + " Capture failed."));
        clearAttack();
    }

    @Override
    public void messageTeam(Component msg) {
        if (this.hasOngoingCaptureAttack()){
            TeamManager.getTeamById(this.data.attackingTeam).message(msg);
        }
        else {
            super.messageTeam(msg);
        }
    }

    private void clearAttack(){
        this.activeAttack.end();
        this.activeAttack = null;
        this.data.attackState = null;
        this.data.attackingTeam = null;
        this.data.attackTimer = 0;
    }

    private boolean hasOngoingCaptureAttack(){
        return this.activeAttack != null && this.data.attackState != null && this.data.attackingTeam != null;
    }
}
