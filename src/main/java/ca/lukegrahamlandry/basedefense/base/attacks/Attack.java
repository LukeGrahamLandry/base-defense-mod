package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


// TODO: currently next wave only happens if you kill all the monsters in the current wave
// so if you just keep one alive you dont have to keep fighting things
// options:
// - make the waves time based so all the waves will always happen over the night
// - have a reward for defeating all the waves (ie. enhanced material production)

public class Attack {
    protected final Level level;
    protected final UUID teamID;
    protected final List<AttackWave> waves = new ArrayList<>();
    protected final AttackLocation target;
    int waveIndex = 0;
    private final ServerBossEvent bar = new ServerBossEvent(Component.literal(""), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    private boolean done = false;

    public Attack(UUID teamID, AttackLocation target){
        this.level = target.level();
        this.teamID = teamID;
        this.target = target;
        this.initWaves();
    }

    protected void initWaves(){
        for (int i=0;i<3;i++){
            AttackWave wave = new AttackWave(this.target);
            for (int j=0;j<=i;j++){
                wave.add(EntityType.HUSK);
            }
            this.waves.add(wave);
        }
    }

    public void start(){
        this.getCurrentWave().doSpawning(this.target);
    }

    public void tick(){
        updateBossBar();
        if (this.isOver()) return;
        if (this.getCurrentWave().isDefeated()){
            this.waveIndex++;
            if (!this.isOver()) {
                this.getCurrentWave().doSpawning(this.target);
            }
        }
    }

    public boolean isOver(){
        return waveIndex >= this.waves.size() || this.done || !this.target.target().isStillAlive();
    }

    public AttackWave getCurrentWave(){
        if (waveIndex >= this.waves.size()) return this.waves.get(this.waves.size() - 1);
        return this.waves.get(this.waveIndex);
    }

    public void updateBossBar(){
        float totalHealth = 0;
        float remainingHealth = 0;

        for (LivingEntity enemy : getCurrentWave().spawned){
            totalHealth += enemy.getMaxHealth();
            if (enemy.isAlive()) remainingHealth += enemy.getHealth();
        }

        this.bar.setProgress(Mth.clamp(remainingHealth / totalHealth, 0.0F, 1.0F));
        this.bar.setName(Component.literal("Attack on (" + this.target.pos().getX() + ", " + this.target.pos().getY() + ", " + this.target.pos().getZ() + ") - Wave " + (this.waveIndex + 1) + "/" + this.waves.size()));

        Team team = TeamManager.getTeamById(teamID);

        if (!this.isOver()){
            for (Player player : level.players()){
                if (team.contains(player)){
                    bar.addPlayer((ServerPlayer) player);
                }
            }

            List<ServerPlayer> players = new ArrayList<>(bar.getPlayers());
            for (ServerPlayer player : players){
                if (!player.level.dimension().equals(this.level.dimension())){
                    bar.removePlayer(player);
                }
            }
        }
    }

    public void forceEnd(){
        this.done = true;
        this.getCurrentWave().killAllEnemies();
        bar.removeAllPlayers();
    }
}
