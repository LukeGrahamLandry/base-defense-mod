package ca.lukegrahamlandry.basedefense.events;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackTracker;
import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import ca.lukegrahamlandry.lib.config.GenerateComments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static int timer = 0;
    private static boolean wasDay = false;
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event){
        if (event.phase == TickEvent.Phase.START && !event.level.isClientSide() && event.level.dimension().equals(Level.OVERWORLD)){
            if (timer >= BaseDefense.CONFIG.get().materialGenerationTickInterval){
                MaterialsUtil.tickGenerators();
                timer = 0;

                // handleAttacks(event.level);  // TODO
            }
            timer++;
        }
    }

    private static void handleAttacks(Level overworld) {
        if (wasDay && !overworld.isDay()){
            System.out.println("start night");
            wasDay = false;
            AttackTracker.startAllAttacks(overworld);
        }
        if (!wasDay && overworld.isDay()){
            System.out.println("start day");
            wasDay = true;

        }
        AttackTracker.tick();
    }

    @SubscribeEvent
    public static void handleBreak(BlockEvent.BreakEvent event){
        if (!event.getLevel().isClientSide() && event.getState().getBlock() == ModRegistry.MATERIAL_GENERATOR_BLOCK.get()){
            MaterialGeneratorTile.getAndDo((Level) event.getLevel(), event.getPos(), MaterialGeneratorTile::unBind);
        }
    }

    @SubscribeEvent
    public static void start(ServerStartedEvent event){
        var empty = new ResourceLocation(ModMain.MOD_ID, "empty");
        var config = BaseDefense.CONFIG.get().terrainGeneratorTypes;
        Registry<Biome> biomeRegistry = event.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        for (var biome : biomeRegistry.entrySet()){
            ResourceLocation key = biome.getKey().location();
            if (!config.containsKey(key)){
                config.put(key, empty);
            }
        }

        String serializedConfig = GenerateComments.commentedJson(BaseDefense.CONFIG.get(), BaseDefense.CONFIG.getGson());
        try {
            Files.write(getFilePath(event.getServer(), BaseDefense.CONFIG), serializedConfig.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: lib should make this public
    private static Path getFilePath(MinecraftServer server, ConfigWrapper<?> config){
        Path path;
        if (config.side.inWorldDir) {
            path = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig");
        } else {
            path = Paths.get("config");
        }

        if (config.getSubDirectory() != null) {
            path = path.resolve(config.getSubDirectory());
        }

        String filename = config.getName() + "-" + config.side.name().toLowerCase(Locale.ROOT) + ".json5";
        return path.resolve(filename);
    }
}
