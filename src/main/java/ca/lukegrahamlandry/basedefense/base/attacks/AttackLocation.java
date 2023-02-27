package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class AttackLocation {
    public static Map<UUID, AttackTargetable> targets = new HashMap<>();
    public static List<AttackTargetable> destroyed = new ArrayList<>();

    public BlockPos pos;
    public UUID id;
    public ResourceLocation dimension;

    public AttackLocation(Level level, BlockPos pos, UUID id){
        this.pos = pos;
        this.id = id;
        this.dimension = level.dimension().location();
    }

    static Random rand = new Random();
    public BlockPos getRandSpawnLocation() {
        List<BlockPos> positions = getTarget().getSpawnLocations();
        return positions.get(rand.nextInt(positions.size()));
    }

    public AttackTargetable getTarget(){
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
}
