package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class MaterialGenerationHandler extends SavedData {
    // [player -> [generator id -> generator data]]
    private final Map<UUID, Map<UUID, MaterialCollection>> generators = new HashMap<>();
    // [player -> materials]
    private final Map<UUID, MaterialCollection> pendingMaterials = new HashMap<>();

    public void addGenerator(UUID player, UUID generatorID, MaterialCollection generator){
        if (!generators.containsKey(player)) generators.put(player, new HashMap<>());
        if (!pendingMaterials.containsKey(player)) pendingMaterials.put(player, new MaterialCollection());

        generators.get(player).put(generatorID, generator);
        this.setDirty();
    }

    public void distributeMaterials(ServerLevel world){
        for (ServerPlayer player : world.getPlayers((p) -> pendingMaterials.containsKey(p.getUUID()))){
            MaterialStorageHandler.get(player).add(pendingMaterials.get(player.getUUID()));
        }
    }

    public void tickProduction(){
        for (UUID player : generators.keySet()){
            pendingMaterials.get(player).add(getProduction(player));
        }
    }

    public MaterialCollection getProduction(UUID player){
        MaterialCollection output = new MaterialCollection();
        for (MaterialCollection production : generators.get(player).values()){
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
