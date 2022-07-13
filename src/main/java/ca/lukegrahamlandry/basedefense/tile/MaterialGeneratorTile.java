package ca.lukegrahamlandry.basedefense.tile;

import ca.lukegrahamlandry.basedefense.events.DataManager;
import ca.lukegrahamlandry.basedefense.init.TileTypeInit;
import ca.lukegrahamlandry.basedefense.material.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;
import java.util.function.Consumer;

public class MaterialGeneratorTile extends BlockEntity implements LeveledMaterialGenerator {
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
    private UUID owner;
    private int tier;
    private ResourceLocation materialProductionType;

    public void tryBind(ServerPlayer player){
        if (this.owner != null && !this.canAccess(player)) return;

        Team team = TeamHandler.get(player.getLevel()).getTeam(player);
        this.owner = team.id;
        MaterialGenerationHandler.get(this.level).addGenerator(this.owner, this.getUUID(), this.getProduction());
        team.addAttackLocation(new Team.AttackLocation(level, this.getBlockPos(), this.uuid, Team.AttackLocType.GENERATOR));
    }

    public void unBind(){
        MaterialGenerationHandler.get(this.level).removeGenerator(owner, this.getUUID());
        this.owner = null;
    }

    private static final String TIER_TAG_KEY = "tier";
    private static final String PLAYER_TAG_KEY = "player";
    private static final String MATERIAL_TAG_KEY = "material";
    private static final String UUID_TAG_KEY = "material";

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TIER_TAG_KEY, this.tier);
        tag.putUUID(PLAYER_TAG_KEY, this.owner);
        tag.putUUID(UUID_TAG_KEY, this.uuid);
        tag.putString(MATERIAL_TAG_KEY, this.materialProductionType.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tier = tag.getInt(TIER_TAG_KEY);
        this.owner = tag.getUUID(PLAYER_TAG_KEY);
        this.uuid = tag.getUUID(UUID_TAG_KEY);
        this.materialProductionType = new ResourceLocation(tag.getString(MATERIAL_TAG_KEY));
    }

    @Override
    public MaterialCollection getProduction() {
        return DataManager.getMaterial(this.materialProductionType).getProduction(this.tier);
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public MaterialCollection getNextProduction() {
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
        return this.owner == null || (player != null && player.getUUID().equals(this.owner));
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
}
