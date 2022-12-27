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

    // Each Participant has a MCCTeam member which contains important
    // information regarding graphics, ie ChatColor, Team Icon, etc.
    public MCCTeam team;

    // This list is to cycle through participants on a team.
    public static List<Participant> participantsOnATeam = new ArrayList<>();

    // Representation of the Player by Minecraft's standard Player class.
    public final Player player;

    // Player name for reference.
    public String ign;

    // Eventually will probably move to separate class
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

    public void Die(Participant victim, Participant killer, PlayerDeathEvent e) {
        String victimName = victim.team.getIcon() + victim.team.getChatColor() + victim.ign + ChatColor.WHITE;
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
                    ? newDeathMessage.replace(killer.ign, killer.team.getIcon() + killer.team.getChatColor() + killer.ign + ChatColor.WHITE)
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
        String victimName = died.team.getIcon() + died.team.getChatColor() + died.ign + ChatColor.WHITE;
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
                    ? newDeathMessage.replace(killedThem.ign, killedThem.team.getIcon() + killedThem.team.getChatColor() + killedThem.ign + ChatColor.WHITE)
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

    public boolean setTeam(String s) {
        // prevent double adding
        if (hasTeam(this)) return false;
        switch (s) {
            case "RedRabbits" -> {
                team = mcc.teams.get(0);
                mcc.teams.get(0).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + team.getIcon() + ChatColor.RED + "Red Rabbits.");
            } case "YellowYaks" -> {
                team = mcc.teams.get(1);
                mcc.teams.get(1).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + ChatColor.YELLOW + "Yellow Yaks.");
            }
            case "GreenGuardians" -> {
                team = mcc.teams.get(2);
                mcc.teams.get(2).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + ChatColor.GREEN + "Green Guardians.");
            }
            case "BlueBats" -> {
                team = mcc.teams.get(3);
                mcc.teams.get(3).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + ChatColor.BLUE + "Blue Bats.");
            }
            case "PurplePandas" -> {
                team = mcc.teams.get(4);
                mcc.teams.get(4).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + ChatColor.DARK_PURPLE + "Purple Pandas.");
            }
            case "PinkPiglets" -> {
                team = mcc.teams.get(5);
                mcc.teams.get(5).getPlayers().add(this);
                Bukkit.broadcastMessage(ChatColor.GOLD + ign + ChatColor.WHITE + " has joined the " + ChatColor.LIGHT_PURPLE + "Pink Piglets.");
            }
        }
        return true;
    }

    /*
     * Returns true if the participant is on a team.
     * Returns false otherwise.
     */
    public boolean hasTeam(Participant p) {
        return p.team != null;
    }

    // THESE ARE MORE FOR PAINTDOWN, MORE ON THIS LATER
    // For games that might need colored leather armor
    public ItemStack getColoredLeatherArmor(ItemStack i) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            assert meta != null;
            meta.setColor(team.getColor());
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
