package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface Upgradable {
    int getTier();
    int getMaxTier();
    boolean canAccess(Player player);
    boolean upgrade(MaterialCollection inv);
    MaterialCollection getUpgradeCost();

    default boolean tryUpgrade(ServerPlayer thePlayer){
        return this.canAccess(thePlayer) && this.upgrade(MaterialsUtil.getMaterials(thePlayer));
    }
}
