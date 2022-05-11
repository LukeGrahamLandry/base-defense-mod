package ca.lukegrahamlandry.basedefense.tile;

import ca.lukegrahamlandry.basedefense.init.TileTypeInit;
import ca.lukegrahamlandry.basedefense.material.LeveledMaterialGenerator;
import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class MaterialGeneratorTile extends BlockEntity implements LeveledMaterialGenerator {
    public MaterialGeneratorTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(TileTypeInit.MATERIAL_GENERATOR.get(), pWorldPosition, pBlockState);
    }

    private UUID player;

    public void bind(UUID player){

    }

    public void unBind(){

    }

    @Override
    public MaterialCollection getProduction() {
        return null;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public MaterialCollection getNextProduction() {
        return null;
    }

    @Override
    public int getTier() {
        return 0;
    }

    @Override
    public MaterialCollection getUpgradeCost() {
        return null;
    }
}
