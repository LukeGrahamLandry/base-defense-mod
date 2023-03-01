package ca.lukegrahamlandry.basedefense.base.material;

import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import net.minecraft.server.level.ServerPlayer;

public class MaterialsUtil {
    public static void tickGenerators(){
        for (var team : TeamManager.getData().getTeams()){  // TODO: check if a player is online
            for (var generator : team.getGenerators().values()){
                team.getMaterials().add(generator.getProduction());
                team.setDirty();
            }
        }
    }

    public static MaterialCollection getTeamMaterials(ServerPlayer player){
        var team = TeamManager.get(player);
        return team.getMaterials();
    }

    public static MaterialCollection getTeamProduction(ServerPlayer player){
        var team = TeamManager.get(player);
        var totalProduction = new MaterialCollection();
        for (var generator : team.getGenerators().values()){
            totalProduction.add(generator.getProduction());
        }
        return totalProduction;
    }
}
