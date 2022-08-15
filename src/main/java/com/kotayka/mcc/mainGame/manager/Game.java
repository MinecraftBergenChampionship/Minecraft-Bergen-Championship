package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.AceRace.AceRace;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.DecisionDome.DecisionDome;
import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Game {
    public String stage = "Waiting";

    private TGTTOS tgttos;
    private Skybattle skybattle;
    private final MCC mcc;
    private final SG sg;
    private final BSABM bsabm;
    private final AceRace aceRace;
    private final DecisionDome decisionDome;

    public Game(MCC mcc, TGTTOS tgttos, SG sg, Skybattle skybattle, BSABM bsabm, AceRace aceRace, DecisionDome decisionDome) {
        this.tgttos = tgttos;
        this.mcc = mcc;
        this.sg = sg;
        this.skybattle = skybattle;
        this.bsabm = bsabm;
        this.aceRace = aceRace;
        this.decisionDome = decisionDome;
    }

    public void changeGame(String game) {
        stage=game;
        if (!(game.equals("Lobby") || game.equals("DD"))) {
            mcc.gameRound++;
        }
        switch (game) {
            case "TGTTOS":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "TGTTOS Game started");
                tgttos.start();
                for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                    mcc.scoreboardManager.createTGTTOSBoard(p);
                }
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
            case "AceRace":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Ace Race Game started");
                aceRace.start();
            case "DD":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "DD Game started");
                decisionDome.start();
                break;
        }
    }

    public void start() {
        stage="Starting";
        mcc.scoreboardManager.clearTeams();
        mcc.scoreboardManager.startTimerForGame(30, "Lobby");
    }
}
