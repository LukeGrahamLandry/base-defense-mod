package ca.lukegrahamlandry.basedefense.client.renderer;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BaseBlockRenderer extends GeoBlockRenderer<BaseTile> {
    public BaseBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new Model());
    }

    static class Model extends GeoModel<BaseTile> {
        private static final ResourceLocation MODEL = new ResourceLocation(ModMain.MOD_ID, "geo/baseblock.geo.json");
        private static final ResourceLocation ANIM = new ResourceLocation(ModMain.MOD_ID, "animations/baseblock.animation.json");
        private static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MOD_ID, "textures/baseblock.png");

        @Override
        public ResourceLocation getModelResource(BaseTile animatable) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(BaseTile animatable) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(BaseTile animatable) {
            return ANIM;
        }
    }
}
