package com.kotayka.mcc.TGTTOS.listeners;

import com.kotayka.mcc.TGTTOS.TGTTOS;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class playerMove implements Listener {
    public final TGTTOS tgttos;
    public final Plugin plugin;

    public playerMove(TGTTOS tgttos, Plugin plugin) {
        this.tgttos = tgttos;
        this.plugin = plugin;
    }

    @EventHandler
    public void PlayerMove(PlayerMoveEvent event) {

        if (tgttos.enabled()) {
            if (event.getPlayer().getLocation().getY() <= -35) {
                Location playerLoc = (Location) tgttos.spawnPoints.get(tgttos.gameOrder[tgttos.roundNum]);
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
    public void blocKBreak(BlockBreakEvent event) {
        if (tgttos.enabled()) {
            if (!(event.getBlock().getType().toString().endsWith("WOOL"))) {
                event.setCancelled(true);
            }
        }
    }
}
