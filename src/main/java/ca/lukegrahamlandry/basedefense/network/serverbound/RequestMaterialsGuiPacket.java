package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGuiPacket;
import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import net.minecraft.server.level.ServerPlayer;

public class RequestMaterialsGuiPacket implements ServerSideHandler {
    @Override
    public void handle(ServerPlayer serverPlayer) {
        new OpenMaterialsGuiPacket(serverPlayer).sendToClient(serverPlayer);
    }
}
