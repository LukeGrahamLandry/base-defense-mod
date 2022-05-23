package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface LeveledMaterialGenerator extends Upgradable {
    MaterialCollection getProduction();
    UUID getUUID();
    MaterialCollection getNextProduction();
    ResourceLocation getGenType();
}
