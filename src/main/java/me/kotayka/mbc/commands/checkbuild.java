package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.bsabmMaps.BuildPlot;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class checkbuild implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!(MBC.getInstance().getGame() instanceof BuildMart)) {
            sender.sendMessage(ChatColor.GREEN+"[checkbuild] " + ChatColor.RESET+ "Cannot use this command outside of Build Mart!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            StringBuilder msg = new StringBuilder(ChatColor.GREEN+"[checkbuild] " +  ChatColor.RESET+"Usage: Checks the plot the user is standing in.\n");
            msg.append(ChatColor.GREEN+"[checkbuild] " + ChatColor.RESET+"Functionality: tells the user the first incorrect block found.");
            sender.sendMessage(msg.toString());
            return true;
        }

        BuildMart game = (BuildMart) MBC.getInstance().getGame();
        Participant p = Participant.getParticipant((Player) sender);

        if (p == null) {
            sender.sendMessage(ChatColor.GREEN + "[checkbuild] " + ChatColor.RESET+ "Not supported for spectator teams, sorry!");
            return true;
        }

        BuildMartTeam team = game.getTeam(p);
        Location l = p.getPlayer().getLocation();
        for (int i = 0; i < BuildMart.NUM_PLOTS_PER_TEAM; i++) {
            BuildPlot plot = team.getPlots()[i][1];
            if (!(plot.inBuildPlot(l))) continue;

            plot.getBuild().checkBuildCommand(p.getPlayer(), plot.getMIDPOINT());
            return true;
        }
        sender.sendMessage(ChatColor.GREEN+"[checkbuild] " + ChatColor.RESET+ "You are not in a plot!");
        return true;
    }
}