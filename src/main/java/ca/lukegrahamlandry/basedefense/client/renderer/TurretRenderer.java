package ca.lukegrahamlandry.basedefense.client.renderer;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TurretRenderer extends GeoBlockRenderer<TurretTile> {
    public TurretRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new Model());
    }

    static class Model extends GeoModel<TurretTile> {
        private static final ResourceLocation MODEL = new ResourceLocation(ModMain.MOD_ID, "geo/turret.geo.json");
        private static final ResourceLocation ANIM = new ResourceLocation(ModMain.MOD_ID, "animations/turret.animation.json");

        @Override
        public ResourceLocation getModelResource(TurretTile animatable) {
            return MODEL;
        }

        @Override
        public ResourceLocation getTextureResource(TurretTile animatable) {
            return animatable.getTexture();
        }

        @Override
        public ResourceLocation getAnimationResource(TurretTile animatable) {
            return ANIM;
        }
    }

    @Override
    public void renderRecursively(PoseStack poseStack, TurretTile animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.getName().equals("bone8") || bone.getName().equals("bone6")){
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(animatable.hRotDeg(partialTick)));
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            poseStack.popPose();
        } else {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
