package com.kotayka.mcc.fullGame;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class Instructions {
    String[] tgttosInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] bsabmInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] aceRaceInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] sgInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] skyBattleInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    String[] paintdownInstructions = {
            "Why did the MBC Player cross the road? To get to the other side!!!",
            "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
            "Make sure to look up, down, left and right; the finish could basically be anywhere.",
            "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
            "Once you do get to the end of the map, punch or right click a chicken to finish.",
            "Points are counted for however many people you finished ahead of, and extra points are given to the first full team to finish the course.",
            "As soon as the game starts, youll be instantly teleported to the course, so be ready to run!"
    };

    Map<String, Location> spawnCoords = new HashMap<>();
    Map<String, String[]> instructions = new HashMap<>();
    private final MCC mcc;
    private final Game game;

    String stage;

    public final int[] taskId = {-1};
    int timer = 0;

    public Instructions(MCC mcc, Game game) {
        this.mcc = mcc;
        this.game = game;
    }

    public void loadCoords() {
        spawnCoords.put("TGTTOS", new Location(mcc.tgttos.world, 688, 77, 215));
        spawnCoords.put("AceRace", new Location(mcc.aceRace.world, 2, 45, 110));
        spawnCoords.put("SG", new Location(mcc.sg.world, -57, 48, 63));
        spawnCoords.put("BSABM", new Location(mcc.bsabm.world, 0, 161, 0));
        spawnCoords.put("Skybattle", new Location(mcc.skybattle.world, -156, 51, -265));
        spawnCoords.put("Paintdown", new Location(mcc.tgttos.world, 62, 32, 64));

        instructions.put("TGTTOS", tgttosInstructions);
        instructions.put("AceRace", aceRaceInstructions);
        instructions.put("SG", sgInstructions);
        instructions.put("BSABM", bsabmInstructions);
        instructions.put("Skybattle", skyBattleInstructions);
        instructions.put("Paintdown", paintdownInstructions);
    }

    public void timerEnds() {
        game.changeToActGame(stage);
    }

    public void starting(String game) {
        stage = game;
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
        timer = 0;
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.teleport(spawnCoords.get(game));
            mcc.scoreboardManager.createInstructionScoreboard(p);
        }
        mcc.scoreboardManager.startTimerForGame(60, "game");
        taskId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.GOLD+"[INSTRUCTION] "+ChatColor.BOLD+instructions.get(game)[timer]);
                if (timer >= 7) {
                    Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
                }
                timer++;
            }
        }, 100, 100);
    }
}
