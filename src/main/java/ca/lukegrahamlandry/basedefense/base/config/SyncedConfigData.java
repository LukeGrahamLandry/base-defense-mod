package ca.lukegrahamlandry.basedefense.base.config;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SyncedConfigData {
    public int materialGenerationTickInterval = 20;
    public boolean doTurretParticles = true;
    public List<ResourceLocation> baseTierOrder;
    public Map<ResourceLocation, ResourceLocation> terrainGeneratorTypes = new HashMap<>();
    public Map<ResourceLocation, ResourceLocation> materialTextures = new HashMap<>();
    public boolean turretsWithInvalidTeamTargetAllPlayers = false;
    public boolean turretsWithInvalidTeamTargetMonsters = false;
    public boolean turretsWithInvalidTeamHaveInfiniteAmmo = false;
    public boolean turretsTargetPlayersOnOtherTeams = true;
    public int blockHealthRegenRateTicks = 30*20;

    public SyncedConfigData(){
        baseTierOrder = Arrays.asList(new ResourceLocation(ModMain.MOD_ID, "zero"), new ResourceLocation(ModMain.MOD_ID, "one"), new ResourceLocation(ModMain.MOD_ID, "two"));
        materialTextures.put(new ResourceLocation(ModMain.MOD_ID, "lemon"), new ResourceLocation("minecraft:textures/item/carrot.png"));
        materialTextures.put(new ResourceLocation(ModMain.MOD_ID, "silver"), new ResourceLocation("minecraft:textures/item/iron_ingot.png"));
        materialTextures.put(new ResourceLocation(ModMain.MOD_ID, "platinum"), new ResourceLocation("minecraft:textures/item/gold_ingot.png"));
        materialTextures.put(new ResourceLocation(ModMain.MOD_ID, "orange"), new ResourceLocation("minecraft:textures/item/apple.png"));
        materialTextures.put(new ResourceLocation(ModMain.MOD_ID, "bullet"), new ResourceLocation("minecraft:textures/item/gold_nugget.png"));
    }

    public ResourceLocation getGeneratorTypeFor(Level level, Holder<Biome> biome) {
        var either = biome.unwrap();
        AtomicReference<ResourceLocation> type = new AtomicReference<>();

        either.ifLeft((key) -> {
            type.set(key.location());
        });

        either.ifRight((inst) -> {
            Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            type.set(biomeRegistry.getKey(inst));
        });

        var result = terrainGeneratorTypes.get(type.get());
        if (result == null){
            result = MaterialGeneratorType.EMPTY.type;
        }
        return result;
    }
}
