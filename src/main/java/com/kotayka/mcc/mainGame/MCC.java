package com.kotayka.mcc.mainGame;

import com.kotayka.mcc.AceRace.AceRace;
import com.kotayka.mcc.AceRace.listener.AceRaceListener;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.BSABM.commands.checkBuild;
import com.kotayka.mcc.BSABM.listeners.BSABMListener;
import com.kotayka.mcc.DecisionDome.DecisionDome;
import com.kotayka.mcc.DecisionDome.listeners.DecisionDomeListener;
import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.SG.listeners.SGListener;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.Skybattle.listeners.SkybattleListener;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.TGTTOS.listeners.TGTTOSGameListener;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.TGTTOS.managers.NPCManager;
import com.kotayka.mcc.mainGame.Listeners.chatUpdater;
import com.kotayka.mcc.mainGame.commands.*;
import com.kotayka.mcc.mainGame.manager.*;
import com.kotayka.mcc.mainGame.manager.tabComplete.startCommand;
import com.kotayka.mcc.mainGame.manager.tabComplete.tCommands;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.kotayka.mcc.mainGame.Listeners.playerJoinLeave;
import com.kotayka.mcc.TGTTOS.listeners.playersAdded;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class MCC extends JavaPlugin implements Listener {

//  Starting
    public startGame startGame = new startGame();

//  Scoreboard
    public ScoreboardManager manager = Bukkit.getScoreboardManager();

    public Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
    public Map<String, Team[]> teams = new HashMap<String, Team[]>();

    public Plugin plugin = this;

//  Scoreboard Variables
    public Map<UUID, String> maps = new HashMap<UUID, String>();
    public Map<UUID, Integer> roundNums = new HashMap<UUID, Integer>();
    public Map<UUID, Integer> time = new HashMap<UUID, Integer>();
    public Map<UUID, Integer[]> previousStandings = new HashMap<UUID, Integer[]>();
    public Map<UUID, Integer> playersAlive = new HashMap<UUID, Integer>();
    public Map<UUID, Integer> teamsAlive = new HashMap<UUID, Integer>();
    public int gameRound = 0;

//  Team
    public List<List<Participant>> teamList = new ArrayList<List<Participant>>(6);

//  Managers
    public final Players players = new Players(this);
    private final NPCManager npcManager = new NPCManager(this, players);
    public teamManager teamManager;

//  Games
    public final TGTTOS tgttos = new TGTTOS(players, npcManager, this, this);
    public final Skybattle skybattle = new Skybattle(players, plugin, this);
    public final AceRace aceRace = new AceRace(this);
    public final SG sg = new SG(players, this, this);
    public final BSABM bsabm = new BSABM(players, this);

    public final DecisionDome decisionDome = new DecisionDome(this);

//  Game Manager
    public final Game game = new Game(this, tgttos, sg, skybattle, bsabm, aceRace, decisionDome);
    public boolean gameIsOver = false;

// Location
    public World spawnWorld;
    public Location SPAWN;

//  Scoreboard
    public Map roundScores = new HashMap();
    public Map teamRoundScores = new HashMap();
    public final com.kotayka.mcc.Scoreboards.ScoreboardManager scoreboardManager = new com.kotayka.mcc.Scoreboards.ScoreboardManager(players, plugin, this);

//  Stat
    public Stats stats = new Stats(this, scoreboardManager);

    @Override
    public void onEnable() {
        loadTeams();
        players.getOnlinePlayers();
        Bukkit.getServer().getConsoleSender().sendMessage("MCC Plugin Loaded!");
        playerJoinLeave pManager = new playerJoinLeave(players);
        getServer().getPluginManager().registerEvents(pManager, this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new chatUpdater(players), this);
        getCommand("start").setExecutor(new start(game));
        getCommand("start").setTabCompleter(new startCommand());
        getCommand("mccteam").setExecutor(new teamCommands(teamManager, this));
        getCommand("mccteam").setTabCompleter(new tCommands());
        getCommand("world").setExecutor(new world());
        getCommand("checkbuild").setExecutor(new checkBuild(this));

        List<Participant> redRabbits = new ArrayList<Participant>(4);
        List<Participant> greenGuardians = new ArrayList<Participant>(4);
        List<Participant> yellowYaks = new ArrayList<Participant>(4);
        List<Participant> blueBats = new ArrayList<Participant>(4);
        List<Participant> purplePandas = new ArrayList<Participant>(4);
        List<Participant> pinkParrots = new ArrayList<Participant>(4);

        teamList.add(redRabbits);
        teamList.add(greenGuardians);
        teamList.add(yellowYaks);
        teamList.add(blueBats);
        teamList.add(purplePandas);
        teamList.add(pinkParrots);

        if (Bukkit.getWorld("world") != null) {
            spawnWorld = Bukkit.getWorld("world");
        }

        SPAWN = new Location(spawnWorld, 0, 2, 0);

        getCommand("ready").setExecutor(new ready(players, this));
        getCommand("eventstart").setExecutor(new eventstart(this));
        TGTTOSGame();
        sgGame();
        SkybattleGame();
        BSABM();
        AceRaceGame();
        DecisionDome();
        scoreboardManager.start();
    }

    public void AceRaceGame() {
        aceRace.loadWorld();
        getServer().getPluginManager().registerEvents(new AceRaceListener(aceRace), this);
    }

    public void DecisionDome() {
        decisionDome.loadWorld();
        getServer().getPluginManager().registerEvents(new DecisionDomeListener(decisionDome), this);
    }
    public void BSABM() {
        bsabm.loadWorld();
        getServer().getPluginManager().registerEvents(new BSABMListener(bsabm, game, players, this, plugin), this);
    }

    public void sgGame() {
        sg.loadWorld();
        getServer().getPluginManager().registerEvents((Listener) new SGListener(sg, game, players,this, this), this);
    }

    public void TGTTOSGame() {
        tgttos.loadMaps();
        playersAdded pAdded = new playersAdded(npcManager);
        TGTTOSGameListener TGTTOSGameListener = new TGTTOSGameListener(tgttos, this);
        getServer().getPluginManager().registerEvents(pAdded, this);
        getServer().getPluginManager().registerEvents(TGTTOSGameListener, this);
    }

    public void SkybattleGame() {
        SkybattleListener skybattleListener = new SkybattleListener(skybattle, this);
        getServer().getPluginManager().registerEvents(skybattleListener, this);

        if (Bukkit.getWorld("Skybattle") == null) {
            skybattle.world = Bukkit.getWorld("world");
        }
        else {
            skybattle.world = Bukkit.getWorld("Skybattle");
        }
    }

    public static void spawnFirework(Participant victim) {
        Firework firework = new Firework();
        firework.spawnFireworkWithColor(victim.player.getLocation(), victim.color);
    }

    public void setGameOver(boolean b) {
        gameIsOver = b;
    }

    public void loadTeams() {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};

        List<String> tn = new ArrayList<>(Arrays.asList(teamNames));
        teamManager = new teamManager(players.participants, tn, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
