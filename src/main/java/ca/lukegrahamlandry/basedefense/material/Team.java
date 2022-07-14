package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.attacks.AttackLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Team {
    public final UUID id;
    Set<UUID> players = new HashSet<>();
    MaterialCollection materials;
    List<AttackLocation> attackLocations = new ArrayList<>();

    public boolean contains(UUID player) {
        return players.contains(player);
    }

    public boolean contains(Player player) {
        return players.contains(player.getUUID());
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
        for (AttackLocation l : this.attackLocations){
            System.out.println(l.pos());
        }
        return new ArrayList<>(this.attackLocations);
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }
}
