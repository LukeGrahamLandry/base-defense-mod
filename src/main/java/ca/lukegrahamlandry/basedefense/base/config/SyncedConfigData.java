package ca.lukegrahamlandry.basedefense.base.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SyncedConfigData {
    public int materialGenerationTickInterval = 20;
    public Map<ResourceLocation, ResourceLocation> terrainGeneratorTypes = new HashMap<>();
}
