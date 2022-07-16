package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GeneratorUpgradeScreen extends Screen {
    private final int tier;
    private final ResourceLocation type;
    private final MaterialCollection currentProduction;
    private final MaterialCollection nextProduction;
    private final MaterialCollection upgradeCost;
    private final MaterialCollection playerMaterials;

    private final int topPos;
    private final int leftPos;
    private final int imageWidth = 400;
    private final int imageHeight = 400;
    private final int middleX;

    public GeneratorUpgradeScreen(int tier, ResourceLocation type, MaterialCollection currentProduction, MaterialCollection nextProduction, MaterialCollection upgradeCost, MaterialCollection playerMaterials) {
        super(new TranslatableComponent("generator." + type.getNamespace() + "." + type.getPath(), tier));
        this.tier = tier;
        this.type = type;
        this.currentProduction = currentProduction;
        this.nextProduction = nextProduction;
        this.upgradeCost = upgradeCost;
        this.playerMaterials = playerMaterials;

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.middleX = this.leftPos + this.imageWidth / 2;
    }

    Component title;

    @Override
    protected void init() {
        super.init();
        this.title = new TranslatableComponent("generator." + type.getNamespace() + "." + type.getPath(), this.tier);

        createMaterialsList(new TextComponent("Production"), this.currentProduction, 10, 20);
        createMaterialsList(new TextComponent("Upgrade Cost"), this.upgradeCost, 110, 20);
        createMaterialsList(new TextComponent("Next Production"), this.nextProduction, 210, 20);
    }

    private void createMaterialsList(Component label, MaterialCollection materials, int x, int y) {
        addRenderableWidget(new PlainTextButton(x + 5, y, 90, 20, label, (b) ->{}, font));
        this.addRenderableWidget(new SimpleMaterialsList(this, materials, Minecraft.getInstance(), x, y + 15, y + 300, 90, 300));
        System.out.println(label.getContents() + ": " + materials.toString());
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        drawCenteredString(pPoseStack, font, this.title, this.width / 2, 5, 0xFFFFFF);
    }
}
