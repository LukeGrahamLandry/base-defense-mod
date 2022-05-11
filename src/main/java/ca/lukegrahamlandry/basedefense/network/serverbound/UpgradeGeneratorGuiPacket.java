package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpgradeGeneratorGuiPacket {
    private final BlockPos tilePosition;

    public static void send(BlockPos pos) {
        NetworkInit.INSTANCE.sendToServer(new UpgradeGeneratorGuiPacket(pos));
    }

    public UpgradeGeneratorGuiPacket(FriendlyByteBuf buf) {
        this.tilePosition = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeBlockPos(this.tilePosition);
    }

    public UpgradeGeneratorGuiPacket(BlockPos pos){
        this.tilePosition = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            MaterialGeneratorTile.getAndDo(ctx.get().getSender().getLevel(), this.tilePosition, (generator) -> {
                if (generator.isOwner(ctx.get().getSender())){
                    generator.tryUpgrade();
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
