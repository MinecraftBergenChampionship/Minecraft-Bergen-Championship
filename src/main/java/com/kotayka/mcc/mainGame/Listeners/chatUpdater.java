package com.kotayka.mcc.mainGame.Listeners;

import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class chatUpdater implements Listener {

    private final Players players;

    public chatUpdater(Players players) {
        this.players = players;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        Player player = event.getPlayer();
        String prefix = "";
        String message = event.getMessage();
        ChatColor color = ChatColor.GRAY;
        for (Participant p : players.participants) {
            if (event.getPlayer() == p.player) {
                color = p.chatColor;
                prefix = p.teamPrefix;
            }
        }

        event.setFormat(prefix + ChatColor.BOLD+""+color+player.getDisplayName() + ChatColor.WHITE +": "+ message);
    }
}
