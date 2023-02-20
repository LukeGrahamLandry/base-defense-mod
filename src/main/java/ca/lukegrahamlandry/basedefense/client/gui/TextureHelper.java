package ca.lukegrahamlandry.basedefense.client.gui;

import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import net.minecraft.resources.ResourceLocation;

public class TextureHelper {
    private static final ResourceLocation DEFAULT = new ResourceLocation("minecraft:textures/item/emerald.png");
    public static ResourceLocation getMaterialTexture(ResourceLocation material){
        return BaseDefense.CONFIG.get().materialTextures.getOrDefault(material, DEFAULT);
    }
}
