package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGuiPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class RequestMaterialsGuiPacket {
    public static void send() {
        NetworkInit.INSTANCE.sendToServer(new RequestMaterialsGuiPacket());
    }

    public RequestMaterialsGuiPacket(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf){

    }

    public RequestMaterialsGuiPacket(){

    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new OpenMaterialsGuiPacket(ctx.get().getSender()));
        });
        ctx.get().setPacketHandled(true);
    }
}
