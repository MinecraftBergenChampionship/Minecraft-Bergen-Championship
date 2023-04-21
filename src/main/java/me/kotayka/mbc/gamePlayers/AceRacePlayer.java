package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AceRacePlayer extends GamePlayer {
    public int lap = 0;
    public int checkpoint = 0;

    public AceRacePlayer(Participant p) {
        super(p);
    }

    public void nextCheckpoint() {
        if (AceRace.world == null) {
            getParticipant().getPlayer().sendMessage("TEST");
        }
        if (!checkCoords()) return;
        if (checkpoint < AceRace.map.mapLength) {
            checkpoint++;
        }
        else {
            Bukkit.broadcastMessage("Not sure");
        }
    }

    public Boolean checkCoords() {
        return (AceRace.map.checkpoints.get(checkpoint).distance(getParticipant().getPlayer().getLocation()) < 5);
    }
}
