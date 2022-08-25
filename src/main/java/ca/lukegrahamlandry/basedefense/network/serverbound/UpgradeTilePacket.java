package ca.lukegrahamlandry.basedefense.network.serverbound;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.material.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.material.Upgradable;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialGeneratorGuiPacket;
import ca.lukegrahamlandry.basedefense.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class UpgradeTilePacket {
    private final BlockPos tilePosition;

    public static void send(BlockPos pos) {
        NetworkInit.INSTANCE.sendToServer(new UpgradeTilePacket(pos));
    }

    public UpgradeTilePacket(FriendlyByteBuf buf) {
        this.tilePosition = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf){
        buf.writeBlockPos(this.tilePosition);
    }

    public UpgradeTilePacket(BlockPos pos){
        this.tilePosition = pos;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            BlockEntity tile = ctx.get().getSender().getLevel().getBlockEntity(this.tilePosition);
            if (tile instanceof Upgradable){
                ((Upgradable) tile).tryUpgrade(ctx.get().getSender());
            }
            if (tile instanceof LeveledMaterialGenerator){
                NetworkInit.INSTANCE.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new OpenMaterialGeneratorGuiPacket(ctx.get().getSender(), (LeveledMaterialGenerator) tile, this.tilePosition));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
