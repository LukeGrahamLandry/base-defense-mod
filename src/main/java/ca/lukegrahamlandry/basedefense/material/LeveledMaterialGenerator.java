package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface LeveledMaterialGenerator {
    MaterialCollection getProduction();
    UUID getUUID();
    MaterialCollection getNextProduction();
    int getTier();
    MaterialCollection getUpgradeCost();
    ResourceLocation getGenType();
}
