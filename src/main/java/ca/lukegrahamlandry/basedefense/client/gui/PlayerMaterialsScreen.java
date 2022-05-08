package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.util.MaterialCollection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class PlayerMaterialsScreen extends Screen {
    public PlayerMaterialsScreen(MaterialCollection stored, MaterialCollection production) {
        super(new TextComponent("hi"));
        System.out.println(stored.toString());
        System.out.println(production.toString());
    }
}
