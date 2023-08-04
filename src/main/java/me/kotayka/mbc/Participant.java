package me.kotayka.mbc;

import me.kotayka.mbc.games.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class Participant {

    // Player's un-multiplied individual score; updates between games
    private int rawTotalScore = 0;
    private int multipliedTotalScore = 0;
    // Player's current score in game; used for display
    private int rawCurrentScore = 0;
    private int multipliedCurrentScore = 0;
    private MBCTeam team;
    private final Player player;

    //public final Scoreboard board = MBC.getInstance().manager.getNewScoreboard();
    public Objective objective;
    public String gameObjective;

    public HashMap<Integer, String> lines = new HashMap<>();

    // COMPARATORS
    public static final Comparator<Participant> rawTotalScoreComparator =
            Comparator.comparingInt(Participant::getRawTotalScore);

    public static final Comparator<Participant> multipliedCurrentScoreComparator =
            Comparator.comparingInt(Participant::getMultipliedCurrentScore);

    public Participant(Player p) {
        player=p;
        p.setScoreboard(MBC.getInstance().board);

        Bukkit.broadcastMessage("[Debug] assigning team");
        changeTeam(MBC.getInstance().spectator);
        MBC.getInstance().participants.add(this);
    }

    public void changeTeam(MBCTeam t) {
        if (t==null) {return;}
        if (team != null) {
            team.removePlayer(this);
        }

        addPlayerToTeamScoreboard(t);

        team = t;
        team.addPlayer(this);
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
    public int getMultipliedTotalScore() {
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
    public int getMultipliedCurrentScore() { return multipliedCurrentScore; }


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

        MBC.getInstance().getGame().updatePlayerCurrentScoreDisplay(this);
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
        return MBC.getInstance().players.contains(p);
    }

    public static boolean contains(Player p) {
        for (Participant x : MBC.getInstance().players) {
            if (Objects.equals(x.getPlayer().getName(), p.getName())) {
                return true;
            }
        }

        return false;
    }

    public static Participant getParticipant(Player p) {
        for (Participant x : MBC.getInstance().players) {
            if (Objects.equals(x.getPlayer().getName(), p.getName())) {
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

    public PlayerInventory getInventory() {
        return getPlayer().getInventory();
    }

    /**
     * Adds player to scoreboard team, and initializes the team if uninitialized.
     * @param t Team of Participant to be added to
     */
    public void addPlayerToTeamScoreboard(MBCTeam t) {
        if (t.scoreboardTeam == null) {
            t.scoreboardTeam = MBC.getInstance().board.registerNewTeam(t.fullName);
            t.scoreboardTeam.setColor(t.getChatColor());
            t.scoreboardTeam.setPrefix(String.format("%s%c ", ChatColor.WHITE, t.getIcon()));
            t.scoreboardTeam.setAllowFriendlyFire(false);
            t.scoreboardTeam.addPlayer(player);
        } else {
            t.scoreboardTeam.addPlayer(player);
        }
    }
}
