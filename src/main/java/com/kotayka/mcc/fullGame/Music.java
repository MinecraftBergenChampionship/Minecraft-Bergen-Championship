package com.kotayka.mcc.fullGame;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.*;

import java.util.HashMap;
import java.util.Map;

public class Music {
    public Map<String, Sound> disks = new HashMap<>();
    public Map<String, World> world = new HashMap<>();
    public Map<String, Integer> durations = new HashMap<>();

    int timer = 0;
    private final MCC mcc;

    public Music(MCC mcc) {
        this.mcc = mcc;
    }

    public void loadMusic() {
        disks.put("AceRace", Sound.MUSIC_DISC_11);
        disks.put("BSABM", Sound.MUSIC_DISC_MALL);
        disks.put("DD", Sound.MUSIC_DISC_CAT);
        disks.put("Dodgebolt", Sound.MUSIC_DISC_CHIRP);
        disks.put("Paintdown", Sound.MUSIC_DISC_MELLOHI);
        disks.put("SG", Sound.MUSIC_DISC_FAR);
        disks.put("Skybattle", Sound.MUSIC_DISC_MELLOHI);
        disks.put("TGTTOS", Sound.MUSIC_DISC_FAR);

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
            p.player.player.playSound(p.player.player.getLocation(), disks.get(game), 1000, 1);
            playSound(game);
        }
    }

    public void resetSound(String game) {
        stopSound(game);
        startSound(game);
    }

    public void playSound(String game) {
        if (mcc.game.stage.equals(game)) {
            timer = durations.get(game);
            Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
                @Override
                public void run() {
                    if (timer <= 0) {
                        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                            p.player.player.playSound(p.player.player.getLocation(), disks.get(game), SoundCategory.RECORDS, 0.2f, 1);
                            playSound(game);
                        }
                    }
                    timer--;
                }
            }, 0, 20);
        }
    }

    public void stopSound(String game) {
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.stopAllSounds();
        }
    }

}
