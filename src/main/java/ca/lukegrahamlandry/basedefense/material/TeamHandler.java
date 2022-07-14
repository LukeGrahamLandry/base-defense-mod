package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class TeamHandler extends SavedData {
    private final HashMap<UUID, Team> teams = new HashMap<>();
    private final HashMap<UUID, UUID> playerTeams = new HashMap<>();
    
    public Set<Team> getTeams(){
        return new HashSet<>(teams.values());
    }

    public Team getTeam(UUID teamID){
        return teams.get(teamID);
    }

    public Team getTeam(Player player){
        if (!playerTeams.containsKey(player.getUUID())) {
            createTeam(player);
        }
        return teams.get(playerTeams.get(player.getUUID()));
    }

    public UUID createTeam(UUID playerID){
        Team team = new Team();
        team.add(playerID);
        teams.put(team.id, team);
        playerTeams.put(playerID, team.id);
        return team.id;
    }

    public UUID createTeam(Player player){
        return createTeam(player.getUUID());
    }

    // TODO: copy over production as well
    public void switchTeam(UUID player, UUID targetTeam){
        MaterialCollection oldMaterials = new MaterialCollection();
        if (playerTeams.containsKey(player)){
            UUID oldTeam = playerTeams.get(player);
            Team theOldTeam = teams.get(oldTeam);

            teams.get(oldTeam).remove(player);
            if (teams.get(oldTeam).players.size() == 0){
                oldMaterials.add(theOldTeam.materials);
            }
        }

        if (targetTeam == null) {
            targetTeam = createTeam(player);
        }

        teams.get(targetTeam).add(player);
        teams.get(targetTeam).materials.add(oldMaterials);
        playerTeams.put(player, targetTeam);
    }
    


    // SaveData methods

    public static TeamHandler get(Level level){
        return ((ServerLevel)level).getServer().overworld().getDataStorage().computeIfAbsent(TeamHandler::load, TeamHandler::new, ModMain.MOD_ID + ":teams");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        int i = 0;
        for (Team team: teams.values()){
            tag.put(String.valueOf(i), team.write());
            i++;
        }
        return tag;
    }

    private static TeamHandler load(CompoundTag tag) {
        TeamHandler handler = new TeamHandler();

        int i = 0;
        while (tag.contains(String.valueOf(i))){
            Team team = new Team(tag.getCompound(String.valueOf(i)));
            handler.teams.put(team.id, team);
            i++;
        }

        return handler;
    }
}
