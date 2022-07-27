package com.kotayka.mcc.Skybattle.listeners;

import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;


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
            p.getWorld().spawn(p.getTargetBlock(null, 5).getLocation().add(0, 1, 0), TNTPrimed.class);
        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            String concrete = e.getBlock().getType().toString();
            e.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(concrete)));
        }
    }

    // Track when players spawn creepers
    @EventHandler
    public void onPlayerSpawnCreeper(PlayerInteractEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        // Add each creeper spawned to a map, use to check kill credit
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.CREEPER_SPAWN_EGG) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            Location spawn = p.getTargetBlock(null, 5).getLocation().add(0, 1, 0);
            skybattle.creepersAndSpawned.put(p.getWorld().spawn(spawn, Creeper.class), p);
        }
    }

    // Player hit by creeper
    @EventHandler
    public void onPlayerDamageByCreeper(EntityDamageByEntityEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Creeper)) return;

        Player player = (Player) e.getEntity();

        // If player died, check who spawned creeper
        if (e.getFinalDamage() >= player.getHealth() && e.getDamager() instanceof Creeper) {
            if (skybattle.creepersAndSpawned.containsKey(e.getDamager())) {
                // todo scoring
                Player killer = skybattle.creepersAndSpawned.get(e.getDamager());
                killer.sendTitle("\n", "[X] " + player.getName(), 0, 60, 40);
                killer.sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + player.getName() + "!");
                player.sendMessage(ChatColor.RED + "You were eliminated by " + killer.getName() + "!");
                skybattle.creepersAndSpawned.remove(e.getDamager());
            }
        }
    }

    // TODO: Arrow knocks into void (or border?) --> Kill AND Creeper Knocks into void --> Kill
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if(e.getEntityType() != EntityType.ARROW) return;
        if(!(e.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) e.getEntity();


    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        // Death messages
        Player p = e.getEntity();
        if (p.getKiller() != null) {
            p.getKiller().sendTitle("\n", "[X] " + p.getName(), 0, 60, 40);
            p.sendMessage(ChatColor.RED + "You were eliminated by " + p.getKiller().getName() + "!");
            p.getKiller().sendMessage("[+0] " + ChatColor.GREEN + "You eliminated " + p.getName() + "!");
        }

        // Death Firework + TP Spectator
        Firework fw = new Firework();
        fw.spawnFirework(p.getLocation());
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(new Location(p.getWorld(), -155, 0, -265));
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
        if (p.getLocation().getY() <= -35 && !(p.getGameMode().equals(GameMode.SPECTATOR))) {
            p.setHealth(0);
        } else if (p.getLocation().getY() >= skybattle.borderHeight) {
            p.damage(1);
        }
    }
}
