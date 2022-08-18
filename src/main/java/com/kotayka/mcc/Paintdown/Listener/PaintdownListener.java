package com.kotayka.mcc.Paintdown.Listener;

import com.kotayka.mcc.Paintdown.Paintdown;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PaintdownListener implements Listener {
    public final Paintdown paintdown;
    public final Plugin plugin;

    public PaintdownListener(Paintdown paintdown, Plugin plugin) {
        this.paintdown = paintdown;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK) && !(e.getAction() == Action.RIGHT_CLICK_AIR)) return;

        Player player = e.getPlayer();
        Participant p = Participant.findParticipantFromPlayer(player);
        assert p != null;

        // Cooldown
        if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_HORSE_ARMOR)) {
            long timeLeft = System.currentTimeMillis() - p.getCooldown();
            if (timeLeft >= 700) {
                Snowball projectile = player.launchProjectile(Snowball.class);
                projectile.setVelocity(new Vector(projectile.getVelocity().getX() * 1.25, projectile.getVelocity().getY() * 1.25, projectile.getVelocity().getZ() * 1.25));
                projectile.setShooter(player); // Not sure if this is necessary
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 2);
                p.setCooldown(System.currentTimeMillis());
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getEntity() instanceof Snowball)) return;
        if (!(e.getHitEntity() instanceof Player)) return;

        Player hitPlayer = (Player) e.getHitEntity();
        assert e.getEntity().getShooter() instanceof Player;
        Player shooter = (Player) e.getEntity().getShooter();

        if (Objects.requireNonNull(Participant.findParticipantFromPlayer(hitPlayer)).team.equals(Objects.requireNonNull(Participant.findParticipantFromPlayer(shooter)).team)) return;

        Vector snowballVelocity = e.getEntity().getVelocity();
        hitPlayer.damage(10);
        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 5);
        hitPlayer.setVelocity(new Vector(snowballVelocity.getX() * 0.1, 0.5, snowballVelocity.getZ() * 0.1));
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getEntity() instanceof Player) && !(e.getDamager() instanceof Snowball)) return;

        Player player = (Player) e.getEntity();
        Participant participant = Participant.findParticipantFromPlayer(player);
        assert participant != null;

        // If they died
        if((player.getHealth()-e.getDamage()) <= 0) {
            participant.setIsPainted(true);

            // Check if whole team died
            int deadTeammates = 0;
            for (List<Participant> l : paintdown.mcc.teamList) {
                if (l.contains(participant)) {
                    for (int i = 0; i < l.size(); i++) {
                        if (l.get(i).getIsPainted()) {
                            deadTeammates++;
                        }
                        if (deadTeammates == l.size()) {
                            Bukkit.broadcastMessage(participant.chatColor + "ALL OF " + participant.team + " HAS BEEN PAINTED!");
                            // kill all team
                            return;
                        }
                    }
                }
            }
            // Died but teammates are alive
            e.setCancelled(true);
            player.setHealth(20);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        Collection<LivingEntity> affected = e.getAffectedEntities();

        for (LivingEntity ent : affected) {
            if (ent instanceof Player) {
                Participant p = Participant.findParticipantFromPlayer((Player) ent);
                Participant potionThrower = Participant.findParticipantFromPlayer((Player) e.getPotion().getShooter());
                assert p != null;
                assert potionThrower != null;

                if (p.getIsPainted()) p.setIsPainted(false);
                else p.player.setHealth(20);
            }
        }
    }


    // Prevent moving when painted
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!(paintdown.getState().equals("PLAYING"))) { return; }
        // Right now, if a player joins during a game and moves, it will spam "This player is not on a team" since they are not on a team
        if (!(Objects.requireNonNull(Participant.findParticipantFromPlayer(e.getPlayer())).getIsPainted())) return;

        e.setCancelled(true);
        e.setTo(e.getPlayer().getLocation());
    }

    // Temporary
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!(e.getPlayer().getWorld().equals(paintdown.world))) return;

        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 2, false, false));
        e.setRespawnLocation(paintdown.getCenter());
    }
}
