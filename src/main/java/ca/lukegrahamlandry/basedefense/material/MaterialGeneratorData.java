package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MaterialGeneratorData {
    public static final MaterialGeneratorData EMPTY = new MaterialGeneratorData(new ResourceLocation(ModMain.MOD_ID, "empty"));

    private ResourceLocation type;
    protected List<MaterialCollection> cost = new ArrayList<>();
    protected List<MaterialCollection> production = new ArrayList<>();

    public MaterialGeneratorData(ResourceLocation name) {
        this.type = name;
    }

    public MaterialCollection getProduction(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        if (tier >= production.size()) return new MaterialCollection();
        return production.get(tier);
    }

    // materials required to upgrade from (tier-1) to (tier)
    public MaterialCollection getUpgradeCost(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        if (tier >= cost.size()) return new MaterialCollection();
        return cost.get(tier);
    }
}
