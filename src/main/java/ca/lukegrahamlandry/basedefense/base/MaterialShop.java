package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.item.TurretPlacer;
import ca.lukegrahamlandry.lib.network.ServerSideHandler;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class MaterialShop {
    private static final ResourcesWrapper<ShopEntry> SHOP_ENTRIES = ResourcesWrapper.data(ShopEntry.class, "shop").synced().onLoad(MaterialShop::reload);
    private static final Map<ResourceLocation, ShopEntry> offerCache = new HashMap<>();

    public static Set<Map.Entry<ResourceLocation, ShopEntry>> getOfferSet() {
        return offerCache.entrySet();
    }

    public static ShopEntry getOffer(ResourceLocation key) {
        return offerCache.get(key);
    }

    public static void reload(){
        offerCache.clear();

        try {
            SHOP_ENTRIES.entrySet().forEach((e) -> offerCache.put(e.getKey(), e.getValue()));
        } catch (NullPointerException e){
            // just ignore when turrets load before this
        }

        try {
            for (var turret : TurretTiers.DATA.entrySet()){
                ItemStack placer = TurretPlacer.create(turret.getKey(), 0);
                var offer = new ShopEntry();
                offer.items = Arrays.asList(placer);
                offer.cost = TurretTiers.upgradeCost(turret.getKey(), 0);
                offer.minBaseTier = turret.getValue().tiers.get(0).minBaseTier;
                offerCache.put(turret.getKey(), offer);
            }
        } catch (NullPointerException e){
            // just ignore when this loads before turrets
        }
    }

    public static class ShopEntry {
        public MaterialCollection cost;
        public List<ItemStack> items;
        public int minBaseTier = 0;
    }

    public static class Buy implements ServerSideHandler {
        private ResourceLocation key;
        public Buy(ResourceLocation key){
            this.key = key;
        }

        @Override
        public void handle(ServerPlayer player) {
            ShopEntry entry = getOffer(this.key);
            if (unableToBuy(player, entry)) return;

            Team team = TeamManager.get(player);
            team.getMaterials().subtract(entry.cost);
            team.setDirty();

            for (ItemStack stack : entry.items){
                safeGive(player, stack.copy());
            }
        }

        // This should always return true because the gui shouldn't let you try to buy something you can't but just in case something breaks or someone cares enough to cheat.
        private boolean unableToBuy(ServerPlayer player, ShopEntry entry) {
            Team team = TeamManager.get(player);
            if (team.getBaseTier() < entry.minBaseTier){
                player.displayClientMessage(Component.literal("Your team doesn't have a high enough base level for " + this.key), false);
                return true;
            }
            if (!team.getMaterials().canAfford(entry.cost)) {
                player.displayClientMessage(Component.literal("Your doesn't have enough materials for " + this.key), false);
                return true;
            }

            return false;
        }

        private void safeGive(ServerPlayer player, ItemStack stack){
            if (!player.addItem(stack)) player.drop(stack, false);
        }
    }

    // for classloading
    public static void init() {}
}
