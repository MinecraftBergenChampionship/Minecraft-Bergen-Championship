package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.List;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Collections;
import java.util.HashMap;

/**
 * Template class representing a Minecraft minigame.
 * The class <b>Game</b> extends this class.
 *
 * As a standalone, this class is suitable for lobby-related games that do not have scoring mechanics.
 * Examples:
 *      Lobby
 *      Decision Dome / Voting gimmick
 *      Any hub minigame put in place, i.e. trick-or-treat/presents/milk the cow
 */
public abstract class Minigame implements Scoreboard, Listener {
    private final String NAME;
    private GameState gameState = GameState.INACTIVE;
    public int timeRemaining = -1;
    private int taskID = -1;
    public StatLogger logger;

    // GLOBAL STRING STORAGE FOR STORING STRINGS TO PRINT WHILE PERFORMING TASKS (ie sorting through game scores)
    protected String TO_PRINT = "";
    private List<String> mutedMessages = new LinkedList<String>();

    public Minigame(String name) {
        NAME = name;
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
        if (timeRemaining != -1) {
            stopTimer();
        }

        timeRemaining = time;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
            if (MBC.getInstance().getMinigame() instanceof Game) {
                if (!gameState.equals(GameState.OVERTIME)) {
                    createLineAll(20, ChatColor.RED + "" + ChatColor.BOLD + "Time left: " + ChatColor.WHITE + getFormattedTime(--timeRemaining));
                } else {
                    createLineAll(20, ChatColor.RED + "" + ChatColor.BOLD + "Overtime: " + ChatColor.WHITE + getFormattedTime(--timeRemaining));
                }
            } else {
                createLineAll(20, getFormattedTime(--timeRemaining));
            }
            if (timeRemaining < 0) {
                stopTimer();
            }
            MBC.getInstance().getMinigame().events();
        }, 20, 20);
    }

    public void initLogger() {
        logger = new StatLogger(this);
    }

    public StatLogger getLogger() {
        return logger;
    }

    public void Pause() {
        if (this instanceof Lobby && timeRemaining != -1) {
            Bukkit.broadcastMessage("Event Paused!");
            gameState = GameState.PAUSED;
            stopTimer();
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendTitle("PAUSED", "", 20, 60, 20);
            }
        }
        if (!(gameState == GameState.STARTING) || this instanceof DecisionDome || this instanceof Lobby) {
            // don't pause if game has started or minigame
            return;
        }

        Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + " Event has been paused!");
        gameState = GameState.PAUSED;
        stopTimer();
        createLineAll(20, "EVENT PAUSED");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle("PAUSED", "", 20, 60, 20);
        }
    }

    public void Unpause() {
        Bukkit.broadcastMessage("Starting!");
        if (timeRemaining < 5) timeRemaining = 5;
        gameState = GameState.STARTING;
        setTimer(timeRemaining);
    }

    public void stopTimer() {
        MBC.getInstance().cancelEvent(taskID);
    }

    public String getFormattedTime(int seconds) {
        return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    public void createScoreboard() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            newObjective(p);
            createScoreboard(p);
        }
    }

    /**
     * Graphics for counting down when a game is about to start.
     * Should only be called when gameState() is GameState.STARTING
     * since it directly uses timeRemaining
     * Does not handle events for when timer hits 0 (countdown finishes).
     */
    public void startingCountdown() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (timeRemaining <= 10 && timeRemaining > 3) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + timeRemaining + "<", 0, 20, 0);
            } else if (timeRemaining == 3) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.RED + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 2) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.YELLOW + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 1) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.GREEN + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            }
        }
    }

    /**
     * Graphics for counting down when a game is about to start.
     * Includes Sound s.
     * Should only be called when gameState() is GameState.STARTING
     * since it directly uses timeRemaining
     * Does not handle events for when timer hits 0 (countdown finishes).
     */
    public void startingCountdown(Sound s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (timeRemaining <= 10 && timeRemaining > 3) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + timeRemaining + "<", 0, 20, 0);
            } else if (timeRemaining == 3) {
                p.playSound(p, s, SoundCategory.RECORDS, 1, 1);
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.RED + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 2) {
                p.playSound(p, s, SoundCategory.RECORDS, 1, 1);
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.YELLOW + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 1) {
                p.playSound(p, s, SoundCategory.RECORDS, 1, 1);
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">" + ChatColor.GREEN + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            }
        }
    }

    public void createLine(int score, String line, Participant p) {
        if (p.objective == null || !Objects.equals(p.gameObjective, NAME)) {
            p.gameObjective = NAME;
            MBC.getInstance().getMinigame().newObjective(p);
            MBC.getInstance().getMinigame().createScoreboard(p);
        }

        resetLine(p, score);

        p.objective.getScore(line).setScore(score);
        p.lines.put(score, line);
    }

    public void createLineAll(int score, String line) {
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
            createLine(1, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getMultipliedCurrentScore(), p);
        }
    }


    public void finalGameScoreboard() {
        List<MBCTeam> teams = getValidTeams();
        teams.sort(new TeamRoundSorter());
        int i = 14;
        for (MBCTeam t : teams) {
            createLineAll(i, String.format("%s: ???", t.teamNameFormat()));
            i--;
        }
    }

    /**
     * Sorts teams by their overall score to place onto scoreboard during lobby/after games
     */
    public void updateTeamStandings() {
        if (this instanceof Lobby && getState().equals(GameState.END_ROUND)) return;
        List<MBCTeam> teams = getValidTeams();
        teams.sort(new TeamScoreSorter());
        Collections.reverse(teams);

        int place = 1;
        for (int i = 14; i > 14-teams.size(); i--) {
            MBCTeam t = teams.get(14-i);
            createLineAll(i,String.format("%s: %.1f", t.teamNameFormat(), t.getMultipliedTotalScore()));
            // ties determined by unmultiplied score
            t.setPlace(place++);
        }

        MBC.getInstance().lobby.colorPodiums();
    }

    /**
     * Displays team's total score in lobby
     * Team Coins: {COIN_AMOUNT}
     * Note: Since team scoreboard is always active in lobby, this may be redundant.
     * @param t Team whose coin count to display
     */
    protected void displayTeamTotalScore(MBCTeam t) {
        for (Participant p : t.teamPlayers) {
            createLine(1, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getMultipliedTotalScore(), p);
        }
    }

    public void newObjective() {
        for (Participant p : MBC.getInstance().players) {
            newObjective(p);
        }
    }

    /**
     * Removes a line from the scoreboard.
     *
     * @param p The Participant whose scoreboard should be changed
     * @param line Index of the line on the scoreboard to be reset.
     */
    public void resetLine(Participant p, int line) {
        if (p.lines.containsKey(line)) {
            p.objective.getScoreboard().resetScores(p.lines.get(line));
        }
    }

    public List<MBCTeam> getValidTeams() {
        return MBC.getInstance().getValidTeams();
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
        if (this.gameState == GameState.TUTORIAL && this instanceof Game) {
            if (((Game) this).disconnect) {
                this.gameState = GameState.STARTING;
                Pause();
                ((Game) this).disconnect = false;
            } else {
                this.gameState = gameState;
            }
        } else {
            this.gameState = gameState;
        }
    }

    public void newObjective(Participant p) {
        if (p.objective != null) {
            p.objective.unregister();
        }

        p.gameObjective = NAME;
        Objective obj;

        if (p.board.getObjective("Objective") == null) {
            obj = p.board.registerNewObjective("Objective", "dummy", ChatColor.YELLOW + "" + ChatColor.BOLD + "MBC");
        } else {
            obj = p.board.getObjective("Objective");
        }
        if (obj == null) {
            obj = p.board.registerNewObjective("Objective", "dummy", ChatColor.YELLOW + "" +ChatColor.BOLD +"MBC");
        }

        p.lines = new HashMap<>();

        p.objective = obj;
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Returns the name of the game represented by the instance of this class
     * @return NAME
     */
    public String name() {
        return NAME;
    }
}