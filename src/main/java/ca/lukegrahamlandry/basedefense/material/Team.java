package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class Team {
    public final UUID id;
    Set<UUID> players = new HashSet<>();
    MaterialCollection materials;
    List<AttackLocation> attackLocations;

    public boolean contains(UUID player) {
        return players.contains(player);
    }

    public boolean contains(Player player) {
        return players.contains(player);
    }

    public void add(UUID player) {
        players.add(player);
    }

    public void remove(UUID player) {
        players.remove(player);
    }

    public MaterialCollection getMaterials() {
        return this.materials;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();

        tag.put("materials", this.materials.toNBT());
        tag.putUUID("id", id);
        int i = 0;
        for (UUID player : players) {
            tag.putUUID(String.valueOf(i), player);
            i++;
        }

        return tag;
    }

    public Team(CompoundTag tag) {
        this.materials = new MaterialCollection(tag.getCompound("materials"));
        this.id = tag.getUUID("id");
        int i = 0;
        while (tag.contains(String.valueOf(i))) {
            add(tag.getUUID(String.valueOf(i)));
            i++;
        }
    }

    public Team() {
        this.id = UUID.randomUUID();
        this.materials = new MaterialCollection();
    }

    // TODO: must be saved to nbt as well
    public void addAttackLocation(AttackLocation attackLocation) {
        this.attackLocations.add(attackLocation);
    }

    public List<AttackLocation> getAttackOptions() {
        return new ArrayList<>(this.attackLocations);
    }

    public record AttackLocation(Level level, BlockPos pos, UUID id, AttackLocType type) {
        public BlockPos getRandSpawnLocation() {
            return pos(); // need to have it know a radius and then pick somewhere on the edge
        }
    }

    public enum AttackLocType {
        GENERATOR, BASE;
    }
}
