package ca.lukegrahamlandry.basedefense.base.teams;

import ca.lukegrahamlandry.lib.data.impl.GlobalDataWrapper;
import ca.lukegrahamlandry.lib.data.impl.PlayerDataWrapper;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamManager {
    private static final PlayerDataWrapper<TeamInfo> PLAYER_TEAMS = new PlayerDataWrapper<>(TeamInfo.class).saved();
    public static final GlobalDataWrapper<TeamManager> TEAMS = new GlobalDataWrapper<>(TeamManager.class).saved();

    private Map<UUID, Team> teams = new HashMap<>();

    public static Team getTeamById(UUID teamID){
        return TEAMS.get().teams.get(teamID);
    }

    public Team getTeam(Player player){
        TeamInfo teamInfo = PLAYER_TEAMS.get(player);

        System.out.println(teamInfo.id);

        if (teamInfo.id == null){
            Team team = new Team();
            teamInfo.id = team.id;
            PLAYER_TEAMS.setDirty(player);
            TEAMS.get().teams.put(team.id, team);
            TEAMS.setDirty();
        }

        return teams.get(teamInfo.id);
    }

    public static Team get(Player player){
        return TEAMS.get().getTeam(player);
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
            TEAMS.get().teams.put(team.id, team);
        }

        PLAYER_TEAMS.get(player).id = targetTeam;
        if (playerCount(oldTeam.id) == 0){
            getTeam(player).getMaterials().add(oldTeam.getMaterials());
            getTeam(player).getGenerators().putAll(oldTeam.getGenerators());
        }

        TEAMS.setDirty();
        PLAYER_TEAMS.setDirty(player);
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public static class TeamInfo {
        private UUID id;

        public TeamInfo(){

        }
    }

    // for class loading
    public static void init(){}
}
