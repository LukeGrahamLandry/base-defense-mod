package ca.lukegrahamlandry.basedefense.material;

import java.util.UUID;

public interface LeveledMaterialGenerator {
    MaterialCollection getProduction();
    UUID getUUID();
    MaterialCollection getNextProduction();
    int getTier();
    MaterialCollection getUpgradeCost();
}
