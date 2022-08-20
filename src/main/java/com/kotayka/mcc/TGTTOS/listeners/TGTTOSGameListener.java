package com.kotayka.mcc.TGTTOS.listeners;

import com.kotayka.mcc.Scoreboards.ScoreboardManager;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import org.bukkit.*;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class TGTTOSGameListener implements Listener {
    public final TGTTOS tgttos;
    public final Plugin plugin;
    public TGTTOSGameListener(TGTTOS tgttos, Plugin plugin) {
        this.tgttos = tgttos;
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerMove(PlayerMoveEvent event) {

        if (tgttos.enabled()) {
            if (event.getPlayer().getLocation().getY() <= -35) {
                Location playerLoc = (Location) tgttos.spawnPoints.get(tgttos.gameOrder[tgttos.roundNum]);
                Player p = (Player) event.getPlayer(); 
                Participant didntgettotheotherside = Participant.findParticipantFromPlayer(p);
                Bukkit.broadcastMessage(didntgettotheotherside.teamPrefix + didntgettotheotherside.chatColor + p.getDisplayName()+ChatColor.GRAY+" couldn't get to the other side.");    
                event.getPlayer().teleport(playerLoc);
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (tgttos.enabled()) {
            if (event.getBlock().getType().toString().endsWith("WOOL")) {
                ItemStack i = new ItemStack(event.getItemInHand());
                i.setAmount(1);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        event.getPlayer().getInventory().addItem(i);
                    }
                }, 20);
            }
        }
    }

    @EventHandler
    public void chickenHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Chicken && event.getDamager() instanceof Player && tgttos.mcc.game.stage.equals("TGTTOS")) {
            ScoreboardManager scoreboardManager = tgttos.mcc.scoreboardManager;
            scoreboardManager.addScore(scoreboardManager.players.get(((Player) event.getDamager()).getUniqueId()), 1);
            scoreboardManager.placementPoints(scoreboardManager.players.get(((Player) event.getDamager()).getUniqueId()), 1, tgttos.playerAmount);
            scoreboardManager.teamFinish(scoreboardManager.players.get(((Player) event.getDamager()).getUniqueId()), 5);
            tgttos.playerAmount++;
            event.getEntity().remove();
            String place;
            switch (tgttos.playerAmount) {
                case 1:
                    place = "1st";
                    break;
                case 2:
                    place = "2nd";
                    break;
                case 3:
                    place="3rd";
                    break;
                default:
                    place=String.valueOf(tgttos.playerAmount)+"th";
                    break;
            }
            Player p = (Player) event.getDamager(); 
            Participant chickenpuncher = Participant.findParticipantFromPlayer(p);
            Bukkit.broadcastMessage(chickenpuncher.teamPrefix + chickenpuncher.chatColor + "" +event.getDamager().getName()+ChatColor.GRAY+ " finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
            event.getDamager().sendMessage(ChatColor.WHITE+"[+"+String.valueOf(tgttos.playerPoints)+"] "+ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GREEN+" place!");
            tgttos.playerPoints--;
            if (tgttos.playerAmount >= scoreboardManager.playerList.size()) {
                tgttos.nextRound();
            }
            if (event.getDamager() instanceof Player) {
                ((Player) event.getDamager()).setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void hitChicken(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Chicken  && tgttos.mcc.game.stage.equals("TGTTOS")) {
            ScoreboardManager scoreboardManager = tgttos.mcc.scoreboardManager;
            scoreboardManager.addScore(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 1);
            scoreboardManager.placementPoints(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 1, tgttos.playerAmount);
            scoreboardManager.teamFinish(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 5);
            tgttos.playerAmount++;
            event.getRightClicked().remove();
            String place;
            switch (tgttos.playerAmount) {
                case 1:
                    place = "1st";
                    break;
                case 2:
                    place = "2nd";
                    break;
                case 3:
                    place="3rd";
                    break;
                default:
                    place=String.valueOf(tgttos.playerAmount)+"th";
                    break;
            }
            Player p = (Player) event.getPlayer(); 
            Participant chickenclicker = Participant.findParticipantFromPlayer(p);
            Bukkit.broadcastMessage(chickenclicker.teamPrefix + chickenclicker.chatColor + "" + event.getPlayer().getName()+ChatColor.GRAY+ " finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
            event.getPlayer().sendMessage(ChatColor.WHITE+"[+"+String.valueOf(tgttos.playerPoints)+"] "+ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GREEN+" place!");
            tgttos.playerPoints--;
            if (tgttos.playerAmount >= scoreboardManager.playerList.size()) {
                tgttos.nextRound();
            }
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void blocKBreak(BlockBreakEvent event) {
        if (tgttos.enabled()) {
            if (!(event.getBlock().getType().toString().endsWith("WOOL"))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void boatExit(VehicleExitEvent event) {
        if (event.getVehicle() instanceof Boat) {
            Boat boat = (Boat) event.getVehicle();
            boat.remove();

            ItemStack boatItem = new ItemStack(Material.OAK_BOAT);
            Player p = (Player) event.getExited();
            p.getInventory().addItem(boatItem);
        }
    }
}
