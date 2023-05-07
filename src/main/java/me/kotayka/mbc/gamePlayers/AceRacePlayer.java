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
    public long totalTime;
    public short placement;

    public AceRacePlayer(Participant p) {
        super(p);
    }

    /**
     * Handles increasing player's lap count and them finishing the game
     */
    private void Lap() {
        ChatColor placementColor;
        AceRace.finishedPlayersByLap[lap-1]++;  // increment amount of players that have finished
        placement = AceRace.finishedPlayersByLap[0];
        long lapTime;
        // apparently "enhanced switches" are only java 14+, idk how much that has any significance
        placementColor = switch (placement) {
            case 1 -> ChatColor.GOLD;
            case 2 -> ChatColor.GRAY;
            case 3 -> ChatColor.getByChar("#CD7F32");
            default -> ChatColor.YELLOW;
        };
        if (lap == 1) {  // first lap uses universal lapStartTime
            lapStartTime = System.currentTimeMillis();
            lapTime = lapStartTime - AceRace.startingTime;
            totalTime += lapTime;   // add to total time
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap 1!", " ", 0, 60, 20);
            String firstTime = new SimpleDateFormat("mm:ss.SS").format(new Date(lapTime)); // get formatted time
            lapTimes[lap-1] = firstTime;
        } else {
            lapTime = System.currentTimeMillis() - lapStartTime;
            totalTime += lapTime;
            String lapTimeFormatted = new SimpleDateFormat("mm:ss.SS").format(new Date(lapTime));
            lapTimes[lap-1] = lapTimeFormatted;
            lapStartTime = System.currentTimeMillis();
        }

        String placementPostfix;
        int x = placement;
        while (x >= 10) {
            x = x % 10;
        }
        placementPostfix = switch(x) {
            case 1 -> placementPostfix = "st";
            case 2 -> placementPostfix = "nd";
            case 3 -> placementPostfix = "rd";
            default -> placementPostfix = "th";
        };

        Bukkit.broadcastMessage(
                this.getParticipant().getPlayerNameWithIcon() + ChatColor.GRAY + " has finished Lap " + lap + " in " +
                placementColor + placement + placementPostfix + ChatColor.GRAY + "! (Split: " + ChatColor.GOLD + lapTimes[lap-1] + ")");

        if (lap < 3) {
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap " + lap + "!", placementColor + "#" + placement + " | " + lapTimes[lap-1], 0, 60, 20);
            lap++;
        } else {
            this.getParticipant().getPlayer().setGameMode(GameMode.SPECTATOR);
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap " + lap + "!", placementColor + "#" + placement + " | " + lapTimes[lap-1], 0, 60, 20);
            // TODO: summon firework
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.GOLD + "Your Times: ");
            this.getParticipant().getPlayer().sendMessage("1: " + lapTimes[0]);
            this.getParticipant().getPlayer().sendMessage("2: " + lapTimes[1]);
            this.getParticipant().getPlayer().sendMessage("3: " + lapTimes[2]);
            this.getParticipant().getPlayer().sendMessage(ChatColor.BLUE + "Overall: " + new SimpleDateFormat("mm:ss.SS").format(new Date(totalTime)));
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
        }
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

        // exit if player is Spectator or done with race
        if (getParticipant().getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;

        // if we are not near a checkpoint, exit
        if (!checkCoords()) return;

        // case for finishing lap
        if (nextCheckpoint == 0) {
            // basically reset to defaults (nC = 1, cC = 0)
            nextCheckpoint++;
            currentCheckpoint = 0;

            // handle lap
           Lap();

        } else if (nextCheckpoint + 1 < AceRace.map.mapLength) {
            currentCheckpoint++;
            nextCheckpoint++;
        } else {
            currentCheckpoint++;
            nextCheckpoint = 0;
        }
    }

    /**
     * Check if player has reached next checkpoint.
     * Does not update checkpoint variable.
     * @see AceRacePlayer setCheckpoint()
     * @return true if next checkpoint is within 5 distance, false otherwise
     */
    public boolean checkCoords() {
        return (
                AceRace.map.checkpoints.get(nextCheckpoint).distance(getParticipant().getPlayer().getLocation()) <= 5
        );
    }
}
