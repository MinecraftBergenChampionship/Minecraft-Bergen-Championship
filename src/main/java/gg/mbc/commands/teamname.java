package gg.mbc.commands;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.teams.EventTeam;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.kyori.adventure.text.Component.text;

public class teamname implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }

        if (args.length == 0) {
            p.sendMessage("Usage: /teamname [change] [name] or /teamname reset");
            return false;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            TeamManager manager = MBCEvent.getInstance().getTeamManager();
            EventTeam team = manager.getTeam(p.getUniqueId());
            team.resetName();
        }
        if (!(args[0].equalsIgnoreCase("change"))) {
            p.sendMessage("Usage: /teamname [change] [name] or /teamname reset");
            return false;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("change")) {
            p.sendMessage(text("Please provide a name at most 16 characters, including spaces.", NamedTextColor.RED));
            return false;
        }
        TeamManager manager = MBCEvent.getInstance().getTeamManager();
        EventTeam team = manager.getTeam(p.getUniqueId());
        if (team == null) {
            p.sendMessage(text("Could not find team for player " + p.name(), NamedTextColor.RED));
            return false;
        }
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        return team.changeName(name.substring(0, Math.min(EventTeam.MAX_NAME_LENGTH, name.length())));
    }
}
