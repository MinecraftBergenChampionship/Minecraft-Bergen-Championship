package me.kotayka.mbc.commands;

import me.kotayka.mbc.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class statlogs implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;

        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED+"Admin restricted command, sorry!");
            return false;
        }

        if (!(MBC.getInstance().getMinigame() instanceof Lobby)) {
            sender.sendMessage(ChatColor.RED+"This command should only be used in lobby!");
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs set [true/false]");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs get [game] [team/individual]");
            sender.sendMessage("Stat logging is currently set to: " + MBC.getInstance().logStats());
            return false;
        }

        if (!(args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("get"))) {
            sender.sendMessage(ChatColor.RED+"Invalid arguments;");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs set [true/false]");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs get [game] [team/individual]");
            return false;
        }

        if ((args.length != 2 && args[0].equalsIgnoreCase("set")) || (args.length != 3 && args[0].equalsIgnoreCase("get"))) {
            sender.sendMessage(ChatColor.RED+"Invalid arguments;");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs set [true/false]");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs get [game] [team/individual]");
            return false;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
                sender.sendMessage(ChatColor.RED+"ERROR: Please provided true or false. Please check your spelling of " + args[1]);
                return false;
            }

            boolean b = args[1].equalsIgnoreCase("true");
            MBC.getInstance().setLogStats(b);
            String s = b ? ChatColor.GREEN+sender.getName() + " successfully enabled stat logging." : ChatColor.GREEN+sender.getName()+" successfully disabled stat logging.";
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isOp()) {
                    p.sendMessage(s);
                }
            }
            return true;
        } else if (args[0].equalsIgnoreCase("get")) {
            if (!MBC.gameNameList.contains(args[1])) {
                sender.sendMessage("Please provide a valid game");
                return false;
            }

            Game game = MBC.getInstance().getGame(args[1]);
            if (game == null) {
                sender.sendMessage("This game has not happened yet!");
                return false;
            }

            if (args[2].equalsIgnoreCase("team")) {
                sender.sendMessage(ChatColor.BOLD+"Team Scores:\n"+ChatColor.RESET);
                int num = 1;
                for (MBCTeam t : game.teamScores) {
                    sender.sendMessage(String.format("%d. %s: %.1f\n", num++, t.teamNameFormat(), game.scoreMap.get(t)));
                }
                return true;
            } else if (args[2].equalsIgnoreCase("individual")) {
                sender.sendMessage(ChatColor.BOLD+"Individual Scores:\n"+ChatColor.RESET);
                int num = 1;
                for (Participant p : game.gameIndividual) {
                    sender.sendMessage(String.format("%d. %s: %d\n", num++, p.getFormattedName(), game.individual.get(p)));
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED+"Invalid arguments;");
                sender.sendMessage(ChatColor.RED+"Usage: /statlogs set [true/false]");
                sender.sendMessage(ChatColor.RED+"Usage: /statlogs get [game] [team/individual]");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED+"Invalid arguments;");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs set [true/false]");
            sender.sendMessage(ChatColor.RED+"Usage: /statlogs get [game] [team/individual]");
            return false;
        }
    }
}
