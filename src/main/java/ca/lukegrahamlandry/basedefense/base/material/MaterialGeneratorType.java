package ca.lukegrahamlandry.basedefense.base.material;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class MaterialGeneratorType {
    //// Data Pack Loading ////

    public static final MaterialGeneratorType EMPTY = new MaterialGeneratorType();
    public static final Set<ResourceLocation> correctedTypes = new HashSet<>();
    static {
        EMPTY.type = new ResourceLocation(ModMain.MOD_ID, "empty");
        EMPTY.tiers = Arrays.asList(new TierStats());
    }

    public static final ResourcesWrapper<MaterialGeneratorType> GENERATOR_TYPES =
            ResourcesWrapper.data(MaterialGeneratorType.class, "materialgenerators").onLoad(MaterialGeneratorType::onLoadDatapack);

    private static void onLoadDatapack() {
        for (var entry : GENERATOR_TYPES.entrySet()){
            entry.getValue().type = entry.getKey();
        }
    }

    //// Loaded Type Data ////

    private List<TierStats> tiers;  // read from json
    public ResourceLocation type;  // don't include in json, it gets injected on load based on file name

    public Instance createInst(int tier){
        return new Instance(type, tier);
    }

    private int clampTier(int tier){
        var effectiveTier = tier >= this.tiers.size() ? this.tiers.size() - 1 : tier;
        if (tier != effectiveTier){
            if (correctedTypes.add(this.type)){
                ModMain.LOGGER.debug(
                        "Generator " + this.type + " tier corrected from " + tier + " to max of " + effectiveTier + ". " +
                                "Probably someone had a high tier generator then the data packs were adjusted to lower the max tier."
                );
            }
        }
        return effectiveTier;
    }

    public static class TierStats {
        MaterialCollection cost = new MaterialCollection();
        MaterialCollection production = new MaterialCollection();
        int minBaseTier = 0;
    }

    //// Instance Data Helper ////

    public static Component getDisplayName(ResourceLocation type, int tier){
        return Component.translatable("generator." + type.getNamespace() + "." + type.getPath(), tier);
    }

    public static class Instance {
        public ResourceLocation type;
        public int tier;

        public Instance(ResourceLocation type, int tier){
            this.type = type;
            this.tier = tier;
        }

        public MaterialCollection getProduction(){
            return this.getStats().production;
        }

        // materials required to upgrade from (tier) to (tier + 1)
        public MaterialCollection getUpgradeCost(){
            return this.getNextTier().getStats().cost;
        }

        public Instance getNextTier(){
            return new MaterialGeneratorType.Instance(this.type, tier + 1);
        }

        public boolean isMaxTier(){
            return tier+1 >= this.safeGetType().tiers.size();
        }

        private TierStats getStats(){
            return safeGetType().tiers.get(safeGetType().clampTier(tier));
        }

        private MaterialGeneratorType safeGetType(){
            MaterialGeneratorType stats = GENERATOR_TYPES.get(this.type);
            if (stats == null){
                if (correctedTypes.add(type)){
                    ModMain.LOGGER.debug(
                            "Generator type " + this.type + " corrected from " + this.type + " to " + EMPTY.type + "." +
                                    "Probably someone had a generator then the data packs were adjusted to remove or rename its type."
                    );
                }
                return EMPTY;
            }
            return stats;
        }
    }

    // for class loading
    public static void init(){}
}
