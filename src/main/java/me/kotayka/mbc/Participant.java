package me.kotayka.mbc;

import me.kotayka.mbc.games.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Objects;

public class Participant {

    private int unmultipliedScore = 0;
    private int score = 0;
    private int roundScore = 0;
    private int roundUnMultipliedScore = 0;
    private Team team;
    private final Player player;

    public final Scoreboard board = MBC.manager.getNewScoreboard();
    public Objective objective;
    public String gameObjective;

    public HashMap<Integer, String> lines = new HashMap<>();

    public Participant(Player p) {
        player=p;
        p.setScoreboard(board);
        changeTeam(MBC.spectator);
    }

    public void changeTeam(Team t) {
        if (t==null) {return;}
        if (team != null) {
            team.removePlayer(this);
        }
        team = t;
        team.addPlayer(this);
        Bukkit.broadcastMessage(getFormattedName()+ChatColor.WHITE+" has joined the "+team.getChatColor()+team.getTeamFullName());
        if (MBC.gameID == 0 && MBC.currentGame != null) {
            ((Lobby) MBC.currentGame).changeTeam(this);
        }
    }
    public Player getPlayer() {
        return player;
    }

    public int getUnMultipliedScore() {
        return unmultipliedScore;
    }

    /* Returns the multiplied score of a participant */
    public int getScore() {
        return score;
    }

    /**
     *
     * @return player's username
     */
    public String getPlayerName() {
        return getPlayer().getName();
    }

    /**
     * for string formatting
     * @return team icon + player's username with color
     */
    public String getFormattedName() {
        return (getTeam().getIcon() + " " + getTeam().getChatColor() + getPlayer().getName()) + ChatColor.WHITE;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public void addRoundScoreToGame() {
        addGameScore(getRoundScore());
        roundScore = 0;
    }

    public void addGameScore(int amount) {

        team.addGameScore(amount);
        unmultipliedScore += amount;
        score += amount*MBC.multiplier;

        MBC.currentGame.updatePlayerGameScore(this);
    }

    public void addRoundScore(int amount) {
        roundUnMultipliedScore += amount;
        roundScore += amount*MBC.multiplier;
        team.addRoundScore(amount);

        MBC.currentGame.updatePlayerRoundScore(this);
    }

    public Team getTeam() {
        return team;
    }

    public static boolean contains(Participant p) {
        return MBC.players.contains(p);
    }

    public static boolean contains(Player p) {
        for (Participant x : MBC.players) {
            if (Objects.equals(x.getPlayer(), p)) {
                return true;
            }
        }

        return false;
    }

    public static Participant getParticipant(Player p) {
        for (Participant x : MBC.players) {
            if (Objects.equals(x.getPlayer(), p)) {
                return x;
            }
        }

        return null;
    }

    public static Participant getParticipant(String p) {
        for (Participant x : MBC.players) {
            if (Objects.equals(x.getPlayer().getName(), p)) {
                return x;
            }
        }

        return null;
    }

    public PlayerInventory getInventory() {
        return getPlayer().getInventory();
    }
}
