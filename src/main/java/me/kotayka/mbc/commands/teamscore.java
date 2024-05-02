package me.kotayka.mbc.commands;

import me.kotayka.mbc.Lobby;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.teams.Spectator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class teamscore implements CommandExecutor {
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
            sender.sendMessage(ChatColor.GREEN+"[score] " + ChatColor.RESET+"Usage: /score [set/add/subtract] [team] [amount]");
            sender.sendMessage(ChatColor.RESET+"Notice: Team score changes will NOT affect any individual scores. This may also mess up stat logging of multiplied scores.");
            return false;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED+"Invalid arguments");
            sender.sendMessage(ChatColor.GREEN+"[score] " + ChatColor.RESET+"Usage: /score [set/add/subtract] [team] [amount]");
            sender.sendMessage(ChatColor.RESET+"Notice: Team score changes will NOT affect any individual scores. This may also mess up stat logging of multiplied scores.");
            return false;
        }

        MBCTeam t = MBCTeam.getTeam(args[1]);
        if (t == null) {
            sender.sendMessage(ChatColor.RED + "Please Provide a valid team name");
            return false;
        }

        if (t instanceof Spectator) {
            sender.sendMessage(ChatColor.RED+"Cannot change stats of Spectator Team!");
            return false;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED+"Could not parse provided amount " + args[2] + ".");
            return false;
        }

        if (args[0].equalsIgnoreCase("set")) {
            t.setTotalScore(amount);
        } else if (args[0].equalsIgnoreCase("add")) {
            t.addTotalScoreManual(amount);
        } else if (args[0].equalsIgnoreCase("subtract")) {
            t.addTotalScoreManual(amount*-1);
        }

        ((Lobby)MBC.getInstance().getMinigame()).colorPodiums();

        Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + sender.getName() + " successfully added " + amount + " points to " + args[1]);

        return true;
    }
}
