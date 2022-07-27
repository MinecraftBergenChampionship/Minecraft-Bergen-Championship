package com.kotayka.mcc.BSABM.listeners;

import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.mainGame.manager.Game;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.*;

public class BSABMListener implements Listener {
    private final BSABM bsabm;
    private final Game game;
    private final Players players;
    private final Plugin plugin;

    Map<UUID, Material> hotbarSelector = new HashMap<UUID, Material>();
    public int portalLoc = 0;
    public int[] teamPortalLoc = {14, 16, 18, 20, 22, 24};

    public BSABMListener(BSABM bsabm, Game game, Players players, Plugin plugin) {
        this.bsabm = bsabm;
        this.game = game;
        this.players = players;
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
        if (game.stage == "BSABM") {
            if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.ORANGE_WOOL) {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(24));
                e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 3, e.getPlayer().getVelocity().getZ()));
            }
            if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.RED_WOOL) {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(6));
                e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.5, e.getPlayer().getVelocity().getZ()));
            }
            if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.BLACK_WOOL) {
                e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 4, e.getPlayer().getVelocity().getZ()));
            }
            if (hotbarSelector.containsKey(e.getPlayer().getUniqueId())) {
                if (e.getPlayer().getTargetBlock(null, 5).getType() != hotbarSelector.get(e.getPlayer().getUniqueId())) {
                    hotbarSelector.put(e.getPlayer().getUniqueId(), e.getPlayer().getTargetBlock(null, 5).getType());
                    if (e.getPlayer().getTargetBlock(null, 5).getType() != Material.AIR) {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(e.getPlayer().getUniqueId()))))));
                    }
                    else {
                        e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    }
                }
            }
            else {
                hotbarSelector.put(e.getPlayer().getUniqueId(), e.getPlayer().getTargetBlock(null, 5).getType());
                e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(e.getPlayer().getUniqueId()))))));
            }
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (game.stage == "BSABM") {
            if (event.getBlock().getType() == Material.BEDROCK) {
                bsabm.start();
            }
            else {
                bsabm.mapUpdate(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent event) {
        if (game.stage == "BSABM") {
            bsabm.mapUpdate(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onNetherPortalEnter(PlayerPortalEvent event) {
        if (game.stage == "BSABM") {
            if (event.getPlayer().getLocation().getX() < 2 && event.getPlayer().getLocation().getX() > -2) {
                int targetX = 0;
                for (Participant p : players.participants) {
                    if (event.getPlayer().getUniqueId() == p.player.getUniqueId()) {
                        switch (p.team) {
                            case "RedRabbits":
                                targetX = teamPortalLoc[0];
                                break;
                            case "YellowYaks":
                                targetX = teamPortalLoc[1];
                                break;
                            case "BlueBats":
                                targetX = teamPortalLoc[2];
                                break;
                            case "GreenGuardians":
                                targetX = teamPortalLoc[3];
                                break;
                            case "PurplePandas":
                                targetX = teamPortalLoc[4];
                                break;
                            case "PinkPiglets":
                                targetX = teamPortalLoc[5];
                                break;
                        }
                        Location targetLoc = new Location(bsabm.world, targetX, 2, 71, event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch());
                        event.getPlayer().teleport(targetLoc);
                    }
                }
            }
            else {
                Location targetLoc = new Location(bsabm.world, 0, 2, 71, event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch());
                event.getPlayer().teleport(targetLoc);
            }
            event.setCancelled(true);
        }
    }
}
