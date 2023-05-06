package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AceRacePlayer extends GamePlayer {
    public int lap = 1;
    public int currentCheckpoint = 0;
    public int nextCheckpoint = 1; //idk how useful this is but it helped me maybe im stupid

    public AceRacePlayer(Participant p) {
        super(p);
    }


    /**
     * Determines whether a player has reached the next checkpoint.
     * Modifies values of currentCheckpoint and nextCheckpoint; both are manually reset each lap
     *
     */
    public void setCheckpoint() {
        // debug: re-add if necessary
        //if (AceRace.world == null) {
        //    getParticipant().getPlayer().sendMessage("ERROR: Ace Race world is null (please report to admin)");
        //}

        // if we are not near a checkpoint, exit
        if (!checkCoords()) return;

        // case for finishing lap
        if (nextCheckpoint == 0) {
            // basically reset to defaults (nC = 1, cC = 0)
            nextCheckpoint++;
            currentCheckpoint = 0;

            // temporary
            lap++;

            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);
        } else if (nextCheckpoint + 1 < AceRace.map.mapLength) {
            currentCheckpoint++;
            nextCheckpoint++;

            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);
        } else {
            currentCheckpoint++;
            nextCheckpoint = 0;

            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);
        }
    }

    /**
     * Check if player has reached next checkpoint.
     * Does not update checkpoint variable.
     * @see AceRacePlayer nextCheckpoint()
     * @return true if next checkpoint is within 10 distance, false otherwise
     */
    public boolean checkCoords() {
        return (
                AceRace.map.checkpoints.get(nextCheckpoint).distance(getParticipant().getPlayer().getLocation()) <= 5
        );
    }
}
