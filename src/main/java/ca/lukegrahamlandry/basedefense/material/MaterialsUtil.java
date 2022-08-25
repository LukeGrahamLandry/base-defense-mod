package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.server.level.ServerPlayer;

public class MaterialsUtil {
    public static MaterialCollection getMaterials(ServerPlayer player){
        Team team = TeamHandler.get(player.getLevel()).getTeam(player);
        return team.getMaterials();
    }

    public static MaterialCollection getProduction(ServerPlayer player){
        Team team = TeamHandler.get(player.getLevel()).getTeam(player);
        return MaterialGenerationHandler.get(player.level).getProduction(team.id);
    }
}
