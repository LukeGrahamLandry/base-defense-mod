package ca.lukegrahamlandry.basedefense.item;

import ca.lukegrahamlandry.basedefense.init.BlockInit;
import ca.lukegrahamlandry.basedefense.init.ItemInit;
import ca.lukegrahamlandry.basedefense.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaterialGeneratorPlacer extends BlockItem {
    private final ResourceLocation type;
    private final int tier;

    public MaterialGeneratorPlacer(ResourceLocation type, int tier) {
        super(BlockInit.MATERIAL_GENERATOR.get(), new Item.Properties());
        this.type = type;
        this.tier = tier;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof MaterialGeneratorTile){
            ((MaterialGeneratorTile) tile).setType(this.type, this.tier);
        }
        return super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
    }
}
