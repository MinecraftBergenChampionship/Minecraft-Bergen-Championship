package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Dodgebolt;
import me.kotayka.mbc.Participant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DodgeboltPlayer extends GamePlayer {
    public final boolean FIRST;
    public boolean outOfBounds = false;
    public boolean dead = false;
    public Participant shotBy = null;
    public boolean fell = false;

    public DodgeboltPlayer(Participant p, boolean first) {
        super(p);
        this.FIRST = first;
    }

    public void removeBow() {
        getPlayer().sendTitle(" ", ChatColor.RED.toString()+ChatColor.BOLD+"You are too close!", 20, 20, 20);
        getPlayer().sendMessage(ChatColor.RED.toString()+ChatColor.BOLD+"Your bow has been taken away!");
        outOfBounds = true;
        for (ItemStack i : getPlayer().getInventory()) {
            if (i != null && i.getType().equals(Material.BOW)) {
                getPlayer().getInventory().remove(i);
            }
        }
    }

    public void giveBow() {
        getPlayer().sendMessage(ChatColor.GREEN.toString()+ChatColor.BOLD+"Your bow has been given back.");
        getPlayer().getInventory().addItem(Dodgebolt.BOW);
        outOfBounds = false;
    }
}
