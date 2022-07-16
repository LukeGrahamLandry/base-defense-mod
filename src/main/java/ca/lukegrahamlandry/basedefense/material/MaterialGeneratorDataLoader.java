package ca.lukegrahamlandry.basedefense.material;

import ca.lukegrahamlandry.basedefense.ModMain;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MaterialGeneratorDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public Map<ResourceLocation, MaterialGeneratorData> generators = new HashMap<>();

    public MaterialGeneratorDataLoader() {
        super(GSON, "materialgenerators");
    }

    protected void apply(Map<ResourceLocation, JsonElement> p_44037_, ResourceManager p_44038_, ProfilerFiller p_44039_) {
        for (Map.Entry<ResourceLocation, JsonElement> entry : p_44037_.entrySet()) {
            ResourceLocation name = entry.getKey();
            if (name.getPath().startsWith("_")) continue; //Forge: filter anything beginning with "_" as it's used for metadata.

            try {
                JsonObject data = entry.getValue().getAsJsonObject();

                JsonArray tiersData = data.get("tiers").getAsJsonArray();
                MaterialGeneratorData generator = new MaterialGeneratorData(name);
                for (JsonElement tierData : tiersData){
                   MaterialCollection cost = new MaterialCollection(tierData.getAsJsonObject().get("cost").getAsJsonObject());
                   MaterialCollection production = new MaterialCollection(tierData.getAsJsonObject().get("production").getAsJsonObject());
                   generator.cost.add(cost);
                   generator.production.add(production);
                }
                generators.put(name, generator);
                ModMain.LOGGER.debug("loaded material generator " + name + " with " + tiersData.size() + " tiers");
            } catch (IllegalArgumentException | JsonParseException error) {
                ModMain.LOGGER.error("Parsing error loading material generator {}", name, error);
            }
        }

        ModMain.LOGGER.info("Loaded {} material generator", generators.size());
    }

    public MaterialGeneratorData get(ResourceLocation type) {
        return generators.get(type);
    }
}



