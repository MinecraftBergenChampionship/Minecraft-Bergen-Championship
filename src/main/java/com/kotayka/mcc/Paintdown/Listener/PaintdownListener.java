package com.kotayka.mcc.Paintdown.Listener;

import com.kotayka.mcc.Paintdown.Paintdown;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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

        if (player.getInventory().getItemInMainHand().getType().equals(Material.IRON_HORSE_ARMOR)) {
            Snowball projectile = player.launchProjectile(Snowball.class);
            projectile.setShooter(player); // Not sure if this is necessary
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 2);
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
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

        hitPlayer.damage(5);

        // Cool iDrg math: snowball send in correct direction
        // Bug: pulls players when they are facing perfectly straight in Z direction against one another
        double shooterX = shooter.getLocation().getX();
        double hitPlayerX = hitPlayer.getLocation().getX();
        double shooterZ = shooter.getLocation().getZ();
        double hitPlayerZ = hitPlayer.getLocation().getZ();
        double angle = Math.atan((hitPlayerZ - shooterZ)/(hitPlayerX - shooterX));

        Vector velocity;
        if (hitPlayerX > shooterX) {
            velocity = new Vector(0.15 * Math.cos(angle), 0.05, 0.15 * Math.sin(angle));
        } else {
            velocity = new Vector(0.15 * Math.cos(angle + Math.PI), 0.05, 0.15 * Math.sin(angle+Math.PI));
        }

        hitPlayer.setVelocity(velocity);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!(e.getPlayer().getWorld().equals(paintdown.world))) return;

        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 2, false, false));
        e.setRespawnLocation(paintdown.getCenter());
    }
}
