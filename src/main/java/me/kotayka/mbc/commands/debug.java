package me.kotayka.mbc.commands;

import me.kotayka.mbc.Game;
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

public class debug implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return false;
        if (!(sender.isOp())) {
            sender.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
            return true;
        }

        if ((args.length == 1 && args[0].equalsIgnoreCase("help")) || args.length == 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("/debug game\n- Gives debug for game\n/debug players\n- Gives info about all players and dummy players");
            msg.append("\n/debug dummy create [name] <team>\n- Creates dummy player with given name and team (optional)\n/debug delete [name]\n- Deletes specified dummy player");
            sender.sendMessage(msg.toString());
            return true;
        }

        if (args[0].equalsIgnoreCase("game")) {
            Game game = MBC.getInstance().getGame();
            sender.sendMessage(game.getDebugInfo());
            return true;
        } else if (args[0].equalsIgnoreCase("players") || args[0].equalsIgnoreCase("p")) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                sender.sendMessage(p.toString());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("dummy")) {
            String operation = args[1];
            if (operation.equals("create")) {
                createDummy(sender, args);
                return true;
            } else if (operation.equals("delete")) {
                deleteDummy(sender, args);
                return true;
            }
        }
    }

    public void Errormsg(@NotNull CommandSender sender) {
        String error = ChatColor.RED+"Invalid usage\n";
        String msg = "Usages:\n/debug game\n- Gives debug for game\n/debug players\n- Gives info about all players and dummy players";
        sender.sendMessage(error+msg);
    }

    public void createDummy(@NotNull CommandSender sender, String[] args) {
        if (args.length != 3 && args.length != 4) {
            Errormsg(sender);
            return;
        }

        if (args.length == 3) {
            Participant dummy = new Participant(args[2]);
        } else {
            Participant dummy = new Participant(args[2], args[3]);
        }

        sender.sendMessage(ChatColor.GREEN+"Successfully made dummy player with name " + args[2]);
    }

    public void deleteDummy(@NotNull CommandSender sender, String[] args) {
        if (args.length != 3) {
            Errormsg(sender);
            return;
        }

        String name = args[2];
        // TODO: participant.remove();
    }
}
