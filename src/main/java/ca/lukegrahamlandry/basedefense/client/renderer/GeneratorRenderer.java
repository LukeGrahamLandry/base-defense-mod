package ca.lukegrahamlandry.basedefense.client.renderer;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GeneratorRenderer extends GeoBlockRenderer<MaterialGeneratorTile> {
    public GeneratorRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new Model());
    }

    static class Model extends GeoModel<MaterialGeneratorTile> {
        private static final ResourceLocation MODEL = new ResourceLocation(ModMain.MOD_ID, "geo/generator.geo.json");
        private static final ResourceLocation ANIM = new ResourceLocation(ModMain.MOD_ID, "animations/generator.animation.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MOD_ID, "textures/generator.png");

        @Override
        public ResourceLocation getModelResource(MaterialGeneratorTile animatable) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(MaterialGeneratorTile animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(MaterialGeneratorTile animatable) {
            return ANIM;
        }
    }
}
