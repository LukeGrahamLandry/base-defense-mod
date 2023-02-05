package ca.lukegrahamlandry.basedefense.base.attacks.old;

import ca.lukegrahamlandry.basedefense.base.teams.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface AttackTargetable {
    List<BlockPos> getSpawnLocations();
    boolean damage(DamageSource source, float amount);
    float health();
    float maxHealth();
    LivingEntity getAvatar();
    Team getOwnerTeam();

    default boolean isStillAlive(){
        return health() > 0;
    }

    default void onDie(){

    }

}
