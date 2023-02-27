package ca.lukegrahamlandry.basedefense.base.attacks;

import ca.lukegrahamlandry.basedefense.base.BaseTier;
import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.events.ServerEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class AttackManager {
    private static final RandomSource rand = RandomSource.create();
    public static List<OngoingAttack> attacks = new ArrayList<>();

    public static void tick(){
        boolean isDay = ServerEvents.server.overworld().isDay();
        if (!isDay && TeamManager.wasDay){
            startAllAttacks();
        } else if (isDay && !TeamManager.wasDay) {
            for (OngoingAttack attack : attacks){
                attack.end();
                attack.team.message(Component.literal("Your team survived the night."));
            }
            attacks.clear();
        }

        if (!isDay){
            for (OngoingAttack attack : attacks){
                attack.tick();
            }
        }

        if (isDay != TeamManager.wasDay){
            TeamManager.wasDay = isDay;
            TeamManager.TEAMS.setDirty();
        }
    }

    private static List<ResourceLocation> getWaves(Team team){
        BaseTier tier = BaseTier.get(team.getBaseTier());
        if (tier.getAttackOptions().isEmpty()) return List.of();
        return tier.getAttackOptions().get(rand.nextInt(tier.getAttackOptions().size()));
    }

    public static void startAllAttacks() {
        for (Team team : TeamManager.TEAMS.get().getTeams()){
            List<AttackLocation> targets = team.getAttackOptions();
            if (targets.isEmpty()) {
                team.message(Component.literal("The monsters had nothing to attack this night."));
                return;
            }

            AttackLocation target = targets.get(rand.nextInt(targets.size()));
            List<ResourceLocation> waves = getWaves(team);
            if (waves.isEmpty()){
                team.message(Component.literal("There was no monster attack this night."));
                return;
            }
            OngoingAttack.State state = new OngoingAttack.State(target, waves);
            OngoingAttack attack = new OngoingAttack(state, team);
            attacks.add(attack);
            attack.startWave(state.getWave());
            team.message(Component.literal("An attack has started at " + target.pos + " in the " + target.dimension.getPath()));
        }
    }

    public static void resume(ServerPlayer player){
        Team team = TeamManager.get(player);
        if (team.attack == null) return;
        OngoingAttack attack = new OngoingAttack(team.attack, team);
        if (attacks.contains(attack)) return;
        attacks.add(attack);
        attack.resume();
        team.message(Component.literal("There's an active attack at " + team.attack.location.pos + " in the " + team.attack.location.dimension.getPath()));
    }
}
