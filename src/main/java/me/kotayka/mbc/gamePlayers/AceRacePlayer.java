package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AceRacePlayer extends GamePlayer {
    public int lap = 1;
    public int currentCheckpoint = 0;
    public int nextCheckpoint = 1; //idk how useful this is but it helped me maybe im stupid
    public String[] lapTimes = new String[3]; // im going to assume all ace race maps we'll make are 3 laps to conserve (minimal) space
    public long lapStartTime;

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

        // case for finishing lap
        if (nextCheckpoint == 0) {
            // basically reset to defaults (nC = 1, cC = 0)
            nextCheckpoint++;
            currentCheckpoint = 0;

            // handle lap
            if (lap == 1) {
                lapStartTime = System.currentTimeMillis();
                long lapOne = lapStartTime - AceRace.startingTime;
                String firstTime = new SimpleDateFormat("mm:ss:SS").format(new Date(lapOne));
                lapTimes[lap-1] = firstTime;
            } else {
                long middleLap = System.currentTimeMillis() - lapStartTime;
                String middleTime = new SimpleDateFormat("mm:ss:SS").format(new Date(middleLap));
                lapTimes[lap-1] = middleTime;
                lapStartTime = System.currentTimeMillis();
            }

            Bukkit.broadcastMessage(this.getParticipant().getPlayerNameWithIcon() + " has finished Lap " + lap + " in " + lapTimes[lap-1]);

            if (lap < 3) {
                lap++;
            } else {
                this.getParticipant().getPlayer().setGameMode(GameMode.SPECTATOR);
                this.getParticipant().getPlayer().sendTitle(ChatColor.DARK_GREEN + "Finished!", " ", 0, 2, 1);
                // TODO: summon firework
                this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
                this.getParticipant().getPlayer().sendMessage("                                ");
                this.getParticipant().getPlayer().sendMessage(ChatColor.GOLD + "Your Times: ");
                this.getParticipant().getPlayer().sendMessage("1: " + lapTimes[0]);
                this.getParticipant().getPlayer().sendMessage("2: " + lapTimes[1]);
                this.getParticipant().getPlayer().sendMessage("3: " + lapTimes[2]);
                this.getParticipant().getPlayer().sendMessage("                                ");
                this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
            }


            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);
        } else if (nextCheckpoint + 1 < AceRace.map.mapLength) {
            currentCheckpoint++;
            nextCheckpoint++;

            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);
        } else {
            currentCheckpoint++;
            nextCheckpoint = 0;

            Bukkit.broadcastMessage("Reached checkpoint " + currentCheckpoint + " on lap " + lap);


    /**
     * Check if player has reached next checkpoint.
     * Does not update checkpoint variable.
     * @see AceRacePlayer setCheckpoint()
     * @return true if next checkpoint is within 5 distance, false otherwise
     */
    public Boolean checkCoords() {
        return (AceRace.map.getCheckpoints().get(checkpoint).distance(getParticipant().getPlayer().getLocation()) < 5);
    }
}
