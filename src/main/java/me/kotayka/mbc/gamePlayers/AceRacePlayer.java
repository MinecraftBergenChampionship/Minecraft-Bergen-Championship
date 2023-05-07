package me.kotayka.mbc.gamePlayers;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.AceRace;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AceRacePlayer extends GamePlayer {
    public int lap = 1;
    public int checkpoint = 0;
    public String[] lapTimes = new String[3]; // im going to assume all ace race maps we'll make are 3 laps to conserve (minimal) space
    public long lapStartTime;
    public long totalTime;
    public short placement;

    public final AceRace aceRace;

    public AceRacePlayer(Participant p, AceRace aceRace) {
        super(p);
        this.aceRace = aceRace;
    }

    /**
     * Handles increasing player's lap count and them finishing the game
     * Also displays text and updates score
     */
    private void Lap() {
        ChatColor placementColor;
        aceRace.finishedPlayersByLap[lap-1]++;  // increment amount of players that have finished
        placement = aceRace.finishedPlayersByLap[lap-1];
        long lapTime;
        // apparently "enhanced switches" are only java 14+, idk how much that has any significance
        placementColor = switch (placement) {
            case 1 -> ChatColor.GOLD;
            case 2 -> ChatColor.GRAY;
            case 3 -> ChatColor.getByChar("#CD7F32");
            default -> ChatColor.YELLOW;
        };

        updateScore(this.getParticipant());

        if (lap == 1) {  // first lap uses universal lapStartTime
            lapStartTime = System.currentTimeMillis();
            lapTime = lapStartTime - aceRace.startingTime;
            totalTime += lapTime;   // add to total time
            String firstTime = new SimpleDateFormat("m:ss.S").format(new Date(lapTime)); // get formatted time
            lapTimes[lap-1] = firstTime;
        } else {
            lapTime = System.currentTimeMillis() - lapStartTime;
            totalTime += lapTime;
            String lapTimeFormatted = new SimpleDateFormat("m:ss.S").format(new Date(lapTime));
            lapTimes[lap-1] = lapTimeFormatted;
            lapStartTime = System.currentTimeMillis();
        }

        if (lap < 3) {
            Bukkit.broadcastMessage(
                    this.getParticipant().getPlayerNameWithIcon() + ChatColor.GRAY + " has finished Lap " + lap + " in " +
                    placementColor + Game.getPlace(placement) + ChatColor.GRAY + "! (Split: " + ChatColor.YELLOW + lapTimes[lap-1] + ")");
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Completed Lap " + lap + "!", placementColor + "#" + placement + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap-1], 0, 60, 20);
            lap++;
        } else {
            String totalTimeFormat = ChatColor.YELLOW + new SimpleDateFormat("m:ss.S").format(new Date(totalTime));
            this.getParticipant().getPlayer().setGameMode(GameMode.SPECTATOR);
            this.getParticipant().getPlayer().sendTitle(ChatColor.AQUA + "Finished!", placementColor + "#" + placement + ChatColor.GRAY + " | " + ChatColor.YELLOW + lapTimes[lap-1], 0, 60, 20);
            Bukkit.broadcastMessage(
                    this.getParticipant().getPlayerNameWithIcon() + ChatColor.GRAY + "" + ChatColor.BOLD + " has finished " + aceRace.map.mapName + " in " +
                    placementColor + Game.getPlace(placement) + ChatColor.GRAY + " with " + ChatColor.YELLOW + totalTimeFormat + ChatColor.GRAY + "! (Split: " + ChatColor.YELLOW + lapTimes[lap-1] + ")");
            // TODO: summon firework
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.YELLOW + "Your Times: ");
            this.getParticipant().getPlayer().sendMessage("1: " + lapTimes[0]);
            this.getParticipant().getPlayer().sendMessage("2: " + lapTimes[1]);
            this.getParticipant().getPlayer().sendMessage("3: " + lapTimes[2]);
            this.getParticipant().getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Overall: " + ChatColor.YELLOW + new SimpleDateFormat("m:ss.S").format(new Date(totalTime)));
            this.getParticipant().getPlayer().sendMessage("                                ");
            this.getParticipant().getPlayer().sendMessage(ChatColor.AQUA + "--------------------------------");
        }
    }

    /**
     * Handles updating player and team score
     */
    private void updateScore(Participant p) {
        int beatPlayers = aceRace.aceRacePlayerList.size() - aceRace.finishedPlayersByLap[lap-1];
        if (lap < 3) {
            p.addRoundScore(beatPlayers * AceRace.PLACEMENT_LAP_POINTS);
        } else {
            // final points are worth more
            p.addRoundScore(beatPlayers * AceRace.PLACEMENT_FINAL_LAP_POINTS);
            p.addRoundScore(AceRace.FINISH_RACE_POINTS);

            // final placement bonuses
            if (placement < 9) {
                p.addRoundScore(AceRace.PLACEMENT_BONUSES[placement-1]);
            }
        }
    }

    /**
     * Determines whether a player has reached the next checkpoint.
     * Modifies values of currentCheckpoint and nextCheckpoint; both are manually reset each lap
     *
     */
    public void setCheckpoint() {
        // exit if player is Spectator or done with race
        if (getParticipant().getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;

        // if we are not near a checkpoint, exit
        if (!checkCoords()) return;

        // case for finishing lap
        if (checkpoint == aceRace.map.mapLength) {
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
        return (aceRace.map.checkpoints.get(checkpoint < aceRace.map.mapLength ? checkpoint : 0).distance(getParticipant().getPlayer().getLocation()) <= 6);
    }
}
