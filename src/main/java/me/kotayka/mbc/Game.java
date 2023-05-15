package me.kotayka.mbc;

import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

public abstract class Game implements Scoreboard, Listener {
    public int gameID;
    public String gameName;
    private GameState gameState = GameState.INACTIVE;

    public SortedMap<Participant, Integer> gameIndividual = new TreeMap<>(Collections.reverseOrder());

    public Game(int gameID, String gameName) {
        this.gameID = gameID;
        this.gameName = gameName;
    }

    public List<GamePlayer> gamePlayers = new ArrayList<GamePlayer>();
    public static int taskID = -1;

    public List<Participant> playersAlive = new ArrayList<>();
    public List<Team> teamsAlive = new ArrayList<>();

    public int timeRemaining;

    public void createScoreboard() {
        for (Participant p : MBC.players) {
            newObjective(p);
            createScoreboard(p);
        }
    }

    // added templates
    /**
     * Should load players into appropriate spots after/during introduction
     * Could/should handle stuff like: clearing inventory, applying potion effects, etc
     */
    public abstract void loadPlayers();

    /**
     * Provide game explanation
     */
    //public abstract void Introduction();


    public void addPlayerScore(Participant p, int score) {

    }

    public void addTeamScore(Participant p, int score) {

    }

    public void addTeamScore(int score) {

    }

    /**
     * Handles kill graphics for both killed player and killer
     * Removes killed player from playersAlive list
     * For use when there is no chance of a player falling into the void
     * @param e Event thrown when a player dies
     */
    public void playerDeathEffects(PlayerDeathEvent e) {
        Participant victim = null;
        Participant killer = null;
        String deathMessage = e.getDeathMessage();
        boolean voidDeath;

        for (Participant p : MBC.getIngamePlayer()) {
            if (p.getPlayerName().equals(e.getPlayer().getName())) {
                victim = p;
                playersAlive.remove(p);
                if (e.getPlayer().getKiller() == null) break;
            } else if (e.getPlayer().getKiller() != null) {
                // ensure that if player killed themselves killer stays null
                // do note this means if you kill yourself you will not get the kill graphic
                if (p.getPlayerName().equals(e.getPlayer().getKiller().getName())) {
                    killer = p;
                }
            }
        }

        // support for void deaths
        voidDeath = deathMessage.contains(victim.getPlayerName() + " died");

        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        deathMessage = deathMessage.replace(victim.getPlayerName(), victim.getFormattedName());

        if (killer != null) {
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
            if (voidDeath) {
                deathMessage = victim.getFormattedName() + " didn't want to live in the same world as " + killer.getFormattedName();
            } else {
                deathMessage = deathMessage.replace(killer.getPlayerName(), killer.getFormattedName());
            }
        } else {
            if (voidDeath) { // no killer but void death
                deathMessage = victim.getFormattedName() + " fell out of the world";
            }
        }

        e.setDeathMessage(deathMessage);
    }

    public boolean checkIfDead(Participant p) {
        return true;
    }

    public boolean checkIfAlive(Participant p) {
        return true;
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

    public void newObjective() {
        for (Participant p : MBC.players) {
            newObjective(p);
        }
    }

    public void resetLine(Participant p, int line) {
        if (p.lines.containsKey(line)) {
            p.objective.getScoreboard().resetScores(p.lines.get(line));
        }
    }

    public void createLine(int score, String line, Participant p) {
        if (p.objective == null || !Objects.equals(p.gameObjective, gameName)) {
            MBC.currentGame.createScoreboard(p);
            p.gameObjective = gameName;
        }

        resetLine(p, score);

        p.objective.getScore(line).setScore(score);
        p.lines.put(score, line);
    }

    public void createLine(int score, String line) {
        for (Participant p : MBC.players) {
            createLine(score, line, p);
        }
    }

    public List<Team> getValidTeams() {
        List<Team> newTeams = new ArrayList<>();
        for (int i = 0; i < MBC.teamNames.size(); i++) {
            if (!Objects.equals(MBC.teams.get(i).fullName, "Spectator") && MBC.teams.get(i).teamPlayers.size() > 0) {
                newTeams.add(MBC.teams.get(i));
            }
        }

        return newTeams;
    }

    /**
     * Sorts teams by their current round score to place onto scoreboard.
     */
    public void teamRounds() {
        List<Team> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamRoundSorter());

        for (int i = 14; i > 14-teamRoundsScores.size(); i--) {
            Team t = teamRoundsScores.get(14-i);
            createLine(i,String.format("%c %s%s %s%5d", t.getIcon(), t.getChatColor(), t.getTeamFullName(), ChatColor.WHITE, t.getRoundScore()));
        }
    }

    public void teamGames() {
        List<Team> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamScoreSorter());
        Collections.reverse(teamRoundsScores);

        for (int i = 14; i > 14-teamRoundsScores.size(); i--) {
            Team t = teamRoundsScores.get(14-i);
            createLine(i,String.format("%c %s%s %s%5d", t.getIcon(), t.getChatColor(), t.getTeamFullName(), ChatColor.WHITE, t.getScore()));
        }
    }

    public void updatePlayerRoundScore(Participant p) {
        createLine(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getRoundScore(), p);
    }

    public void updatePlayerGameScore(Participant p) {
        createLine(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getScore(), p);
    }

    public void updateTeamRoundScore(Team t) {
        for (Participant p : t.teamPlayers) {
            createLine(2, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getRoundScore(), p);
        }
        teamRounds();
    }

    public void updateTeamGameScore(Team t) {
        for (Participant p : t.teamPlayers) {
            createLine(2, ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE + t.getScore(), p);
        }
        teamGames();
    }

    public boolean isGameActive() {
        if (MBC.getGameID() != this.gameID) return false;

        return (this.getState().equals(GameState.ACTIVE));
    }

    public void setTimer(int time) {
        timeRemaining = time;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.plugin, () -> {
            createLine(20, ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+getFormattedTime(--timeRemaining));
            if (timeRemaining < 0) {
                MBC.cancelEvent(taskID);
            }
            MBC.currentGame.events();
        }, 20, 20);
    }

    public String getFormattedTime(int seconds) {
        return String.format("%02d", seconds/60) +":"+String.format("%02d", seconds%60);
    }

    public String getPlace(int place) {
        if (place > 10) {
            place = place % 10;
        }

        switch (place) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
            default:
                return place+"th";
        }
    }

    public ChatColor getPlacementColor(int place) {
        ChatColor placementColor;
        placementColor = switch (place) {
            case 1 -> ChatColor.GOLD;
            case 2 -> ChatColor.GRAY;
            case 3 -> ChatColor.getByChar("#CD7F32"); // idk if this works yet tbh
            default -> ChatColor.YELLOW;
        };
        return placementColor;
    }

    public void start() {
        loadPlayers();
        createScoreboard();
    }

    public abstract void events();

    /**
     * Called at the end of games
     * Handles formatting and reveals of team and individual scores
     *
     * NOTE: games with additional information, (ex: Ace Race and lap times)
     * must implement a separate function for those stats.
     */
    public void gameEndEvents() {
        // GAME_END should have 35 seconds by default
        switch(timeRemaining) {
            case 30:
                Bukkit.broadcastMessage(ChatColor.BOLD+"Each team scored this game:");
                break;
            case 28:
                printRoundScores();
                break;
            case 22:
                Bukkit.broadcastMessage(ChatColor.BOLD+"Top 5 players this game:");
                break;
            case 20:
                getScores();
                break;
            case 14:
                Bukkit.broadcastMessage(ChatColor.BOLD+"Current event standings:");
                break;
            case 12:
                printEventScores();
                break;
            case 1:
                Bukkit.broadcastMessage(ChatColor.RED + "Returning to lobby...");
            case 0:
                // TODO return to lobby
                break;
        }
    }

    /**
     * Prints out top 5 players (or more if there is a tie).
     * Calls addRoundScoreToGame() to load stats from this game to overall MBC.
     * @see Participant addRoundScoreToGame()
     */
    public void getScores() {
        int num = 1; // separate var incase there is an absurd amount of ties
        StringBuilder topFive = new StringBuilder();
        int lastScore = -1;
        int counter = 0;

        for (GamePlayer p : gamePlayers) {
            gameIndividual.put(p.getParticipant(), p.getParticipant().getUnmultipliedRoundScore());
        }

        for (Participant p : gameIndividual.keySet()) {
            if (p.getUnmultipliedRoundScore() != lastScore) {
                num++;
                if (counter == 4) {
                    counter--; // keep going until no ties
                }
            }

            if (counter < 5) {
                topFive.append(String.format(
                        (num) + ". %-18s %-5d (<%-4d x %.2f)\n", p.getFormattedName(), p.getRoundScore(), p.getUnmultipliedRoundScore(), MBC.multiplier)
                );
                lastScore = p.getUnMultipliedScore();
                counter++;
            }
            p.addRoundScoreToGame();
        }
        Bukkit.broadcastMessage(topFive.toString());
    }

    public void printEventScores() {
        List<Team> gameScores = new ArrayList<>(getValidTeams());
        gameScores.sort(new TeamScoreSorter());
        Collections.reverse(gameScores);
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            str.append(String.format(
                    ChatColor.BOLD + "" + (i+1) + ChatColor.RESET + ". <-%18s> <-%5d>\n",
                    gameScores.get(i).teamNameFormat(), gameScores.get(i).getScore())
            );
        }
    }

    public void printRoundScores() {
        List<Team> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamRoundSorter());
        Collections.reverse(teamRoundsScores);
        StringBuilder teamString = new StringBuilder();

        for (int i = 0; i < teamRoundsScores.size(); i++) {
            teamString.append(String.format(
                    ChatColor.BOLD + "" + (i+1) + ChatColor.RESET + ". %-18s %-5d\n",
                    teamRoundsScores.get(i).teamNameFormat(), teamRoundsScores.get(i).getRoundScore())
            );
        }

        Bukkit.broadcastMessage(teamString.toString());
    }

    /**
     * Graphics for counting down when a game is about to start.
     * Should only be called when gameState() is GameState.STARTING
     * since it directly uses timeRemaining
     * Does not handle events for when timer hits 0 (countdown finishes).
     */
    public void startingCountdown() {
        for (GamePlayer p : gamePlayers) {
            if (timeRemaining <= 10 && timeRemaining > 3) {
                p.getPlayer().sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+timeRemaining+"<", 0,20,0);
            } else if (timeRemaining == 3) {
                p.getPlayer().sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.RED+""+ChatColor.BOLD+ timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 2) {
                p.getPlayer().sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.YELLOW+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 1) {
                p.getPlayer().sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.GREEN+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            }
        }
    }

    /**
     * Apply win effects to player
     * Does NOT check if player won, logic must be implemented before call
     * @param p GamePlayer that has won a round/game.
     */
    public void winEffects(GamePlayer p) {
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
        p.getPlayer().setInvulnerable(true);
    }

    /**
     * Removes win effects from player that has won a round/game/is in god mode.
     * @param p GamePlayer that has won a round, game, or is invulnerable for some reason that needs to be reset.
     */
    public void removeWinEffect(GamePlayer p) {
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(false);
        p.getPlayer().setFlying(false);
        p.getPlayer().setInvulnerable(false);
    }

    /**
     * Display red text "Game Over!" and send chat message
     * Does not proceed to game scoring or call anything else.
     * (gonna see if this is used at all in the future before deleting)
     */
    public void gameOverGraphics() {
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!");
        for (GamePlayer p : gamePlayers) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!","",0, 60, 20);
        }
    }

    /**
     * Custom version of above function if other player-specific events are to be triggered
     * within loops not handled by raw forEach loop
     */
    public void gameOverGraphics(GamePlayer p) {
        p.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!");
        p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!","",0, 60, 20);
    }

    /**
     * Display red text "Round Over!"
     */
    public void roundOverGraphics() {
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!");
        for (GamePlayer p : gamePlayers) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!", "", 0, 60, 20);
        }
    }

    /**
     * Custom version of above function if other player-specific events are to be triggered
     * within loops not handled by raw forEach loop
     */
    public void roundOverGraphics(GamePlayer p) {
        p.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!");
        p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!","",0, 60, 20);
    }

    /**
     * Accessor for current game's state
     * @see GameState
     * @return Enum GameState representing the current state of the game
     */
    public GameState getState() {
        return gameState;
    }

    /**
     * Mutator for current game's state
     * @see GameState
     * @param gameState the state which the game will change to
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}
