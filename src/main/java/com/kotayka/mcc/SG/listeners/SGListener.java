package com.kotayka.mcc.SG.listeners;

import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Game;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class SGListener implements Listener {
    private final SG sg;
    private final Game game;
    private final Players players;
    private final Plugin plugin;

    public SGListener(SG sg, Game game, Players players, Plugin plugin) {
        this.sg = sg;
        this.game = game;
        this.players = players;
        this.plugin = plugin;
    }

    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent e) {
        if (sg.stage.equals("Starting")) {
            if (sg.playerSpawnTeleported.contains(e.getPlayer().getUniqueId())) {
                if (!(e.getTo().getBlockX() == e.getFrom().getBlockX() && e.getTo().getBlockY() == e.getFrom().getBlockY() && e.getTo().getBlockZ() == e.getFrom().getBlockZ())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void playerKillEvent(PlayerDeathEvent event) {
        if (sg.stage.equals("Game")) {
            sg.playersDeadList.add(event.getEntity().getUniqueId());
            Player victim = event.getEntity();
            victim.setGameMode(GameMode.SPECTATOR);
            final Location deathLoc = victim.getLocation();
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    victim.teleport(deathLoc);
                }
            }, 30);
            for (Participant p : Participant.participantsOnATeam) {
                if (event.getEntity().getKiller() != null && p.player.getUniqueId() == event.getEntity().getKiller().getUniqueId()) {
                    sg.kill(p);
                    p.Die(p.player, event.getEntity().getKiller(), event);
                    sg.checkIfGameEnds(p);
                }
            }

            victim.teleport(new Location(event.getEntity().getWorld(), 0, 6, 0));
            sg.playersDead--;
            for (Participant p : players.participants) {
                if (Objects.equals(p.ign, event.getEntity().getName())) {
                    sg.teamsAlive.remove(p.team);
                    if (!sg.teamsAlive.contains(p.team)) {
                        sg.teamsDead--;
                    }
                }
            }
            sg.outLivePlayer();
        }
    }

    public boolean checkIfEmpty(Inventory inv) {
        for(ItemStack it : inv.getContents())
        {
            if(it != null) return false;
        }
        return true;
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
            if (checkIfEmpty(event.getInventory())) {
                for (ShulkerBox box : sg.boxes) {
                    if (checkIfEmpty(box.getInventory())) {
                        World world;
                        if (Bukkit.getWorld("Survival_Games") == null) {
                            world = Bukkit.getWorld("world");
                        }
                        else {
                            world = Bukkit.getWorld("Survival_Games");
                        }
                        Block supplyDrop = world.getBlockAt(box.getLocation());
                        supplyDrop.setType(Material.CHEST);
                    }
                }
            }
        }
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        if (sg.stage.equals("Starting")) {
            if (!(event.getBlock().getType() == Material.COBWEB || String.valueOf(event.getBlock().getType()).endsWith("PANE"))) {
                event.setCancelled(true);
            }
        }
    }
}