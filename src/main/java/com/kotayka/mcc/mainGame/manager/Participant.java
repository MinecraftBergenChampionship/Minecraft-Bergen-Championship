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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Participant {
    public int totalCoins;
    public int roundCoins;
    public final MCC mcc;

    public String team = "Spectator";
    public Color color = Color.WHITE;
    public ChatColor chatColor = ChatColor.GRAY;
    public static List<Participant> participantsOnATeam = new ArrayList<>();
    public String teamPrefix = "";
    public String fullName;

    // Used mostly to make indexing easier (and to prevent nesting for loops wherever necessary)
    // Used for mcc.teamList.get(teamIndex); (skip looping to find whether team matches)
    // Red = 0, Yellow = 1, Green = 2, Blue = 3, Purple = 4, Pink = 5;
    public int teamIndex = -1;

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
        if (newOne.team == null || newTwo.team == null) {
            Bukkit.broadcastMessage("Either " + newOne.ign + " or " + newTwo.ign + " is not on a team! I sure hope this message never gets sent!");
        }
        return (newOne.team).equals(newTwo.team);
    }

    public void setTeam(String teamName) {
        team = teamName;

        switch (teamName) {
            case "RedRabbits":
                color = Color.RED;
                chatColor = ChatColor.RED;
                teamPrefix = ChatColor.WHITE+"Ⓡ ";
                teamIndex = 0;
                mcc.teams.get("Red Rabbits").add(this);
                break;
            case "YellowYaks":
                color = Color.YELLOW;
                chatColor = ChatColor.YELLOW;
                teamPrefix = ChatColor.WHITE+"Ⓨ ";
                teamIndex = 1;
                mcc.teams.get("Yellow Yaks").add(this);
                break;
            case "GreenGuardians":
                color = Color.GREEN;
                chatColor = ChatColor.GREEN;
                teamPrefix = ChatColor.WHITE+"Ⓖ ";
                teamIndex = 2;
                mcc.teams.get("Green Guardians").add(this);
                break;
            case "BlueBats":
                color = Color.BLUE;
                chatColor = ChatColor.BLUE;
                teamPrefix = ChatColor.WHITE+"Ⓑ ";
                teamIndex = 3;
                mcc.teams.get("Blue Bats").add(this);
                break;
            case "PurplePandas":
                color = Color.PURPLE;
                chatColor = ChatColor.DARK_PURPLE;
                teamPrefix = ChatColor.WHITE+"Ⓤ ";
                teamIndex = 4;
                mcc.teams.get("Purple Pandas").add(this);
                break;
            case "PinkPiglets":
                color = Color.fromRGB(243, 139, 170);
                chatColor = ChatColor.LIGHT_PURPLE;
                teamPrefix = ChatColor.WHITE+"Ⓟ ";
                teamIndex = 5;
                mcc.teams.get("Pink Piglets").add(this);
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

    // not necessarily paintdown specific but for now yes
    public static String indexToName(int index) {
        switch(index) {
            case 1:
                return "Red Rabbits";
            case 2:
                return "Yellow Yaks";
            case 3:
                return "Green Guardians";
            case 4:
                return "Blue Bats";
            case 5:
                return "Purple Pandas";
            case 6:
                return "Pink Parrots";
            default:
                return "Invalid Index!";
        }
    }
}
