package ca.lukegrahamlandry.basedefense.base.material;

import ca.lukegrahamlandry.lib.base.GenericHolder;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// can use this for how much player has, how much an upgrade costs, players current production
public class MaterialCollection {
    private Map<ResourceLocation, Integer> materials = new HashMap<>();

    public static MaterialCollection empty() {
        return new MaterialCollection();
    }

    // Calculate what <other> has that <this> doesn't.
    // This can be negative.
    public MaterialCollection getDifference(MaterialCollection other){
        MaterialCollection difference = new MaterialCollection();

        for (ResourceLocation rl : other.keys()){
            if (!has(rl)){
                difference.add(rl, other.get(rl));
            } else {
                difference.add(rl, other.get(rl) - get(rl));
            }
        }

        return difference;
    }

    public boolean canAfford(MaterialCollection cost){
        return this.getDifference(cost).isEmpty();
    }

    public MaterialCollection copy(){
        return new MaterialCollection().getDifference(this);
    }

    // Negatives are seen as 0 so this can be used safely on results from getDifference
    public boolean isEmpty(){
        if (this.materials.isEmpty()) return true;
        for (ResourceLocation rl : keys()){
            if (get(rl) > 0) return false;
        }
        return true;
    }

    public void add(ResourceLocation rl, int amount){
        if (amount == 0) return;

        if (materials.containsKey(rl)){
            materials.put(rl, get(rl) + amount);
        } else {
            materials.put(rl, amount);
        }
    }

    public void add(MaterialCollection other){
        if (other.isEmpty()) return;

        for (ResourceLocation rl : other.keys()){
           add(rl, other.get(rl));
        }
    }

    public void subtract(MaterialCollection other) {
        if (other.isEmpty()) return;

        for (ResourceLocation rl : other.keys()){
            add(rl, -other.get(rl));
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (ResourceLocation key : keys()){
            out.append(key.toString()).append("-").append(get(key)).append(",");
        }
        return out.toString();
    }

    // MAP

    public Set<ResourceLocation> keys(){
        return new HashSet<>(materials.keySet());
    }

    public int get(ResourceLocation key){
        if (!this.materials.containsKey(key)) return 0;
        return this.materials.get(key);
    }

    public boolean has(ResourceLocation key){
        return this.materials.containsKey(key);
    }

    public MaterialCollection(){

    }

    public void clear() {
       this.materials.clear();
    }


    public static class TypeAdapter implements JsonDeserializer<MaterialCollection>, JsonSerializer<MaterialCollection> {
        public TypeAdapter() {
        }

        public MaterialCollection deserialize(JsonElement data, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            Map<ResourceLocation, Integer> materials = (Map<ResourceLocation, Integer>) JsonHelper.get().fromJson(data, TypeToken.getParameterized(HashMap.class, new Type[]{ResourceLocation.class, Integer.class}));
            MaterialCollection m = new MaterialCollection();
            m.materials = materials;
            return m;
        }

        public JsonElement serialize(MaterialCollection obj, Type type, JsonSerializationContext ctx) {
            return JsonHelper.get().toJsonTree(obj.materials);
        }
    }
}
