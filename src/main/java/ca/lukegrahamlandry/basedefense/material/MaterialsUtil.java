package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.server.level.ServerPlayer;

public class MaterialsUtil {
    public static MaterialCollection getMaterials(ServerPlayer player){
        TeamHandler.Team team = TeamHandler.get(player.getLevel()).getTeam(player);
        return team.getMaterials();
    }
}
