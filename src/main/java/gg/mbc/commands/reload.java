package gg.mbc.commands;

import gg.mbc.EventPlugin;
import gg.mbc.event.MBCEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class reload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!(player.isOp())) {
            player.sendMessage(Component.text("You must be admin to use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage(Component.text("For confirmation, do /reload confirm", NamedTextColor.RED));
            return false;
        }

        EventPlugin plugin = MBCEvent.getInstance().getPlugin();
        plugin.reloadPlugin();


        return true;
    }
}
