package ca.lukegrahamlandry.basedefense.commands;

import ca.lukegrahamlandry.basedefense.base.teams.Team;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

public class BaseTeamCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(register());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("baseteam")
                .then(Commands.literal("invite")
                        .then(Commands.argument("player_to_invite", EntityArgument.player())
                                .executes(BaseTeamCommand::handleInvite)))
                .then(Commands.literal("accept_invite")
                        .then(Commands.argument("player_to_accept_invite_from", EntityArgument.player())
                                .executes(BaseTeamCommand::handleAcceptInvite)))
                .then(Commands.literal("leave")
                        .executes(BaseTeamCommand::handleLeave))
                .then(Commands.literal("list")
                        .executes(BaseTeamCommand::handleList))
                .then(Commands.literal("kick")
                        .then(Commands.argument("player_to_kick", EntityArgument.player())
                                .executes(BaseTeamCommand::handleKick)));
    }

    private static int handleList(CommandContext<CommandSourceStack> source) {
        ServerPlayer player = source.getSource().getPlayer();
        if (player == null) return Command.SINGLE_SUCCESS;
        Team team = TeamManager.get(player);
        StringBuilder result = new StringBuilder();
        result.append("Team ID: ").append(team.getId());
        for (var uuid : team.getMembers()){
            Player member = source.getSource().getServer().getPlayerList().getPlayer(uuid);
            String name = member == null ? team.getOwner().toString() : member.getScoreboardName();
            if (Objects.equals(uuid, team.getOwner())){
                result.append("\n").append("- Owner: ").append(name);
            } else {
                result.append("\n").append("- Member: ").append(name);
            }
        }
        source.getSource().sendSuccess(() -> Component.literal(result.toString()), false);
        return 0;
    }

    private static int handleKick(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer kickedPlayer = EntityArgument.getPlayer(source, "player_to_kick");
        ServerPlayer ownerPlayer = source.getSource().getPlayer();
        if (ownerPlayer == null) return Command.SINGLE_SUCCESS;

        Team team = TeamManager.get(ownerPlayer);
        if (!ownerPlayer.getUUID().equals(team.getOwner())){
            Player actualOwner = source.getSource().getServer().getPlayerList().getPlayer(team.getOwner());
            String ownerName = actualOwner == null ? team.getOwner().toString() : actualOwner.getScoreboardName();
            source.getSource().sendFailure(Component.literal("You are not the owner of your team (owner=" + ownerName + ")."));
            return Command.SINGLE_SUCCESS;
        }

        if (!team.equals(TeamManager.get(kickedPlayer))){
            source.getSource().sendFailure(Component.literal(kickedPlayer.getScoreboardName() + " is not on your team."));
            return Command.SINGLE_SUCCESS;
        }

        if (ownerPlayer.getUUID().equals(kickedPlayer.getUUID())){
            source.getSource().sendFailure(Component.literal("You cannot kick yourself."));
            return Command.SINGLE_SUCCESS;
        }

        team.message(Component.literal(kickedPlayer.getScoreboardName() + " has been kicked from your team."));
        TeamManager.getData().leaveTeam(kickedPlayer);
        return Command.SINGLE_SUCCESS;
    }

    private static int handleLeave(CommandContext<CommandSourceStack> source) {
        ServerPlayer player = source.getSource().getPlayer();
        if (player == null) return Command.SINGLE_SUCCESS;

        Team team = TeamManager.get(player);
        if (team.getMembers().size() == 1){
            source.getSource().sendFailure(Component.literal("You cannot leave your team if you are the only member. If you accept an invite from another team your current one will automatically be deleted."));
        } else {
            TeamManager.getData().leaveTeam(player);
            source.getSource().sendSuccess(() -> Component.literal("You have left your team. A new team has been created for you. If this was a mistake, the owner can invite you back by using /baseteam invite " + player.getScoreboardName()), false);
        }

        return 0;
    }

    private static int handleAcceptInvite(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer ownerPlayer = EntityArgument.getPlayer(source, "player_to_accept_invite_from");
        ServerPlayer invitedPlayer = source.getSource().getPlayer();
        if (invitedPlayer == null) return Command.SINGLE_SUCCESS;

        Team team = TeamManager.get(ownerPlayer);
        for (var invite : TeamManager.invites){
            if (invite.player.equals(invitedPlayer.getUUID()) && invite.team.equals(team.getId())){
                team.message(Component.literal(invitedPlayer.getScoreboardName() + " has joined your team."));
                TeamManager.getData().switchTeam(invitedPlayer, team.getId());
                source.getSource().sendSuccess(() -> Component.literal("You have joined " + ownerPlayer.getScoreboardName() + "'s team."), false);
                TeamManager.invites.remove(invite);
                return Command.SINGLE_SUCCESS;
            }
        }

        source.getSource().sendFailure(Component.literal("You have not been invited to " + ownerPlayer.getScoreboardName() + "'s team. Ask them to use /baseteam invite " + invitedPlayer.getScoreboardName()));
        return Command.SINGLE_SUCCESS;
    }

    private static int handleInvite(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer invitedPlayer = EntityArgument.getPlayer(source, "player_to_invite");
        ServerPlayer ownerPlayer = source.getSource().getPlayer();
        if (ownerPlayer == null) return Command.SINGLE_SUCCESS;

        Team team = TeamManager.get(ownerPlayer);
        if (!ownerPlayer.getUUID().equals(team.getOwner())){
            Player actualOwner = source.getSource().getServer().getPlayerList().getPlayer(team.getOwner());
            String ownerName = actualOwner == null ? team.getOwner().toString() : actualOwner.getScoreboardName();
            source.getSource().sendFailure(Component.literal("You are not the owner of your team (owner=" + ownerName + ")."));
            return Command.SINGLE_SUCCESS;
        }

        if (team.equals(TeamManager.get(invitedPlayer))){
            source.getSource().sendFailure(Component.literal(invitedPlayer.getScoreboardName() + " is already on your team."));
            return Command.SINGLE_SUCCESS;
        }

        team.message(Component.literal(invitedPlayer.getScoreboardName() + " has been invited to your team."));
        invitedPlayer.displayClientMessage(Component.literal(ownerPlayer.getScoreboardName() + " has invited you to their team. Use /baseteam accept_invite " + ownerPlayer.getScoreboardName()), false);
        TeamManager.invites.add(new InviteData(invitedPlayer.getUUID(), team.getId()));

        return Command.SINGLE_SUCCESS;
    }

    public static class InviteData {
        UUID player;
        UUID team;
        InviteData(UUID player, UUID team){
            this.player = player;
            this.team = team;
        }
    }
}
