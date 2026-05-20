package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.acerace.AceRace;
import me.kotayka.mbc.games.acerace.PowerupHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class AceRacePlayer extends GamePlayer {
    protected static AceRace ACE_RACE;  // essentially a global const
    public int lap = 1;
    public int checkpoint = 0;
    public String[] lapTimes = new String[3]; // im going to assume all ace race maps we'll make are 3 laps to conserve (minimal) space
    public long lapStartTime;
    public long totalTime;
    public int placement;
    public int currentPlace;
    public boolean completedFirstLap = false;
    public boolean fastestLapsContainsPlayer;
    public long fastestLap;
    public ArrayList<Player> hiddenPlayers = new ArrayList<Player>();
    public int cooldownTimer = 240;

    public AceRacePlayer(Participant p, AceRace ar) {
        super(p);

        ACE_RACE = ar;
    }

    /**
     * Handles increasing player's lap count and them finishing the game
     * Also displays text and updates score
     */
    private void Lap() {
        if (!(ACE_RACE.getState().equals(GameState.ACTIVE)) && !(ACE_RACE.getState().equals(GameState.PAUSED))) { return; }

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

        if (ACE_RACE.fastestLaps.size() == 5 && completedFirstLap) {
            if (ACE_RACE.fastestLaps.get(fastestLap) == null) {
                fastestLapsContainsPlayer = false;
            }
        }
        completedFirstLap = true;
        // only add to Top 5 if this lap's split was faster or equal to the 5th (map is maintained to be at max size 5).
        // TODO: this may be buggy and may require a second review. The use of a set means equivalent laps are also not tracked
        if (ACE_RACE.fastestLaps.isEmpty()) {
            //this is the first lap completed
            List<String> t = new ArrayList<String>();
            t.add(this.getParticipant().getFormattedName());
            ACE_RACE.fastestLaps.put(lapTime,t);
            fastestLapsContainsPlayer = true;
            fastestLap = lapTime;
        } else if ((!fastestLapsContainsPlayer && lapTime <= ACE_RACE.fastestLaps.lastKey()) || (!fastestLapsContainsPlayer && ACE_RACE.fastestLaps.size() < 5)) {
            // either top 5 currently doesnt have player and player completes faster lap than top 5
            // or top 5 currently doesnt have player and there are not 5 laps completed yet
            Set<Long> times = ACE_RACE.fastestLaps.keySet();
            if (times.contains(lapTime)) {
                ACE_RACE.fastestLaps.get(lapTime).add(this.getParticipant().getFormattedName());
            } else {
                List<String> t = new ArrayList<String>();
                t.add(this.getParticipant().getFormattedName());
                ACE_RACE.fastestLaps.put(lapTime,t);
            }
            fastestLapsContainsPlayer = true;
            fastestLap = lapTime;

            // trim map to only contain top 5
            if (ACE_RACE.fastestLaps.size() > 5) {
                long tmp = ACE_RACE.fastestLaps.lastKey();
                ACE_RACE.fastestLaps.remove(tmp);
            }
        }
        else if (fastestLapsContainsPlayer && lapTime <= ACE_RACE.fastestLaps.lastKey() && lapTime < fastestLap) {
            // top 5 has player but current lap is faster than fastest lap
            ACE_RACE.fastestLaps.remove(fastestLap);
            List<String> t = new ArrayList<String>();
            t.add(this.getParticipant().getFormattedName());
            ACE_RACE.fastestLaps.put(lapTime,t);
            fastestLapsContainsPlayer = true;
            fastestLap = lapTime;
        }

        if (lap < 3) {
            String str =
                    this.getParticipant().getFormattedName() + ChatColor.GRAY + " has finished Lap " + lap + " in " +
                            placementColor + placementString + ChatColor.GRAY + "! (Split: " + ChatColor.YELLOW + lapTimes[lap - 1] + ")";
            ACE_RACE.getLogger().log(str);
            Bukkit.broadcastMessage(str);
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap " + lap + "!", placementColor + placementString + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap - 1], 0, 60, 20);
            int pointsGained = (ACE_RACE.aceRacePlayerMap.size() - ACE_RACE.finishedPlayersByLap[lap-1]) * AceRace.PLACEMENT_LAP_POINTS + AceRace.LAP_COMPLETION_POINTS;
            this.getParticipant().getPlayer().sendMessage(ChatColor.GREEN + "You completed a lap!" + MBC.scoreFormatter(pointsGained));
            lap++;
            ACE_RACE.createLine(6, ChatColor.GREEN.toString()+ChatColor.BOLD+"Lap: " + ChatColor.WHITE+lap+"/3", getParticipant());
        } else {
            String totalTimeFormat = ChatColor.YELLOW + new SimpleDateFormat("m:ss.S").format(new Date(totalTime));
            this.getParticipant().getPlayer().setGameMode(GameMode.SPECTATOR);
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Finished!", placementColor +  placementString + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap - 1], 0, 60, 20);

            String str =
                    this.getParticipant().getFormattedName() + " finished " +ChatColor.BOLD+ ACE_RACE.map.mapName + ChatColor.RESET+" in " +
                            placementColor + placementString + ChatColor.RESET + " with " + ChatColor.YELLOW + totalTimeFormat + ChatColor.RESET + "! (Split: " + ChatColor.YELLOW + lapTimes[lap - 1] + ChatColor.RESET+ ")";
            ACE_RACE.getLogger().log(str);
            Bukkit.broadcastMessage(str);

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

            int pointsGained = (ACE_RACE.aceRacePlayerMap.size() - ACE_RACE.finishedPlayersByLap[lap-1]) * AceRace.PLACEMENT_FINAL_LAP_POINTS + AceRace.LAP_COMPLETION_POINTS + AceRace.FINISH_RACE_POINTS;
            if (placement < 11) {
                pointsGained+=AceRace.PLACEMENT_BONUSES[placement-1];
            }
            this.getParticipant().getPlayer().sendMessage(ChatColor.GREEN + "You completed the race!" + MBC.scoreFormatter(pointsGained));

            ACE_RACE.createLine(6, ChatColor.GREEN.toString()+ChatColor.BOLD+"Lap: " + ChatColor.RESET+"Complete!", this.getParticipant());
            ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " + ChatColor.RESET+"Complete!", this.getParticipant());
            
            MBC.getInstance().showPlayers(this.getParticipant());

            // check if all players on team have finished last lap
            int done = 0;
            for (Participant p : this.getParticipant().getTeam().teamPlayers) {
                if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) done++;
            }
            if (done == this.getParticipant().getTeam().teamPlayers.size()) {
                Bukkit.broadcastMessage(this.getParticipant().getTeam().teamNameFormat() + " have all finished the race!");
            }

            // since this was the last lap, check if all players have finished the last lap
            if (ACE_RACE.finishedPlayersByLap[2] == ACE_RACE.aceRacePlayerMap.size()) {
                ACE_RACE.timeRemaining = 1; // end the game
            }
        }
    }

    /**
     * Handles updating player and team score
     */
    private void updateScore(Participant p) {
        int beatPlayers = ACE_RACE.aceRacePlayerMap.size() - ACE_RACE.finishedPlayersByLap[lap-1];
        if (lap < 3) {
            p.addCurrentScore(beatPlayers * AceRace.PLACEMENT_LAP_POINTS + AceRace.LAP_COMPLETION_POINTS);
        } else {
            // final points are worth more
            p.addCurrentScore(beatPlayers * AceRace.PLACEMENT_FINAL_LAP_POINTS + AceRace.LAP_COMPLETION_POINTS);
            p.addCurrentScore(AceRace.FINISH_RACE_POINTS);

            // final placement bonuses
            if (placement < 11) {
                p.addCurrentScore(AceRace.PLACEMENT_BONUSES[placement-1]);
            }
        }
    }

    public boolean addHiddenPlayer(Player p) {
        if (hiddenPlayers.contains(p)) return false;
        hiddenPlayers.add(p);
        return true;
    }

    public boolean checkHiddenPlayer(Player p) {
        return hiddenPlayers.contains(p);
    }

    public boolean removeHiddenPlayer(Player p) {
        if (!hiddenPlayers.contains(p)) return false;
        hiddenPlayers.remove(p);
        return true;
    }

    /**
     * Determines whether a player has reached the next checkpoint.
     * A player has reached the next checkpoint if they are within a distance of 6 of either the next or subsequent checkpoint.
     * Modifies values of currentCheckpoint and nextCheckpoint; both are manually reset each lap
     *
     * @param givePowerup Whether or not to provide a powerup to the `this` player.
     * @implNote Note that this function is currently only triggered when a player walks over a "checkpoint checker" special block,
     *           including jump pads and carpet.
     */
    public void setCheckpoint(boolean givePowerup) {
        // exit if player is Spectator or done with race
        if (getParticipant().getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;

        // check distance to next checkpoint and the following checkpoint
        Location location = getParticipant().getPlayer().getLocation();
        boolean nextCheckpoint = ACE_RACE.map.checkpoints.get(checkpoint % ACE_RACE.map.mapLength).distance(location) <= 12;
        boolean skipCheckpoint = ACE_RACE.map.checkpoints.get((checkpoint + 1) % ACE_RACE.map.mapLength).distance(location) <= 12;

        if (!nextCheckpoint && !skipCheckpoint) return;

        // Prioritize if the player has reached the first checkpoint.
        if (nextCheckpoint) {
            // case for finishing lap
            if (checkpoint == ACE_RACE.map.mapLength) {
                checkpoint = 0;
                ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
                Lap();
            } else { // not last checkpoint
                if (checkpoint != 0) {
                    // This code segment specifically does not run for the initial checkpoint.
                    this.getParticipant().getPlayer().sendTitle(" ", ChatColor.YELLOW + "Checkpoint " + (checkpoint+1) + "/" + ACE_RACE.map.mapLength, 0, 40, 20);
                    if (givePowerup)
                        PowerupHandler.givePowerup(this);
                }
                checkpoint++;
                ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
            }
            if (ACE_RACE.getState().equals(GameState.ACTIVE) && !(lap == 1 && checkpoint == 0)) {
                currentPlace = ACE_RACE.checkpointPlacement(this, lap, checkpoint);
                String currentPlacementString = AceRace.getPlace(currentPlace);
                this.getParticipant().getPlayer().sendMessage(ChatColor.GREEN + "You are currently in " + currentPlacementString + " place.");
            }
        } else {
            if (checkpoint != 0) {
                // This code segment specifically does not run for the initial checkpoint.
                this.getParticipant().getPlayer().sendTitle(" ", ChatColor.YELLOW + "Checkpoint " + (checkpoint+1) + "/" + ACE_RACE.map.mapLength, 0, 40, 20);
                if (givePowerup)
                    PowerupHandler.givePowerup(this);
            }
            checkpoint++;
            ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
        }
    }

    /**
     * For practice mode: will set your checkpoint to i.
     */
    public void checkpointSetter(int i) {
        if (i >= ACE_RACE.map.mapLength || i < 0) {
            checkpoint = 0;
            ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
        } else {
            checkpoint = i;
            ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
        }
    }

    /**
     * Utility for Warp powerup.
     * Returns a boolean indicating whether the checkpoint was set.
     * @see PowerupHandler useTeleporter()
     * @param i Checkpoint to set `this` player to.
     */
    public void warpCheckpointSetter(int i) {
        if (!(i >= (ACE_RACE.map.mapLength) || i < 0)) {
            checkpoint = i;
            ACE_RACE.createLine(5, ChatColor.GREEN+"Checkpoint: " +ChatColor.RESET+ checkpoint + "/" + ACE_RACE.map.checkpoints.size(), this.getParticipant());
        }
    }

    public void reset() {
        Arrays.fill(lapTimes, "");
        lap = 1;
        checkpoint = 0;
        lapStartTime = 0;
        totalTime = 0;
        placement = 0;
    }
}
