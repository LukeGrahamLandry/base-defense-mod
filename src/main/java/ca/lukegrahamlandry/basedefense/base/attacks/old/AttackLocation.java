package ca.lukegrahamlandry.basedefense.base.attacks.old;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public record AttackLocation(Level level, BlockPos pos, UUID id, AttackTargetable target) {
    static Random rand = new Random();
    public BlockPos getRandSpawnLocation() {
        List<BlockPos> positions = target.getSpawnLocations();
        return positions.get(rand.nextInt(positions.size()));
    }
}
