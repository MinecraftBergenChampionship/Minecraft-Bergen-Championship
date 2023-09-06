package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class invincible implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return false;

        if (!sender.isOp()) {
            sender.sendMessage("Admin only command, sorry!");
            return false;
        }

        if (args.length == 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setInvulnerable(false);
                if (p.isOp()) {
                    p.sendMessage(ChatColor.GREEN+sender.getName() + " toggled invulnerability false for all players.");
                }
            }
            sender.sendMessage("For help: /invincible help");
            return true;
        } else {
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("-h")) {
                sender.sendMessage("Usages:");
                sender.sendMessage("/invincible [no args] -> sets all players invulnerability to false");
                sender.sendMessage("/invincible [player] -> returns whether or not they are invincible");
                sender.sendMessage("/invincible [player] [true/false] -> sets a player's invulnerability.");
                return true;
            }

            Player target = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(args[0])) {
                    target = p;
                    break;
                }
            }
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Could not find that player!");
                return false;
            } else {
                if (args.length == 1) {
                    boolean b = target.isInvulnerable();
                    sender.sendMessage(target.getName() + "'s invulnerability status: " + b);
                    return false;
                } else {
                    if (args.length != 2) {
                        sender.sendMessage(ChatColor.RED+"Invalid args");
                        sender.sendMessage("Usages:");
                        sender.sendMessage("/invincible [no args] -> sets all players invulnerability to false");
                        sender.sendMessage("/invincible [player] -> returns whether or not they are invincible");
                        sender.sendMessage("/invincible [player] [true/false] -> sets a player's invulnerability.");
                        return false;
                    }

                    if (args[1].equalsIgnoreCase("true")) {
                        target.setInvulnerable(true);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(ChatColor.GREEN+sender.getName() + " changed " + target.getName() + "'s invulnerability to true. ");
                                sender.sendMessage(ChatColor.GREEN+target.getName() + "'s current invulnerability status: " + target.isInvulnerable());
                            }
                        }
                        return true;
                    } else if (args[1].equalsIgnoreCase("false")) {
                        target.setInvulnerable(false);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.isOp()) {
                                p.sendMessage(ChatColor.GREEN + sender.getName() + " changed " + target.getName() + "'s invulnerability to false. ");
                                sender.sendMessage(ChatColor.GREEN + target.getName() + "'s current invulnerability status: " + target.isInvulnerable());
                            }
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED+"Invalid args");
                        sender.sendMessage("Usages:");
                        sender.sendMessage("/invincible [no args] -> sets all players invulnerability to false");
                        sender.sendMessage("/invincible [player] -> returns whether or not they are invincible");
                        sender.sendMessage("/invincible [player] [true/false] -> sets a player's invulnerability.");
                        return false;
                    }
                }
            }
        }
    }
}
