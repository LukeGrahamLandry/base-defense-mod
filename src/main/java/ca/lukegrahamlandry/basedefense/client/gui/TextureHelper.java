package ca.lukegrahamlandry.basedefense.client.gui;

import net.minecraft.resources.ResourceLocation;

public class TextureHelper {
    public static ResourceLocation getMaterialTexture(ResourceLocation material){
        switch (material.getPath()){
            case "apple":
                return new ResourceLocation("minecraft:textures/item/apple.png");
            case "lemon":
                return new ResourceLocation("minecraft:textures/item/carrot.png");
            case "silver":
                return new ResourceLocation("minecraft:textures/item/iron_ingot.png");
            case "platinum":
                return new ResourceLocation("minecraft:textures/item/gold_ingot.png");
            default:
                return new ResourceLocation("minecraft:textures/item/emerald.png");
        }
    }
}
