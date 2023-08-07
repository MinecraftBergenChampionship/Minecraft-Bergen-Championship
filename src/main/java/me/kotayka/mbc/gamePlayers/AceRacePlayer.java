package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AceRacePlayer extends GamePlayer {
    protected static AceRace ACE_RACE;  // essentially a global const
    public int lap = 1;
    public int checkpoint = 0;
    public String[] lapTimes = new String[3]; // im going to assume all ace race maps we'll make are 3 laps to conserve (minimal) space
    public long lapStartTime;
    public long totalTime;
    public int placement;

    public AceRacePlayer(Participant p, AceRace ar) {
        super(p);

        ACE_RACE = ar;
    }

    /**
     * Handles increasing player's lap count and them finishing the game
     * Also displays text and updates score
     */
    private void Lap() {
        if (!(ACE_RACE.getState().equals(GameState.ACTIVE))) { return; }

        long lapTime;
        ACE_RACE.finishedPlayersByLap[lap - 1]++;  // increment amount of players that have finished
        placement = ACE_RACE.finishedPlayersByLap[lap - 1];
        String placementColor = AceRace.getColorStringFromPlacement(placement);
        String placementString = AceRace.getPlace(placement);

        updateScore(this.getParticipant());

        if (lap == 1) {  // first lap uses universal lapStartTime
            lapStartTime = System.currentTimeMillis();
            lapTime = lapStartTime - ACE_RACE.startingTime;
            totalTime += lapTime;   // add to total time
            String firstTime = new SimpleDateFormat("m:ss.S").format(new Date(lapTime)); // get formatted time
            lapTimes[lap - 1] = firstTime;
        } else {
            lapTime = System.currentTimeMillis() - lapStartTime;
            totalTime += lapTime;
            lapTimes[lap - 1] = new SimpleDateFormat("m:ss.S").format(new Date(lapTime));
            lapStartTime = System.currentTimeMillis();
        }

        // only add to Top 5 if this lap's split was faster or equal to the 5th (map is maintained to be at max size 5).
        if (ACE_RACE.fastestLaps.size() == 0) {
            List<String> t = new ArrayList<String>();
            t.add(this.getParticipant().getFormattedName());
            ACE_RACE.fastestLaps.put(lapTime,t);
        } else if (lapTime <= ACE_RACE.fastestLaps.lastKey()) {
            Set<Long> times = ACE_RACE.fastestLaps.keySet();
            if (times.contains(lapTime)) {
                ACE_RACE.fastestLaps.get(lapTime).add(this.getParticipant().getFormattedName());
            } else {
                List<String> t = new ArrayList<String>();
                t.add(this.getParticipant().getFormattedName());
                ACE_RACE.fastestLaps.put(lapTime,t);
            }

            // trim map to only contain top 5
            if (ACE_RACE.fastestLaps.size() > 5) {
                long tmp = ACE_RACE.fastestLaps.lastKey();
                ACE_RACE.fastestLaps.remove(tmp);
            }
        }

        if (lap < 3) {
            Bukkit.broadcastMessage(
                    this.getParticipant().getFormattedName() + ChatColor.GRAY + " has finished Lap " + lap + " in " +
                            placementColor + placementString + ChatColor.GRAY + "! (Split: " + ChatColor.YELLOW + lapTimes[lap - 1] + ")");
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap " + lap + "!", placementColor + placementString + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap - 1], 0, 60, 20);
            lap++;
        } else {
            String totalTimeFormat = ChatColor.YELLOW + new SimpleDateFormat("m:ss.S").format(new Date(totalTime));
            this.getParticipant().getPlayer().setGameMode(GameMode.SPECTATOR);
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Finished!", placementColor +  placementString + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap - 1], 0, 60, 20);
            Bukkit.broadcastMessage(
                    this.getParticipant().getFormattedName() + " finished " +ChatColor.BOLD+ ACE_RACE.map.mapName + ChatColor.RESET+" in " +
                            placementColor + placementString + " with " + ChatColor.YELLOW + totalTimeFormat + ChatColor.RESET + "! (Split: " + ChatColor.YELLOW + lapTimes[lap - 1] + ChatColor.RESET+ ")");
            MBC.spawnFirework(this.getParticipant());
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.YELLOW + "Your Times: ");
            this.getParticipant().getPlayer().sendMessage("1: " + lapTimes[0]);
            this.getParticipant().getPlayer().sendMessage("2: " + lapTimes[1]);
            this.getParticipant().getPlayer().sendMessage("3: " + lapTimes[2]);
            this.getParticipant().getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Overall: " + ChatColor.YELLOW + new SimpleDateFormat("m:ss.S").format(new Date(totalTime)));
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");

            // check if all players on team have finished last lap
            int done = 0;
            for (Participant p : this.getParticipant().getTeam().teamPlayers) {
                if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) done++;
            }
            if (done == this.getParticipant().getTeam().teamPlayers.size()) {
                Bukkit.broadcastMessage(this.getParticipant().getTeam().teamNameFormat() + " have all finished the race!");
            }

            // since this was the last lap, check if all players have finished the last lap
            if (ACE_RACE.finishedPlayersByLap[2] == ACE_RACE.aceRacePlayerList.size()) {
                ACE_RACE.timeRemaining = 1; // end the game
            }
        }
    }

    /**
     * Handles updating player and team score
     */
    private void updateScore(Participant p) {
        int beatPlayers = ACE_RACE.aceRacePlayerList.size() - ACE_RACE.finishedPlayersByLap[lap-1];
        if (lap < 3) {
            p.addCurrentScore(beatPlayers * AceRace.PLACEMENT_LAP_POINTS);
        } else {
            // final points are worth more
            p.addCurrentScore(beatPlayers * AceRace.PLACEMENT_FINAL_LAP_POINTS);
            p.addCurrentScore(AceRace.FINISH_RACE_POINTS);

            // final placement bonuses
            if (placement < 9) {
                p.addCurrentScore(AceRace.PLACEMENT_BONUSES[placement-1]);
            }
        }
    }

    /**
     * Determines whether a player has reached the next checkpoint.
     * Modifies values of currentCheckpoint and nextCheckpoint; both are manually reset each lap
     */
    public void setCheckpoint() {
        // exit if player is Spectator or done with race
        if (getParticipant().getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;

        // if we are not near a checkpoint, exit
        if (!checkCoords()) return;

        // case for finishing lap
        if (checkpoint == ACE_RACE.map.mapLength) {
            checkpoint = 0;
            Lap();
        } else { // not last checkpoint
            checkpoint++;
        }
    }

    /**
     * Check if player has reached next checkpoint.
     * Does not update checkpoint variable.
     * @see AceRacePlayer setCheckpoint()
     * @return true if next checkpoint is within 6 distance, false otherwise
     */
    public boolean checkCoords() {
        // If checkpoint is out of bounds, player is on last lap and should compare to first checkpoint
        return (ACE_RACE.map.checkpoints.get(checkpoint < ACE_RACE.map.mapLength ? checkpoint : 0).distance(getParticipant().getPlayer().getLocation()) <= 6);
    }
}
