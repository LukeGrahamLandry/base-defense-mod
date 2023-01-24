package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.init.NetworkInit;
import ca.lukegrahamlandry.basedefense.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.network.serverbound.UpgradeTilePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class GeneratorUpgradeScreen extends Screen {
    private final int tier;
    private final ResourceLocation type;
    private final MaterialCollection currentProduction;
    private final MaterialCollection nextProduction;
    private final MaterialCollection upgradeCost;
    private final MaterialCollection playerMaterials;
    private BlockPos pos;

    private final int topPos;
    private final int leftPos;
    private final int imageWidth = 400;
    private final int imageHeight = 400;
    private final int middleX;

    public GeneratorUpgradeScreen(int tier, ResourceLocation type, MaterialCollection currentProduction, MaterialCollection nextProduction, MaterialCollection upgradeCost, MaterialCollection playerMaterials, BlockPos pos) {
        super(Component.translatable("generator." + type.getNamespace() + "." + type.getPath(), tier));
        this.tier = tier;
        this.type = type;
        this.currentProduction = currentProduction;
        this.nextProduction = nextProduction;
        this.upgradeCost = upgradeCost;
        this.playerMaterials = playerMaterials;
        this.pos = pos;

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.middleX = this.leftPos + this.imageWidth / 2;
    }

    Component title;
    Button upgrade;

    @Override
    protected void init() {
        super.init();
        this.title = Component.translatable("generator." + type.getNamespace() + "." + type.getPath(), this.tier);

        createMaterialsList(Component.literal("Production"), this.currentProduction, 10, 20);
        createMaterialsList(Component.literal("Upgrade Cost"), this.upgradeCost, 110, 20);
        createMaterialsList(Component.literal("Next Production"), this.nextProduction, 210, 20);

        this.upgrade = new PlainTextButton(0, 0, 100, 20, Component.literal("Upgrade (" + (this.tier+1) + ")"), this::doUpgrade, Minecraft.getInstance().font);
        this.upgrade.active = this.playerMaterials.canAfford(this.upgradeCost);
        this.addRenderableWidget(this.upgrade);
    }

    private void doUpgrade(Button button) {
        NetworkInit.INSTANCE.sendToServer(new UpgradeTilePacket(this.pos));
        Minecraft.getInstance().setScreen(null);
    }

    private void createMaterialsList(Component label, MaterialCollection materials, int x, int y) {
        addRenderableWidget(new PlainTextButton(x + 5, y, 90, 20, label, (b) ->{}, font));
        this.addRenderableWidget(new SimpleMaterialsList(this, materials, Minecraft.getInstance(), x, y + 15, y + 300, 90, 300));
        System.out.println(label.getContents() + ": " + materials.toString());
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        drawString(pPoseStack, font, this.title, 110, 5, 0xFFFFFF);
    }
}
