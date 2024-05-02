package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.teams.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class playerscore implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;

        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED+"This command is admin restricted!");
            return false;
        }

        if (!(MBC.getInstance().getMinigame() instanceof Lobby)) {
            sender.sendMessage(ChatColor.RED+"This command should only be done in the lobby!");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN+"[score] " + ChatColor.RESET+"Usage: /score [set/add/subtract] [player] [amount]");
            sender.sendMessage(ChatColor.RESET+"Notice: individual score changes will directly affect team score. This may also mess up stat logging of multiplied scores.");
            return false;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED+"Invalid arguments");
            sender.sendMessage(ChatColor.GREEN+"[score] " + ChatColor.RESET+"Usage: /score [set/add/subtract] [player] [amount]");
            sender.sendMessage(ChatColor.RESET+"Notice: individual score changes will directly affect team score. This may also mess up stat logging of multiplied scores.");
            return false;
        }

        Participant p = Participant.getParticipant(args[1]);
        if (p == null) {
            sender.sendMessage(ChatColor.RED+"Could not find player " + args[1] + ", please check capitalization and other spelling errors.");
            return false;
        }

        if (p.getTeam() instanceof Spectator) {
            sender.sendMessage(ChatColor.RED+"Player " + args[1] + " is not on a team!");
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED+"Could not parse provided amount " + args[2] + ".");
            return false;
        }

        if (args[0].equalsIgnoreCase("set")) {
            p.setTotalScore(amount);
        } else if (args[0].equalsIgnoreCase("add")) {
            p.addTotalScore(amount);
        } else if (args[0].equalsIgnoreCase("subtract")) {
            p.addTotalScore(amount*-1);
        }

        // TODO update this to use a sorting structure
        MBC.getInstance().updatePlacings();
        ((Lobby)MBC.getInstance().getMinigame()).colorPodiums();
        ((Lobby)MBC.getInstance().getMinigame()).updatePlayerTotalScoreDisplay(p);

        Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + sender.getName() + " successfully added " + amount + " points to " + args[1]);

        return true;
    }
}
