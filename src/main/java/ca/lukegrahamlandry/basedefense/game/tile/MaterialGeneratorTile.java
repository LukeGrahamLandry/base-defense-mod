package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackTargetable;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import ca.lukegrahamlandry.basedefense.base.material.old.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MaterialGeneratorTile extends BlockEntity implements LeveledMaterialGenerator, AttackTargetable {
    private static class SaveData {
        private UUID uuid;
        private UUID ownerTeamId;
        private MaterialGeneratorType.Instance genInfo;
    }

    private SaveData data = new SaveData();

    public MaterialGeneratorTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.MATERIAL_GENERATOR_TILE.get(), pWorldPosition, pBlockState);
        this.data.uuid = UUID.randomUUID();
    }

    public static void getAndDo(Level level, BlockPos pos, Consumer<MaterialGeneratorTile> action){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof MaterialGeneratorTile && !level.isClientSide()){
            action.accept((MaterialGeneratorTile) tile);
        }
    }

    public void tryBind(ServerPlayer player){
        if (this.data.ownerTeamId != null && !this.canAccess(player)) return;

        Team team = TeamManager.get(player);
        this.data.ownerTeamId = team.getId();
        team.addGenerator(this.data.uuid, this.data.genInfo);
        team.addAttackLocation(new AttackLocation(level, this.getBlockPos(), this.data.uuid, this));
        System.out.println("new attack options: " +  team.getAttackOptions().size());

        player.displayClientMessage(Component.literal("Bound player to generator!"), true);

        this.setChanged();
    }

    public void unBind(){
        TeamManager.getTeamById(this.data.ownerTeamId).removeGenerator(this.data.uuid);
        this.data.ownerTeamId = null;
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
    }

    @Override
    public MaterialCollection getProduction() {
        if (this.data.genInfo == null) return MaterialCollection.empty();
        return this.data.genInfo.getProduction();
    }

    @Override
    public UUID getUUID() {
        return this.data.uuid;
    }

    @Override
    public MaterialCollection getNextProduction() {
        if (this.data.genInfo.next() == null) return MaterialCollection.empty();
        return this.data.genInfo.next().getProduction();
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
        return TeamManager.get(player).getId().equals(this.data.ownerTeamId);
    }

    @Override
    public boolean tryUpgrade(ServerPlayer thePlayer){
        System.out.println("try upgrade");
        boolean success = this.canAccess(thePlayer) && this.upgrade(MaterialsUtil.getTeamMaterials(thePlayer));
        System.out.println(success);
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
                inventory.subtract(this.getUpgradeCost());
                this.data.genInfo.tier++;
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
        this.data.genInfo.type = prodType;
        this.data.genInfo.tier = prodTier;
        this.setChanged();
    }

    @Override
    public ResourceLocation getGenType() {
        return this.data.genInfo.type;
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
        return TeamManager.getTeamById(this.data.ownerTeamId);
    }

    @Override
    public void onDie() {
        AttackTargetable.super.onDie();
        this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 4.0F, Level.ExplosionInteraction.TNT);
        this.level.removeBlock(this.getBlockPos(), false);


        getOwnerTeam().getAttackOptions().removeIf(location -> location.pos().equals(this.getBlockPos()));
    }
}
