package ca.lukegrahamlandry.basedefense.material;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// can use this for how much player has, how much an upgrade costs, players current production
// should really learn how to use codecs
public class MaterialCollection {
    private final Map<ResourceLocation, Integer> materials = new HashMap<>();

    // what does <other> have that <this> doesn't
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

    public MaterialCollection copy(){
        return new MaterialCollection().getDifference(this);
    }

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

    // SERIALIZATION

    public MaterialCollection(){

    }

    public MaterialCollection(JsonObject json){
        for (Map.Entry<String, JsonElement> entry : json.entrySet()){
            materials.put(new ResourceLocation(entry.getKey()), entry.getValue().getAsInt());
        }
    }

    public MaterialCollection(FriendlyByteBuf bytes){
        int size = bytes.readInt();
        for (int i=0;i<size;i++){
            ResourceLocation rl = bytes.readResourceLocation();
            int amount = bytes.readInt();
            materials.put(rl, amount);
        }
    }

    public MaterialCollection(CompoundTag tag){
        for (String key : tag.getAllKeys()){
            materials.put(new ResourceLocation(key), tag.getInt(key));
        }
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        for (ResourceLocation rl : keys()){
            json.addProperty(rl.toString(), get(rl));
        }
        return json;
    }

    public FriendlyByteBuf toBytes(FriendlyByteBuf bytes){
        bytes.writeInt(materials.size());
        for (ResourceLocation rl : keys()){
            bytes.writeResourceLocation(rl);
            bytes.writeInt(get(rl));
        }
        return bytes;
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();
        for (ResourceLocation rl : keys()){
            tag.putInt(rl.toString(), get(rl));
        }
        return tag;
    }

}
