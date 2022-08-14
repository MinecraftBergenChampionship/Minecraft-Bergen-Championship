package com.kotayka.mcc.mainGame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.kotayka.mcc.AceRace.AceRace;
import com.kotayka.mcc.AceRace.listener.AceRaceListener;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.BSABM.commands.checkBuild;
import com.kotayka.mcc.BSABM.listeners.BSABMListener;
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
    private final Players players = new Players(this);
    private final NPCManager npcManager = new NPCManager(this, players);
    public teamManager teamManager;

//  Games
    public final TGTTOS tgttos = new TGTTOS(players, npcManager, this, this);
    public final Skybattle skybattle = new Skybattle(players, plugin, this);
    public final AceRace aceRace = new AceRace(this);
    public final SG sg = new SG(players, this, this);
    public final BSABM bsabm = new BSABM(players, this);

//  Game Manager
    public final Game game = new Game(this, tgttos, sg, skybattle, bsabm, aceRace);
    public boolean gameIsOver = false;


// Location
    public World spawnWorld;
    public Location SPAWN;

//  Scoreboard
    public Map roundScores = new HashMap();
    public Map teamRoundScores = new HashMap();
    public final com.kotayka.mcc.Scoreboards.ScoreboardManager scoreboardManager = new com.kotayka.mcc.Scoreboards.ScoreboardManager(players, plugin, this);

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

        TGTTOSGame();
        sgGame();
        SkybattleGame();
        BSABM();
        AceRaceGame();
    }

    public void AceRaceGame() {
        aceRace.loadWorld();
        getServer().getPluginManager().registerEvents(new AceRaceListener(aceRace), this);
    }

    public void BSABM() {
        bsabm.loadWorld();
        getServer().getPluginManager().registerEvents(new BSABMListener(bsabm, game, players, this, plugin), this);
    }

    public void sgGame() {
        getServer().getPluginManager().registerEvents((Listener) new SGListener(sg, game, players,this), this);
    }

    public void TGTTOSGame() {
        tgttos.loadMaps();
        playersAdded pAdded = new playersAdded(npcManager);
        TGTTOSGameListener TGTTOSGameListener = new TGTTOSGameListener(tgttos, this);
        getServer().getPluginManager().registerEvents(pAdded, this);
        getServer().getPluginManager().registerEvents(TGTTOSGameListener, this);
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                        PacketContainer packet = event.getPacket();
                        if (Objects.equals(game.stage, "TGTTOS")) {
                            int entityId = packet.getIntegers().read(0);
                            int npcID = npcManager.CheckIfValidID(entityId);
                            if (npcID != -1 && !players.spectators.contains(event.getPlayer())) {
                                scoreboardManager.addScore(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 1);
                                scoreboardManager.placementPoints(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 1, tgttos.playerAmount);
                                scoreboardManager.teamFinish(scoreboardManager.players.get(event.getPlayer().getUniqueId()), 5);
                                tgttos.playerAmount++;
                                npcManager.removeNPC(npcID);
                                players.spectators.add(event.getPlayer());
                                String place;
                                switch (tgttos.playerAmount) {
                                    case 1:
                                        place = "1st";
                                        break;
                                    case 2:
                                        place = "2nd";
                                        break;
                                    case 3:
                                        place="3rd";
                                        break;
                                    default:
                                        place=String.valueOf(tgttos.playerAmount)+"th";
                                        break;
                                }
                                Bukkit.broadcastMessage(ChatColor.GOLD+event.getPlayer().getName()+ChatColor.GRAY+ " finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
                                event.getPlayer().sendMessage(ChatColor.WHITE+"[+"+String.valueOf(tgttos.playerPoints)+"] "+ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
                                tgttos.playerPoints--;
                                if (tgttos.playerAmount >= scoreboardManager.playerList.size()) {
                                    tgttos.nextRound();
                                }
                                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                            }
                        }
                    }
                }, 0);
            }
        });
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

    public void returnToSpawn() {
        for (Participant participanto : Participant.participantsOnATeam) {
            participanto.player.teleport(SPAWN);
        }
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
