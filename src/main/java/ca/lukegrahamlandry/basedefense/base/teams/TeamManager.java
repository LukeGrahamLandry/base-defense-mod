package ca.lukegrahamlandry.basedefense.base.teams;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.commands.BaseTeamCommand;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.data.impl.GlobalDataWrapper;
import ca.lukegrahamlandry.lib.data.impl.PlayerDataWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamManager {
    public static final PlayerDataWrapper<TeamInfo> PLAYER_TEAMS = new PlayerDataWrapper<>(TeamInfo.class).saved();
    private static final GlobalDataWrapper<TeamManager> TEAMS = new GlobalDataWrapper<>(TeamManager.class).saved();
    public static List<BaseTeamCommand.InviteData> invites = new ArrayList<>();

    private Map<UUID, Team> teams = new HashMap<>();
    public static boolean wasDay = true;

    public static Team getTeamById(UUID teamID){
        return getData().teams.get(teamID);
    }

    public static void setDirty() {
        if (TEAMS.get() == null) {  // make sure its loaded just in case. TODO: ?
            TEAMS.clear();
        }

        TEAMS.setDirty();
    }

    public Team getTeam(Player player){
        TeamInfo teamInfo = PLAYER_TEAMS.get(player);

        if (teamInfo.id == null){
            Team team = new Team();
            teamInfo.id = team.id;
            PLAYER_TEAMS.setDirty(player);
            getData().teams.put(team.id, team);
            setDirty();
        }

        return teams.get(teamInfo.id);
    }

    public static Team get(Player player){
        return getData().getTeam(player);
    }

    public static TeamManager getData(){
        if (TEAMS.get() == null) {
            TEAMS.clear();  // TODO: ?
            setDirty();
        }
        return TEAMS.get();
    }

    public static int playerCount(UUID teamId){
        int count = 0;
        for (var checkTeam : PLAYER_TEAMS.data.values()){
            if (teamId.equals(checkTeam.id)){
                count++;
            }
        }
        return count;
    }

    public void switchTeam(Player player, UUID targetTeam){
        Team oldTeam = getTeam(player);

        if (targetTeam == null){
            Team team = new Team();
            PLAYER_TEAMS.get(player).id = team.id;
            getData().teams.put(team.id, team);
        }

        PLAYER_TEAMS.get(player).id = targetTeam;
        if (playerCount(oldTeam.id) == 0){
            ModMain.LOGGER.debug("Deleting empty team: " + JsonHelper.get().toJson(oldTeam));

            AtomicInteger turretCount = new AtomicInteger();
            AtomicInteger generatorCount = new AtomicInteger();
            oldTeam.getTrackedLocations().forEach((location) -> {
                if (location.canAttack) AttackLocation.destroyed.add(location.getTarget());

                BlockEntity tile = player.level.getBlockEntity(location.pos);
                if (tile instanceof TurretTile turret){
                    turret.onTeamBaseDie();
                    turret.setTeam(getTeam(player));
                    turretCount.getAndIncrement();
                }
                if (tile instanceof MaterialGeneratorTile generator){
                    generator.onTeamBaseDie();
                    generator.tryBind((ServerPlayer) player);  // re-adds to attack locations and generators list
                    generatorCount.getAndIncrement();
                }
            });

            getTeam(player).getMaterials().add(oldTeam.getMaterials());
            player.displayClientMessage(Component.literal("Since you were the last member in your team, you took your materials with you to your new team.\nTransferred: " + generatorCount.get() + " material generators, "  + turretCount.get() + " turrets, and " + JsonHelper.get().toJson(oldTeam.getMaterials())), false);
            oldTeam.getMaterials().clear();

            getData().teams.remove(oldTeam.getId());
        }

        setDirty();
        PLAYER_TEAMS.setDirty(player);
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public void leaveTeam(ServerPlayer player) {
        PLAYER_TEAMS.remove(player);
        getTeam(player);
    }

    public static class TeamInfo {
        public UUID id;

        public TeamInfo(){

        }
    }

    // for class loading
    public static void init(){}
}
