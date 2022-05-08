package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class PlayerMaterialsScreen extends Screen {
    protected final MaterialCollection stored;
    protected final MaterialCollection production;
    private PlayerMaterialsList materialsList;

    public PlayerMaterialsScreen(MaterialCollection stored, MaterialCollection production) {
        super(new TextComponent("hi"));
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.materialsList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
