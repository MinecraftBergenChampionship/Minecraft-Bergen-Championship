package com.kotayka.mcc.mainGame;

import com.kotayka.mcc.AceRace.AceRace;
import com.kotayka.mcc.AceRace.listener.AceRaceListener;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.BSABM.commands.checkBuild;
import com.kotayka.mcc.BSABM.listeners.BSABMListener;
import com.kotayka.mcc.Paintdown.Listener.PaintdownListener;
import com.kotayka.mcc.Paintdown.Paintdown;
import com.kotayka.mcc.DecisionDome.DecisionDome;
import com.kotayka.mcc.DecisionDome.listeners.DecisionDomeListener;
import com.kotayka.mcc.Skybattle.SG.SG;
import com.kotayka.mcc.Skybattle.SG.listeners.SGListener;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.Skybattle.listeners.SkybattleListener;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.TGTTOS.listeners.TGTTOSGameListener;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.TGTTOS.managers.NPCManager;
import com.kotayka.mcc.mainGame.Listeners.chatUpdater;
import com.kotayka.mcc.mainGame.commands.*;
import com.kotayka.mcc.mainGame.manager.*;
import com.kotayka.mcc.mainGame.manager.Team;
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

    /* Values for tournament structure */
    public static final short NUM_TEAMS = 6;
    public static final short PLAYERS_PER_TEAM = 4;

//  Starting
    public startGame startGame = new startGame();

//  Scoreboard
    public ScoreboardManager manager = Bukkit.getScoreboardManager();

    public Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();

// MCCTeam Objects
    private MCCTeam red = new MCCTeam(this, Team.RED_RABBITS);
    private MCCTeam yellow = new MCCTeam(this, Team.YELLOW_YAKS);
    private MCCTeam green = new MCCTeam(this, Team.GREEN_GUARDIANS);
    private MCCTeam blue = new MCCTeam(this, Team.BLUE_BATS);
    private MCCTeam purple = new MCCTeam(this, Team.PURPLE_PANDAS);
    private MCCTeam pink = new MCCTeam(this, Team.PINK_PIGLETS);
    //MCCTeam spectator = new MCCTeam(this, Team.SPECTATORS);
    //teams.add(spectator);

    public List<MCCTeam> teams = new ArrayList<>(Arrays.asList(red, yellow, green, blue, purple, pink));

    public Plugin plugin = this;

//  Scoreboard Variables
    public Map<UUID, String> maps = new HashMap<UUID, String>();
    public Map<UUID, Integer> roundNums = new HashMap<UUID, Integer>();
    public Map<UUID, Integer> time = new HashMap<UUID, Integer>();
    public Map<UUID, Integer[]> previousStandings = new HashMap<UUID, Integer[]>();
    public Map<UUID, Integer> playersAlive = new HashMap<UUID, Integer>();
    public Map<UUID, Integer> teamsAlive = new HashMap<UUID, Integer>();
    public int gameRound = 0;

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
    public final Paintdown paintdown = new Paintdown(players, this);
    public final DecisionDome decisionDome = new DecisionDome(this);

//  Game Manager
    public final Game game = new Game(this, tgttos, sg, skybattle, bsabm, aceRace, decisionDome, paintdown);
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

        if (Bukkit.getWorld("world") != null) {
            spawnWorld = Bukkit.getWorld("world");
        }

        SPAWN = new Location(spawnWorld, 0.5, 2.5, 0.5);

        getCommand("ready").setExecutor(new ready(players, this));
        getCommand("eventstart").setExecutor(new eventstart(this));
        TGTTOSGame();
        sgGame();
        SkybattleGame();
        BSABM();
        paintdownGame();
        AceRaceGame();
        DecisionDome();
        scoreboardManager.start();
    }

    /*
     * From a team name, retrieve the appropriate
     * MCCTeam object. Works for all iterations
     * of the String, ie "Green Guardians" returns
     * the same value as "greenGuardians"
     */
    public MCCTeam getTeam(String teamName) {
        Team t = getTeamValue(teamName);

        for (MCCTeam mt : teams) {
            if (mt.getTeam().equals(t)) {
                return mt;
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "ERROR in MCC.java --> getTeam(String), invalid team name!");
        // return last team as fail case
        return teams.get(teams.size()-1);
    }

    // Alias for getTeam()
    public MCCTeam getMCCTeam(String teamName) {
        Team t = getTeamValue(teamName);

        for (MCCTeam mt : teams) {
            if (mt.getTeam().equals(t)) {
                return mt;
            }
        }

        Bukkit.broadcastMessage(ChatColor.RED + "ERROR in MCC.java --> getMCCTeam(String), invalid team name!");
        // return last team as fail case
        return teams.get(teams.size()-1);
    }

    /*
     * Helper function to make things really
     * neat and concise: used in getMCCTeam()
     * for clean access to Team Enum, without
     * hard coding team indexes.
     */
    private Team getTeamValue(String teamName) {
        /*
         * FOR CONSISTENCY (and sanity)
         * make everything lowercase and remove spaces
         * (to prevent errors from just loop checking
         */
        String newName = teamName.toLowerCase().replaceAll("\\s", "");
        // conversion
        return switch (newName) {
            case "redrabbits" -> Team.RED_RABBITS;
            case "yellowyaks" -> Team.YELLOW_YAKS;
            case "greenguardians" -> Team.GREEN_GUARDIANS;
            case "bluebats" -> Team.BLUE_BATS;
            case "purplepandas" -> Team.PURPLE_PANDAS;
            case "pinkpiglets", "pinkparrots" -> Team.PINK_PIGLETS;
            default -> Team.SPECTATORS;
        };
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

    public void paintdownGame() {
        getServer().getPluginManager().registerEvents((Listener) new PaintdownListener(paintdown, this), this);
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
        firework.spawnFireworkWithColor(victim.player.getLocation(), victim.team.getColor());
    }

    public void setGameOver(boolean b) {
        gameIsOver = b;
    }
    public void loadTeams() {
        teamManager = new teamManager(players.participants, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
