package me.kotayka.mbc.commands;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class loadPlayers implements CommandExecutor {

    public static Map<String, String> playerTeams = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return false;

        if (!sender.isOp()) {
            sender.sendMessage("Admin only command, sorry!");
            return false;
        }

        Bukkit.broadcastMessage(ChatColor.GREEN+"Started Automatic Team Assignments");

        String filename = "../../players.txt";

        if (args.length != 0) {
            filename = args[1];
        }

        File file = new File(MBC.getInstance().plugin.getDataFolder(), filename);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                String currentTeam = null;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();

                    if (line.isEmpty()) continue;

                    if (line.endsWith(":")) {
                        currentTeam = line.substring(0, line.length() - 1).trim();
                    } else if (line.startsWith("-")) {
                        if (currentTeam != null) {
                            String playerName = line.substring(2).trim();
                            playerTeams.put(playerName, currentTeam);
                        }
                    }
                }
            } catch (IOException e) {
                sender.sendMessage("An error occurred while reading the file.");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Unable to find file " + file.getAbsolutePath());
            return false;
        }

        for (Participant p : MBC.getInstance().participants) {
            String team = playerTeams.getOrDefault(p.getPlayer().getName(), null);

            if (team != null) {
                p.changeTeam(MBCTeam.getTeam(team));
            }
        }

        return true;
    }

}
