package ca.lukegrahamlandry.basedefense.base.teams;

import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Team {
    UUID id;
    MaterialCollection materials;
    Map<UUID, MaterialGeneratorType.Instance> generators;
    // List<AttackLocation> attackLocations;
    int baseBlockTier = 0;

    public Team() {
        this.id = UUID.randomUUID();
        this.materials = new MaterialCollection();
        this.generators = new HashMap<>();
        // this.attackLocations = new ArrayList<>();
    }

    public boolean contains(Player player) {
        return TeamManager.get(player).id.equals(this.id);
    }

    public MaterialCollection getMaterials() {
        return this.materials;
    }

    public Map<UUID, MaterialGeneratorType.Instance> getGenerators(){
        return generators;
    }

    public void addAttackLocation(AttackLocation attackLocation) {
//        this.attackLocations.add(attackLocation);
    }

    public List<AttackLocation> getAttackOptions() {
//        for (AttackLocation l : this.attackLocations){
//            System.out.println(l.pos());
//        }
//        return new ArrayList<>(this.attackLocations);
        return  new ArrayList<>();
    }

    public UUID getId() {
        return this.id;
    }

    public void addGenerator(UUID uuid, MaterialGeneratorType.Instance instance) {
        generators.put(uuid, instance);
        TeamManager.TEAMS.setDirty();
    }

    public void removeGenerator(UUID uuid) {
        generators.remove(uuid);
        TeamManager.TEAMS.setDirty();
    }

    public int getBaseTier() {
        return this.baseBlockTier;
    }

    public void setDirty(){
        TeamManager.TEAMS.setDirty();
    }

    public void upgradeBaseTier() {
        this.baseBlockTier++;
    }
}
