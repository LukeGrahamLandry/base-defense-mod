package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.Config;
import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MaterialGenerationHandler extends SavedData {
    // [team -> [generator id -> generator data]]
    private final Map<UUID, Map<UUID, MaterialCollection>> generators = new HashMap<>();
    // [team -> materials]
    private final Map<UUID, MaterialCollection> pendingMaterials = new HashMap<>();

    public void addGenerator(UUID owner, UUID generatorID, MaterialCollection generator){
        if (!generators.containsKey(owner)) generators.put(owner, new HashMap<>());
        if (!pendingMaterials.containsKey(owner)) pendingMaterials.put(owner, new MaterialCollection());

        generators.get(owner).put(generatorID, generator);
        this.setDirty();
    }

    public void removeGenerator(UUID owner, UUID generatorID){
        if (generators.containsKey(owner)){
            generators.get(owner).remove(generatorID);
            this.setDirty();
        }
    }

    public void distributeMaterials(MinecraftServer world){
        TeamHandler teams = TeamHandler.get(world.overworld());

        List<UUID> onlinePlayers = world.getPlayerList().getPlayers().stream().map((player -> player.getUUID())).collect(Collectors.toList());

        for (TeamHandler.Team team : teams.getTeams()){
            if (Config.requiresOnlineForGenereation()){
                AtomicBoolean hasPlayerOnline = new AtomicBoolean(false);
                for (UUID check : onlinePlayers){
                    if (team.contains(check)){
                        hasPlayerOnline.set(true);
                        break;
                    }
                }
                if (!hasPlayerOnline.get()) continue;
            }

            pendingMaterials.get(team.id).add(getProduction(team.id));
            team.getMaterials().add(pendingMaterials.get(team.id));
        }
    }

    public MaterialCollection getProduction(UUID owner){
        MaterialCollection output = new MaterialCollection();
        for (MaterialCollection production : generators.get(owner).values()){
            output.add(production);
        }
        return output;
    }

    // SaveData methods

    public static MaterialGenerationHandler get(Level level){
        return ((ServerLevel)level).getServer().overworld().getDataStorage().computeIfAbsent(MaterialGenerationHandler::load, MaterialGenerationHandler::new, ModMain.MOD_ID + ":materialgenerators");
    }

    private static final String GENERATORS_TAG_KEY = "generators";
    private static final String PENDING_TAG_KEY = "pending";

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag generatorsTag = new CompoundTag();
        for (UUID player : generators.keySet()){
            CompoundTag playerGeneratorsTag = new CompoundTag();
            for (UUID gen : generators.get(player).keySet()){
                playerGeneratorsTag.put(gen.toString(), generators.get(player).get(gen).toNBT());
            }
            generatorsTag.put(player.toString(), playerGeneratorsTag);
        }
        tag.put(GENERATORS_TAG_KEY, generatorsTag);

        CompoundTag pendingTag = new CompoundTag();
        for (UUID player : pendingMaterials.keySet()){
            pendingTag.put(player.toString(), pendingMaterials.get(player).toNBT());
        }
        tag.put(PENDING_TAG_KEY, pendingTag);
        return tag;
    }

    private static MaterialGenerationHandler load(CompoundTag tag) {
        MaterialGenerationHandler handler = new MaterialGenerationHandler();

        CompoundTag generatorsTag = tag.getCompound(GENERATORS_TAG_KEY);
        for (String playerid : generatorsTag.getAllKeys()){
            UUID player = UUID.fromString(playerid);
            CompoundTag playerGeneratorsTag = generatorsTag.getCompound(playerid);
            for (String genid : playerGeneratorsTag.getAllKeys()){
                UUID gen = UUID.fromString(genid);
                handler.addGenerator(player, gen, new MaterialCollection(playerGeneratorsTag.getCompound(genid)));
            }
        }

        CompoundTag pendingTag = tag.getCompound(PENDING_TAG_KEY);
        for (String playerid : pendingTag.getAllKeys()){
            UUID player = UUID.fromString(playerid);
            handler.pendingMaterials.put(player, new MaterialCollection(pendingTag.getCompound(playerid)));
        }

        return handler;
    }
}
