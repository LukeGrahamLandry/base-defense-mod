package ca.lukegrahamlandry.basedefense.events;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackManager;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetable;
import ca.lukegrahamlandry.basedefense.base.material.MaterialsUtil;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import ca.lukegrahamlandry.lib.config.GenerateComments;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static int timer = 0;
    public static MinecraftServer server = null;
    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event){
        if (event.phase == TickEvent.Phase.START && !event.level.isClientSide() && event.level.dimension().equals(Level.OVERWORLD)){
            AttackManager.tick();
            if (timer >= BaseDefense.CONFIG.get().materialGenerationTickInterval){
                MaterialsUtil.tickGenerators();
                timer = 0;
            }
            timer++;
        }
    }

    @SubscribeEvent
    public static void handleBreak(BlockEvent.BreakEvent event){
        if (!event.getLevel().isClientSide() && event.getState().getBlock() == ModRegistry.LOOTED_GENERATOR_BLOCK.get()){
            MaterialGeneratorTile.getAndDo((Level) event.getLevel(), event.getPos(), MaterialGeneratorTile::unBind);
        }
    }

    @SubscribeEvent
    public static void start(ServerStartedEvent event){
        server = event.getServer();
        updateBiomeConfigCache();
        updateAttackTargetCache();
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event){
        if (event.getEntity().level.isClientSide()) return;
        AttackManager.resume((ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public static void handleSleep(PlayerSleepInBedEvent event){
        if (AttackManager.attacksInProgress()){
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity().displayClientMessage(Component.literal("Cannot sleep while any teams are under attack"), true);
        }
    }

    private static void updateAttackTargetCache() {
        for (Team team : TeamManager.getData().getTeams()){
            for (AttackLocation target : team.getAttackOptions()){
                ServerLevel level = target.getLevel();
                if (level == null) return;

                boolean forceIdk = !level.isLoaded(target.pos);
                if (forceIdk) level.setChunkForced(target.chunk().x, target.chunk().z, true);
                BlockEntity tile = level.getBlockEntity(target.pos);
                if (tile instanceof AttackTargetable tileTarget){
                    if (tileTarget.getOwnerTeam() != team){
                        team.removeAttackLocation(target.id);
                        ModMain.LOGGER.error("Removed attack target (" + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tile.getType()) + ") at " + target.pos + " in " + target.dimension + " because world saved data says it should be owned by " + team + " but the tile thinks its owned by " + tileTarget.getOwnerTeam());
                    } else {
                        AttackLocation.targets.put(tileTarget.getUUID(), tileTarget);
                        ModMain.LOGGER.debug("Loaded attack target (" + ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tile.getType()) + ") at " + target.pos + " in " + target.dimension + " (" + tileTarget.getOwnerTeam() + ")");
                    }
                } else {
                    team.removeAttackLocation(target.id);
                    ModMain.LOGGER.error("Removed attack target (" + (tile == null ? "null" : ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(tile.getType())) + ") at " + target.pos + " in " + target.dimension + " (" + team + ") because it had unknown tile type.");
                }
                if (forceIdk) level.setChunkForced(target.chunk().x, target.chunk().z, false);
            }
        }
    }

    private static void updateBiomeConfigCache() {
        var empty = new ResourceLocation(ModMain.MOD_ID, "empty");
        var config = BaseDefense.CONFIG.get().terrainGeneratorTypes;
        Registry<Biome> biomeRegistry = server.registryAccess().registryOrThrow(Registries.BIOME);
        for (var biome : biomeRegistry.entrySet()){
            ResourceLocation key = biome.getKey().location();
            if (!config.containsKey(key)){
                config.put(key, empty);
            }
        }

        String serializedConfig = GenerateComments.commentedJson(BaseDefense.CONFIG.get(), BaseDefense.CONFIG.getGson());
        try {
            Files.write(getFilePath(server, BaseDefense.CONFIG), serializedConfig.getBytes());
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
