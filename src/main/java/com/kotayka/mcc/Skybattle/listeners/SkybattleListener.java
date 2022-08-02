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

import java.util.Objects;


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

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if (e.getBlock().getType().toString().endsWith("CONCRETE")) {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
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
            // testing
            Location spawn = p.getTargetBlock(null, 5).getLocation().add(0, 1, 0);
            skybattle.creepersAndSpawned.put(p.getWorld().spawn(spawn, Creeper.class), p);
        }
    }

    // Player hit by creeper
    @EventHandler
    public void onPlayerDamageByCreeper(EntityDamageByEntityEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if (!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (!(e.getDamager() instanceof Creeper)) {
            // Remove player from hashmaps if didn't die (only contain when last damaged)
            skybattle.creepersAndSpawned.remove(player);
            skybattle.playersShot.remove(player);
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
            playerGotShot.damage(1);
        }

        skybattle.playersShot.put(e.getEntity(), shooter);
    }

    /*
     * Spawn firework on death
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        // Death messages
        Participant p = new Participant(e.getEntity());
        Player player = e.getEntity();
        if (p.player.getKiller() != null) {
            Participant killer = Participant.findParticipantFromPlayer(p.player.getKiller());
            assert killer != null;
            p.Die(p, killer, e);
            e.setDeathMessage(p.teamPrefix + p.chatColor + p.ign + " was slain by " + killer.teamPrefix + killer.chatColor + killer.ign);
        }

        if (skybattle.creepersAndSpawned.containsKey(Objects.requireNonNull(player.getLastDamageCause()).getEntity())) {
            p.Die(player, skybattle.creepersAndSpawned.get(player.getLastDamageCause().getEntity()), e);
            skybattle.creepersAndSpawned.remove(player);
        }
        if (skybattle.playersShot.containsKey(player.getLastDamageCause().getEntity())) {
            p.Die(player, skybattle.playersShot.get(player.getLastDamageCause().getEntity()), e);
            skybattle.playersShot.remove(player);
        }

        // Death Firework + TP Spectator
        Firework firework = new Firework();
        firework.spawnFireworkWithColor(p.player.getLocation(), p.color);

        p.player.setGameMode(GameMode.SPECTATOR);

        //skybattle.playersAlive--;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(skybattle.getCenter());
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
        Participant participant = new Participant(p);
        if (p.getLocation().getY() <= -55 && !(p.getGameMode().equals(GameMode.SPECTATOR))) {
            p.setHealth(0);
        } else if (p.getLocation().getY() >= skybattle.borderHeight) {
            p.damage(1);
        }
    }
}
