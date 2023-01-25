package ca.lukegrahamlandry.basedefense.base.attacks.goal;

import ca.lukegrahamlandry.basedefense.base.attacks.AttackLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class AttackTargetSelectGoal extends Goal {
    private Mob mob;
    private AttackLocation target;

    public AttackTargetSelectGoal(Mob mob, AttackLocation target) {
        this.mob = mob;
        this.target = target;
    }

    @Override
    public boolean canUse() {
        boolean noTarget = this.mob.getTarget() == null || !this.mob.getTarget().isAlive();
        return noTarget && this.target.target().isStillAlive();
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target.target().getAvatar());
    }
}
