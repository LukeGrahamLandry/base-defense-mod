package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.MaterialShop;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopScreen extends Screen {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private MaterialCollection storage;
    private int baseTier;

    private static final int buttonCount = 8;
    private Button[] offerButtons = new Button[buttonCount];
    private int scrollOffset = 0;

    public ShopScreen(MaterialCollection storage, int baseTier) {
        super(Component.literal("Material Shop"));
        this.storage = storage;
        this.baseTier = baseTier;
    }

    List<ResourceLocation> validOffers = new ArrayList<>();

    @Override
    protected void init() {
        super.init();

        for (int i=0;i<buttonCount;i++){
            int finalI = i;

            this.offerButtons[i] = this.addRenderableWidget(
                    Button.builder(Component.literal(""), pButton -> clickOffer(finalI)).bounds(20, 50 + i*30, 200, 20).build()
            );
        }

        this.validOffers.clear();
        for (var entry : MaterialShop.SHOP_ENTRIES.entrySet()){
            if (entry.getValue().minBaseTier > baseTier) continue;
            validOffers.add(entry.getKey());
        }
    }

    private void clickOffer(int buttonIndex) {
        int realIndex = buttonIndex + scrollOffset;
        if (realIndex >= this.validOffers.size()) return;

        ResourceLocation key = this.validOffers.get(realIndex);
        MaterialShop.ShopEntry offer = MaterialShop.SHOP_ENTRIES.get(key);
        if (!this.storage.canAfford(offer.cost)) return;

        this.storage.subtract(offer.cost);
        new MaterialShop.Buy(key).sendToServer();

        // TODO: would be better if it stayed open but your inventory was shown so you could see the new items you get
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        for (int i=0;i<buttonCount;i++){
            int realIndex = i + scrollOffset;

            if (realIndex >= this.validOffers.size()) {
                this.offerButtons[i].visible = false;
                continue;
            }
            this.offerButtons[i].visible = true;

            ResourceLocation key = this.validOffers.get(realIndex);
            MaterialShop.ShopEntry offer = MaterialShop.SHOP_ENTRIES.get(key);
            renderOffer(pPoseStack, offer, 20, 20, 50 + i * 30);

            this.offerButtons[i].active = this.storage.canAfford(offer.cost);
        }

        int xOffest = 10;
        for (ResourceLocation material : this.storage.keys()){
            int amount = this.storage.get(material);
            ResourceLocation texture = TextureHelper.getMaterialTexture(material);

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.enableBlend();
            GuiComponent.blit(pPoseStack, xOffest, 5, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();

            Minecraft.getInstance().font.draw(pPoseStack, String.valueOf(amount), xOffest, 20, ChatFormatting.LIGHT_PURPLE.getColor());

            xOffest += 40;
        }
    }

    // TODO: optimise ordering so i dont have to keep rebinding the villager texture. just do all arrows at once
    private void renderOffer(PoseStack pPoseStack, MaterialShop.ShopEntry offer, int xOffset, int xDelta, int btnY){
        // Render price in materials.
        for (ResourceLocation material : offer.cost.keys()){
            int amount = offer.cost.get(material);
            ResourceLocation texture = TextureHelper.getMaterialTexture(material);

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.enableBlend();
            GuiComponent.blit(pPoseStack, xOffset, btnY, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();
            xOffset += xDelta;

            Minecraft.getInstance().font.draw(pPoseStack, String.valueOf(amount), xOffset-xDelta, btnY+15, ChatFormatting.LIGHT_PURPLE.getColor());
        }

        // Render arrow.
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, VILLAGER_LOCATION);
        if (!this.storage.canAfford(offer.cost)) {
            blit(pPoseStack, xOffset, btnY + 3, this.getBlitOffset(), 25.0F, 171.0F, 10, 9, 512, 256);
        } else {
            blit(pPoseStack, xOffset, btnY + 3, this.getBlitOffset(), 15.0F, 171.0F, 10, 9, 512, 256);
        }
        RenderSystem.disableBlend();
        xOffset += xDelta;

        // Render result items.
        for (ItemStack stack : offer.items){
            Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, xOffset, btnY);
            Minecraft.getInstance().font.draw(pPoseStack, String.valueOf(stack.getCount()), xOffset, btnY+15, ChatFormatting.LIGHT_PURPLE.getColor());
            xOffset += xDelta;
        }
    }
}
