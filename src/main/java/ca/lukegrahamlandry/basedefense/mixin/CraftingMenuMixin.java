package ca.lukegrahamlandry.basedefense.mixin;

import ca.lukegrahamlandry.basedefense.base.LockedCrafting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
    @Inject(at=@At("HEAD"), method = "slotChangedCraftingGrid", cancellable = true)
    private static void preventCraftLockedItems(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult, CallbackInfo ci){
        if (!LockedCrafting.allow(pPlayer, pContainer)){
            pResult.setItem(0, ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
