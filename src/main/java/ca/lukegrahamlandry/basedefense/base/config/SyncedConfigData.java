package ca.lukegrahamlandry.basedefense.base.config;

import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class SyncedConfigData {
    public int materialGenerationTickInterval = 20;
    public Map<ResourceLocation, ResourceLocation> terrainGeneratorTypes = new HashMap<>();

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
