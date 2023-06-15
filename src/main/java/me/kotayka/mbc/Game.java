package me.kotayka.mbc;

import me.kotayka.mbc.gamePlayers.GamePlayer;
import me.kotayka.mbc.games.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffectType;
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
        for (Participant p : MBC.getInstance().players) {
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
     * Does not handle void kills (death messages will not be descriptive).
     * Separate functions should be made to handle void kills:
     * and as such, Skybattle has a completely separate implementation.
     * Should work for any other PVP game not including the void.
     * Checks if one team remains, and adjusts timeRemaining if necessary.
     * Removes team from teamsAlive.
     * Does not handle scoring.
     * @param e Event thrown when a player dies
     */
    public void playerDeathEffects(PlayerDeathEvent e) {
        Participant victim = Participant.getParticipant(e.getPlayer());
        Participant killer = Participant.getParticipant(e.getPlayer().getKiller());
        String deathMessage = e.getDeathMessage();

        if (victim == null) return;

        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        MBC.spawnFirework(victim);
        deathMessage = deathMessage.replace(victim.getPlayerName(), victim.getFormattedName());

        if (killer != null) {
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
            deathMessage = deathMessage.replace(killer.getPlayerName(), killer.getFormattedName());
        }

        e.setDeathMessage(deathMessage);

        // Check if only one team remains
        updatePlayersAlive(victim);
    }

    /**
     * Variant of playerDeathEffects() but where the death message is displayed as:
     * {Player A} was slain by {Player B} [♥ Player 2 Health]
     * This is to resemble Minecraft Monday.
     */
    public void deathEffectsWithHealth(PlayerDeathEvent e) {
        Participant victim = Participant.getParticipant(e.getPlayer());
        Participant killer = Participant.getParticipant(e.getPlayer().getKiller());
        String deathMessage = e.getDeathMessage();

        if (victim == null) return;

        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        deathMessage = deathMessage.replace(victim.getPlayerName(), victim.getFormattedName());

        if (killer != null) {
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
            String health = String.format("["+ChatColor.RED+"♥ %.2f"+ChatColor.RESET+"]", killer.getPlayer().getHealth());
            deathMessage = deathMessage.replace(killer.getPlayerName(), killer.getFormattedName()+health);
        }

        e.setDeathMessage(deathMessage);

        // Check if only one team remains
        updatePlayersAlive(victim);
    }

    /**
     * Removes player from playersAlive list
     * WIP: likely needs more docs, this is kind of important
     * Optional: Additional restructure TBD
     * @param p Participant to be removed
     */
    public void updatePlayersAlive(Participant p) {
        playersAlive.remove(p);

        checkLastTeam(p.getTeam());
    }

    /**
     * If only one team remains,
     * @param t
     */
    private void checkLastTeam(Team t) {
        if (checkTeamEliminated(t)) {
            teamsAlive.remove(t);
            t.announceTeamDeath();

            if (teamsAlive.size() <= 1) {
                timeRemaining = 1;
            }
        }
    }

    public boolean checkIfDead(Participant p) {
        return !playersAlive.contains(p);
    }

    public boolean checkIfAlive(Participant p) {
        return playersAlive.contains(p);
    }

    /**
     * Makes use of teamsAlive and playersAlive lists.
     * @return true if the team has been fully eliminated (there are no players on that team alive).
     * @see Game checkIfDead
     */
    public boolean checkTeamEliminated(Team team) {
        int deadPlayers = 0;
        for (Participant p : team.teamPlayers) {
            if (checkIfDead(p)) {
                deadPlayers++;
            }
        }

        return deadPlayers == team.teamPlayers.size();
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
        for (Participant p : MBC.getInstance().players) {
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
            p.gameObjective = gameName;
            MBC.getInstance().currentGame.createScoreboard(p);
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

    public List<Team> getValidTeams() {
        List<Team> newTeams = new ArrayList<>();
        for (int i = 0; i < MBC.teamNames.size(); i++) {
            if (!Objects.equals(MBC.getInstance().teams.get(i).fullName, "Spectator") && MBC.getInstance().teams.get(i).teamPlayers.size() > 0) {
                newTeams.add(MBC.getInstance().teams.get(i));
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
            createLine(i,String.format("%-15s %s%5d", t.teamNameFormat(), ChatColor.WHITE, t.getRoundScore()));
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

    /**
     * Updates each player's scoreboards to match that of playersAlive and teamsAlive.
     * Does not do any computation and assumes playersAlive and teamsAlive has been properly
     * computed by the function call.
     *
     * Standardizes the following:
     *  Players Remaining is on line 2
     *  Teams Remaining is on line 1
     *
     *  Loops for all players by default.
     */
    public void updatePlayersAliveScoreboard() {
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS);
        createLine(1, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
    }

    /**
     * Updates each player's scoreboards to match that of playersAlive and teamsAlive.
     * Does not do any computation and assumes playersAlive and teamsAlive has been properly
     * computed by the function call.
     *
     * Standardizes the following:
     *  Players Remaining is on line 2
     *  Teams Remaining is on line 1
     *
     * @param p Specific participant to update scoreboard
     */
    public void updatePlayersAliveScoreboard(Participant p) {
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS, p);
        createLine(1, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS, p);
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
        if (MBC.getInstance().getGameID() != this.gameID) { return false; }

        // if lobby, return true no matter what
        if (MBC.getInstance().getGameID() == 0) { return true; }

        return (this.getState().equals(GameState.ACTIVE));
        // return (this.getState().equals(GameState.ACTIVE) || this.getState().equals(GameState.PAUSED)); <- might be dependent on event? gonna hold off
    }

    public void setTimer(int time) {
        timeRemaining = time;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
            if (!gameState.equals(GameState.OVERTIME)) {
                createLine(20, ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+getFormattedTime(--timeRemaining));
            } else {
                createLine(20, ChatColor.RED+""+ChatColor.BOLD+"Overtime: " +ChatColor.WHITE+getFormattedTime(--timeRemaining));
            }
            if (timeRemaining < 0) {
                stopTimer();
            }
            MBC.getInstance().currentGame.events();
        }, 20, 20);
    }

    public void stopTimer() {
        Bukkit.broadcastMessage("Stopping timer!");
        MBC.getInstance().cancelEvent(taskID);
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

    /**
     * General formatting function to get winners of a round and broadcast text.
     * Does not handle scoring.
     */
    public void roundWinners() {
        if (playersAlive.size() > 1) {
            StringBuilder survivors = new StringBuilder("The winners of this round are: ");
            for (int i = 0; i < playersAlive.size(); i++) {
                winEffects(playersAlive.get(i));
                playersAlive.get(i).getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");

                if (i == playersAlive.size()-1) {
                    survivors.append("and ").append(playersAlive.get(i).getFormattedName());
                } else {
                    survivors.append(playersAlive.get(i).getFormattedName()).append(", ");
                }
            }
            Bukkit.broadcastMessage(survivors.toString()+ChatColor.WHITE+"!");
        } else if (playersAlive.size() == 1) {
            playersAlive.get(0).getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
            winEffects(playersAlive.get(0));
            Bukkit.broadcastMessage("The winner of this round is " + playersAlive.get(0).getFormattedName()+"!");
        } else {
            Bukkit.broadcastMessage("Nobody survived the round.");
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
        HandlerList.unregisterAll(MBC.getInstance().lobby); // unregister lobby temporarily
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
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
                break;
            case 0:
                returnToLobby();
                break;
        }
    }

    /**
     * Prints out top 5 players (or more if there is a tie).
     * Calls addRoundScoreToGame() to load stats from this game to overall MBC.
     * @see Participant addRoundScoreToGame()
     */
    public void getScores() {
        int num = 0; // separate var incase there is an absurd amount of ties
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
                        (num) + ". %-18s %5d (%d x %.2f)\n", p.getFormattedName(), p.getRoundScore(), p.getUnmultipliedRoundScore(), MBC.getInstance().multiplier)
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

        for (int i = 0; i < gameScores.size(); i++) {
            str.append(String.format(
                    ChatColor.BOLD + "" + (i+1) + ChatColor.RESET + ". %-18s %5d\n",
                    gameScores.get(i).teamNameFormat(), gameScores.get(i).getScore())
            );
        }
        Bukkit.broadcastMessage(str.toString());
    }

    public void printRoundScores() {
        List<Team> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamRoundSorter());
        Collections.reverse(teamRoundsScores);
        StringBuilder teamString = new StringBuilder();

        for (int i = 0; i < teamRoundsScores.size(); i++) {
            teamString.append(String.format(
                    ChatColor.BOLD + "" + (i+1) + ChatColor.RESET + ". %-18s %5d\n",
                    teamRoundsScores.get(i).teamNameFormat(), teamRoundsScores.get(i).getRoundScore())
            );
        }

        Bukkit.broadcastMessage(teamString.toString());
    }

    public void returnToLobby() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        MBC.getInstance().gameID = 0;
        for (GamePlayer p : gamePlayers) {
            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
            updateTeamRoundScore(p.getParticipant().getTeam());
            updatePlayerRoundScore(p.getParticipant());
            p.getPlayer().teleport(Lobby.LOBBY);
            p.getParticipant().resetGameScores();
        }
        MBC.getInstance().lobby.start();
    }

    /**
     * Graphics for counting down when a game is about to start.
     * Should only be called when gameState() is GameState.STARTING
     * since it directly uses timeRemaining
     * Does not handle events for when timer hits 0 (countdown finishes).
     */
    public void startingCountdown() {
        for (Participant p : MBC.getInstance().getPlayers()) {
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
    public void winEffects(Participant p) {
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
