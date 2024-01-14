package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.MaterialShop;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.network.serverbound.RequestGuiPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends Screen {
    private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
    private MaterialCollection storage;
    private int baseTier;

    private static final int buttonCount = 5;
    private Button[] offerButtons = new Button[buttonCount];
    private int scrollOffset = 0;
    private Button backButton;
    private Button nextButton;

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
        for (var entry : MaterialShop.getOfferSet()){
            if (entry.getValue().minBaseTier > baseTier) continue;

            validOffers.add(entry.getKey());
        }

        this.backButton = this.addRenderableWidget(Button.builder(Component.literal("<"), pButton -> changePage(-1)).bounds(20, this.height - 20, 40, 20).build());
        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), pButton -> changePage(1)).bounds(65, this.height - 20, 40, 20).build());
        this.changePage(0);

        this.addRenderableWidget(Button.builder(Component.literal("[Upgrade Base]"), pButton -> RequestGuiPacket.BASE.sendToServer()).bounds(110, this.height - 20, 75, 20).build());
    }

    private void clickOffer(int buttonIndex) {
        int realIndex = buttonIndex + scrollOffset;
        if (realIndex >= this.validOffers.size()) return;

        ResourceLocation key = this.validOffers.get(realIndex);
        MaterialShop.ShopEntry offer = MaterialShop.getOffer(key);
        if (!this.storage.canAfford(offer.cost)) return;

        this.storage.subtract(offer.cost);
        new MaterialShop.Buy(key).sendToServer();

        highlightIndex = buttonIndex;
        highlightTime = 10;
    }

    private void changePage(int delta){
        scrollOffset += delta * buttonCount;
        this.backButton.active = this.scrollOffset > 0;
        this.nextButton.active = this.scrollOffset < (validOffers.size() - buttonCount);
    }

    int highlightTime = -1;
    int highlightIndex = -1;

    @Override
    public void tick() {
        super.tick();
        if (highlightTime > 0) highlightTime--;
    }

    @Override
    public void render(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(gui, pMouseX, pMouseY, pPartialTick);

        for (int i=0;i<buttonCount;i++){
            int realIndex = i + scrollOffset;

            if (realIndex >= this.validOffers.size()) {
                this.offerButtons[i].visible = false;
                continue;
            }
            this.offerButtons[i].visible = true;

            ResourceLocation key = this.validOffers.get(realIndex);
            MaterialShop.ShopEntry offer = MaterialShop.getOffer(key);
            boolean highlight = highlightTime > 0 && i == highlightIndex;
            renderOffer(gui, offer, 20, 20, 50 + i * 30, highlight);

            this.offerButtons[i].active = this.storage.canAfford(offer.cost);
        }

        var font = Minecraft.getInstance().font;
        int xOffest = 10;
        for (ResourceLocation material : this.storage.keys()){
            int amount = this.storage.get(material);
            ResourceLocation texture = TextureHelper.getMaterialTexture(material);

            RenderSystem.enableBlend();
            gui.blit(texture, xOffest, 5, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();

            gui.drawString(font, String.valueOf(amount), xOffest, 20, ChatFormatting.LIGHT_PURPLE.getColor());

            xOffest += 40;
        }
    }

    // TODO: optimise ordering so i dont have to keep rebinding the villager texture. just do all arrows at once
    private void renderOffer(GuiGraphics gui, MaterialShop.ShopEntry offer, int xOffset, int xDelta, int btnY, boolean highlight){
        int color = highlight ? ChatFormatting.DARK_PURPLE.getColor() : ChatFormatting.LIGHT_PURPLE.getColor();
        var font = Minecraft.getInstance().font;

        // Render price in materials.
        for (ResourceLocation material : offer.cost.keys()){
            int amount = offer.cost.get(material);
            ResourceLocation texture = TextureHelper.getMaterialTexture(material);

            RenderSystem.enableBlend();
            gui.blit(texture, xOffset, btnY, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();
            xOffset += xDelta;

            gui.drawString(font, String.valueOf(amount), xOffset - xDelta, btnY + 15, color);
        }

        // Render arrow.
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (!this.storage.canAfford(offer.cost)) {
            gui.blit(VILLAGER_LOCATION, xOffset, btnY + 3, 0, 25.0F, 171.0F, 10, 9, 512, 256);
        } else {
            gui.blit(VILLAGER_LOCATION, xOffset, btnY + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
        }
        RenderSystem.disableBlend();
        xOffset += xDelta;

        // Render result items.
        for (ItemStack stack : offer.items){
            gui.renderItem(stack, xOffset, btnY);
            gui.drawString(font, String.valueOf(stack.getCount()), xOffset, btnY+15, color);
            xOffset += xDelta;
        }

        // Render result materials.
        for (ResourceLocation material : offer.materials.keys()){
            int amount = offer.materials.get(material);
            ResourceLocation texture = TextureHelper.getMaterialTexture(material);

            RenderSystem.enableBlend();
            gui.blit(texture, xOffset, btnY, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();
            xOffset += xDelta;

            gui.drawString(font, String.valueOf(amount), xOffset - xDelta, btnY + 15, ChatFormatting.GREEN.getColor());
        }
    }
}

// TODO: (for all guis) idk if I still need enableBlend and disableBlend for every blit.,