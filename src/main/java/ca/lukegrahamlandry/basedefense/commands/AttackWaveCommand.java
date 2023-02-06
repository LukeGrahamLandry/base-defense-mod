package ca.lukegrahamlandry.basedefense.commands;

import ca.lukegrahamlandry.basedefense.base.attacks.AttackWave;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AttackWaveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(register());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("attackwave").requires((ctx) -> {
            return ctx.hasPermission(2);
        }).then(Commands.argument("waveid", new AttackWaveArgumentType())
                .executes(AttackWaveCommand::handle));

    }

    public static int handle(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ResourceLocation type = AttackWaveArgumentType.get(source, "waveid");
        AttackWave wave = AttackWave.DATA.get(type);
        ServerPlayer player = source.getSource().getPlayer();

        int i = 0;
        if (player != null){
            for (var entityType : wave.toSpawn(player.level)){
                Entity e = entityType.create(player.level);
                Direction r = Direction.from2DDataValue(i%4);
                e.setPos(player.getX() + (r.getStepX() * 5), player.getY(), player.getZ() + (r.getStepZ() * 5));
                player.level.addFreshEntity(e);
                i++;
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
