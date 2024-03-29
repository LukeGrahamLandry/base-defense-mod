package ca.lukegrahamlandry.basedefense.client.gui;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

public class PlayerMaterialsList extends ContainerObjectSelectionList<PlayerMaterialsList.Entry> {
    final PlayerMaterialsScreen screen;
    int maxNameWidth;

    public PlayerMaterialsList(PlayerMaterialsScreen screen, Minecraft pMinecraft) {
        super(pMinecraft, screen.width + 45, screen.height, 20, screen.height - 32, 25);
        this.screen = screen;
        // this.setRenderBackground(false); this.setRenderTopAndBottom(false);
        
        Set<ResourceLocation> reliventMaterials = new HashSet<>(screen.production.keys());
        reliventMaterials.addAll(screen.stored.keys());
        List<ResourceLocation> materials = new ArrayList<>(reliventMaterials);
        // todo: sort

        Random rand = new Random();
        this.addEntry(new TitleEntry());
        for(ResourceLocation rl : materials) {
            // Component name = Component.translatable("material." + rl.getNamespace() + "." + rl.getPath());
            Component name = Component.translatable(rl.getPath());
            int i = pMinecraft.font.width(name);
            if (i > this.maxNameWidth) {
                this.maxNameWidth = i;
            }

            this.addEntry(new PlayerMaterialsList.MaterialEntry(TextureHelper.getMaterialTexture(rl), name, screen.stored.get(rl), screen.production.get(rl)));
        }

    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }


    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<PlayerMaterialsList.Entry> {
    }

    public class TitleEntry extends PlayerMaterialsList.Entry {
        private final Component name;
        private final Component stored;
        private final Component produced;

        public TitleEntry() {
            this.name = Component.literal("Material");
            this.stored = Component.literal("Stored");
            this.produced = Component.literal("Production");
        }

        public void render(GuiGraphics gui, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            int f = pLeft + 10 - PlayerMaterialsList.this.maxNameWidth;
            var font = PlayerMaterialsList.this.minecraft.font;
            gui.drawString(font, this.name, f, (pTop + pHeight / 2 - 9 / 2), 16777215);
            gui.drawString(font, this.stored,  pLeft + 105, pTop, 16777215);
            gui.drawString(font, this.produced, pLeft + 190 + 20, pTop, 16777215);
        }

        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class MaterialEntry extends PlayerMaterialsList.Entry {
        private final Component name;
        private final Component stored;
        private final Component produced;
        private final ResourceLocation texture;

        public MaterialEntry(ResourceLocation texture, Component name, int stored, int produced) {
            this.texture = texture;
            this.name = name;
            this.stored = Component.literal(String.valueOf(stored));
            this.produced = Component.literal(String.valueOf(produced));
        }

        public void render(GuiGraphics gui, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            int f = pLeft + 10 - PlayerMaterialsList.this.maxNameWidth;

            var font = PlayerMaterialsList.this.minecraft.font;
            gui.drawString(font, this.name, f, (pTop + pHeight / 2 - 9 / 2), 16777215);
            gui.drawString(font, this.stored,  pLeft + 105, pTop, 16777215);
            gui.drawString(font, this.produced, pLeft + 190 + 20, pTop, 16777215);

            RenderSystem.enableBlend();
            gui.blit(this.texture, (int) (f-20), pTop, 0.0F, 0.0F, 16, 16, 16, 16);
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
