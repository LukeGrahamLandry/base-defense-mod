package ca.lukegrahamlandry.basedefense.attacks;

import ca.lukegrahamlandry.basedefense.attacks.goal.AttackTargetSelectGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class AttackWave {
    private final List<Function<Level, LivingEntity>> toSpawn = new ArrayList<>();
    final List<LivingEntity> spawned = new ArrayList<>();
    private final AttackLocation target;

    public AttackWave(AttackLocation target){
        this.target = target;
    }

    public void add(Supplier<LivingEntity> spawn){
        toSpawn.add((level) -> spawn.get());
    }

    public void add(EntityType<? extends LivingEntity> entityType){
        toSpawn.add(entityType::create);
    }

    public void doSpawning(AttackLocation target){
        for (Function<Level, LivingEntity> spawner : this.toSpawn){
            BlockPos pos = target.getRandSpawnLocation();
            LivingEntity enemy = spawner.apply(target.level());
            if (enemy instanceof Mob) addAttackGoals((Mob) enemy);
            enemy.setPos(pos.getX(), pos.getY(), pos.getZ());
            target.level().addFreshEntity(enemy);
            doParticles(enemy, ParticleTypes.DRAGON_BREATH);
            spawned.add(enemy);
        }
    }

    public void addAttackGoals(Mob mob){
        mob.targetSelector.addGoal(0, new AttackTargetSelectGoal(mob, this.target));
    }

    public boolean isDefeated(){
        for (LivingEntity check : spawned){
            if (check.isAlive()) return false;
        }
        return true;
    }

    public void killAllEnemies() {
        for (LivingEntity check : spawned){
            if (check.isAlive()) doParticles(check, ParticleTypes.LARGE_SMOKE);
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
