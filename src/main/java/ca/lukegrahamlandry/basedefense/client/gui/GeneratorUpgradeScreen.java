package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class GeneratorUpgradeScreen extends Screen {
    private final int tier;
    private final ResourceLocation type;
    private final MaterialCollection currentProduction;
    private final MaterialCollection nextProduction;
    private final MaterialCollection upgradeCost;
    private final MaterialCollection playerMaterials;

    public GeneratorUpgradeScreen(int tier, ResourceLocation type, MaterialCollection currentProduction, MaterialCollection nextProduction, MaterialCollection upgradeCost, MaterialCollection playerMaterials) {
        super(new TranslatableComponent("generator." + type.getNamespace() + "." + type.getPath(), tier));
        this.tier = tier;
        this.type = type;
        this.currentProduction = currentProduction;
        this.nextProduction = nextProduction;
        this.upgradeCost = upgradeCost;
        this.playerMaterials = playerMaterials;
    }
}
