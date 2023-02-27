package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class TurretTiers {
    public static final ResourcesWrapper<Type> DATA = ResourcesWrapper.data(Type.class, "turrets").synced().onLoad(MaterialShop::reload).onReceiveSync(MaterialShop::reload);
    public static final Stats EMPTY_STATS = new Stats();

    public static class Stats {
        public int damage = 1;
        public int rangeInBlocks = 5;
        public int shotDelayTicks = 20;
        public int flameSeconds = 0;
        public List<MobEffectInstance> potionEffects = new ArrayList<>();
        public String color = "none";
        public float rotationDegreesPerTick = 10F;
        public MaterialCollection ammo = MaterialCollection.empty();
    }

    public static class Tier {
        public MaterialCollection cost = MaterialCollection.empty();
        public Stats stats = new Stats();
        public int minBaseTier = 0;
    }

    public static class Type {
        public List<Tier> tiers = new ArrayList<>();
    }

    public static boolean isMaxTier(ResourceLocation type, int tier){
        Type data = DATA.get(type);
        return data == null || (tier + 1) >= data.tiers.size();
    }

    public static MaterialCollection upgradeCost(ResourceLocation type, int targetTier){
        if (isMaxTier(type, targetTier - 1)) return null;
        return DATA.get(type).tiers.get(targetTier).cost;
    }

    public static Stats getStats(ResourceLocation type, int tier){
        Type data = DATA.get(type);
        if (data == null || data.tiers.isEmpty()) {
            ModMain.LOGGER.error("Invalid turret type: " + type);
            return EMPTY_STATS;
        }
        if (tier >= data.tiers.size()){
            int max = data.tiers.size() - 1;
            ModMain.LOGGER.error("Turret type: " + type + " tier " + tier + " corrected to max of " + max + ". Data pack changed?");
            return data.tiers.get(max).stats;
        }

        return data.tiers.get(tier).stats;
    }

    // for classloading
    public static void init() {}
}
