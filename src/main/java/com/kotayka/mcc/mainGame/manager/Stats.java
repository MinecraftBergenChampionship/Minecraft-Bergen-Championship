package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.Scoreboards.ScoreboardManager;
import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.Scoreboards.ScoreboardTeam;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Stats {
    int[] taskId = {-1};

    private final MCC mcc;
    private final ScoreboardManager manager;

    Map<UUID, Player> playerMap = new HashMap<>();

    //One
    List<String> teamKeysRound = new ArrayList<>();
    Map<String, Integer> teamScoresRound = new HashMap<>();

    //Two
    List<UUID> playerKeys = new ArrayList<>();
    Map<UUID, Integer> playerScores = new HashMap<>();
    Map<UUID, String> playerNames = new HashMap<>();

    //Three
    Map<String, List<UUID>> teamForPlayers = new HashMap<>();

    //Four
    List<String> teamKeysGame = new ArrayList<>();
    Map<String, Integer> teamScoresGame = new HashMap<>();

    int timer = 0;

    public Stats(MCC mcc, ScoreboardManager manager) {
        this.mcc = mcc;
        this.manager = manager;
    }

    public void printTeamRound() {
        List<Integer> scores = new ArrayList<>();
        Map<Integer, List<String>> teamScores = new HashMap<>();
        int places=1;

        for (String team : manager.teamList) {
            if (scores.contains(teamScoresRound.get(team))) {
                teamScores.get(teamScoresRound.get(team)).add(team);
            }
            else {
                scores.add(teamScoresRound.get(team));
                teamScores.put(teamScoresRound.get(team), new ArrayList<>(Arrays.asList(team)));
            }
        }

        Collections.sort(scores, Collections.reverseOrder());

        for (Integer i : scores) {
            for (String t : teamScores.get(i)) {
                Bukkit.broadcastMessage(ChatColor.WHITE+""+places+". "+manager.teamColors.get(t)+t+": "+ChatColor.WHITE+i);
            }
            places++;
        }
    }

    public void teamScoresRound() {
        for (String t : teamKeysRound) {
            teamScoresRound.put(t, manager.teams.get(t).roundScore);
        }
        Bukkit.broadcastMessage(teamScoresRound.toString());
    }

    public void playerScoreRound() {
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            playerScores.put(p.player.player.getUniqueId(), p.roundScore);
        }
    }

    public void teamScoresGame() {
        List<Integer> scores = new ArrayList<>();
        Map<Integer, List<String>> teamScores = new HashMap<>();
        int places=1;

        for (String team : manager.teamList) {
            if (scores.contains(manager.teams.get(team).gameScore)) {
                teamScores.get(manager.teams.get(team).gameScore).add(team);
            }
            else {
                scores.add(manager.teams.get(team).gameScore);
                teamScores.put(manager.teams.get(team).gameScore, new ArrayList<>(Arrays.asList(team)));
            }
        }

        Collections.sort(scores, Collections.reverseOrder());

        for (Integer i : scores) {
            for (String t : teamScores.get(i)) {
                Bukkit.broadcastMessage(ChatColor.WHITE+""+places+". "+manager.teamColors.get(t)+t+": "+ChatColor.WHITE+i);
            }
            places++;
        }
    }

    public void playerTeams() {
        for (String team : teamKeysRound) {
            List<Integer> scores = new ArrayList<>();
            Map<Integer, List<String>> player = new HashMap<>();
            int places=1;

            for (UUID pu : teamForPlayers.get(team)) {
                if (scores.contains(playerScores.get(pu))) {
                    player.get(playerScores.get(pu)).add(playerNames.get(pu));
                }
                else {
                    scores.add(playerScores.get(pu));
                    player.put(playerScores.get(pu), new ArrayList<>(Arrays.asList(playerNames.get(pu))));
                }
            }

            Collections.sort(scores, Collections.reverseOrder());

            for (Integer i : scores) {
                for (String t : player.get(i)) {
                    for (UUID pu : teamForPlayers.get(team)) {
                        playerMap.get(pu).sendMessage(ChatColor.WHITE+""+places+". "+manager.teamColors.get(t)+t+": "+ChatColor.WHITE+i);
                    }
                }
                places++;
            }
        }
    }

    public void printPlayerScores() {
        List<Integer> scores = new ArrayList<>();
        Map<Integer, List<String>> player = new HashMap<>();
        int places=1;

        for (UUID pu : playerKeys) {
            if (scores.contains(playerScores.get(pu))) {
                player.get(playerScores.get(pu)).add(playerNames.get(pu));
            }
            else {
                scores.add(playerScores.get(pu));
                player.put(playerScores.get(pu), new ArrayList<>(Arrays.asList(playerNames.get(pu))));
            }
        }

        Collections.sort(scores, Collections.reverseOrder());

        for (Integer i : scores) {
            for (String t : player.get(i)) {
                Bukkit.broadcastMessage(ChatColor.WHITE+""+places+". "+manager.teamColors.get(t)+t+": "+ChatColor.WHITE+i);
            }
            places++;
        }
    }

    public void initVars() {
        for (String team : manager.teamList) {
            teamKeysRound.add(team);
            teamForPlayers.put(team, new ArrayList<>());
        }
        for (ScoreboardPlayer p : manager.playerList) {
            playerMap.put(p.player.player.getUniqueId(), p.player.player);
            playerNames.put(p.player.player.getUniqueId(), p.player.ign);
            playerKeys.add(p.player.player.getUniqueId());
            playerScores.put(p.player.player.getUniqueId(), 0);
            teamForPlayers.get(manager.playerTeams.get(p).teamName).add(p.player.player.getUniqueId());
        }
    }

    public void deleteTask() {
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
    }

    public void createStats() {
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
        timer = 0;
        teamScoresRound();
        playerScoreRound();
        taskId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
            @Override
            public void run() {
                switch (timer) {
                    case 1:
                        Bukkit.broadcastMessage(ChatColor.BOLD+"Each team scored this game:");
                        break;
                    case 2:
                        printTeamRound();
                        break;
                    case 5:
                        Bukkit.broadcastMessage(ChatColor.BOLD+"Each player scored this game:");
                        break;
                    case 6:
                        printPlayerScores();
                        break;
                    case 9:
                        Bukkit.broadcastMessage(ChatColor.BOLD+"Each player on your team scores during the game:");
                        break;
                    case 10:
                        playerTeams();
                        break;
                    case 13:
                        Bukkit.broadcastMessage(ChatColor.BOLD+"The current standings are:");
                        break;
                    case 14:
                        teamScoresGame();
                        break;
                }
                if (timer > 15) {
                    deleteTask();
                }
                timer++;
            }
        }, 20, 20);
    }
}
