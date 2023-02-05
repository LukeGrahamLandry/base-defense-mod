package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.network.clientbound.OpenBaseUpgradeGui;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialShopGui;
import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import net.minecraft.server.level.ServerPlayer;

public enum RequestGuiPacket implements ServerSideHandler {
    BASE,
    SHOP;

    @Override
    public void handle(ServerPlayer serverPlayer) {
        if (this == BASE){
            new OpenBaseUpgradeGui(serverPlayer).sendToClient(serverPlayer);
        } else if (this == SHOP){
            new OpenMaterialShopGui(serverPlayer).sendToClient(serverPlayer);
        }
    }
}
