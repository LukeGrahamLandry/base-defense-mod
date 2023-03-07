package ca.lukegrahamlandry.basedefense.base.teams;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.attacks.OngoingAttack;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.events.ServerEvents;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    UUID id;
    MaterialCollection materials;
    Map<UUID, MaterialGeneratorType.Instance> generators;
    public OngoingAttack.State attack = null;
    Set<AttackLocation> attackLocations;
    int baseBlockTier = 0;
    UUID owner;
    int days = 0;
    AttackLocation baseLocation;

    public Team() {
        this.id = UUID.randomUUID();
        this.materials = new MaterialCollection();
        this.generators = new HashMap<>();
        this.attackLocations = new HashSet<>();
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

    public void updateBaseLocation(AttackLocation attackLocation){
        if (Objects.equals(attackLocation, this.baseLocation)) return;

        if (this.baseLocation != null){
            this.baseLocation.getLevel().getBlockEntity(this.baseLocation.pos);
            if (this.baseLocation.getTarget() instanceof BaseTile oldTile){
                oldTile.invalidate();
            }
            this.attackLocations.remove(baseLocation);
        }
        this.baseLocation = attackLocation;
        this.addAttackLocation(attackLocation);
    }

    public AABB getNoBreakArea(ServerLevel level){
        BaseTier tier = BaseTier.get(this.getBaseTier());
        if (this.baseLocation == null || tier.preventBlockBreakRadius <= 0 || !this.baseLocation.getLevel().equals(level)) return null;
        return new AABB(this.baseLocation.pos).inflate(tier.preventBlockBreakRadius);
    }

    public void addAttackLocation(AttackLocation attackLocation) {
         this.attackLocations.add(attackLocation);
         this.setDirty();
    }

    public void removeAttackLocation(UUID attackLocationId) {
        this.attackLocations.removeIf((l) -> l.id.equals(attackLocationId));
    }

    public List<AttackLocation> getAttackOptions() {
        return this.attackLocations.stream().filter((l) -> l.canAttack).collect(Collectors.toList());
    }

    public List<AttackLocation> getTrackedLocations() {
        return new ArrayList<>(this.attackLocations);
    }

    public UUID getId() {
        return this.id;
    }

    public void addGenerator(UUID uuid, MaterialGeneratorType.Instance instance) {
        generators.put(uuid, instance);
        TeamManager.setDirty();
    }

    public void removeGenerator(UUID uuid) {
        generators.remove(uuid);
        TeamManager.setDirty();
    }

    public int getBaseTier() {
        return this.baseBlockTier;
    }

    public void setDirty(){
        TeamManager.setDirty();
    }

    public void upgradeBaseTier() {
        this.baseBlockTier++;
    }

    public void message(Component text) {
        text.getStyle().applyFormat(ChatFormatting.LIGHT_PURPLE);
        ServerEvents.server.getPlayerList().getPlayers().forEach((player -> {
            if (contains(player)) player.displayClientMessage(text, false);
        }));
    }

    public Collection<UUID> getMembers(){
        return TeamManager.PLAYER_TEAMS.getMap().entrySet().stream().filter((entry) -> entry.getValue().id.equals(this.getId())).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "Team:" + this.id;
    }

    public UUID getOwner() {
        var members = getMembers();
        if (this.owner == null || !members.contains(this.owner)){
            if (!members.isEmpty()){
                this.owner = members.stream().findFirst().get();
            }
        }
        return this.owner;
    }

    public void onBaseDie() {
        ModMain.LOGGER.debug("Clearing team because base destroyed. " + JsonHelper.get().toJson(this));
        this.baseLocation = null;

        getTrackedLocations().forEach((location) -> {
            if (location.canAttack) AttackLocation.destroyed.add(location.getTarget());

            BlockEntity tile = location.getLevel().getBlockEntity(location.pos);
            if (tile instanceof TurretTile turret){
                turret.onTeamBaseDie();
            }
            if (tile instanceof MaterialGeneratorTile generator){
                generator.onTeamBaseDie();
            }
        });

        this.days = 0;
        this.baseBlockTier = 0;
        attackLocations.clear();
        getMaterials().clear();
        generators.clear();
        setDirty();
        message(Component.literal("Your base block was destroyed! Your materials are gone and you've lost ownership of your generators/turrets."));
    }

    public boolean isAnyPlayerOnline() {
        for (var playerId : getMembers()){
            if (ServerEvents.server.getPlayerList().getPlayer(playerId) != null) return true;
        }
        return false;
    }

    public void incrementDays() {
        this.days++;
    }

    public int getDays() {
        return this.days;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Team && this.id.equals(((Team) obj).id);
    }
}
