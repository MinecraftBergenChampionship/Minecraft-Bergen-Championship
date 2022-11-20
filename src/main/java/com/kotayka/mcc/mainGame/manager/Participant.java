package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

public class Participant {
    public int totalCoins;
    public int roundCoins;
    public final MCC mcc;

    public String team = "Spectator";
    public Color color = Color.WHITE;
    public ChatColor chatColor = ChatColor.GRAY;
    public static List<Participant> participantsOnATeam = new ArrayList<>();
    public String teamPrefix = "";

    // FULL NAME IS THE TEAM NAME
    // WHY DID NOBODY SAY THIS BEFORE LMAO
    public String teamNameFull = "Spectator";


    public final Player player;
    public String ign;

    // Eventually will probably move to separate class
    // but that requires effort
    public long paintballCooldown = System.currentTimeMillis();
    public boolean isPainted = false;
    public boolean hasTelepick = false;
    public int availablePotions = 3;
    public Player paintedBy;

    public Participant(Player player, MCC mcc) {
        this.player = player;
        this.ign = player.getName();
        this.mcc = mcc;
    }
/*
    public static void announceTeamDeath() {

    }
 */
    public void Die(Participant victim, Participant killer, PlayerDeathEvent e) {
        String victimName = victim.teamPrefix + victim.chatColor + victim.ign + ChatColor.WHITE;
        String oldDeathMessage = e.getDeathMessage();
        String newDeathMessage = "";

        assert oldDeathMessage != null;
        newDeathMessage = oldDeathMessage.replace(victim.ign, victimName);

        if (killer != null) {
            killer.player.sendTitle("\n", "[X] " + victimName, 0, 60, 40);
            victim.player.sendMessage(ChatColor.RED + "You were eliminated by " + killer.ign + "!");
            victim.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
            killer.player.sendMessage(ChatColor.GREEN + "You eliminated " + victim.ign + "!");

            victim.player.setGameMode(GameMode.SPECTATOR);
            MCC.spawnFirework(victim);

            e.setDeathMessage(!(killer.ign.equals(victim.ign))
                    ? newDeathMessage.replace(killer.ign, killer.teamPrefix + killer.chatColor + killer.ign + ChatColor.WHITE)
                    : newDeathMessage);
            return;
        }

        MCC.spawnFirework(victim);
        victim.player.setGameMode(GameMode.SPECTATOR);
        victim.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
        victim.player.sendMessage(ChatColor.RED + "You eliminated yourself!");
        e.setDeathMessage(newDeathMessage);
    }

    public void Die(Player victim, Player killer, PlayerDeathEvent e) {
        Participant died = Participant.findParticipantFromPlayer(victim);
        assert died != null;
        String victimName = died.teamPrefix + died.chatColor + died.ign + ChatColor.WHITE;
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
            killedThem.player.sendMessage(ChatColor.GREEN + "You eliminated " + died.player.getName() + "!");
            died.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
            victim.setGameMode(GameMode.SPECTATOR);
            MCC.spawnFirework(died);

            e.setDeathMessage(!(killedThem.ign.equals(died.ign))
                    ? newDeathMessage.replace(killedThem.ign, killedThem.teamPrefix + killedThem.chatColor + killedThem.ign + ChatColor.WHITE)
                    : newDeathMessage);
            return;
        }

        victim.setGameMode(GameMode.SPECTATOR);
        died.player.sendTitle(ChatColor.RED + "You died!", null, 0, 60, 40);
        MCC.spawnFirework(died);

        died.player.sendMessage(ChatColor.RED + "You eliminated yourself!");
        e.setDeathMessage(newDeathMessage);
    }

    // RETURNS TRUE IF TEAMS ARE THE SAME
    public static boolean checkTeams(Participant one, Participant two) {
        return (one.team).equals(two.team);
    }

    public static boolean checkTeams(Player one, Player two) {
        Participant newOne = findParticipantFromPlayer(one);
        Participant newTwo = findParticipantFromPlayer(two);

        assert newOne != null;
        assert newTwo != null;

        return (newOne.team).equals(newTwo.team);
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
            case "PinkPiglets":
                color = Color.fromRGB(243, 139, 170);
                chatColor = ChatColor.LIGHT_PURPLE;
                teamPrefix = ChatColor.WHITE+"Ⓟ ";
                break;
        }
    }

    // For games that might need colored leather armor
    public ItemStack getColoredLeatherArmor(ItemStack i) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            assert meta != null;
            meta.setColor(color);
            i.setItemMeta(meta);
            return i;
        } catch (ClassCastException e) {
            Bukkit.broadcastMessage("Passed Item Stack was not leather armor!");
            return i;
        }
    }

    public static Participant findParticipantFromPlayer(Player player) {
        for (Participant p : participantsOnATeam) {
            if (p.ign.equals(player.getName())) return p;
        }
        Bukkit.broadcastMessage("That player is not on a team!");
        return null;
    }

    // Paintdown specific
    public void setCooldown(long n) {
        paintballCooldown = n;
    }
    public long getCooldown() { return paintballCooldown; }
    public void setIsPainted(boolean b) { isPainted = b; }
    public boolean getIsPainted() { return isPainted; }
    public void setPaintedBy(Player p) { paintedBy = p; }
    public Player getPaintedBy() { return paintedBy; }
}
