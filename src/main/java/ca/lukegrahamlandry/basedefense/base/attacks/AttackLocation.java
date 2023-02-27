package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import ca.lukegrahamlandry.basedefense.events.ServerEvents;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class AttackLocation {
    public static Map<UUID, AttackTargetable> targets = new HashMap<>();
    public static List<AttackTargetable> destroyed = new ArrayList<>();

    public BlockPos pos;
    public UUID id;
    public ResourceLocation dimension;
    public boolean canAttack = true;
    String info = "?";

    public AttackLocation(Level level, BlockPos pos, UUID id){
        this.pos = pos;
        this.id = id;
        this.dimension = level.dimension().location();

        BlockEntity tile = level.getBlockEntity(pos);
        if (tile != null){
            this.info = Objects.toString(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tile.getType()));
            if (tile instanceof MaterialGeneratorTile generator){
                this.info += generator.isTerrain() ? " (terrain)" : " (looted)";
            }
        }
    }

    public static AttackLocation justTrackNoAttack(Level level, BlockPos pos, UUID id){
        var result = new AttackLocation(level, pos, id);
        result.canAttack = false;
        return result;
    }

    static Random rand = new Random();
    public BlockPos getRandSpawnLocation() {
        List<BlockPos> positions = getTarget().getSpawnLocations();
        return positions.get(rand.nextInt(positions.size()));
    }

    public AttackTargetable getTarget(){
        if (!this.canAttack) ModMain.LOGGER.error("getTarget called fpr AttackLocation created with justTrackNoAttack.");
        return targets.get(this.id);
    }

    public boolean validTarget(){
        return targets.containsKey(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AttackLocation)) return false;
        return Objects.equals(this.id, ((AttackLocation) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public ChunkPos chunk(){
        return new ChunkPos(this.pos);
    }

    public ServerLevel getLevel(){
        return ServerEvents.server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }
}
