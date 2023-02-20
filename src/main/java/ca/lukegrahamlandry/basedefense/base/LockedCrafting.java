package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public class LockedCrafting {
    public static boolean allow(Player player, CraftingContainer craftingInputItems) {
        // The inventory 4x4 crafting seems to fire on both but the normal crafting is only on the server.
        if (player.level.getServer() == null) return true;

        Optional<CraftingRecipe> optional = player.level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInputItems, player.level);
        if (optional.isEmpty()) return true;
        ItemStack result = optional.get().assemble(craftingInputItems);

        int tier = TeamManager.get(player).getBaseTier();
        BaseTier base = BaseTier.get(tier);

        return base.canCraft(result);
    }
}
