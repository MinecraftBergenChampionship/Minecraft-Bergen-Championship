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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class SGListener implements Listener {
    private final SG sg;
    private final Game game;
    private final Players players;
    private final Plugin plugin;

    private final MCC mcc;

    public SGListener(SG sg, Game game, Players players, Plugin plugin, MCC mcc) {
        this.sg = sg;
        this.game = game;
        this.players = players;
        this.plugin = plugin;
        this.mcc = mcc;
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
    public void playerDeath(PlayerDeathEvent event) {
        if (sg.stage.equals("Game")) {
            event.getEntity().spigot().respawn();
            if (event.getEntity() instanceof Player) {
                if (event.getEntity().getKiller() != null && event.getEntity().getKiller() instanceof Player) {
                    for (Participant p : Participant.participantsOnATeam) {
                        if (p.player.getUniqueId() == event.getEntity().getKiller().getUniqueId()) {
                            sg.kill(p);
                            sg.checkIfGameEnds(p);
                        }
                    }
                    sg.playersDead--;
                }
                sg.names.remove(((Player) event.getEntity()).getName());
                sg.playersDeadList.add(event.getEntity().getUniqueId());
                Player victim = (Player) event.getEntity();
                victim.setGameMode(GameMode.SPECTATOR);
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
                sg.PlayerDied((Player) event.getEntity());

                Firework fw = new Firework();
                fw.spawnFireworkWithColor(event.getEntity().getLocation(), Color.RED);
                ((Player) event.getEntity()).setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void PlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.broadcastMessage("Test");
        event.setRespawnLocation(new Location(sg.world, 1,7,1));
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
    public void blockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.STRING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        if (mcc.game.stage.equals("SG")) {
            if (!(event.getBlock().getType() == Material.COBWEB || String.valueOf(event.getBlock().getType()).endsWith("PANE"))) {
                event.setCancelled(true);
            }
        }
    }
}