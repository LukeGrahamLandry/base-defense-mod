package ca.lukegrahamlandry.basedefense.attacks;

import ca.lukegrahamlandry.basedefense.material.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AttackWave {
    private final List<Function<Level, LivingEntity>> toSpawn = new ArrayList<>();
    final List<LivingEntity> spawned = new ArrayList<>();

    public void add(Supplier<LivingEntity> spawn){
        toSpawn.add((level) -> spawn.get());
    }

    public void add(EntityType<? extends LivingEntity> entityType){
        toSpawn.add(entityType::create);
    }

    public void doSpawning(Team.AttackLocation target){
        for (Function<Level, LivingEntity> spawner : this.toSpawn){
            BlockPos pos = target.getRandSpawnLocation();
            LivingEntity enemy = spawner.apply(target.level());
            enemy.setPos(pos.getX(), pos.getY(), pos.getZ());
            target.level().addFreshEntity(enemy);
            spawned.add(enemy);
        }
    }

    public boolean isDefeated(){
        for (LivingEntity check : spawned){
            if (check.isAlive()) return false;
        }
        return true;
    }

    public int countKilled() {
        int count = 0;
        for (LivingEntity check : spawned){
            if (!check.isAlive()) count++;
        }
        return count;
    }

    public void killAllEnemies() {
        for (LivingEntity check : spawned){
            doParticles(check, ParticleTypes.LARGE_SMOKE);
            check.discard();
        }
    }

    public static void doParticles(LivingEntity mob, ParticleOptions particle){
        if (!mob.level.isClientSide()) {
            for (int i = 0; i < 20; i++) {
                Vec3 pos = new Vec3(mob.getRandomX(1.5F), mob.getRandomY(), mob.getRandomZ(1.5F));
                ((ServerLevel) mob.getLevel()).sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
