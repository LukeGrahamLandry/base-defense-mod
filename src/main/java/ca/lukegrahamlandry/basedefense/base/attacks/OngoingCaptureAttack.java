package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.base.teams.Team;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.List;

public class OngoingCaptureAttack extends OngoingAttack {
    private final boolean failOnTimeout;
    float timerFraction = 1;
    int timerTotalSeconds = -1;
    protected final ServerBossEvent timerBar = new ServerBossEvent(Component.literal(""), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    public OngoingCaptureAttack(State state, Team team, boolean failOnTimeout) {
        super(state, team);
        this.failOnTimeout = failOnTimeout;
        this.bar.setColor(BossEvent.BossBarColor.PURPLE);
    }


    public void setTimer(float current, float total) {
        this.timerFraction = 1 - Mth.clamp(current / total, 0.0F, 1.0F);
        this.timerTotalSeconds = total < 0 ? -1 : (int) (total / 20);
    }

    @Override
    public void updateBossBar() {
        super.updateBossBar();
        if (state.action != WaveAction.ACTIVE) return;

        if (this.timerTotalSeconds < 0) {
            this.timerBar.removeAllPlayers();
            return;
        }

        this.timerBar.setProgress(this.timerFraction);
        if (this.failOnTimeout){
            this.timerBar.setName(Component.literal("Defeat monsters within " + this.timerTotalSeconds + " seconds to capture."));
        } else {
            this.timerBar.setName(Component.literal("Defend for " + this.timerTotalSeconds + " seconds to capture."));
        }

        for (ServerPlayer player : level.players()){
            if (team.contains(player)){
                timerBar.addPlayer(player);
            }
        }

        List<ServerPlayer> players = new ArrayList<>(timerBar.getPlayers());
        for (ServerPlayer player : players){
            if (!player.level().dimension().equals(this.level.dimension())){
                timerBar.removePlayer(player);
            }
        }
    }

    @Override
    public void end() {
        super.end();
        timerBar.removeAllPlayers();
    }
}
