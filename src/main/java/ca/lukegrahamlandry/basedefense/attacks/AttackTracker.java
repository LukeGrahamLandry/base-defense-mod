package ca.lukegrahamlandry.basedefense.attacks;

import ca.lukegrahamlandry.basedefense.material.Team;
import ca.lukegrahamlandry.basedefense.material.TeamHandler;
import net.minecraft.world.level.Level;

import java.util.*;

public class AttackTracker {
    private static final Random rand = new Random();

    static HashMap<UUID, Attack> attacks = new HashMap<>();
    public static void startAttack(Team team){
        List<Team.AttackLocation> targets = team.getAttackOptions();
        Team.AttackLocation target = targets.get(rand.nextInt(targets.size()));
        attacks.put(team.id, new Attack(team.id, target));
    }

    public static void startAllAttacks(Level overworld) {
        for (Team team : TeamHandler.get(overworld).getTeams()){
            startAttack(team);
        }
    }

    public static void tick(){
        for (Iterator<Attack> it = attacks.values().stream().iterator(); it.hasNext(); ) {
            Attack attack = it.next();

            if (attack.isOver() || attack.level.isDay()) {
                attack.forceEnd();
            } else {
                attack.tick();
            }
        }
    }

    private static void sendAttackOver(Level level, UUID teamID) {

    }
}
