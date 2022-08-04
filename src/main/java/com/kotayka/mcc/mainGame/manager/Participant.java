package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.TGTTOS.managers.Firework;
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
        String victimName = victim.teamPrefix + victim.chatColor + victim.ign;
        String killerName = killer.teamPrefix + killer.chatColor + killer.ign;
        killer.player.sendTitle("\n", "[X] " + victimName, 0, 60, 40);
        victim.player.sendMessage(ChatColor.RED + "You were eliminated by " + killer.ign + "!");
        killer.player.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + victim.ign + "!");

        String oldDeathMessage = e.getDeathMessage();
        String newDeathMessage = "";
        if (e.getDeathMessage().contains(victim.ign)) {
            assert oldDeathMessage != null;
            newDeathMessage = oldDeathMessage.replace(victim.ign, victimName);
        }
        if (e.getDeathMessage().contains(killer.ign)) {
            assert oldDeathMessage != null;
            newDeathMessage = oldDeathMessage.replace(killer.ign, killerName);
        }
        e.setDeathMessage(newDeathMessage);

        Firework firework = new Firework();
        firework.spawnFireworkWithColor(victim.player.getLocation(), victim.color);

        victim.player.setGameMode(GameMode.SPECTATOR);
    }

    public void Die(Player victim, Player killer, PlayerDeathEvent e) {
        Participant died = Participant.findParticipantFromPlayer(victim);
        Participant killedThem = Participant.findParticipantFromPlayer(killer);
        assert died != null;
        assert killedThem != null;
        String victimName = died.teamPrefix + died.chatColor + died.ign;
        String killerName = killedThem.teamPrefix + killedThem.chatColor + killedThem.ign;
        killedThem.player.sendTitle("\n", "[X] " + victimName, 0, 60, 40);
        died.player.sendMessage(ChatColor.RED + "You were eliminated by " + killedThem.player.getName() + "!");
        killedThem.player.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + died.player.getName() + "!");

        String oldDeathMessage = e.getDeathMessage();
        String newDeathMessage = "";
        if (e.getDeathMessage().contains(died.ign)) {
            assert oldDeathMessage != null;
            newDeathMessage = oldDeathMessage.replace(died.ign, victimName);
        }
        if (e.getDeathMessage().contains(killedThem.ign)) {
            assert oldDeathMessage != null;
            newDeathMessage = oldDeathMessage.replace(killedThem.ign, killerName);
        }
        e.setDeathMessage(newDeathMessage);

        Firework firework = new Firework();
        firework.spawnFireworkWithColor(victim.getLocation(), died.color);

        victim.setGameMode(GameMode.SPECTATOR);
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
