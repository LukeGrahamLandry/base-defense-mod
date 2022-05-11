package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class GeneratorUpgradeScreen extends Screen {
    public GeneratorUpgradeScreen(int tier, ResourceLocation type, MaterialCollection currentProduction, MaterialCollection nextProduction, MaterialCollection upgradeCost, MaterialCollection playerMaterials) {
        super(new TextComponent("hi"));
    }
}
