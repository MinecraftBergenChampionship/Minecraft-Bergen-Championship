package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Game {
    public String stage = "Lobby";

    private final TGTTOS tgttos;
    private final MCC mcc;
    private final SG sg;

    public Game(TGTTOS tgttos, MCC mcc, SG sg) {
        this.tgttos = tgttos;
        this.mcc = mcc;
        this.sg = sg;
    }

    public void changeGame(String game) {
        stage=game;
        mcc.changeScoreboard(game);
        if (!(game.equals("Lobby"))) {
            mcc.gameRound++;
        }
        switch (game) {
            case "TGTTOS":
                Bukkit.broadcastMessage(ChatColor.YELLOW+"TGTTOS Game started");
                tgttos.start();
                break;
            case "SG":
                Bukkit.broadcastMessage(ChatColor.YELLOW+"SG Game started");
                sg.start();
                break;
        }
    }
}
