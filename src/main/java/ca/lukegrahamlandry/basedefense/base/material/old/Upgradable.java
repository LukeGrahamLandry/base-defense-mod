package ca.lukegrahamlandry.basedefense.base.material.old;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface Upgradable {
    int getTier();
    int getMaxTier();
    boolean canAccess(Player player);
    boolean upgrade(MaterialCollection inv);
    MaterialCollection getUpgradeCost();

    default boolean tryUpgrade(ServerPlayer thePlayer){
        return this.canAccess(thePlayer) && this.upgrade(MaterialsUtil.getTeamMaterials(thePlayer));
    }
}
