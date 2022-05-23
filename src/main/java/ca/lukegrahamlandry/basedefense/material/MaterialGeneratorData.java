package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.resources.ResourceLocation;

public class MaterialGeneratorData {
    public static final MaterialGeneratorData EMPTY = new MaterialGeneratorData();

    private ResourceLocation type;

    public MaterialCollection getProduction(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        return null;
    }

    public MaterialCollection getCost(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        return null;
    }
}
