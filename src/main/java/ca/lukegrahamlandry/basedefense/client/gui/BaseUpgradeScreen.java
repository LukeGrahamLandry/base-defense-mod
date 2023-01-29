package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.network.serverbound.UpgradeBasePacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BaseUpgradeScreen extends Screen {
    private final MaterialCollection playerMaterials;
    private final MaterialCollection upgradeCost;
    private final int nextLevel;
    private final List<String> itemsUnlockedNextLevel;
    private Component info;
    private List<ItemStack> displayItems = new ArrayList<>();
    private MutableComponent unlockTitle;

    public BaseUpgradeScreen(MaterialCollection storage, MaterialCollection upgradeCost, int nextLevel, List<String> itemsUnlockedNextLevel) {
        super(Component.literal("hi"));
        this.playerMaterials = storage;
        this.upgradeCost = upgradeCost;
        this.nextLevel = nextLevel;
        this.itemsUnlockedNextLevel = itemsUnlockedNextLevel;
    }

    @Override
    protected void init() {
        super.init();

        if (upgradeCost == null){
            this.info = Component.literal("Base Tier " + (this.nextLevel - 1) + " (MAX)");
            return;
        }

        this.info = Component.literal("Base Tier " + (this.nextLevel - 1));
        this.unlockTitle = Component.literal("Crafting Unlocked");

        createMaterialsList(Component.literal("Your Materials"), this.playerMaterials, 10, 20);
        createMaterialsList(Component.literal("Upgrade Cost"), this.upgradeCost, 110, 20);

        Button upgrade = Button.builder(Component.literal("Upgrade (to tier " + this.nextLevel + ")"), this::doUpgrade).bounds(0, 0, 100, 20).build();
        upgrade.active = this.playerMaterials.canAfford(this.upgradeCost);
        this.addRenderableWidget(upgrade);

        for (String name : this.itemsUnlockedNextLevel){
            if (!name.startsWith("#")){
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(name));
                this.displayItems.add(new ItemStack(item));
            }
        }
    }

    private void doUpgrade(Button button) {
        new UpgradeBasePacket().sendToServer();
        Minecraft.getInstance().setScreen(null);
    }

    private void createMaterialsList(Component label, MaterialCollection materials, int x, int y) {
        addRenderableWidget(new PlainTextButton(x + 5, y, 90, 20, label, (b) ->{}, font));
        this.addRenderableWidget(new SimpleMaterialsList(this, materials, Minecraft.getInstance(), x, y + 15, y + 300, 90, 300));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        drawString(pPoseStack, font, this.info, 110, 5, 0xFFFFFF);

        if (upgradeCost == null){
            return;
        }

        drawString(pPoseStack, font, this.unlockTitle, 210, 5, 0xFFFFFF);

        int y = 40;
        for (ItemStack stack : this.displayItems){
            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 210, y);
            y += 20;
        }
    }
}
