package com.kotayka.mcc.mainGame.Listeners;

import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class playerJoinLeave implements Listener {

    public final Players players;

    public playerJoinLeave(Players players) {
        this.players = players;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        players.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        players.removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("Flying is not enabled on this server"))
            e.setCancelled(true);
    }
}
