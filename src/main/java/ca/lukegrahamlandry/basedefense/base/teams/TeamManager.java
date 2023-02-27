package ca.lukegrahamlandry.basedefense.base.teams;

import ca.lukegrahamlandry.basedefense.commands.BaseTeamCommand;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.data.impl.GlobalDataWrapper;
import ca.lukegrahamlandry.lib.data.impl.PlayerDataWrapper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.codec.language.ColognePhonetic;

import java.util.*;

public class TeamManager {
    public static final PlayerDataWrapper<TeamInfo> PLAYER_TEAMS = new PlayerDataWrapper<>(TeamInfo.class).saved();
    public static final GlobalDataWrapper<TeamManager> TEAMS = new GlobalDataWrapper<>(TeamManager.class).saved();
    public static List<BaseTeamCommand.InviteData> invites = new ArrayList<>();

    private Map<UUID, Team> teams = new HashMap<>();
    public static boolean wasDay = true;

    public static Team getTeamById(UUID teamID){
        return TEAMS.get().teams.get(teamID);
    }

    public Team getTeam(Player player){
        TeamInfo teamInfo = PLAYER_TEAMS.get(player);

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
        if (TEAMS.get() == null) TEAMS.clear();  // TODO: ?
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
            if (!oldTeam.getMaterials().isEmpty()){
                player.displayClientMessage(Component.literal("Since you were the last member in your team, you took your materials with you to your new team.\nTransferred: " + JsonHelper.get().toJson(oldTeam.getMaterials())), false);
            }
            oldTeam.getMaterials().subtract(oldTeam.getMaterials());

            // TODO: generators and turrets but have to update the data in the tile entity as well.
            // getTeam(player).getGenerators().putAll(oldTeam.getGenerators());
            // oldTeam.getGenerators().clear();
        }

        TEAMS.setDirty();
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
