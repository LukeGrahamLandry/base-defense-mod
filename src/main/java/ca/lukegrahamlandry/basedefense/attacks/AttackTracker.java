package ca.lukegrahamlandry.basedefense.attacks;

import ca.lukegrahamlandry.basedefense.material.Team;
import ca.lukegrahamlandry.basedefense.material.TeamHandler;
import net.minecraft.world.level.Level;

import java.util.*;

public class AttackTracker {
    private static final Random rand = new Random();

    static HashMap<UUID, Attack> attacks = new HashMap<>();
    public static void startAttack(Team team){
        List<AttackLocation> targets = team.getAttackOptions();
        if (targets.isEmpty()) return;
        AttackLocation target = targets.get(rand.nextInt(targets.size()));
        Attack attack = new Attack(team.id, target);
        attacks.put(team.id, attack);
        attack.start();
    }

    public static void startAllAttacks(Level overworld) {
        for (Team team : TeamHandler.get(overworld).getTeams()){
            startAttack(team);
        }
    }

    public static void tick(){
        List<UUID> done = new ArrayList<>();
        for (UUID id : attacks.keySet()) {
            Attack attack = attacks.get(id);

            if (attack.isOver() || attack.level.isDay()) {
                attack.forceEnd();
                done.add(id);
            } else {
                attack.tick();
            }
        }
        for (UUID id : done){
            attacks.remove(id);
        }
    }

    private static void sendAttackOver(Level level, UUID teamID) {

    }
}
