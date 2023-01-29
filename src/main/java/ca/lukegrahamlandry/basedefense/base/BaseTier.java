package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BaseTier {
    private static final List<BaseTier> tiers = new ArrayList<>();
    private static final ResourcesWrapper<BaseTier> BASE_TIERS =
            ResourcesWrapper.data(BaseTier.class, "basetiers")
                    .onLoad((BaseTier::computeTierList));

    int tier;
    MaterialCollection cost;
    List<String> unlockedCraftableItems;

    public boolean canCraft(ItemStack item){
        if (item.isEmpty()) return true;
        String itemRL = BuiltInRegistries.ITEM.getResourceKey(item.getItem()).get().location().toString();

        // If the item is unlocked at a tier above us, you can't craft it yet.
        System.out.println(this.tier);
        for (int i=tiers.size()-1;i>this.tier;i--){
            System.out.println(i);
            var tier = get(i);
            for (String itemDescriptor : tier.unlockedCraftableItems){
                System.out.println("Check " + itemDescriptor);
                if (itemDescriptor.startsWith("#")) {
                    // TODO: tags
                } else {
                    if (itemRL.equals(itemDescriptor)) return false;
                }
            }
        }

        return true;
    }

    public MaterialCollection getNextUpgradeCost(){
        if (this.tier + 1 >= tiers.size()) return null;
        return get(this.tier + 1).cost;
    }

    // TODO: instead of having the data pack declare the level, put an array of resource locations in the config. that way pack makers can have more direct control
    private static void computeTierList() {
        tiers.clear();
        BASE_TIERS.entrySet().forEach((entry) -> tiers.add(entry.getValue()));
        tiers.sort(Comparator.comparingInt(o -> o.tier));
    }

    public static BaseTier get(int teamBaseLevel) {
        return tiers.get(teamBaseLevel);
    }

    // for classloading
    public static void init(){}

    public List<String> getNewItems() {
        return this.unlockedCraftableItems;
    }
}
