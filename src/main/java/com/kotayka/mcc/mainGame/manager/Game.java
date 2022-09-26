package com.kotayka.mcc.mainGame.manager;

import com.kotayka.mcc.AceRace.AceRace;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.Paintdown.Paintdown;
import com.kotayka.mcc.DecisionDome.DecisionDome;
import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.fullGame.Instructions;
import com.kotayka.mcc.fullGame.Music;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Game {
    public String stage = "Waiting";

    private TGTTOS tgttos;
    private Skybattle skybattle;
    private final MCC mcc;
    private final SG sg;
    private final BSABM bsabm;
    private final Paintdown paintdown;

    private final AceRace aceRace;
    private final DecisionDome decisionDome;
    private Stats stats;
    public Instructions instructions;

    public Music music;

    public Game(MCC mcc, TGTTOS tgttos, SG sg, Skybattle skybattle, BSABM bsabm, AceRace aceRace, DecisionDome decisionDome, Paintdown paintdown) {
        this.tgttos = tgttos;
        this.mcc = mcc;
        this.sg = sg;
        this.skybattle = skybattle;
        this.bsabm = bsabm;
        this.paintdown = paintdown;
        this.aceRace = aceRace;
        this.decisionDome = decisionDome;
    }

    public void changeToActGame(String game) {
        stage=game;
        music.startSound(game);
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
            case "Paintdown":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Paintdown Game started");
                paintdown.start();
            case "AceRace":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Ace Race Game started");
                aceRace.start();
                break;
            case "DD":
                Bukkit.broadcastMessage(ChatColor.YELLOW + "DD Game started");
                decisionDome.start();
                break;
        }
    }

    public void changeGame(String game) {
        InitVars();
        instructions.starting(game);
    }

    public void InitVars() {
        instructions = new Instructions(mcc, this);
        stats=mcc.stats;
        stage="Starting";
        mcc.scoreboardManager.clearTeams();
        stats.initVars();
        instructions.loadCoords();
        music = new Music(mcc);
        music.loadMusic();
    }

    public void start() {
        InitVars();
        mcc.scoreboardManager.startTimerForGame(10, "Lobby");
    }

    public void endGame() {
        music.stopSound();
        stats.createStats();
        mcc.scoreboardManager.endGame();
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            mcc.scoreboardManager.createMidLobbyBoard(p);
            p.player.player.teleport(new Location(Bukkit.getWorld("world"), 0, 0, 0));
            for(PotionEffect effect : p.player.player.getActivePotionEffects())
            {
                p.player.player.removePotionEffect(effect.getType());
            }
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000000, 255, false, false));
            p.player.player.getInventory().clear();
            p.player.player.setInvulnerable(false);
            p.player.player.setAllowFlight(false);
            p.player.player.setFlying(false);
        }
        mcc.gameRound++;
        stage="Starting";
        mcc.scoreboardManager.startTimerForGame(60, "Lobby");
    }
}
