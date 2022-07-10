package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Game {
    public String stage = "Lobby";

    private final TGTTOS tgttos;
    private final MCC mcc;

    public Game(TGTTOS tgttos, MCC mcc) {
        this.tgttos = tgttos;
        this.mcc = mcc;
    }

    public void changeGame(String game) {
        stage=game;
        mcc.changeScoreboard(game);
        if (!(game.equals("Lobby"))) {
            mcc.gameRound++;
        }
        if (game.equals("TGTTOS")) {
            Bukkit.broadcastMessage(ChatColor.YELLOW+"TGTTOS Game started");
            tgttos.start();
        }
    }
}
