package com.kotayka.mcc.Skybattle.listeners;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftFirework;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
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

        if (e.getDamager() instanceof CraftFirework) {
            e.setCancelled(true);
            return;
        }

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
            skybattle.lastDamage.put(e.getDamager(), player);
        }
    }

    // TODO: Arrow knocks into void (or border?) --> Kill AND Creeper Knocks into void --> Kill
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }

        if(e.getEntityType() != EntityType.ARROW && e.getEntityType() != EntityType.SNOWBALL) return;
        if(!(e.getEntity().getShooter() instanceof Player) || !(e.getHitEntity() instanceof Player)) return;
        if (e.getHitEntity() == null) return;

        Player shooter = (Player) e.getEntity().getShooter();
        Player playerGotShot = (Player) e.getHitEntity();

        if (e.getEntityType() == EntityType.SNOWBALL) {
            assert playerGotShot != null;
            Vector snowballVelocity = e.getEntity().getVelocity();
            playerGotShot.damage(0.1);
            playerGotShot.setVelocity(new Vector(snowballVelocity.getX() * 0.1, 0.5, snowballVelocity.getZ() * 0.1));
        }

        if (skybattle.lastDamage.containsKey(playerGotShot)) {
            skybattle.lastDamage.remove(playerGotShot);
            skybattle.lastDamage.put(playerGotShot, shooter);
        }
    }

    @EventHandler
    public void onSplashEvent(PotionSplashEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        if (!(e.getPotion().getShooter() instanceof Player) && !(e.getHitEntity() instanceof Player)) return;

        ThrownPotion potion = e.getPotion();

        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect effect : effects) {
            PotionEffectType potionType = effect.getType();
            if (!potionType.equals(PotionEffectType.HARM)) return;
        }

        Player potionedPlayer = (Player) e.getHitEntity();

        if (skybattle.lastDamage.containsKey(potionedPlayer)) {
            skybattle.lastDamage.remove(potionedPlayer);
            skybattle.lastDamage.put(potionedPlayer, (Player) potion.getShooter());
        }
    }

    // Kill credit for fishing rod
    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        if (!(e.getCaught() instanceof Player)) { return; }

        Player hooked = (Player) e.getCaught();

        if (skybattle.lastDamage.containsKey(hooked)) {
            skybattle.lastDamage.remove(hooked);
            skybattle.lastDamage.put(hooked, e.getPlayer());
        }
    }

    /*
     * Give kill credit to lastDamager
     */
    @EventHandler
    public void playerDie(PlayerDeathEvent e) {
        if (!(skybattle.getState().equals("PLAYING"))) { return; }
        Player player = e.getEntity();
        Participant p = Participant.findParticipantFromPlayer(e.getEntity());
        assert p != null;

        skybattle.playersDeadList.add(player.getUniqueId());

        skybattle.outLivePlayer();

        // DEBUG MODE (ONE PLAYER)
        /*
        if (skybattle.playersDeadList.size() >= 1)
            skybattle.mcc.scoreboardManager.timer = 0;

         */

        // TODO: end game if only one team is left
        skybattle.mcc.scoreboardManager.lastOneStanding(skybattle.mcc.scoreboardManager.players.get(p.player.getUniqueId()), "Skybattle");

        // If player dies to direct combat
        if (p.player.getKiller() != null) {
            Participant killer = Participant.findParticipantFromPlayer(p.player.getKiller());
            assert killer != null;
            p.Die(p, killer, e);
            if (!Participant.checkTeams(p, killer))
                skybattle.kill(killer);
            return;
        }

        // Give kill credit to last player hit
        if (skybattle.lastDamage.containsKey(player)) {
            Player potentialKiller = skybattle.lastDamage.get(player);
            p.Die(player, potentialKiller, e);
            if (potentialKiller != null && !Participant.checkTeams(potentialKiller, player)) {
                skybattle.kill(skybattle.lastDamage.get(player));
            }
            skybattle.lastDamage.remove(player);
        }
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

        // Kill players immediately in void
        // Damage players in border
        Player p = e.getPlayer();
        if (p.getLocation().getY() <= -40 && !(p.getGameMode().equals(GameMode.SPECTATOR))) {
            p.setHealth(1);
            p.teleport(new Location(p.getWorld(), p.getLocation().getX(), -64, p.getLocation().getY()));
        } else if (p.getLocation().getY() >= skybattle.borderHeight) {
            p.damage(1);
        }
    }
}
