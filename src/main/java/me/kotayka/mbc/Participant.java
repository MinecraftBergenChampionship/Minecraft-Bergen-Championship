package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TotalIndividualComparator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Participant {

    // Player's un-multiplied individual score; updates between games
    private int rawTotalScore = 0;
    private double multipliedTotalScore = 0;
    // Player's current score in game; used for display
    private int rawCurrentScore = 0;
    private double multipliedCurrentScore = 0;
    private MBCTeam team;
    private Player player;

    public Scoreboard board = MBC.getInstance().manager.getNewScoreboard();
    public Objective objective;
    public String gameObjective;

    public HashMap<Integer, String> lines = new HashMap<>();
    public int totalPlacement = -1;
    public boolean winner = false;

    // COMPARATORS
    public static final Comparator<Participant> rawTotalScoreComparator =
            Comparator.comparingInt(Participant::getRawTotalScore);

    public static final Comparator<Participant> multipliedCurrentScoreComparator =
            Comparator.comparingDouble(Participant::getMultipliedCurrentScore);

    public Participant(Player p) {
        player=p;
        p.setScoreboard(board);

        Bukkit.broadcastMessage("[Debug] assigning team");
        changeTeam(MBC.getInstance().spectator);
        MBC.getInstance().participants.add(this);
    }

    public void changeTeam(MBCTeam t) {
        if (t==null) {return;}
        if (team != null) {
            team.removePlayer(this);
        }

        team = t;
        team.addPlayer(this);

        setupScoreboardTeams();

        Bukkit.broadcastMessage(getFormattedName()+ChatColor.WHITE+" has joined the "+team.getChatColor()+team.getTeamFullName());
        if (MBC.getInstance().getMinigame() != null && MBC.getInstance().getMinigame() instanceof Lobby) {
            MBC.getInstance().lobby.changeTeam(this);
        }
    }

    public Player getPlayer() {
        return player;
    }

    /* Return unmultiplied total score */
    public int getRawTotalScore() {
        return rawTotalScore;
    }

    /* Returns the multiplied total score*/
    public double getMultipliedTotalScore() {
        return multipliedTotalScore;
    }

    /**
     * @return player's username
     */
    public String getPlayerName() {
        return getPlayer().getName();
    }

    /**
     * for string formatting; no hanging space
     * @return team icon + player's username with color
     */
    public String getFormattedName() {
        return (ChatColor.WHITE + "" + getTeam().getIcon() + " " + getTeam().getChatColor() + getPlayer().getName()) + ChatColor.WHITE;
    }

    public int getRawCurrentScore() {
        return rawCurrentScore;
    }
    public double getMultipliedCurrentScore() { return multipliedCurrentScore; }


    /**
     * Takes each current (game) scores and adds to Participant's stat totals.
     * Adds score to team, and resets the round variables for the next game.
     * @see MBCTeam addCurrentScoreToTotal()
     * @see Game gameEndEvents()
     */
    public void addCurrentScoreToTotal() {
        team.addCurrentScoreToTotal();
        rawTotalScore += rawCurrentScore;
        multipliedTotalScore += multipliedCurrentScore;
        resetCurrentScores();
    }

    public void addTotalScore(int amount) {
        team.addTotalScoreManual(amount);
        rawTotalScore += amount;
        multipliedTotalScore += amount;
    }

    /**
     * Called inbetween games to reset scores for each game to 0
     * Does not check whether or not game scores have been added to total event score.
     */
    public void resetCurrentScores() {
        multipliedCurrentScore = 0;
        rawCurrentScore = 0;
    }

    public void addCurrentScore(int amount) {
        rawCurrentScore += amount;
        multipliedCurrentScore += amount*MBC.getInstance().multiplier;
        team.addCurrentTeamScore(amount);

        if (!MBC.getInstance().finalGame) {
            MBC.getInstance().getGame().updatePlayerCurrentScoreDisplay(this);
        }
    }

    public void addCurrentScoreNoDisplay(int amount) {
        rawCurrentScore += amount;
        multipliedCurrentScore += amount*MBC.getInstance().multiplier;
        team.addCurrentTeamScore(amount);
    }

    public MBCTeam getTeam() {
        return team;
    }

    public static boolean contains(Participant p) {
        return MBC.getInstance().participants.contains(p);
    }

    public static boolean contains(Player p) {
        for (Participant x : MBC.getInstance().participants) {
            if (Objects.equals(x.getPlayer().getName(), p.getName())) {
                return true;
            }
        }

        return false;
    }

    public static Participant getParticipant(Player p) {
        if (p == null) return null;
        for (Participant x : MBC.getInstance().players) {
            if (Objects.equals(x.getPlayer().getUniqueId(), p.getUniqueId())) {
                return x;
            }
        }

        return null;
    }

    public static Participant getParticipant(String p) {
        for (Participant x : MBC.getInstance().players) {
            if (Objects.equals(x.getPlayer().getName(), p)) {
                return x;
            }
        }

        return null;
    }

    // using total unmultiplied placement
    public static List<Participant> getParticipant(int placement) {
        List<Participant> players = new ArrayList<>();
        MBC.getInstance().participants.sort(new TotalIndividualComparator());
        for (Participant p : MBC.getInstance().participants) {
            if (p.totalPlacement == placement) {
               players.add(p);
            }
        }
        return players;
    }

    public PlayerInventory getInventory() {
        return getPlayer().getInventory();
    }

    public void setPlacement(int placement) {
        this.totalPlacement = placement;
    }
    public int getPlacement() { return totalPlacement; }
    public void setPlayer(Player p) { player = p; } // used after relog
    public void setTotalScore(int amount) {
        rawTotalScore = amount;
        multipliedTotalScore = amount;
        team.resetTotalScores();
        team.addTotalScoreManual(amount);
    }

    /**
     * Adds player to own scoreboard
     * Add this new participant to everyone else's scoreboard
     */
    public void setupScoreboardTeams() {
        // add self to own scoreboard
        if (board.getTeam(team.fullName) == null) {
            Team thisScoreboardTeam = board.registerNewTeam(team.fullName);
            thisScoreboardTeam.setColor(team.getChatColor());
            thisScoreboardTeam.setPrefix(String.format("%s%c ", ChatColor.WHITE, team.getIcon()));
            thisScoreboardTeam.setAllowFriendlyFire(false);
            thisScoreboardTeam.addPlayer(player);
        } else {
            board.getTeam(team.fullName).addPlayer(player);
        }

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            // add everyone else to this player's scoreboard
            if (board.getTeam(p.getTeam().fullName) == null) {
                Team scoreboardTeam = board.registerNewTeam(p.getTeam().fullName);
                scoreboardTeam.setColor(p.getTeam().getChatColor());
                scoreboardTeam.setPrefix(String.format("%s%c ", ChatColor.WHITE, p.getTeam().getIcon()));
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.addPlayer(p.getPlayer());
            } else {
                board.getTeam(p.getTeam().fullName).addPlayer(p.getPlayer());
            }

            // adds this player to everyone else's teams
            if (p.board.getTeam(team.fullName) == null) {
                Team scoreboardTeam = p.board.registerNewTeam(team.fullName);
                scoreboardTeam.setColor(team.getChatColor());
                scoreboardTeam.setPrefix(String.format("%s%c ", ChatColor.WHITE, team.getIcon()));
                scoreboardTeam.setAllowFriendlyFire(false);
                scoreboardTeam.addPlayer(player);
            } else {
                // add player to team
                p.board.getTeam(team.fullName).addPlayer(player);
            }
        }
    }
}
