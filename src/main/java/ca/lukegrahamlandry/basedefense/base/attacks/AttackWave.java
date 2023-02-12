package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.lib.resources.ResourcesWrapper;
import com.ibm.icu.text.ArabicShaping;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AttackWave {
    public static ResourcesWrapper<AttackWave> DATA = ResourcesWrapper.data(AttackWave.class, "attackwaves").synced();
    private static RandomSource rand = RandomSource.create();
    private static List<ResourceLocation> EMPTY_MONSTERS = List.of(new ResourceLocation("minecraft:pig"));

    public List<MonsterEntry> monsters;
    public Integer rolls;

    // This does not actually spawn the entities!
    public List<EntityType<?>> toSpawn(Level level){
        List<EntityType<?>> results = new ArrayList<>();
        Registry<EntityType<?>> registry = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE);

        List<ResourceLocation> toSpawn = this.randToSpawn();
        for (ResourceLocation key : toSpawn){
            EntityType<?> type = registry.get(key);
            if (type == null){
                ModMain.LOGGER.debug("Invalid entity type (" + key + ") found in attack wave.");
                continue;
            }
            results.add(type);
        }

        return results;
    }

    public List<ResourceLocation> randToSpawn(){
        List<MonsterEntry> chosen = monsters;

        if (monsters == null || monsters.isEmpty()) return EMPTY_MONSTERS;

        if (rolls != null) {
            chosen = new ArrayList<>();
            for (int i=0;i<rolls;i++){
                chosen.add(WeightedRandom.getRandomItem(rand, monsters).orElseThrow());
            }
        }

        List<ResourceLocation> toSpawn = new ArrayList<>();
        for (var entry : chosen){
            int n = entry.randCount();
            for (int i=0;i<n;i++){
                toSpawn.add(entry.entity);
            }
        }

        return toSpawn;
    }

    public static class MonsterEntry implements WeightedEntry {
        public ResourceLocation entity;
        private Integer max;
        private Integer min;
        private Integer count;
        private Integer weight = 1;

        public int randCount() {
            if (count != null){
                return count;
            }
            if (min == null){
                min = 1;
            }
            if (max == null){
                return min;
            }

            return rand.nextInt(min, max + 1);
        }

        @Override
        public Weight getWeight() {
            return Weight.of(this.weight);
        }
    }

    public static void init(){}
}
