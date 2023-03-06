package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetAvatar;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.UUID;
import java.util.function.Consumer;

public class MaterialGeneratorTile extends AttackableTile implements LeveledMaterialGenerator, AttackTargetable, GeoBlockEntity {
    private static class SaveData {
        private MaterialGeneratorType.Instance genInfo;
        private boolean isTerrainGenerated = false;
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

    public void tryBind(ServerPlayer player){
        if (this.teamUUID != null) return;

        Team team = TeamManager.get(player);
        this.teamUUID = team.getId();
        AttackLocation.targets.put(this.uuid, this);
        team.addAttackLocation(new AttackLocation(level, this.getBlockPos(), this.uuid));

        this.setChanged();  // adds to team generator list
    }

    public void unBind(){
        TeamManager.getTeamById(this.teamUUID).removeGenerator(this.uuid);
        this.teamUUID = null;
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
}
