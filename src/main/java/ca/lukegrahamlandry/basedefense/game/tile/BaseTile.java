package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.concurrent.atomic.AtomicInteger;

public class BaseTile extends BlockEntity {
    int tier = 0;
    Integer rfGenerated;
    public BaseTile(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistry.BASE_TILE.get(), pPos, pBlockState);
    }

    public static void setTier(ServerLevel level, BlockPos pos, int tier){
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BaseTile){
            ((BaseTile) tile).tier = tier;
            ((BaseTile) tile).rfGenerated = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("teamTier", tier);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("teamTier")){
            this.tier = pTag.getInt("teamTier");
        }
        this.rfGenerated = null;
    }

    public void serverTick() {
        if (this.rfGenerated == null){
            this.rfGenerated = BaseTier.get(this.tier).rfPerTick;
        }

        if (this.rfGenerated == 0 || this.rfGenerated == null) {
            return;
        }

        AtomicInteger capacity = new AtomicInteger(this.rfGenerated);

        for (Direction direction : Direction.values()) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be != null) {
                boolean doContinue = be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).map(handler -> {
                            if (handler.canReceive()) {
                                int received = handler.receiveEnergy(capacity.get(), false);
                                capacity.addAndGet(-received);
                                setChanged();
                                return capacity.get() > 0;
                            } else {
                                return true;
                            }
                        }
                ).orElse(true);
                if (!doContinue) {
                    return;
                }
            }
        }
    }
}
