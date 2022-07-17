package com.kotayka.mcc.Skybattle.listeners;

import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;


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
     * I have no idea if these work
     */
    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (!(skybattle.enabled())) return;

        if (e.getBlock().getType().equals(Material.TNT)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            p.getWorld().spawn(p.getTargetBlock(null, 5).getLocation().add(0, 1, 0), TNTPrimed.class);
        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            String concrete = e.getBlock().getType().toString();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    e.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(concrete)));
                }
            }, 20);
        }
    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {

        Player p = e.getEntity();
        if (p.getKiller() != null) {
            p.getKiller().sendTitle("[X] " + p, null, 0, 60, 40);
        }
        Firework fw = new Firework();
        fw.spawnFirework(p.getLocation());
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(new Location(skybattle.world, -155, -7, -265));
        /* TODO: Set death message + scoring */
    }
}
