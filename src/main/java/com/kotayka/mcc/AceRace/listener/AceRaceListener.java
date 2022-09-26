package com.kotayka.mcc.AceRace.listener;

import com.kotayka.mcc.AceRace.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class AceRaceListener implements Listener {
    private final AceRace aceRace;

    public AceRaceListener(AceRace aceRace) {
        this.aceRace = aceRace;
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        if (aceRace.mcc.game.stage.equals("AceRace")) {
            if(event.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SMOOTH_QUARTZ) {
                aceRace.playerFinishLap(event.getPlayer());
            }
            if (event.getPlayer().getLocation().getY() < 0) {
                event.getPlayer().teleport(aceRace.respawnPoints.get(aceRace.playerProgress.get(event.getPlayer().getUniqueId())));
            }
            if ((event.getPlayer().getLocation().getBlock().getType() == Material.LAVA)) {
                event.getPlayer().teleport(aceRace.respawnPoints.get(aceRace.playerProgress.get(event.getPlayer().getUniqueId())));
                event.getPlayer().setFireTicks(0);
            }
        }
    }
}
