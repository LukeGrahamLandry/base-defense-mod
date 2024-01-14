package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class SimpleMaterialsList extends ContainerObjectSelectionList<SimpleMaterialsList.Entry> {
    int maxNameWidth = 0;
    Screen screen;
    private int xStart;
    private int yStart;

    public SimpleMaterialsList(Screen screen, MaterialCollection materialsToShow, Minecraft pMinecraft, int xStart, int yStart, int yStop, int w, int h) {
        super(pMinecraft, w, h, yStart, yStop, 25);
        this.screen = screen;
        this.xStart = xStart;
        this.yStart = yStart;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        
        Set<ResourceLocation> reliventMaterials = new HashSet<>(materialsToShow.keys());
        List<ResourceLocation> materials = new ArrayList<>(reliventMaterials);
        // todo: sort

        Random rand = new Random();
        for(ResourceLocation rl : materials) {
            // Component name = Component.translatable("material." + rl.getNamespace() + "." + rl.getPath());
            Component name = Component.translatable(rl.getPath());

            this.addEntry(new SimpleMaterialsList.MaterialEntry(TextureHelper.getMaterialTexture(rl), name, materialsToShow.get(rl)));
        }

    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth();
    }


    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<SimpleMaterialsList.Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public class MaterialEntry extends SimpleMaterialsList.Entry {
        private final Component name;
        private final Component stored;
        private final ResourceLocation texture;

        public MaterialEntry(ResourceLocation texture, Component name, int stored) {
            this.texture = texture;
            this.name = name;
            this.stored = Component.literal(String.valueOf(stored));
        }

        public void render(GuiGraphics gui, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            int f = SimpleMaterialsList.this.xStart;

            var font = SimpleMaterialsList.this.minecraft.font;
            gui.drawString(font, this.stored, f + 20, pTop, 16777215);
            gui.drawString(font, this.name, f + 40, pTop, 16777215);

            RenderSystem.enableBlend();
            gui.blit(this.texture, f, pTop-5, 0.0F, 0.0F, 16, 16, 16, 16);
            RenderSystem.disableBlend();

        }
        
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of();
        }
    }
}
