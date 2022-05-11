package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.world.level.Level;

public class MaterialGeneratorType {
    public static final MaterialGeneratorType EMPTY = new MaterialGeneratorType();

    public MaterialCollection getProduction(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        return null;
    }

    public MaterialCollection getCost(int tier) {
        if (this == EMPTY) return new MaterialCollection();
        return null;
    }
}
