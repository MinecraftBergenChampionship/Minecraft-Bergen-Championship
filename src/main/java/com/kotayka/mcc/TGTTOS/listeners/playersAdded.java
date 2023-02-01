package com.kotayka.mcc.TGTTOS.listeners;

import com.kotayka.mcc.TGTTOS.managers.NPCManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class playersAdded implements Listener {

    public final NPCManager npcManager;

    public playersAdded(NPCManager npcManager) {
        this.npcManager = npcManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        npcManager.addPlayer(event.getPlayer());
    }

}
