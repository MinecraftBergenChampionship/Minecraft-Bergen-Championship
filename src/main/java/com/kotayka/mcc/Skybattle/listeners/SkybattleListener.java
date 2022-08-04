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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.Objects;

/*
Map of each player and what last damaged them AND map of creeper spawns

onSpawnCreeper --> Put (creeper, spawner)
lastDamaged --> Put(Entity, player)
onDamage:
    if damagedPlayer was damaged by creeper (but they lived), remove THAT CREEPER from creeper map

    REMOVE that player from the lastDamaged, and then PUT the last thing that damaged them instead

on Death:
    if playerDied lastDamage is creeper, get the creeper from the creeperMap
    else, give credit to whatever is on the map
 */

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
        if (!(skybattle.getState().equals("PLAYING")) && !(skybattle.getState().equals("STARTING"))) { return; }

        if (skybattle.getState().equals("STARTING")) {
            e.setCancelled(true);
            return;
        }

        Block b = e.getBlock();
        Player p = e.getPlayer();
        if (e.getBlock().getType().equals(Material.TNT)) {
            b.setType(Material.AIR);
            Location loc = p.getTargetBlock(null, 5).getLocation().add(0, 1, 0);
            skybattle.whoPlacedThatTNT.put(p.getWorld().spawn(loc, TNTPrimed.class), p);
        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            String concrete = e.getBlock().getType().toString();
            e.getPlayer().getInventory().addItem(new ItemStack(Material.getMaterial(concrete)));
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!(skybattle.getState().equals("PLAYING")) && !(skybattle.getState().equals("STARTING"))) { return; }

        if (skybattle.getState().equals("STARTING")) {
            e.setCancelled(true);
            return;
        }

        if (e.getBlock().getType().toString().endsWith("CONCRETE")) {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        }
    }

    // Track when players spawn creepers
    @EventHandler
    public void onPlayerSpawnCreeper(PlayerInteractEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.CREEPER_SPAWN_EGG) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            for(int i = 0; i < p.getInventory().getSize(); i++){
                ItemStack itm = p.getInventory().getItem(i);
                if(itm != null && itm.getType().equals(Material.CREEPER_SPAWN_EGG)) {
                    int amt = itm.getAmount() - 1;
                    itm.setAmount(amt);
                    p.getInventory().setItem(i, amt > 0 ? itm : null);
                    p.updateInventory();
                    break;
                }
            }

            // Add each creeper spawned to a map, use to check kill credit
            Location spawn = p.getTargetBlock(null, 5).getLocation().add(0, 1, 0);
            skybattle.creepersAndSpawned.put(p.getWorld().spawn(spawn, Creeper.class), p);
        }
    }

    // Damage by Entity
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        if (!(e.getEntity() instanceof Player)) return;

        Bukkit.broadcastMessage("Before Death: " + skybattle.lastDamage);
        Bukkit.broadcastMessage("Before Death (C): " + skybattle.creepersAndSpawned);
        Bukkit.broadcastMessage("e.getDamager() == " + e.getDamager());
        Bukkit.broadcastMessage("skybattle.creepersAndSpawned.get(e.getDamager()) == " + skybattle.creepersAndSpawned.get(e.getDamager()));

        Player player = (Player) e.getEntity();

        // Place appropriate damager in map
        /*
         * If creeper hurt player, remove that creeper from creeper map, put spawner on last damaged map
         */
        if (skybattle.creepersAndSpawned.containsKey(e.getDamager())) {
            skybattle.lastDamage.put(player, skybattle.creepersAndSpawned.get(e.getDamager()));
            skybattle.creepersAndSpawned.remove(e.getDamager());
            return;
        }

        /*
         * If TNT hurt player, remove from TNT map, put spawner on last damaged map
         */
        if (skybattle.whoPlacedThatTNT.containsKey(e.getDamager())) {
            skybattle.lastDamage.put(player, skybattle.whoPlacedThatTNT.get(e.getDamager()));
            skybattle.whoPlacedThatTNT.remove(e.getDamager());
            return;
        }

        // If last damager was not a projectile
        if (skybattle.lastDamage.containsValue(player) && (!(e.getDamager() instanceof Arrow) && !(e.getDamager() instanceof Snowball))) {
            skybattle.lastDamage.remove(player);
            skybattle.lastDamage.put((Player) e.getDamager(), player);
        }
    }

    // TODO: Arrow knocks into void (or border?) --> Kill AND Creeper Knocks into void --> Kill
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if(e.getEntityType() != EntityType.ARROW && e.getEntityType() != EntityType.SNOWBALL) return;
        if(!(e.getEntity().getShooter() instanceof Player) && !(e.getHitEntity() instanceof Player)) return;

        Player shooter = (Player) e.getEntity().getShooter();
        Player playerGotShot = (Player) e.getHitEntity();

        if (e.getEntityType() == EntityType.SNOWBALL) {
            assert playerGotShot != null;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable) new Runnable() {
                @Override
                public void run() {
                    playerGotShot.damage(0.1);
                    final Vector plrV = playerGotShot.getVelocity();
                    final Vector velocity = new Vector(plrV.getX() * 4, plrV.getY() * 5, plrV.getZ() * 3);
                    playerGotShot.setVelocity(velocity);
                }
            }, 0L);
        }

        if (skybattle.lastDamage.containsKey(playerGotShot)) {
            skybattle.lastDamage.remove(playerGotShot);
            skybattle.lastDamage.put(playerGotShot, shooter);
        }
    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        Participant p = new Participant(e.getEntity());
        Player player = e.getEntity();

        // If player dies to direct combat
        if (p.player.getKiller() != null) {
            Participant killer = Participant.findParticipantFromPlayer(p.player.getKiller());
            assert killer != null;
            p.Die(p, killer, e);
            return;
        }

        // Give kill credit to last player hit
        if (skybattle.lastDamage.containsKey(player)) {
            p.Die(player, skybattle.lastDamage.get(player), e);
            skybattle.lastDamage.remove(player);
        }

        Bukkit.broadcastMessage("After Death: " + skybattle.lastDamage);
        Bukkit.broadcastMessage("After Death (C): " + skybattle.creepersAndSpawned);

        //skybattle.playersAlive--;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!(e.getPlayer().getWorld().equals(skybattle.world))) return;

        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.setRespawnLocation(skybattle.getCenter());
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        if (!(e.getPlayer().getWorld().equals(skybattle.world))) return;

        // Kill players immediately on void
        // Damage players in border
        Player p = e.getPlayer();
        if (p.getLocation().getY() <= -60 && !(p.getGameMode().equals(GameMode.SPECTATOR))) {
            p.setHealth(1);
        } else if (p.getLocation().getY() >= skybattle.borderHeight) {
            p.damage(1);
        }
    }
}
