package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Optional;

public class LockedCrafting {
    public static boolean allow(Player player, CraftingContainer craftingInputItems) {
        // The inventory 4x4 crafting seems to fire on the client but the normal crafting is only on the server.
        // TODO: make sure the locks still work there
        if (player.level.getServer() == null) {
            ModMain.LOGGER.error("LockedCrafting#allow called on client");
            return true;
        }

        Optional<CraftingRecipe> optional = player.level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInputItems, player.level);
        if (optional.isEmpty()) return true;
        ItemStack result = optional.get().assemble(craftingInputItems);

        int tier = TeamManager.get(player).getBaseTier();
        BaseTier base = BaseTier.get(tier);

        return base.canCraft(result);
    }
}
