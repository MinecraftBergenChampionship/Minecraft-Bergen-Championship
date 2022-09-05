package com.kotayka.mcc.fullGame;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Music {
    public Map<String, String> discs = new HashMap<>();
    public Map<String, World> world = new HashMap<>();
    public Map<String, Integer> durations = new HashMap<>();

    int timer = 0;
    private final MCC mcc;

    public Music(MCC mcc) {
        this.mcc = mcc;
    }

    public void loadMusic() {
        discs.put("AceRace", "11");
        discs.put("BSABM", "mall");
        discs.put("DD", "cat");
        discs.put("Dodgebolt", "chirp");
        discs.put("Paintdown", "mellohi");
        discs.put("SG", "far");
        discs.put("Skybattle", "stall");
        discs.put("TGTTOS", "otherside");

        world.put("AceRace", mcc.aceRace.world);
        world.put("BSABM", mcc.bsabm.world);
        world.put("DD", mcc.decisionDome.world);
        world.put("Dodgebolt", mcc.aceRace.world);
        world.put("Paintdown", mcc.aceRace.world);
        world.put("SG", mcc.sg.world);
        world.put("Skybattle", mcc.skybattle.world);
        world.put("TGTTOS", mcc.tgttos.world);

        durations.put("AceRace", 310);
        durations.put("BSABM", 250);
        durations.put("DD", 134);
        durations.put("Dodgebolt", 217);
        durations.put("Paintdown", 294);
        durations.put("SG", 248);
        durations.put("Skybattle", 254);
        durations.put("TGTTOS", 124);
    }

    public void startSound(String game) {
        playSound(game);
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            UUID musicListener = p.player.player.getUniqueId();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute at " + musicListener + " run playsound minecraft:music_disc." + discs.get(game) + " record @p");
        }
    }

    public void resetSound(String game) {
        stopSound();
        startSound(game);
    }

    public void playSound(String game) {
        timer = durations.get(game);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timer <= 0) {
                    if (mcc.game.stage.equals(game)) {
                    Bukkit.broadcastMessage("Sound playing again");
                    resetSound(game);
                    }
                    this.cancel();
                }
                timer--;
            }
        }.runTaskTimer(mcc, 0, 20);
    }

    public void stopSound() {
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.stopAllSounds();
        }
    }

}
