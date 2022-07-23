package com.kotayka.mcc.Skybattle.listeners;

import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class SkybattleListener implements Listener {
    public final Skybattle skybattle;
    public final Plugin plugin;

    public SkybattleListener(Skybattle skybattle, Plugin plugin) {
        this.skybattle = skybattle;
        this.plugin = plugin;
    }

    /*
     * Auto Prime TNT
     * Infinite Blocks
     */
    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if (e.getBlock().getType().equals(Material.TNT)) {
            Block b = e.getBlock();
            b.setType(Material.AIR);
            Player p = e.getPlayer();
            p.getWorld().spawn(p.getTargetBlock(null, 5).getLocation().add(0, 0.5, 0), TNTPrimed.class);
        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            String concrete = e.getBlock().getType().toString();
            e.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(concrete)));
        }
    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        Player p = e.getEntity();
        if (p.getKiller() != null) {
            p.getKiller().sendTitle("[X] " + p.getName(), null, 0, 60, 40);
            p.sendMessage(ChatColor.RED + "You were eliminated by " + p.getKiller() + "!");
            p.getKiller().sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + p + "!");
        }
        Firework fw = new Firework();
        fw.spawnFirework(p.getLocation());
        p.setGameMode(GameMode.SPECTATOR);
        //p.teleport(new Location(skybattle.world, -155, -7, -265));
        /* TODO: Set death message + scoring */
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        if (!(skybattle.getState().equals("STARTING") || skybattle.getState().equals("PLAYING"))) { return; }

        // Prevent moving during countdown
        if (skybattle.getState().equals("STARTING")) {
            e.setTo(e.getFrom());
            e.setCancelled(true);
            return;
        }

        // Kill players immediately on void
        // Damage players in border
        Player p = e.getPlayer();
        if (p.getLocation().getY() <= -35) {
            p.setHealth(0);
        } else if (p.getLocation().getY() >= skybattle.borderHeight) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1, 1, true, true));
        }
    }
}
