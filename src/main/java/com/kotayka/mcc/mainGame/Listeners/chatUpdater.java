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
        String message = event.getMessage();
        ChatColor color = ChatColor.GRAY;
        String icon = "";
        for (Participant p : players.participants) {
            if (event.getPlayer() == p.player) {
                switch (p.team) {
                    case "RedRabbits":
                        color = ChatColor.RED;
                        break;
                    case "YellowYaks":
                        color = ChatColor.YELLOW;
                        break;
                    case "GreenGuardians":
                        color = ChatColor.GREEN;
                        break;
                    case "BlueBats":
                        color = ChatColor.BLUE;
                        break;
                    case "PurplePandas":
                        color = ChatColor.DARK_PURPLE;
                        break;
                    case "PinkPiglets":
                        color = ChatColor.LIGHT_PURPLE;
                        break;
                }
            }
        }

        event.setFormat(ChatColor.BOLD+""+color+player.getDisplayName() + ChatColor.WHITE +" "+ message);
    }
}
