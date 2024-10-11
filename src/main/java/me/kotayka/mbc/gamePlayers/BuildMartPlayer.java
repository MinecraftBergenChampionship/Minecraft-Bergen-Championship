package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BuildMartPlayer extends GamePlayer {

    private final BuildMart bsabm;
    private BuildMartTeam team = null;

    public BuildMartPlayer(Participant p, BuildMart buildMart) {
        super(p);
        this.bsabm = buildMart;

        switch (p.getTeam().getTeamName()) {
            case "RedRabbits", "RedRavens", "RedReindeer" -> team = buildMart.red;
            case "YellowYaks", "MustardMummies", "YellowYetis" -> team = buildMart.yellow;
            case "GreenGuardians", "GreenGoblins", "EmeraldElves" -> team = buildMart.green;
            case "BlueBats", "BlueBanshees", "SapphireSantas" -> team = buildMart.blue;
            case "PurplePandas", "VioletVampires", "PurplePenguins" -> team = buildMart.purple;
            case "PinkPiglets", "PinkParrots", "PinkPresents", "FuchsiaFrankensteins" -> team = buildMart.pink;
        }
    }

    public void respawn() {
        Player p = getParticipant().getPlayer();
        p.getInventory().clear();
        p.teleport(team.getSPAWN());
        p.sendMessage(ChatColor.RED + "You fell off and lost your items...");

        Bukkit.broadcastMessage(getParticipant().getFormattedName() + ChatColor.GRAY + " shopped til they dropped.");

        ItemStack[] items = BuildMart.getItemsForBuildMart();

        for (ItemStack i : items) {
            if (i.getType().equals(Material.ELYTRA)) {
                p.getInventory().setChestplate(i);
            } else {
                p.getInventory().addItem(i);
            }
        }
    }

    public void spawn() {
        Player p = getParticipant().getPlayer();
        p.getInventory().clear();
        if (team == null) {
            Bukkit.broadcastMessage(p.getName());
        }
        p.teleport(team.getSPAWN());

        ItemStack[] items = BuildMart.getItemsForBuildMart();

        for (ItemStack i : items) {
            if (i.getType().equals(Material.ELYTRA)) {
                p.getInventory().setChestplate(i);
            } else {
                p.getInventory().addItem(i);
            }
        }

    }
}
