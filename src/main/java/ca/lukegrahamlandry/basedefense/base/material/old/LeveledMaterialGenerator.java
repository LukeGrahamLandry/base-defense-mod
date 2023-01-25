package ca.lukegrahamlandry.basedefense.base.material.old;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public interface LeveledMaterialGenerator extends Upgradable {
    MaterialCollection getProduction();
    UUID getUUID();
    MaterialCollection getNextProduction();
    ResourceLocation getGenType();
}
