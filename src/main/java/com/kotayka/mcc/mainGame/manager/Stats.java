package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.Scoreboards.ScoreboardManager;
import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.Scoreboards.ScoreboardTeam;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

public class Stats {
    int[] taskId = {-1};

    private final MCC mcc;
    private final ScoreboardManager manager;

    //One
    List<String> teamKeysRound = new ArrayList<>();
    Map<String, Integer> teamScoresRound = new HashMap<>();

    //Two
    List<UUID> playerKeys = new ArrayList<>();
    Map<UUID, Integer> playerScores = new HashMap<>();

    //Three
    Map<String, List<UUID>> teamForPlayers = new HashMap<>();

    //Four
    List<String> teamKeysGame = new ArrayList<>();
    Map<String, Integer> teamScoresGame = new HashMap<>();

    public Stats(MCC mcc, ScoreboardManager manager) {
        this.mcc = mcc;
        this.manager = manager;
    }

    public void cancelStatRepeatedTimer() {

    }

    public void teamScoresRound() {
        List<Integer> scores = new ArrayList<>();
        List<Integer> scoresAlreadyUsed = new ArrayList<>();
        Map<Integer, List<String>> teamScores = new HashMap<>();
        int places=1;

        for (String team : manager.teamList) {
            if (scores.contains(manager.teams.get(team).roundScore)) {
                teamScores.get(manager.teams.get(team).roundScore).add(team);
            }
            else {
                scores.add(manager.teams.get(team).roundScore);
                teamScores.put(manager.teams.get(team).roundScore, new ArrayList<>(Arrays.asList(team)));
            }
        }

        Collections.sort(scores, Collections.reverseOrder());

        for (Integer i : scores) {
            for (String t : teamScores.get(i)) {
                Bukkit.broadcastMessage(ChatColor.WHITE+""+i+". "+manager.teamColors.get(t)+t+": "+ChatColor.WHITE+i);
            }
        }
    }

    public void initVars() {
        for (String team : manager.teamList) {
            teamForPlayers.put(team, new ArrayList<>());
        }
        for (ScoreboardPlayer p : manager.playerList) {
            playerKeys.add(p.player.player.getUniqueId());
            playerScores.put(p.player.player.getUniqueId(), 0);
            teamForPlayers.get(manager.playerTeams.get(p).teamName).add(p.player.player.getUniqueId());
        }
    }


    public void createStats() {
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
        taskId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
            @Override
            public void run() {

            }
        }, 20, 20);
    }
}
