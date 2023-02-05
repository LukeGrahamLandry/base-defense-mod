package ca.lukegrahamlandry.basedefense.game.item;

import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LootedGeneratorPlacer extends BlockItem {
    public LootedGeneratorPlacer() {
        super(ModRegistry.LOOTED_GENERATOR_BLOCK.get(), new Item.Properties());
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof MaterialGeneratorTile){
            ((MaterialGeneratorTile) tile).setType(getType(pStack), getTier(pStack));
        }
        return super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
    }

    public Component getName(ItemStack pStack) {
        return MaterialGeneratorType.getDisplayName(getType(pStack), getTier(pStack));
    }

    public static ResourceLocation getType(ItemStack stack){
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains("type")) return new ResourceLocation(nbt.getString("type"));
        return MaterialGeneratorType.EMPTY.type;
    }

    public static int getTier(ItemStack stack){
        CompoundTag nbt = stack.getOrCreateTag();
        if (nbt.contains("tier")) return nbt.getInt("tier");
        return 0;
    }

}
