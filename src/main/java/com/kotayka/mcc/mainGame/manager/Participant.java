package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.mainGame.MCC;
import jline.internal.Nullable;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;

public class Participant {
    public int totalCoins;
    public int roundCoins;
    public String team = "Spectator";
    public Color color = Color.WHITE;
    public ChatColor chatColor = ChatColor.GRAY;
    public static List<Participant> participantsOnATeam = new ArrayList<>();
    public String teamPrefix = "";
    public String fullName;

    public final Player player;
    public String ign;

    public Participant(Player player) {
        this.player = player;
        this.ign = player.getName();
    }

    public void Die(Participant victim, Participant killer, PlayerDeathEvent e) {
        String victimName = victim.teamPrefix + victim.chatColor + victim.ign + ChatColor.GRAY;
        Bukkit.broadcastMessage("victimName = " + victimName);
        String oldDeathMessage = e.getDeathMessage();
        String newDeathMessage = "";

        assert oldDeathMessage != null;
        newDeathMessage = oldDeathMessage.replace(victim.ign, victimName);

        if (killer != null) {
            killer.player.sendTitle("\n", "[X] " + victimName, 0, 60, 40);
            victim.player.sendMessage(ChatColor.RED + "You were eliminated by " + killer.ign + "!");
            victim.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
            killer.player.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + victim.ign + "!");

            victim.player.setGameMode(GameMode.SPECTATOR);
            Firework firework = new Firework();
            firework.spawnFireworkWithColor(victim.player.getLocation(), victim.color);

            e.setDeathMessage(newDeathMessage.replace(victim.ign, victimName));
            return;
        }

        Firework firework = new Firework();
        firework.spawnFireworkWithColor(victim.player.getLocation(), victim.color);
        victim.player.setGameMode(GameMode.SPECTATOR);
        victim.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
        victim.player.sendMessage(ChatColor.RED + "You eliminated yourself!");
        e.setDeathMessage(newDeathMessage);
    }

    public void Die(Player victim, Player killer, PlayerDeathEvent e) {
        Participant died = Participant.findParticipantFromPlayer(victim);
        assert died != null;
        String victimName = died.teamPrefix + died.chatColor + died.ign + ChatColor.GRAY;
        String oldDeathMessage = e.getDeathMessage();
        String newDeathMessage = "";

        assert oldDeathMessage != null;
        newDeathMessage = oldDeathMessage.replace(died.ign, victimName);
        // if there was a killer
        if (killer != null) {
            Participant killedThem = Participant.findParticipantFromPlayer(killer);
            assert killedThem != null;
            killedThem.player.sendTitle("\n", "[X] " + victimName, 0, 60, 40);
            died.player.sendMessage(ChatColor.RED + "You were eliminated by " + killedThem.player.getName() + "!");
            killedThem.player.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + died.player.getName() + "!");
            died.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
            victim.setGameMode(GameMode.SPECTATOR);
            Firework firework = new Firework();
            firework.spawnFireworkWithColor(victim.getLocation(), died.color);

            String killerName = killedThem.teamPrefix + killedThem.chatColor + killedThem.ign + ChatColor.GRAY;
            e.setDeathMessage(newDeathMessage.replace(killedThem.ign, killerName));
            return;
        }

        victim.setGameMode(GameMode.SPECTATOR);
        died.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
        Firework firework = new Firework();
        firework.spawnFireworkWithColor(victim.getLocation(), died.color);

        died.player.sendMessage(ChatColor.RED + "You eliminated yourself!");
        e.setDeathMessage(newDeathMessage);
    }

    public void setTeam(String teamName) {
        team = teamName;

        switch (teamName) {
            case "RedRabbits":
                color = Color.RED;
                chatColor = ChatColor.RED;
                teamPrefix = ChatColor.WHITE+"Ⓡ ";

                break;
            case "YellowYaks":
                color = Color.YELLOW;
                chatColor = ChatColor.YELLOW;
                teamPrefix = ChatColor.WHITE+"Ⓨ ";
                break;
            case "GreenGuardians":
                color = Color.GREEN;
                chatColor = ChatColor.GREEN;
                teamPrefix = ChatColor.WHITE+"Ⓖ ";
                break;
            case "BlueBats":
                color = Color.BLUE;
                chatColor = ChatColor.BLUE;
                teamPrefix = ChatColor.WHITE+"Ⓑ ";
                break;
            case "PurplePandas":
                color = Color.PURPLE;
                chatColor = ChatColor.DARK_PURPLE;
                teamPrefix = ChatColor.WHITE+"Ⓤ ";
                break;
            case "PinkParrots":
                color = Color.fromRGB(255, 0, 164);
                chatColor = ChatColor.LIGHT_PURPLE;
                teamPrefix = ChatColor.WHITE+"Ⓟ ";
                break;
        }
    }

    public static Participant findParticipantFromPlayer(Player player) {
        for (Participant p : participantsOnATeam) {
            if (p.ign.equals(player.getName())) return p;
        }
        Bukkit.broadcastMessage("That player is not on a team!");
        return null;
    }
}
