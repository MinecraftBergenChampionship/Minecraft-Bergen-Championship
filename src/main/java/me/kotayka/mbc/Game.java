package me.kotayka.mbc;

import me.kotayka.mbc.gamePlayers.GamePlayer;
import me.kotayka.mbc.games.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * @see Minigame for non-point scoring games
 */
public abstract class Game extends Minigame {
    // TRUE = pvp is on ; FALSE = pvp is off
    private boolean PVP_ENABLED = false;

    public List<Participant> gameIndividual = new ArrayList<Participant>(MBC.getInstance().getPlayers().size());

    public Game(String gameName) {
        super(gameName);
    }

    public List<Participant> playersAlive = new ArrayList<>();
    public List<MBCTeam> teamsAlive = new ArrayList<>();

    // Used to signal whether or not there has been a disconnect when moving from any state -> starting; if so, pause
    public boolean disconnect = false;


    public void createScoreboard() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            newObjective(p);
            createScoreboard(p);
        }
    }


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
        MBC.spawnFirework(victim);
        deathMessage = deathMessage.replace(victim.getPlayerName(), victim.getFormattedName());

        if (killer != null) {
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
            String health = String.format(" ["+ChatColor.RED+"♥ %.2f"+ChatColor.RESET+"]", killer.getPlayer().getHealth());
            deathMessage = deathMessage.replace(killer.getPlayerName(), killer.getFormattedName()+health);
        }

        e.setDeathMessage(deathMessage);

        // Check if only one team remains
        updatePlayersAlive(victim);
    }

    /**
     * Removes player from playersAlive list
     * Updates display for players alive
     * Checks if last team is remaining
     * @param p Participant to be removed
     */
    public void updatePlayersAlive(Participant p) {
        playersAlive.remove(p);
        checkLastTeam(p.getTeam());
        updatePlayersAliveScoreboard();
    }

    /**
     * If only one team remains, "cancel" game by setting timeRemaining to 1
     * @param t Team to check whether any players remain.
     */
    private void checkLastTeam(MBCTeam t) {
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
     * Resets both playersAlive and teamsAlive;
     * Best used for multi-round survival-type games
     * Might need a better name
     */
    public void resetAliveLists() {
        playersAlive.clear();
        teamsAlive.clear();

        playersAlive.addAll(MBC.getInstance().getPlayers());
        teamsAlive.addAll(getValidTeams());
    }

    /**
     * Makes use of teamsAlive and playersAlive lists.
     * @return true if the team has been fully eliminated (there are no players on that team alive).
     * @see Game checkIfDead
     */
    public boolean checkTeamEliminated(MBCTeam team) {
        int deadPlayers = 0;
        for (Participant p : team.teamPlayers) {
            if (checkIfDead(p)) {
                deadPlayers++;
            }
        }

        return deadPlayers == team.teamPlayers.size();
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
        createLine(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS);
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
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
        createLine(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS, p);
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS, p);
    }

    /**
     * Updates the player's current coin count in-game on the scoreboard
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    public void updatePlayerCurrentScoreDisplay(Participant p) {
        Bukkit.broadcastMessage("CurrentScore: " + p.getMultipliedCurrentScore() + " for " + p.getPlayerName());
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getMultipliedCurrentScore(), p);
    }

    public String getPlace(int place) {
        int temp = place;
        if (temp > 10) {
            temp %= 10;
        }

        switch (temp) {
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
     * Adds scoring if specified.
     * @param points Amount of points to give. Specify 0 for no points.
     */
    public void roundWinners(int points) {
        if (playersAlive.size() > 1) {
            StringBuilder survivors = new StringBuilder("The winners of this round are: ");
            for (int i = 0; i < playersAlive.size(); i++) {
                Participant p = playersAlive.get(i);
                winEffects(p);
                p.getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
                if (points > 0) {
                    p.addCurrentScore(points);
                }

                if (i == playersAlive.size()-1) {
                    survivors.append("and ").append(p.getFormattedName());
                } else {
                    survivors.append(p.getFormattedName()).append(", ");
                }
            }
            Bukkit.broadcastMessage(survivors.toString()+ChatColor.WHITE+"!");
        } else if (playersAlive.size() == 1) {
            playersAlive.get(0).getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
            playersAlive.get(0).addCurrentScore(points);
            winEffects(playersAlive.get(0));
            Bukkit.broadcastMessage("The winner of this round is " + playersAlive.get(0).getFormattedName()+"!");
        } else {
            Bukkit.broadcastMessage("Nobody survived the round.");
        }
    }

    public String getColorStringFromPlacement(int place) {
        String colorStr = switch (place) {
            case 1 -> ChatColor.GOLD.toString();
            case 2 -> ChatColor.GRAY.toString();
            case 3 -> ChatColor.DARK_RED.toString();
            case 4, 5 -> ChatColor.RED.toString();
            default -> ChatColor.YELLOW.toString();
        };
        if (place < 9) { colorStr = colorStr + ChatColor.BOLD; }
        return colorStr;
    }

   public void start() {
       // unregister both lobby and decision dome to prepare for games
       HandlerList.unregisterAll(MBC.getInstance().lobby);
       HandlerList.unregisterAll(MBC.getInstance().decisionDome);
       MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);

       // if timer hasn't reached 1, stop it
       if (timeRemaining > 0) {
           stopTimer();
       }

       // standards
       for (Participant p : MBC.getInstance().getPlayers()) {
           p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
           p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
           p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
           p.getPlayer().setGameMode(GameMode.ADVENTURE);
           p.getPlayer().setLevel(0);
           p.getPlayer().setExp(0);
       }

       loadPlayers();
       createScoreboard();
       createLine(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, gameName));
       createLine(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET));
   }

   /**
    * Called at the end of games
    * Handles formatting and reveals of team and individual scores
    *
    * NOTE: games with additional information, (ex: Ace Race and lap times)
    * must implement a separate function for those stats.
    */
    public void gameEndEvents() {
        // GAME_END should have 35 seconds by default
        switch (timeRemaining) {
            case 30 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Each team scored this game:");
                TO_PRINT = printRoundScores();
            }
            case 22 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Top 5 players this game:");
                TO_PRINT = getScores();
            }
            case 14 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Current event standings:");
                TO_PRINT = printEventScores();
            }
            case 1 -> Bukkit.broadcastMessage(ChatColor.RED + "Returning to lobby...");
            case 28, 20, 12 -> Bukkit.broadcastMessage(TO_PRINT);
            case 0 -> returnToLobby();
        }
    }

    /**
     * Called at the end of team games-skips print out individual scores
     * Should have at least 25 seconds when switching to GAME_END
     */
    public void teamGameEndEvents() {
        // Team Games should leave at least 25 seconds for GAME_END
        switch (timeRemaining) {
            case 20 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Each team scored this game:");
                TO_PRINT = printRoundScores();
            }
            case 16 -> getScoresNoPrint();
            case 14 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD+"Current event standings:");
                TO_PRINT = printEventScores();
            }
            case 1 -> Bukkit.broadcastMessage(ChatColor.RED+"Returning to lobby...");
            case 18, 12 -> Bukkit.broadcastMessage(TO_PRINT);
            case 0 -> returnToLobby();
        }
    }

    /**
     * Prints out top 5 players (or more if there is a tie).
     * Calls addRoundScoreToGame() to load stats from this game to overall MBC.
     * @see Participant addRoundScoreToGame()
     */
    public String getScores() {
        int num = 0; // separate var incase there is an absurd amount of ties
        StringBuilder topFive = new StringBuilder();
        int lastScore = -1;
        int counter = 0;

        gameIndividual.addAll(MBC.getInstance().getPlayers());
        gameIndividual.sort(Participant.multipliedCurrentScoreComparator);
        Collections.reverse(gameIndividual);

        for (Participant p : gameIndividual) {
            Bukkit.broadcastMessage("[Debug] p == " + p);
            if (p.getRawCurrentScore() != lastScore) {
                num++;
                if (counter == 4) {
                    counter--; // keep going until no ties
                }
            }

            if (counter < 5) {
                topFive.append(String.format(
                        (num) + ". %s: %d (%d x %.1f)\n", p.getFormattedName(), p.getMultipliedCurrentScore(), p.getRawCurrentScore(), MBC.getInstance().multiplier)
                );
                lastScore = p.getRawCurrentScore();
                counter++;
                Bukkit.broadcastMessage("[Debug] counter == " + counter);
            }
            p.addCurrentScoreToTotal();
        }
        return topFive.toString();
    }

    public String printEventScores() {
        // each player's scores are added to event total in
        // getScores() by calling p.addCurrentScoreToTotal()
        List<MBCTeam> gameScores = new ArrayList<>(getValidTeams());
        gameScores.sort(new TeamScoreSorter());
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < gameScores.size(); i++) {
            MBCTeam t = gameScores.get(i);
            str.append(ChatColor.BOLD+""+(i+1)+". ")
               .append(String.format(
                   "%s: %d\n", t.teamNameFormat(), t.getMultipliedTotalScore()
            ));
        }
        return str.toString();
    }

    public String printRoundScores() {
        List<MBCTeam> teamRoundsScores = new ArrayList<>(getValidTeams());
        teamRoundsScores.sort(new TeamRoundSorter());
        StringBuilder teamString = new StringBuilder();

        for (int i = 0; i < teamRoundsScores.size(); i++) {
            MBCTeam t = teamRoundsScores.get(i);
            teamString.append(ChatColor.BOLD+""+(i+1)+". ")
                .append(String.format(
                    "%s: %d\n", t.teamNameFormat(), t.getMultipliedCurrentScore()
            ));
        }
        return teamString.toString();
    }

    public void getScoresNoPrint() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.addCurrentScoreToTotal();
        }
    }

    public void returnToLobby() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayers()) {
            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);

            updatePlayerCurrentScoreDisplay(p);
            p.resetCurrentScores();
            p.getPlayer().teleport(Lobby.LOBBY);
        }
        if (MBC.getInstance().decisionDome == null) {
            Bukkit.broadcastMessage("[Debug] Decision Dome has not been loaded, you may need to start another game manually or reload!");
        }

        MBC.getInstance().gameNum++;
        MBC.getInstance().lobby.start();
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
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+timeRemaining+"<", 0,20,0);
            } else if (timeRemaining == 3) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.RED+""+ChatColor.BOLD+ timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 2) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.YELLOW+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 1) {
                p.sendTitle(ChatColor.AQUA + "Starting in:", ChatColor.BOLD + ">"+ChatColor.GREEN+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            }
        }
    }

    /**
     * Apply win effects to player
     * Does NOT check if player won, logic must be implemented before call
     * @param p Participant that has won a round/game.
     */
    public void winEffects(Participant p) {
        MBC.spawnFirework(p);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
        p.getPlayer().setInvulnerable(true);
    }

    /**
     * Emulate win effects (flight, invulnerability, etc)
     * without the firework; example usage by not completing
     * Ace Race or a TGTTOS map.
     * @param p Participant to give effects.
     */
    public void flightEffects(Participant p) {
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
        p.getPlayer().setInvulnerable(true);
    }

    /**
     * Removes win effects from player that has won a round/game/is in god mode.
     * @param p GamePlayer that has won a round, game, or is invulnerable for some reason that needs to be reset.
     */
    public void removeWinEffect(Participant p) {
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
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!","",0, 60, 20);
        }
    }

    /**
     * Display red text "Round Over!"
     */
    public void roundOverGraphics() {
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!");
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
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

    public void setPVP(boolean b) {
        String s = b ? "[Debug] PVP now enabled. " : "[Debug] PVP now disabled.";
        Bukkit.broadcastMessage(s);
        PVP_ENABLED = b;
    }

    /**
     * General rules for handling a disconnected player
     * @param p Participant
     */
    public void handleDisconnect(Participant p) {} // TODO

    public boolean PVP() { return PVP_ENABLED; }
}
