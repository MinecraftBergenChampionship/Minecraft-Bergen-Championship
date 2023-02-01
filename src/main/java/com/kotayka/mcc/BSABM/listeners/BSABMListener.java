package com.kotayka.mcc.BSABM.listeners;

import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.BSABM.managers.BlockBreakManager;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Game;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class BSABMListener implements Listener {
    private final BSABM bsabm;
    private final Game game;
    private final Players players;
    private final BlockBreakManager blockBreakManager = new BlockBreakManager();
    private final MCC mcc;

    private final Plugin plugin;

    Map<UUID, Material> hotbarSelector = new HashMap<UUID, Material>();
    public int[] teamPortalLoc = {-107, -68, -29, 10, 50, 88};

    public BSABMListener(BSABM bsabm, Game game, Players players, MCC mcc, Plugin plugin) {
        this.bsabm = bsabm;
        this.game = game;
        this.players = players;
        this.mcc = mcc;
        this.plugin = plugin;
    }

    public StringBuilder createActionBarString (String material) {
        String[] strs = material.split("_");
        StringBuilder finalString = new StringBuilder();
        for (String str : strs) {
            str = str.toLowerCase();
            finalString.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
            finalString.append(" ");
        }
        return finalString;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_WEATHERED_CUT_COPPER) {
            if (game.stage.equals("AceRace")) {
                mcc.aceRace.nextCheckpoint(e.getPlayer());
            }
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_EXPOSED_CUT_COPPER) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_WEATHERED_COPPER) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.OBSERVER) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        }

        // since all the lobby stuff is above, i'm just gonna put this here
        // in the future we could prob move it but...for now
        if (e.getPlayer().getWorld().equals(mcc.spawnWorld)) {
            if (e.getPlayer().getLocation().getY() <= -60 && e.getPlayer().getWorld().equals(mcc.spawnWorld)) {
                e.getPlayer().teleport(mcc.SPAWN);
            }
        }

        if (game.stage.equals("BSABM")) {
            if (e.getPlayer().getLocation().getY() <= -33) {
                bsabm.givePlayerItems(e.getPlayer());
                e.getPlayer().teleport(new Location(bsabm.world, 11, 1, 0));
            }
            if (e.getPlayer().getLocation().getBlock().getType().equals(Material.NETHER_PORTAL)) {
                netherPortalTeleporter(e.getPlayer(), e.getPlayer().getLocation());
            }
            if (Math.sqrt(Math.pow((e.getPlayer().getLocation().getX()+3),2)+(Math.pow(e.getPlayer().getLocation().getZ(),2))) <= 8 && e.getPlayer().getLocation().getY() < 10) {
                e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1000, e.getPlayer().getVelocity().getZ()));
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 3, e.getPlayer().getVelocity().getZ()));
                    }
                }, 17);
            }
//            if (Math.sqrt(Math.pow((e.getPlayer().getLocation().getX()+3),2)+(Math.pow(e.getPlayer().getLocation().getZ(),2))) <= 8 && e.getPlayer().getLocation().getY() < 120) {
//                e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
//            }
            if (hotbarSelector.containsKey(e.getPlayer().getUniqueId())) {
                if (e.getPlayer().getTargetBlock(null, 5).getType() != hotbarSelector.get(e.getPlayer().getUniqueId())) {
                    hotbarSelector.put(e.getPlayer().getUniqueId(), e.getPlayer().getTargetBlock(null, 5).getType());
                    if (e.getPlayer().getTargetBlock(null, 5).getType() != Material.AIR) {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(e.getPlayer().getUniqueId()))))));
                    }
                    else {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    }
                }
            }
            else {
                hotbarSelector.put(e.getPlayer().getUniqueId(), e.getPlayer().getTargetBlock(null, 5).getType());
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(e.getPlayer().getUniqueId()))))));
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (game.stage.equals("BSABM")) {
            event.setCancelled(true);
            if (checkZ((int) event.getBlock().getLocation().getZ()) && (event.getBlock().getLocation().getY() >= 2 && event.getBlock().getLocation().getY() <= 6) && checkX((int) event.getBlock().getLocation().getX())) {
                bsabm.mapUpdate(event.getBlock().getLocation());
                event.setCancelled(false);
            }
        }
    }

    public boolean checkZ(int zcoord) {
        if (zcoord <= 164 && zcoord >= 158) {
            return true;
        }
        if (zcoord <= 153 && zcoord >= 147) {
            return true;
        }
        if (zcoord <= 142 && zcoord >= 136) {
            return true;
        }
        return false;
    }
    public boolean checkX(int xcoord) {
        for (int x : bsabm.teamsCoordsBuilding) {
            if (xcoord >= x && xcoord<=x+6) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        if (game.stage.equals("BSABM")) {
            event.setCancelled(true);
            if (checkZ((int) event.getBlock().getLocation().getZ()) && (event.getBlock().getLocation().getY() >= 2 && event.getBlock().getLocation().getY() <= 6) && checkX((int) event.getBlock().getLocation().getX())) {

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        bsabm.mapUpdate(event.getBlock().getLocation());
                    }
                },5);
                event.setCancelled(false);
            }
            else {
                if (event.getBlock().getType().toString().endsWith("CORAL") || event.getBlock().getType().toString().endsWith("FAN")) {
                    ItemStack itemStack = new ItemStack(event.getBlock().getType());
                    event.getPlayer().getInventory().addItem(itemStack);
                }
                else if (event.getBlock().getType().toString().startsWith("POTTED")) {
                    ItemStack itemStack = new ItemStack(Material.AIR);
                    switch (event.getBlock().getType()) {
                        case POTTED_LILY_OF_THE_VALLEY:
                            itemStack.setType(Material.LILY_OF_THE_VALLEY);
                            break;
                        case POTTED_ALLIUM:
                            itemStack.setType(Material.ALLIUM);
                            break;
                        case POTTED_CORNFLOWER:
                            itemStack.setType(Material.CORNFLOWER);
                            break;
                        case POTTED_ORANGE_TULIP:
                            itemStack.setType(Material.ORANGE_TULIP);
                            break;
                        case POTTED_DANDELION:
                            itemStack.setType(Material.DANDELION);
                            break;
                        case POTTED_PINK_TULIP:
                            itemStack.setType(Material.PINK_TULIP);
                            break;
                        case POTTED_RED_TULIP:
                            itemStack.setType(Material.RED_TULIP);
                            break;
                        case POTTED_OXEYE_DAISY:
                            itemStack.setType(Material.OXEYE_DAISY);
                            break;
                        case POTTED_BLUE_ORCHID:
                            itemStack.setType(Material.BLUE_ORCHID);
                            break;
                        case POTTED_WHITE_TULIP:
                            itemStack.setType(Material.WHITE_TULIP);
                            break;
                    }
                    event.getPlayer().getInventory().addItem(itemStack);
                }
                else if (blockBreakManager.checkMaterial(event.getBlock().getType(), event.getBlock().getLocation())) {
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void CoralDespawn(BlockFadeEvent event) {
        if (event.getBlock().getType().toString().endsWith("FAN") || event.getBlock().getType().toString().endsWith("CORAL")) {
            event.setCancelled(true);
        }
    }

    public void netherPortalTeleporter(Player player, Location playerLoc) {
            if (playerLoc.getX() > 12 && playerLoc.getX() < 16) {
                int targetX = 0;
                for (Participant p : players.participants) {
                if (player.getUniqueId() == p.player.getUniqueId()) {
                    switch (p.team.getTeamName()) {
                        case "Red Rabbits":
                            targetX = teamPortalLoc[0];
                            break;
                        case "Yellow Yaks":
                            targetX = teamPortalLoc[1];
                            break;
                        case "Blue Bats":
                            targetX = teamPortalLoc[2];
                            break;
                        case "Green Guardians":
                            targetX = teamPortalLoc[3];
                            break;
                        case "Purple Pandas":
                            targetX = teamPortalLoc[4];
                            break;
                        case "Pink Piglets":
                            targetX = teamPortalLoc[5];
                            break;
                    }
                    Location targetLoc = new Location(bsabm.world, targetX, 1, playerLoc.getZ()+150, playerLoc.getYaw(), playerLoc.getPitch());
                    player.teleport(targetLoc);
                    player.setAllowFlight(true);
                }
            }
        }
        else {
            Location targetLoc = new Location(bsabm.world, 13, 1, playerLoc.getZ()-150, playerLoc.getYaw(), playerLoc.getPitch());
            player.teleport(targetLoc);
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void netherPortalEnter(PlayerPortalEvent event) {
        event.setCancelled(true);
    }
}