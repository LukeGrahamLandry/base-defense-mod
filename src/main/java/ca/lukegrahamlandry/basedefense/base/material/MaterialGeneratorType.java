package ca.lukegrahamlandry.basedefense.base.material;

import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MaterialGeneratorType {
    public static final ResourcesWrapper<MaterialGeneratorType> GENERATOR_TYPES = ResourcesWrapper.data(MaterialGeneratorType.class, "materialgenerators");
    public static final MaterialGeneratorType EMPTY = new MaterialGeneratorType();

    public List<LevelInfo> tiers;

    public static class Instance {
        public ResourceLocation type;
        public int tier;

        public Instance(ResourceLocation type, int tier){
            this.type = type;
            this.tier = tier;
        }

        public MaterialCollection getProduction(){
            return GENERATOR_TYPES.get(this.type).tiers.get(tier).production;
        }

        // materials required to upgrade from previous (tier-1) to this (tier)
        public MaterialCollection getUpgradeCost(){
            var data = GENERATOR_TYPES.get(this.type);
            if (tier >= data.tiers.size()) return null;
            return data.tiers.get(tier).cost;
        }

        public Instance next(){
            var data = GENERATOR_TYPES.get(this.type);
            if (tier+1 >= data.tiers.size()) return null;
            return new MaterialGeneratorType.Instance(this.type, tier + 1);
        }
    }

    public static class LevelInfo {
        MaterialCollection cost;
        MaterialCollection production;
        int minBaseLevel = 0;
    }

    // for class loading
    public static void init(){}
}
