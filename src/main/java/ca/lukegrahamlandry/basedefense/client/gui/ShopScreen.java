package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ShopScreen extends Screen {
    private MaterialCollection storage;
    private int baseTier;

    public ShopScreen(MaterialCollection storage, int baseTier) {
        super(Component.literal("Material Shop"));
        this.storage = storage;
        this.baseTier = baseTier;
    }
}
