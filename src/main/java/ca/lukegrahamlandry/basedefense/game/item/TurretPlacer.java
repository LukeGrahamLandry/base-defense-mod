package ca.lukegrahamlandry.basedefense.game.item;

import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TurretPlacer extends BlockItem {
    public TurretPlacer() {
        super(ModRegistry.TURRET_BLOCK.get(), new Properties());
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof TurretTile turret){
            if (pPlayer != null){
                if (turret.data.uuid == null) turret.data.uuid = UUID.randomUUID();
                turret.data.hRotDefault = turret.calculateRot(pPlayer.getBoundingBox().getCenter());
                turret.setTeam(TeamManager.get(pPlayer));
            }
            turret.setType(getType(pStack), getTier(pStack));
        }
        return super.updateCustomBlockEntityTag(pPos, pLevel, pPlayer, pStack, pState);
    }

    public Component getName(ItemStack pStack) {
        return getDisplayName(getType(pStack), getTier(pStack));
    }

    public static Component getDisplayName(ResourceLocation type, int tier){
        return Component.translatable("turret." + type.getNamespace() + "." + type.getPath(), tier);
    }

    public static ItemStack create(ResourceLocation type, int tier){
        ItemStack stack = new ItemStack(ModRegistry.TURRET_ITEM.get());
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putString("type", type.toString());
        nbt.putInt("tier", tier);
        stack.setTag(nbt);
        return stack;
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
