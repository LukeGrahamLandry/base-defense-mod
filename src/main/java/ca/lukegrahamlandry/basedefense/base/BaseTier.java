package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class BaseTier {
    private static final List<BaseTier> tiers = new ArrayList<>();
    private static final ResourcesWrapper<BaseTier> BASE_TIERS =
            ResourcesWrapper.data(BaseTier.class, "basetiers")
                    .onLoad((BaseTier::computeTierList));

    int tier;
    ResourceLocation key;  // dont set. injected based on filename
    MaterialCollection cost = MaterialCollection.empty();
    List<String> unlockedCraftableItems = new ArrayList<>();
    List<List<ResourceLocation>> attacks;  // optional
    List<AttributeModifierDef> anyMonsterModifiers;  // optional
    Map<ResourceLocation, List<AttributeModifierDef>> monsterModifiers;  // optional
    public int rfPerTick = 0;

    public boolean canCraft(ItemStack item){
        if (item.isEmpty()) return true;
        String itemRL = BuiltInRegistries.ITEM.getResourceKey(item.getItem()).get().location().toString();

        // If the item is unlocked at a tier above us, you can't craft it yet.
        for (int i=tiers.size()-1;i>this.tier;i--){
            var tier = get(i);
            for (String itemDescriptor : tier.unlockedCraftableItems){
                if (itemDescriptor.startsWith("#")) {
                    // TODO: tags
                } else {
                    if (itemRL.equals(itemDescriptor)) return false;
                }
            }
        }

        return true;
    }

    public MaterialCollection getNextUpgradeCost(){
        if (this.tier + 1 >= tiers.size()) return null;
        return get(this.tier + 1).cost;
    }

    // If this tier doesn't define the attacks field, default to using the previous tier.
    // Tier zero is assigned an empty list if null on data pack load.
    public List<List<ResourceLocation>> getAttackOptions(){
        return this.attacks == null ? this.previous().getAttackOptions() : this.attacks;
    }

    private BaseTier previous(){
        return tiers.get(this.tier - 1);
    }

    // TODO: instead of having the data pack declare the level, put an array of resource locations in the config. that way pack makers can have more direct control
    private static void computeTierList() {
        tiers.clear();
        BASE_TIERS.entrySet().forEach((entry) -> {
            entry.getValue().key = entry.getKey();
            tiers.add(entry.getValue());
        });
        tiers.sort(Comparator.comparingInt(o -> o.tier));
        for (int i=0;i< tiers.size();i++){
            tiers.get(i).tier = i;
        }

        if (tiers.isEmpty()){
            var b = new BaseTier();
            tiers.add(b);
        }

        // Tier zero must always have defined fields so inheritance works.
        if (tiers.get(0).anyMonsterModifiers == null){
            tiers.get(0).anyMonsterModifiers = new ArrayList<>();
        }
        if (tiers.get(0).monsterModifiers == null){
            tiers.get(0).monsterModifiers = new HashMap<>();
        }
        if (tiers.get(0).attacks == null){
            tiers.get(0).attacks = new ArrayList<>();
        }
    }

    public static BaseTier get(int teamBaseLevel) {
        if (teamBaseLevel >= tiers.size()) return tiers.get(tiers.size() - 1);
        return tiers.get(teamBaseLevel);
    }

    // for classloading
    public static void init(){}

    public List<String> getNewItems() {
        return this.unlockedCraftableItems;
    }

    public static class AttributeModifierDef {
        String attribute;
        Double amount;
        AttributeModifier.Operation operation;
    }

    // If this tier doesn't define the monsterModifiers or anyMonsterModifiers field, default to using the previous tier.
    // Tier zero is assigned an empty list/map if null on data pack load.
    private List<AttributeModifierDef> getMonsterAttributes(ResourceLocation entityType){
        BaseTier check = this;
        while (check.anyMonsterModifiers == null){
            check = check.previous();
        }

        List<AttributeModifierDef> modifiers = new ArrayList<>(check.anyMonsterModifiers);

        check = this;
        while (check.monsterModifiers == null){
            check = check.previous();
        }

        if (check.monsterModifiers.get(key) != null) modifiers.addAll(check.monsterModifiers.get(key));

        return modifiers;
    }

    // When a monster is spawned as part of an attack wave, some attribute modifiers will be applied based on the team's base tier.
    public void applyMonsterModifiers(LivingEntity entity){
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getResourceKey(entity.getType()).orElseThrow().location();
        Map<String, Integer> repeatCount = new HashMap<>();  // ensure unique name

        for (var info : getMonsterAttributes(key)){
            if (info.attribute == null || info.amount == null || info.operation == null){
                continue;
            }

            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(info.attribute));
            if (attribute == null) continue;
            ModMain.LOGGER.debug("Unrecognised attribute key (" + info.attribute + ") in monster modifiers for base tier " + this.key);
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) continue;

            int count = repeatCount.getOrDefault(info.attribute, 0);
            String name = "Base Tier " + this.tier + " " + info.attribute + (count == 0 ? "" : " (r" + count + ")");
            repeatCount.put(info.attribute, 1);
            UUID uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
            AttributeModifier modifier = new AttributeModifier(uuid, name, info.amount, info.operation);
            if (!instance.hasModifier(modifier)){
                instance.addTransientModifier(modifier);
            }
        }
    }
}
