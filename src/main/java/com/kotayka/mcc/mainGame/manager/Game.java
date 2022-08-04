package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Game {
    public String stage = "Lobby";

    private TGTTOS tgttos;
    private Skybattle skybattle;
    private final MCC mcc;
    private final SG sg;
    private final BSABM bsabm;

    public Game(MCC mcc, TGTTOS tgttos, SG sg, Skybattle skybattle, BSABM bsabm) {
        this.tgttos = tgttos;
        this.mcc = mcc;
        this.sg = sg;
        this.skybattle = skybattle;
        this.bsabm = bsabm;
    }

    public void changeGame(String game) {
        stage=game;
        mcc.changeScoreboard(game);
        if (!(game.equals("Lobby"))) {
            mcc.gameRound++;
        }
        switch (game) {
            case "TGTTOS":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "TGTTOS Game started");
                tgttos.start();
                break;
            case "SG":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "SG Game started");
                sg.start();
                break;
            case "Skybattle":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Skybattle Game started");
                skybattle.start();
                break;
            case "BSABM":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "BSABM Game started");
                bsabm.start();
                break;
        }
    }
}
