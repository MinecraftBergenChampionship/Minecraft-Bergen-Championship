package gg.mbc.commands;

import gg.mbc.event.MBCEvent;
import gg.mbc.event.players.EventPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

public class ping implements CommandExecutor {
    private TextColor pingColor(double ping) {
        if (ping >= 110) {
            return NamedTextColor.RED;
        } else if (ping >= 80) {
            return TextColor.color(0xF9A734);
        } else if (ping >= 51) {
            return NamedTextColor.YELLOW;
        } else {
            return NamedTextColor.GREEN;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length == 1) {
            player = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    player = p;
                    break;
                }
            }
            if (player == null) {
                sender.sendMessage(text("Could not find that player!", NamedTextColor.RED));
                return false;
            }
        } else if (args.length != 0){
            sender.sendMessage(text("Usage: /ping [player]"));
            return false;
        }
        MBCEvent mbc = MBCEvent.getInstance();
        EventPlayer eventPlayer = mbc.getPlayer(player.getUniqueId());
        Plugin plugin = mbc.getPlugin();

        sender.sendMessage("Conducting ping test...");
        Player tmpPlayer= player;
        new BukkitRunnable() {
            double total = 0;
            int i = 0;
            @Override
            public void run() {
                if (!tmpPlayer.isOnline()) {
                    this.cancel();
                    return;
                }
                long ping = tmpPlayer.getPing();
                total += ping;
                sender.sendMessage(eventPlayer.getEventName().append(text("'s ping: ").append(text(ping + "ms", pingColor(ping)))));
                ((Player) sender).playSound((Player) sender, Sound.ENTITY_ARROW_HIT_PLAYER, 1,1);
                i++;
                if (i >= 5) {
                    this.cancel();
                    double avg = total / i;
                    sender.sendMessage(text("Average ping: ").append(text(avg + "ms", pingColor(avg))));
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }
}
