package me.kotayka.mbc;

import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

public abstract class Game implements Scoreboard, Listener {

    public int gameID;
    public String gameName;

    public Game(int gameID, String gameName) {
        this.gameID = gameID;
        this.gameName = gameName;
    }

    int playersRemaining;
    int teamsRemaining;

    public List<GamePlayer> gamePlayers = new ArrayList<GamePlayer>();
    public static int taskID = -1;

    List<Participant> playersAlive = new ArrayList<>();
    List<Team> teamsAlive = new ArrayList<>();

    public static int timeRemaining;

    public void createScoreboard() {
        for (Participant p : MBC.players) {
            newObjective(p);
            createScoreboard(p);
        }
    }


    public void addPlayerScore(Participant p, int score) {

    }

    public void addTeamScore(Participant p, int score) {

    }

    public void addTeamScore(int score) {

    }

    public void playerDeath() {

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
        return (MBC.getGameID() == this.gameID);
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
}
