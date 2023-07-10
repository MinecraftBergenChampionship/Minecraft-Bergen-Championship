package me.kotayka.mbc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

/**
 * This class is for lobby-related minigames that do not necessitate the full package of a point-scoring game.
 * Examples:
 *      Lobby
 *      Decision Dome / Voting gimmick
 *      Any hub minigame put in place, i.e. trick-or-treat/presents/milk the cow
 */
public abstract class Minigame implements Scoreboard, Listener {
    public String gameName;
    private GameState gameState = GameState.INACTIVE;
    public int timeRemaining = -1;
    public static int taskID = -1;

    public Minigame(String name) {
        gameName = name;
    }

    /**
     * @see Game start()
     * all games should call a super.start()
     * minigames may have their own implementation
     * since they require less
     */
    public abstract void start();

    public boolean isGameActive() {
        if (!(MBC.getInstance().getMinigame() instanceof Game)) {
            return false;
        }

        return (this.getState().equals(GameState.ACTIVE) || this.getState().equals(GameState.OVERTIME));
        // return (this.getState().equals(GameState.ACTIVE) || this.getState().equals(GameState.PAUSED)); <- might be dependent on event? gonna hold off
    }

    /**
     * Should load players into appropriate spots after/during introduction
     * Could/should handle stuff like: clearing inventory, applying potion effects, etc
     */
    public abstract void loadPlayers();

    public abstract void events();

    public void setTimer(int time) {
        Bukkit.broadcastMessage("Setting Timer!");
        if (timeRemaining != -1) {
            // if the time hasn't run out yet, stop the time and start it again
            stopTimer();
        }

        timeRemaining = time;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
            if (MBC.getInstance().getMinigame() instanceof Game) {
                if (!gameState.equals(GameState.OVERTIME)) {
                    createLine(20, ChatColor.RED + "" + ChatColor.BOLD + "Time left: " + ChatColor.WHITE + getFormattedTime(--timeRemaining));
                } else {
                    createLine(20, ChatColor.RED + "" + ChatColor.BOLD + "Overtime: " + ChatColor.WHITE + getFormattedTime(--timeRemaining));
                }
            } else {
                createLine(20, getFormattedTime(--timeRemaining));
            }
            if (timeRemaining < 0) {
                stopTimer();
            }
            MBC.getInstance().getMinigame().events();
        }, 20, 20);
    }

    public void stopTimer() {
        Bukkit.broadcastMessage("[Debug] Stopping timer!");
        MBC.getInstance().cancelEvent(taskID);
    }

    public String getFormattedTime(int seconds) {
        return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    public void createScoreboard() {
        for (Participant p : MBC.getInstance().players) {
            newObjective(p);
            createScoreboard(p);
        }
    }

    public void createLine(int score, String line, Participant p) {
        if (p.objective == null || !Objects.equals(p.gameObjective, gameName)) {
            p.gameObjective = gameName;
            MBC.getInstance().getMinigame().createScoreboard(p);
        }

        resetLine(p, score);

        p.objective.getScore(line).setScore(score);
        p.lines.put(score, line);
    }

    public void createLine(int score, String line) {
        for (Participant p : MBC.getInstance().players) {
            createLine(score, line, p);
        }
    }
    /**
     * Updates display of team's current score on bottom of player's scoreboard;
     * Note: may be redundant
     * @param t Team for which to update for each player
     */
    public void displayTeamCurrentScore(MBCTeam t) {
        for (Participant p : t.teamPlayers) {
            createLine(2, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getMultipliedCurrentScore(), p);
        }
    }

    /**
     * Sorts teams by their current round score to place onto scoreboard.
     */
    public void updateInGameTeamScoreboard() {
        List<MBCTeam> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamRoundSorter());

        for (int i = 14; i > 14-teamRoundsScores.size(); i--) {
            MBCTeam t = teamRoundsScores.get(14-i);
            createLine(i,String.format("%s%5d", t.teamNameFormatPadding(), t.getMultipliedCurrentScore()));
        }
    }

    /**
     * Sorts teams by their overall score to place onto scoreboard during lobby/after games
     */
    public void updateTeamStandings() {
        List<MBCTeam> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamScoreSorter());

        int lastScore = -1;
        int ties = 0;
        for (int i = 14; i > 14-teamRoundsScores.size(); i--) {
            MBCTeam t = teamRoundsScores.get(14-i);
            createLine(i,String.format("%s%5d", t.teamNameFormatPadding(), t.getMultipliedTotalScore()));

            if (MBC.getInstance().gameNum == 1) continue;

            // if after first game, determine placement
            if (lastScore == t.getMultipliedTotalScore()) {
                ties++;
                t.setPlace((14-i)+ties);
            } else {
                ties = 0;
                t.setPlace(14-i);
            }

            lastScore = t.getMultipliedTotalScore();
        }
    }

    /**
     * Displays team's total score in lobby
     * Team Coins: {COIN_AMOUNT}
     * Note: Since team scoreboard is always active in lobby, this may be redundant.
     * @param t Team whose coin count to display
     */
    protected void displayTeamTotalScore(MBCTeam t) {
        for (Participant p : t.teamPlayers) {
            createLine(2, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getMultipliedTotalScore(), p);
        }
    }


    public void newObjective() {
        for (Participant p : MBC.getInstance().players) {
            newObjective(p);
        }
    }

    public void resetLine(Participant p, int line) {
        if (p.lines.containsKey(line)) {
            p.objective.getScoreboard().resetScores(p.lines.get(line));
        }
    }

    public List<MBCTeam> getValidTeams() {
        List<MBCTeam> newTeams = new ArrayList<>();
        for (int i = 0; i < MBC.teamNames.size(); i++) {
            if (!Objects.equals(MBC.getInstance().teams.get(i).fullName, "Spectator") && MBC.getInstance().teams.get(i).teamPlayers.size() > 0) {
                newTeams.add(MBC.getInstance().teams.get(i));
            }
        }

        return newTeams;
    }

    /**
     * Accessor for current game's state
     *
     * @return Enum GameState representing the current state of the game
     * @see GameState
     */
    public GameState getState() {
        return gameState;
    }

    /**
     * Mutator for current game's state
     *
     * @param gameState the state which the game will change to
     * @see GameState
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public void newObjective(Participant p) {
        if (p.objective != null) {
            p.objective.unregister();
        }

        p.gameObjective = gameName;
        Objective obj = p.board.registerNewObjective("Objective", "dummy", ChatColor.BOLD + "" + ChatColor.YELLOW + "MCC");
        p.lines = new HashMap<>();

        p.objective = obj;
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
}