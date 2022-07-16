package ca.lukegrahamlandry.basedefense.tile;

import ca.lukegrahamlandry.basedefense.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.attacks.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.attacks.AttackTargetable;
import ca.lukegrahamlandry.basedefense.events.DataManager;
import ca.lukegrahamlandry.basedefense.init.TileTypeInit;
import ca.lukegrahamlandry.basedefense.material.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MaterialGeneratorTile extends BlockEntity implements LeveledMaterialGenerator, AttackTargetable {
    public MaterialGeneratorTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(TileTypeInit.MATERIAL_GENERATOR.get(), pWorldPosition, pBlockState);
        this.uuid = UUID.randomUUID();
    }

    public static void getAndDo(Level level, BlockPos pos, Consumer<MaterialGeneratorTile> action){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof MaterialGeneratorTile && !level.isClientSide()){
            action.accept((MaterialGeneratorTile) tile);
        }
    }

    private UUID uuid;
    private UUID ownerTeamId;
    private int tier;
    private ResourceLocation materialProductionType;

    public void tryBind(ServerPlayer player){
        if (this.ownerTeamId != null && !this.canAccess(player)) return;

        Team team = TeamHandler.get(player.getLevel()).getTeam(player);
        this.ownerTeamId = team.id;
        MaterialGenerationHandler.get(this.level).addGenerator(this.ownerTeamId, this.getUUID(), this.getProduction());
        team.addAttackLocation(new AttackLocation(level, this.getBlockPos(), this.uuid, this));
        System.out.println("new attack options: " +  team.getAttackOptions().size());

        player.displayClientMessage(new TextComponent("Bound player to generator!"), true);
    }

    public void unBind(){
        MaterialGenerationHandler.get(this.level).removeGenerator(ownerTeamId, this.getUUID());
        this.ownerTeamId = null;
    }

    private static final String TIER_TAG_KEY = "tier";
    private static final String PLAYER_TAG_KEY = "player";
    private static final String MATERIAL_TAG_KEY = "material";
    private static final String UUID_TAG_KEY = "tileuuid";

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TIER_TAG_KEY, this.tier);
        if (this.ownerTeamId != null) tag.putUUID(PLAYER_TAG_KEY, this.ownerTeamId);
        tag.putUUID(UUID_TAG_KEY, this.uuid);
        if (this.materialProductionType != null) tag.putString(MATERIAL_TAG_KEY, this.materialProductionType.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tier = tag.getInt(TIER_TAG_KEY);
        this.ownerTeamId = tag.contains(PLAYER_TAG_KEY) ? tag.getUUID(PLAYER_TAG_KEY) : null;
        this.uuid = tag.getUUID(UUID_TAG_KEY);
        this.materialProductionType = tag.contains(MATERIAL_TAG_KEY) ? new ResourceLocation(tag.getString(MATERIAL_TAG_KEY)) : null;
    }

    @Override  // TODO
    public MaterialCollection getProduction() {
        if (this.materialProductionType == null) return MaterialCollection.empty();
        return DataManager.getMaterial(this.materialProductionType).getProduction(this.tier);
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public MaterialCollection getNextProduction() {
        if (this.materialProductionType == null) return MaterialCollection.empty();
        return DataManager.getMaterial(this.materialProductionType).getProduction(this.tier + 1);
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public int getMaxTier() {
        return 0;
    }

    @Override
    public boolean canAccess(Player player) {
        return this.ownerTeamId == null || (player != null && player.getUUID().equals(this.ownerTeamId));
    }

    @Override
    public boolean tryUpgrade(ServerPlayer thePlayer){
        boolean success = this.canAccess(thePlayer) && this.upgrade(MaterialsUtil.getMaterials(thePlayer));
        if (success){
            this.unBind();
            this.tryBind(thePlayer);
        }
        return success;
    }

    @Override
    public boolean upgrade(MaterialCollection inventory) {
        if (!this.level.isClientSide()){
            if (inventory.getDifference(this.getUpgradeCost()).isEmpty()){
                this.tier++;
                return true;
            }
        }
        return false;
    }

    @Override
    public MaterialCollection getUpgradeCost() {
        return DataManager.getMaterial(this.materialProductionType).getUpgradeCost(this.tier + 1);
    }

    public void setType(ResourceLocation prodType, int prodTier){
        this.materialProductionType = prodType;
        this.tier = prodTier;
    }

    @Override
    public ResourceLocation getGenType() {
        return this.materialProductionType;
    }





    float health = maxHealth();
    AttackTargetAvatar avatar;
    @Override
    public List<BlockPos> getSpawnLocations() {
        return Arrays.asList(this.getBlockPos().east(10), this.getBlockPos().west(10), this.getBlockPos().north(10), this.getBlockPos().south(10));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        this.health -= amount;
        System.out.println("took damage! " + amount + ". now have " + this.health);
        if (!isStillAlive()) {
            this.onDie();
        }
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
        return TeamHandler.get(this.level).getTeam(this.ownerTeamId);
    }

    @Override
    public void onDie() {
        AttackTargetable.super.onDie();
        this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 4.0F, Explosion.BlockInteraction.BREAK);
        this.level.removeBlock(this.getBlockPos(), false);


        getOwnerTeam().getAttackOptions().removeIf(location -> location.pos().equals(this.getBlockPos()));
    }
}
