package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetSelectGoal;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.events.ServerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OngoingAttack {
    enum WaveAction {
        ACTIVE, DONE, FROZEN
    }
    public static class State {
        AttackLocation location;
        ResourceLocation dimension;
        int currentWave = 0;
        List<ResourceLocation> waves;
        List<UUID> currentWaveMonsters = new ArrayList<>();
        List<ChunkPos> forcedChunks = new ArrayList<>();
        float currentWaveMaxHealth = 0;
        WaveAction action = WaveAction.ACTIVE;
        UUID id;

        public State(AttackLocation location, List<ResourceLocation> waves){
            this.location = location;
            this.waves = waves;
            dimension = location.dimension;
            id = UUID.randomUUID();
        }

        AttackWaveType getWave(){
            return AttackWaveType.DATA.get(waves.get(currentWave));
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) return false;
            return Objects.equals(this.id, ((State) obj).id);
        }

        @Override
        public int hashCode() {
            return this.id.hashCode();
        }
    }

    private final State state;
    private final ServerLevel level;
    final Team team;
    private final List<LivingEntity> monsters = new ArrayList<>();
    private final ServerBossEvent bar = new ServerBossEvent(Component.literal(""), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    public OngoingAttack(State state, Team team){
        this.state = state;
        this.team = team;
        this.level = ServerEvents.server.getLevel(ResourceKey.create(Registries.DIMENSION, state.dimension));
    }

    public void resume(){
        for (ChunkPos pos : state.forcedChunks){
            level.setChunkForced(pos.x, pos.z, true);
        }

        List<UUID> found = new ArrayList<>();
        for (UUID id : state.currentWaveMonsters){
            Entity e = level.getEntity(id);
            if (e instanceof LivingEntity monster){
                found.add(id);
                monsters.add(monster);
                if (monster instanceof Mob) addAttackGoals((Mob) monster);
            }
        }
        state.currentWaveMonsters = found;
        if (!found.isEmpty()){
            state.action = WaveAction.ACTIVE;
        }
    }

    public void startWave(AttackWaveType wave){
        if (wave == null){
            state.action = WaveAction.FROZEN;
            state.currentWave++;
            return;
        }

        if (!state.location.validTarget()){  // I think I fixed this with updateAttackTargetCache.
            // Here I could force load that chunk and update the target just in case this still happens.
            team.message(Component.literal("ERROR: failed to start attack wave."));
            state.action = WaveAction.FROZEN;
            return;
        }

        state.action = WaveAction.ACTIVE;

        state.currentWaveMaxHealth = 0;
        for (EntityType<?> type : wave.toSpawn(level)){
            BlockPos pos = state.location.getRandSpawnLocation();
            LivingEntity enemy = (LivingEntity) type.create(level);
            if (enemy == null) continue;
            enemy.setPos(pos.getX(), pos.getY(), pos.getZ());
            if (enemy instanceof Mob) addAttackGoals((Mob) enemy);
            enemy.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20*60*10, 0));
            BaseTier.get(team.getBaseTier()).applyMonsterModifiers(enemy);
            level.addFreshEntity(enemy);
            doParticles(enemy, ParticleTypes.DRAGON_BREATH);
            monsters.add(enemy);
            state.currentWaveMonsters.add(enemy.getUUID());
            state.currentWaveMaxHealth += enemy.getMaxHealth();
        }
    }

    public void tick(){
        if (state.action == WaveAction.DONE) return;
        if (!state.location.validTarget()){
            state.action = WaveAction.FROZEN;
            return;
        } else if (state.action == WaveAction.FROZEN){
            startWave(state.getWave());
            if (state.action == WaveAction.FROZEN) return;
        }

        if (AttackLocation.destroyed.contains(this.state.location.getTarget())){
            AttackLocation.targets.remove(this.state.location.id);
            team.message(Component.literal("The monsters attack was successful :("));
            end();
            return;
        }

        if (currentWaveDead()){
            state.currentWave++;
            if (state.currentWave >= state.waves.size()){
                team.message(Component.literal("Your team defeated all the attack waves."));
                end();
                return;
            }

            startWave(state.getWave());
        }

        updateBossBar();
    }

    public boolean currentWaveDead(){
        monsters.removeIf((e) -> !e.isAlive());
        return monsters.isEmpty();
    }

    public void end(){
        killAllEnemies();
        for (ChunkPos pos : state.forcedChunks){
            level.setChunkForced(pos.x, pos.z, false);
        }
        bar.removeAllPlayers();
        state.action = WaveAction.DONE;
    }

    public void addAttackGoals(Mob mob){
        mob.targetSelector.addGoal(0, new AttackTargetSelectGoal(mob, this.state.location));
    }

    public void updateBossBar(){
        if (state.action != WaveAction.ACTIVE) return;

        float totalHealth = state.currentWaveMaxHealth;
        float remainingHealth = 0;

        for (LivingEntity enemy : monsters){
            if (enemy.isAlive()) remainingHealth += enemy.getHealth();
        }

        if (remainingHealth == 0){  // probably never happens cause action should be done but just to be safe
            this.bar.setProgress(1);
            this.bar.setName(Component.literal("Attack Defeated"));
        } else {
            this.bar.setProgress(Mth.clamp(remainingHealth / totalHealth, 0.0F, 1.0F));
            this.bar.setName(Component.literal("Attack on (" + this.state.location.pos.getX() + ", " +  this.state.location.pos.getY() + ", " +  this.state.location.pos.getZ() + ") - Wave " + (this.state.currentWave + 1) + "/" + this.state.waves.size()));
        }

        for (ServerPlayer player : level.players()){
            if (team.contains(player)){
                bar.addPlayer(player);
            }
        }

        List<ServerPlayer> players = new ArrayList<>(bar.getPlayers());
        for (ServerPlayer player : players){
            if (!player.level.dimension().equals(this.level.dimension())){
                bar.removePlayer(player);
            }
        }
    }

    public void killAllEnemies() {
        for (LivingEntity check : monsters){
            if (check.isAlive()) doParticles(check, ParticleTypes.LARGE_SMOKE);
            check.discard();
        }
        monsters.clear();
        state.currentWaveMonsters.clear();
    }

    public static void doParticles(LivingEntity mob, ParticleOptions particle){
        if (!mob.level.isClientSide()) {
            for (int i = 0; i < 20; i++) {
                Vec3 pos = new Vec3(mob.getRandomX(1.5F), mob.getRandomY(), mob.getRandomZ(1.5F));
                ((ServerLevel) mob.getLevel()).sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OngoingAttack)) return false;
        return Objects.equals(this.state, ((OngoingAttack) obj).state);
    }

    @Override
    public int hashCode() {
        return this.state.hashCode();
    }
}
