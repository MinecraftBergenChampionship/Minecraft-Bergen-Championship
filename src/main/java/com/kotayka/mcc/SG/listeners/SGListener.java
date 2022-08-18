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
/*
    @EventHandler
    public void playerKillEvent(EntityDamageByEntityEvent event) {
        if (sg.stage.equals("Game") && event.getEntity() instanceof Player && event.getDamager() instanceof Player && ((Player) event.getEntity()).getHealth()-event.getDamage() < 1) {
            for (Participant p : Participant.participantsOnATeam) {
                if (p.player.getUniqueId() == event.getDamager().getUniqueId()) {
                    sg.kill(p);
                    sg.checkIfGameEnds(p);
                }
            }
            sg.playersDead--;
            Bukkit.broadcastMessage(mcc.scoreboardManager.teamColors.get(mcc.scoreboardManager.playerTeams.get(mcc.scoreboardManager.players.get(event.getEntity().getUniqueId())).teamName)+event.getEntity().getName()+ChatColor.WHITE+" was killed by "+mcc.scoreboardManager.teamColors.get(mcc.scoreboardManager.playerTeams.get(mcc.scoreboardManager.players.get(event.getDamager().getUniqueId())).teamName));
        }
    } */

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (!sg.stage.equals("Game")) return;

        Participant p = Participant.findParticipantFromPlayer(e.getEntity());
        assert p != null;
        Participant killer = Participant.findParticipantFromPlayer(p.player.getKiller());
        p.Die(e.getEntity(), p.player.getKiller(), e);
        sg.playersDead--;
        sg.outLivePlayer();
        if (killer != null) {
            sg.kill(killer);
            sg.checkIfGameEnds(killer);
        }
        for (Participant p2 : Participant.participantsOnATeam) {
            if (Objects.equals(p2.ign, e.getEntity().getName())) {
                sg.teamsAlive.remove(p2.team);
                if (!sg.teamsAlive.contains(p2.team)) {
                    sg.teamsDead--;
                }
            }
        }
    }
/*
    @EventHandler
    public void EntityDameEvent(EntityDamageEvent event) {
        if (sg.stage.equals("Game")) {
            if (event.getEntity() instanceof Player) {
                if (((Player) event.getEntity()).getHealth()-event.getDamage()<1) {
                    for (ItemStack itemStack : ((Player) event.getEntity()).getInventory()) {
                        if (itemStack != null) {
                            ((Player) event.getEntity()).getWorld().dropItemNaturally(((Player) event.getEntity()).getLocation(), itemStack);
                            ((Player) event.getEntity()).getInventory().removeItem(itemStack);
                        }
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

                    event.setCancelled(true);
                    Firework fw = new Firework();
                    fw.spawnFireworkWithColor(event.getEntity().getLocation(), Color.RED);
                    ((Player) event.getEntity()).setGameMode(GameMode.SPECTATOR);
                    if (!event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                        Bukkit.broadcastMessage(mcc.scoreboardManager.teamColors.get(mcc.scoreboardManager.playerTeams.get(mcc.scoreboardManager.players.get(event.getEntity().getUniqueId())).teamName)+event.getEntity().getName()+ChatColor.WHITE+" was killed by "+ChatColor.GOLD+event.getCause());
                    }
                }
            }
        }
    } */

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