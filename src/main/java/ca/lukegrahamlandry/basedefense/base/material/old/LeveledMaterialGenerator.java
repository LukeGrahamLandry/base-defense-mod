package ca.lukegrahamlandry.basedefense.base.material.old;

import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;

public interface LeveledMaterialGenerator extends Upgradable {
    MaterialGeneratorType.Instance getStats();
}
