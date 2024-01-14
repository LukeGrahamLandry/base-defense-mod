package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlayerMaterialsScreen extends Screen {
    protected final MaterialCollection stored;
    protected final MaterialCollection production;
    private PlayerMaterialsList materialsList;

    public PlayerMaterialsScreen(MaterialCollection stored, MaterialCollection production) {
        super(Component.literal("hi"));
        this.stored = stored;
        this.production = production;
    }

    @Override
    protected void init() {
        super.init();
        this.materialsList = new PlayerMaterialsList(this, Minecraft.getInstance());
        this.addRenderableWidget(this.materialsList);
    }

    @Override
    public void render(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(gui, pMouseX, pMouseY, pPartialTick);
        this.materialsList.render(gui, pMouseX, pMouseY, pPartialTick);
    }
}
