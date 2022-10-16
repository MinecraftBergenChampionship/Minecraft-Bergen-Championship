package com.kotayka.mcc.Dodgebolt.Listener;

import com.kotayka.mcc.Dodgebolt.Dodgebolt;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class DodgeboltListener implements Listener {
    private final Dodgebolt dodgebolt;
    private final MCC mcc;

    public DodgeboltListener(Dodgebolt dodgebolt, MCC mcc) {
        this.dodgebolt = dodgebolt;
        this.mcc = mcc;
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent e) {
        if (mcc.game.stage.equals("Dodgebolt")) {
            if (dodgebolt.stage.equals("Starting")) {
                if (!(e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())) {
                    e.setCancelled(true);
                }
            }
            if (dodgebolt.team1Names.contains(e.getPlayer().getName()) && !dodgebolt.deadPlayersNames.contains(e.getPlayer().getName())) {
                if (e.getPlayer().getLocation().getBlockX() < 0) {
                    playerDied(e.getPlayer());
                }
            }
            else if (dodgebolt.team2Names.contains(e.getPlayer().getName()) && !dodgebolt.deadPlayersNames.contains(e.getPlayer().getName())) {
                if (e.getPlayer().getLocation().getBlockX() > 0) {
                    playerDied(e.getPlayer());
                }
            }
            if (dodgebolt.playerNames.contains(e.getPlayer().getName()) && !dodgebolt.deadPlayersNames.contains(e.getPlayer().getName())) {
                if (e.getPlayer().getLocation().getBlockX() == 0) {
                    e.getPlayer().setVelocity(new Vector(0,e.getPlayer().getVelocity().getY(),0));
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20, 255, false, false));
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 10, false, false));
                    e.getPlayer().sendTitle(ChatColor.RED+"GO BACK!!!!!!!", ChatColor.WHITE+"or die :)", 5, 10, 5);
                }
            }
        }
    }

    public void playerDied(Player p) {
        dodgebolt.deadPlayersNames.add(p.getName());

        for (ItemStack i : p.getInventory()) {
            if (i != null && i.getType().equals(Material.ARROW)) {
                dodgebolt.world.dropItem(p.getLocation(), i);
            }
        }

        if (dodgebolt.team1Names.contains(p.getName())) {
            dodgebolt.team1Remaining--;
            if (dodgebolt.team1Remaining <= 0) {
                dodgebolt.team2Wins++;
                Bukkit.broadcastMessage("Team 2 Wins");
                dodgebolt.roundOver("Team2");
            }
        }
        else if (dodgebolt.team2Names.contains(p.getName())) {
            dodgebolt.team2Remaining--;
            if (dodgebolt.team2Remaining <= 0) {
                dodgebolt.team1Wins++;
                Bukkit.broadcastMessage("Team 1 Wins");
                dodgebolt.roundOver("Team1");
            }
        }

        p.getInventory().clear();
        p.teleport(new Location(dodgebolt.world, 0, 26, 25));
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent event) {
        if (mcc.game.stage.equals("Dodgebolt")) {
            if (event.getItemDrop().getItemStack().getType().equals(Material.BOW)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void ArrowHit(ProjectileHitEvent event) {
        if (event.getEntity().getType().equals(EntityType.ARROW) && event.getHitBlock() == null) {
            if (event.getHitBlock().getLocation().getBlockY() >= 18) {
                if (event.getHitBlock().getLocation().getBlockX() >= 0) {
                    Arrow a1 = (Arrow) dodgebolt.world.spawnEntity(new Location(dodgebolt.world, 6.5, 20, -0.5), EntityType.ARROW);
                    a1.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
                }
                else {
                    Arrow a1 = (Arrow) dodgebolt.world.spawnEntity(new Location(dodgebolt.world, -6.5, 20, -0.5), EntityType.ARROW);
                    a1.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
                }
            }
        }
    }

    @EventHandler
    public void Damage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow) {
            dodgebolt.world.dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.ARROW));
            playerDied((Player) event.getEntity());
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (mcc.game.stage.equals("Dodgebolt")) {
            event.setCancelled(true);
        }
    }
}
