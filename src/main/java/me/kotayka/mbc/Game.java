package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @see Minigame for non-point scoring games
 */
public abstract class Game extends Minigame {
    // TRUE = pvp is on ; FALSE = pvp is off
    private boolean PVP_ENABLED = false;

    // TODO: refactor to use auto sorting structures to remove redundancy
    public List<Participant> gameIndividual = new ArrayList<>(MBC.getInstance().getPlayers().size());
    public Map<Participant, Integer> individual = new HashMap<>();

    public List<MBCTeam> teamScores = new ArrayList<>(MBC.getInstance().getValidTeams().size());
    public Map<MBCTeam, Double> scoreMap = new HashMap<>();
    public List<Participant> playersAlive = new ArrayList<>();
    public List<MBCTeam> teamsAlive = new ArrayList<>();
    // Used to signal whether or not there has been a disconnect when moving from any state -> starting; if so, pause
    public boolean disconnect = false;
    protected final String[] INTRODUCTION;
    protected int introLine = 0;

    // NOTE: although it may be better for each Game class to have an icon,
    // I'm not doing this currently to avoid future issues of postponing game development
    // due to a lack of icons, or similar issues.
    public Game(String gameName, String[] INTRO) {
        super(gameName);
        this.INTRODUCTION= INTRO;
        initLogger();
    }

    public void createScoreboard() {
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            newObjective(p);
            createScoreboard(p);
        }
    }

    /**
     * Print game explanation, one line at a time.
     *
     * @modifies introLine
     */
    public void Introduction() {
        if (INTRODUCTION == null || introLine > INTRODUCTION.length) return;

        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(INTRODUCTION[introLine++]);
        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(ChatColor.GREEN + "---------------------------------------");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    /**
     * Returns a string detailing how the game is scored.
     * @requires Scoring string is the last string of an introduction.
     * @return String of how the game will be scored.
     */
    public String getScoring() {
        return INTRODUCTION[INTRODUCTION.length-1];
    }

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

    public void playerDeathEffectsDisconnect(Participant victim, @Nullable Participant killer) {
        String deathMessage = victim.getFormattedName() + " disconnected!";

        if (killer != null){
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed "+victim.getPlayerName()+"!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
        }
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
     * @modifies timeRemaining
     * @param t Team to check whether any players remain.
     */
    public void checkLastTeam(MBCTeam t) {
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
     *  Players & Teams Remaining on line 3
     *
     *  Loops for all players by default.
     */
    public void updatePlayersAliveScoreboard() {
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players: " + ChatColor.RESET+playersAlive.size() + "/"+MBC.getInstance().getPlayers().size() + " | " +
                                    ChatColor.GREEN + "" + ChatColor.BOLD+"Teams: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
        /*
        createLineAll(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.getInstance().getPlayers().size());
        createLineAll(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS);
         */
    }

    /**
     * Updates the player's current coin count in-game on the scoreboard
     * Your Coins: {COIN_AMOUNT}
     * @param p Participant whose scoreboard to update
     */
    public void updatePlayerCurrentScoreDisplay(Participant p) {
        if (MBC.getInstance().finalGame) { finalGamePlayerCurrentScoreDisplay(p); return; }
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getMultipliedCurrentScore(), p);
    }

    /**
     * Updates each player's scoreboards to match that of playersAlive and teamsAlive.
     * Does not do any computation and assumes playersAlive and teamsAlive has been properly
     * computed by the function call.
     *
     * Standardizes the following:
     *  Players & Teams Remaining on line 3
     *
     * @param p Specific participant to update scoreboard
     */
    public void updatePlayersAliveScoreboard(Participant p) {
        createLine(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players: " + ChatColor.RESET+playersAlive.size() + "/"+MBC.getInstance().getPlayers().size() + " | " +
                ChatColor.GREEN + "" + ChatColor.BOLD+"Teams: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS, p);
        /*
        createLine(3, ChatColor.GREEN+""+ChatColor.BOLD+"Players Remaining: " + ChatColor.RESET+playersAlive.size()+"/"+MBC.MAX_PLAYERS, p);
        createLine(2, ChatColor.GREEN+""+ChatColor.BOLD+"Teams Remaining: " + ChatColor.RESET+teamsAlive.size()+"/"+MBC.MAX_TEAMS, p);
         */
    }


    public void finalGamePlayerCurrentScoreDisplay(Participant p) {
        createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"???", p);
    }

    public void finalGameScoreDisplay() {
        teamScores.sort(new TeamRoundSorter());
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {

            for (int i = 14; i > 14-teamScores.size(); i--) {
                MBCTeam t = teamScores.get(14-i);
                createLine(i,String.format("%s: %.1f", t.teamNameFormat(), t.getMultipliedCurrentScore()), p);
            }

            createLine(0, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.getMultipliedCurrentScore(), p);
            p.getPlayer().playSound(p.getPlayer(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 2);
        }
    }

    public void snowballHit(Snowball proj, Player p) {
        Vector snowballVelocity = proj.getVelocity();
        p.damage(0.1);
        p.setVelocity(new Vector(snowballVelocity.getX() * 0.1, 0.18, snowballVelocity.getZ() * 0.1));
        proj.remove();
    }

    public static String getPlace(int place) {
        if (place > 10 && place < 21) {
            return (place + "th");
        }
        return switch (place) {
            case 1 -> place + "st";
            case 2 -> place + "nd";
            case 3 -> place + "rd";
            default -> place + "th";
        };
    }

    /**
     * Sorts teams by their current round score to place onto scoreboard.
     */
    public void updateInGameTeamScoreboard() {
        if (MBC.getInstance().finalGame) { finalGameScoreboard(); return; }

        List<MBCTeam> teamRoundsScores = getValidTeams();
        teamRoundsScores.sort(new TeamRoundSorter());

        for (int i = 14; i > 14-teamRoundsScores.size(); i--) {
            MBCTeam t = teamRoundsScores.get(14-i);
            createLineAll(i,String.format("%s: %.1f", t.teamNameFormat(), t.getMultipliedCurrentScore()));
        }
    }

    /**
     * General formatting function to get winners of a round and broadcast text.
     * Adds scoring if specified.
     * @param winPoints Amount of win points to give. Specify 0 if none.
     * @param survivalPoints Amount of survival points to give, which are awarded based on teammate survival. Specify 0 if none or N/A.
     */
    public void roundWinners(int winPoints, int survivalPoints) {
        String s;
        if (playersAlive.size() > 1) {
            StringBuilder survivors = new StringBuilder("The winners of this round are: ");
            for (int i = 0; i < playersAlive.size(); i++) {
                Participant p = playersAlive.get(i);
                winEffects(p);
                if (winPoints > 0) {
                    p.getPlayer().sendMessage(ChatColor.GREEN + "You survived the round!" + MBC.scoreFormatter(winPoints));
                    p.addCurrentScore(winPoints);
                } else {
                    p.getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
                }

                // check number of alive teammates
                int count = 0;
                for (Participant teammate : p.getTeam().getPlayers()) {
                    if (teammate.getPlayer().getGameMode() != GameMode.SPECTATOR)
                        count++;
                }

                if (count > 1) {
                    p.getPlayer().sendMessage(ChatColor.GREEN + "You've earned bonus survival points from your teammates!" + MBC.scoreFormatter(survivalPoints * (count-1)));
                    p.addCurrentScore(survivalPoints * (count-1));
                }

                if (i == playersAlive.size()-1) {
                    survivors.append("and ").append(p.getFormattedName());
                } else {
                    survivors.append(p.getFormattedName()).append(", ");
                }
            }
            s = survivors.toString()+ChatColor.WHITE+"!";
        } else if (playersAlive.size() == 1) {
            if (winPoints > 0) {
                playersAlive.getFirst().getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!" + MBC.scoreFormatter(winPoints));
                playersAlive.getFirst().addCurrentScore(winPoints);
            } else {
                playersAlive.getFirst().getPlayer().sendMessage(ChatColor.GREEN+"You survived the round!");
            }
            winEffects(playersAlive.getFirst());
            s = "The winner of this round is " + playersAlive.getFirst().getFormattedName()+"!";
        } else {
            s = "Nobody survived the round.";
        }
        logger.log(s+"\n");
        Bukkit.broadcastMessage(s);
    }

    public static String getColorStringFromPlacement(int place) {
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
       stopTimer();

       // standards
       for (Participant p : MBC.getInstance().getPlayers()) {
           p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
           p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
           p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
           p.getPlayer().setGameMode(GameMode.ADVENTURE);
           p.getPlayer().setLevel(0);
           p.getPlayer().setExp(0);
           //MBC.getInstance().individual.add(p);

           // for intro
           p.getPlayer().addPotionEffect(MBC.SATURATION);
           p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 180, 255, true, false));
       }


       loadPlayers();
       createScoreboard();
       createLineAll(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, name()));
       createLineAll(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET));
   }

   /**
    * Called at the end of games
    * Handles formatting and reveals of team and individual scores
    * Redirects if last game
    *
    * NOTE: games with additional information, (ex: Ace Race and lap times)
    * must implement a separate function for those stats.
    */
    public void gameEndEvents() {
        if (MBC.getInstance().finalGame) {
            finalGameEndEvents();
            return;
        }
        // GAME_END should have ~35 seconds by default
        switch (timeRemaining) {
            case 30 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Each team scored this game:");
                TO_PRINT = printRoundScores();
            }
            case 22 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Top 5 players this game:");
                TO_PRINT = getScores();
            }
            case 18 -> MBC.getInstance().updatePlacings();
            case 14 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Current event standings:");
                TO_PRINT = printEventScores();
            }
            case 2 -> Bukkit.broadcastMessage(ChatColor.RED + "Returning to lobby...");
            case 28, 20, 12 -> Bukkit.broadcastMessage(TO_PRINT);
            case 0 -> {
                logger.logStats();
                returnToLobby();
            }
        }
    }

    /**
     * Called at the end of team games-skips print out individual scores
     * Should have at least 25 seconds when switching to GAME_END
     */
    public void teamGameEndEvents() {
        if (MBC.getInstance().finalGame) {
            finalGameEndEvents();
            return;
        }
        // Team Games should leave at least 25 seconds for GAME_END
        switch (timeRemaining) {
            case 20 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD + "Each team scored this game:");
                TO_PRINT = printRoundScores();
            }
            case 16 -> getIndivScoresNoPrint();
            case 14 -> {
                Bukkit.broadcastMessage(ChatColor.BOLD+"Current event standings:");
                TO_PRINT = printEventScores();
            }
            case 11 -> MBC.getInstance().updatePlacings();
            case 18, 12 -> Bukkit.broadcastMessage(TO_PRINT);
            case 2 -> Bukkit.broadcastMessage(ChatColor.RED+"Returning to lobby...");
            case 0 -> {
                logger.logStats();
                returnToLobby();
            }
        }
    }

    public void finalGameEndEvents() {
        if (timeRemaining > 12) {
            timeRemaining = 12;
        }

        switch (timeRemaining) {
            case 10 -> {
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX +  ChatColor.BOLD + "Skipping score report for last game!");
                getTeamScoresNoPrint();
                getIndivScoresNoPrint();
            }
            case 8 -> Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.BOLD+"Check with admins or <implemented command> to find your score post-reveal!");
            case 5 -> getIndivScoresNoPrint();
            case 2 -> Bukkit.broadcastMessage(ChatColor.RED + "Preparing finale...");
            case 3 -> MBC.getInstance().updatePlacings();
            case 0 -> {
                logger.logStats();
                returnToLobby();
            }
        }
    }

    /**
     * Prints out top 5 players (or more if there is a tie).
     * Calls addRoundScoreToGame() to load stats from this game to overall MBC.
     * @see Participant addRoundScoreToGame()
     */
    public String getScores() {
        StringBuilder topFive = new StringBuilder();
        int num = 0;
        int lastScore = -1;
        int ties = 0;

        gameIndividual.addAll(MBC.getInstance().getPlayers());
        gameIndividual.sort(Participant.multipliedCurrentScoreComparator);
        Collections.reverse(gameIndividual);

        for (Participant p : gameIndividual) {
            if (ties > 0 && p.getRawCurrentScore() != lastScore) {
                num+=ties+1;
                ties = 0;
            } else if (p.getRawCurrentScore() == lastScore) {
                ties++;
            } else {
                num++;
            }

            String score = String.format(
                    (num) + ". %s: %.1f (%d x %.1f)\n", p.getFormattedName(), p.getMultipliedCurrentScore(), p.getRawCurrentScore(), MBC.getInstance().multiplier
            );
            logger.logIndividual(score);
            individual.put(p, p.getRawCurrentScore());
            if (num <= 5) {
                topFive.append(score);
            }
            lastScore = p.getRawCurrentScore();
            p.addCurrentScoreToTotal();
        }
        return topFive.toString();
    }

    public String printEventScores() {
        // each player's scores are added to event total in
        // getScores() by calling p.addCurrentScoreToTotal()
        List<MBCTeam> gameScores = new ArrayList<>(getValidTeams());
        gameScores.sort(new TeamScoreSorter());
        Collections.reverse(gameScores);
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < gameScores.size(); i++) {
            MBCTeam t = gameScores.get(i);
            str.append(ChatColor.BOLD+""+(i+1)+". ")
               .append(String.format(
                   "%s: %.1f\n", t.teamNameFormat(), t.getMultipliedTotalScore()
            ));
        }
        return str.toString();
    }

    public String printRoundScores() {
        List<MBCTeam> gameScores = new ArrayList<>(getValidTeams());
        gameScores.sort(new TeamRoundSorter());
        StringBuilder teamString = new StringBuilder();

        for (int i = 0; i < gameScores.size(); i++) {
            MBCTeam t = gameScores.get(i);
            String str = ChatColor.BOLD+""+(i+1)+". "+String.format("%s: %.1f\n", t.teamNameFormat(), t.getMultipliedCurrentScore());
            teamString.append(ChatColor.BOLD+""+(i+1)+". ").append(String.format(
                    "%s: %.1f\n", t.teamNameFormat(), t.getMultipliedCurrentScore()
            ));
            logger.logTeamScores(str);
            scoreMap.put(t, t.getMultipliedCurrentScore());
        }
        return teamString.toString();
    }

    public void getIndivScoresNoPrint() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            String score = String.format(
                    "%s: %.1f (%d x %.1f)\n", p.getFormattedName(), p.getMultipliedCurrentScore(), p.getRawCurrentScore(), MBC.getInstance().multiplier
            );
            logger.logIndividual(score);

            p.addCurrentScoreToTotal();
        }
    }

    public void getTeamScoresNoPrint() {
        List<MBCTeam> gameScores = new ArrayList<>(getValidTeams());
        gameScores.sort(new TeamRoundSorter());
        StringBuilder teamString = new StringBuilder();

        for (int i = 0; i < gameScores.size(); i++) {
            MBCTeam t = gameScores.get(i);
            String str = ChatColor.BOLD+""+(i+1)+". "+String.format("%s: %.1f\n", t.teamNameFormat(), t.getMultipliedCurrentScore());
            teamString.append(ChatColor.BOLD+""+(i+1)+". ").append(String.format(
                    "%s: %.1f\n", t.teamNameFormat(), t.getMultipliedCurrentScore()
            ));
            logger.logTeamScores(str);
            scoreMap.put(t, t.getMultipliedCurrentScore());
        }
    }

    public void returnToLobby() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        stopTimer();
        MBC.getInstance().showAllPlayers();
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayers()) {
            for (Participant p2 : MBC.getInstance().getPlayers()) {
                p2.getPlayer().showPlayer(p.getPlayer());
            }
            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);

            p.resetCurrentScores();
        }

        MBC.getInstance().gameNum++;

        if (MBC.getInstance().finalGame) {
            MBC.getInstance().lobby.prepareScoreReveal();
        } else {
            // if 2nd to last game just ended
            if (MBC.getInstance().gameNum == MBC.GAME_COUNT) {
                MBC.getInstance().finalGame = true;
            }
            MBC.getInstance().lobby.start();
            MBC.getInstance().lobby.populatePodium();
        }
    }

    /**
     * Apply win effects to player
     * Does NOT check if player won, logic must be implemented before call
     * @param p Participant that has won a round/game.
     */
    public void winEffects(Participant p) {
        p.getPlayer().setInvulnerable(true);
        MBC.spawnFirework(p);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
    }

    /**
     * Emulate win effects (flight, invulnerability, etc)
     * without the firework; example usage by not completing
     * Ace Race or a TGTTOS map.
     * @param p Participant to give effects.
     */
    public void flightEffects(Participant p) {
        p.getPlayer().setInvulnerable(true);
        p.getPlayer().setGameMode(GameMode.ADVENTURE);
        p.getPlayer().setAllowFlight(true);
        p.getPlayer().setFlying(true);
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
        logger.log(ChatColor.BOLD+""+ChatColor.RED+"Game Over!");
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!");
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Game Over!","",0, 60, 20);
        }
    }

    /**
     * Display red text "Round Over!"
     */
    public void roundOverGraphics() {
        logger.log(ChatColor.BOLD+""+ChatColor.RED+"Round Over!");
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
        //String s = b ? "[Debug] PVP now enabled. " : "[Debug] PVP now disabled.";
        //Bukkit.broadcastMessage(s);
        PVP_ENABLED = b;
    }

    /**
     * @implNote This is called when a game needs to restart. This should reset variables such as
     * roundNum, maps, and reset any gamePlayers, such that when the game starts again, the instance
     * variables are properly initialized
     */
    public abstract void onRestart();

    public StatLogger getLogger() {
        return logger;
    }

    public boolean PVP() { return PVP_ENABLED; }
}
