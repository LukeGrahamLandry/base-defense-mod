package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGuiPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class TestPacket {
    public static void send() {
        NetworkInit.INSTANCE.sendToServer(new TestPacket());
    }

    public TestPacket(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf){

    }

    public TestPacket(){

    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

        });
        ctx.get().setPacketHandled(true);
    }
}
