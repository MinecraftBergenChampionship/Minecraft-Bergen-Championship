package com.kotayka.mcc.mainGame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.kotayka.mcc.BSABM.BSABM;
import com.kotayka.mcc.BSABM.listeners.BSABMListener;
import com.kotayka.mcc.SG.SG;
import com.kotayka.mcc.SG.listeners.SGListener;
import com.kotayka.mcc.Skybattle.Skybattle;
import com.kotayka.mcc.Skybattle.listeners.SkybattleListener;
import com.kotayka.mcc.TGTTOS.TGTTOS;
import com.kotayka.mcc.TGTTOS.listeners.TGTTOSGameListener;
import com.kotayka.mcc.TGTTOS.managers.NPCManager;
import com.kotayka.mcc.mainGame.Listeners.chatUpdater;
import com.kotayka.mcc.mainGame.commands.*;
import com.kotayka.mcc.mainGame.manager.*;
import com.kotayka.mcc.mainGame.manager.tabComplete.startCommand;
import com.kotayka.mcc.mainGame.manager.tabComplete.tCommands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.kotayka.mcc.mainGame.Listeners.playerJoinLeave;
import com.kotayka.mcc.TGTTOS.listeners.playersAdded;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class MCC extends JavaPlugin implements Listener {

//  Scoreboard
    public ScoreboardManager manager;

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

//  Managers
    private final Players players = new Players(this);
    private final NPCManager npcManager = new NPCManager(this, players);
    public teamManager teamManager;

//  Games
    public final TGTTOS tgttos = new TGTTOS(players, npcManager, this, this);
    public final Skybattle skybattle = new Skybattle(players, plugin, this);

    public final SG sg = new SG(players, this, this);
    public final BSABM bsabm = new BSABM(players);

//  Game Manager
    private final Game game = new Game(this, tgttos, sg, skybattle, bsabm);

//  Scoreboard
    public Map roundScores = new HashMap();
    public Map teamRoundScores = new HashMap();
    public final com.kotayka.mcc.Scoreboards.ScoreboardManager scoreboardManager = new com.kotayka.mcc.Scoreboards.ScoreboardManager(players, plugin, this);

    @Override
    public void onEnable() {
        manager = Bukkit.getScoreboardManager();
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
        getCommand("spec").setExecutor(new playerCommand(players));
        TGTTOSGame();
        sgGame();

        // Temp
        if (Bukkit.getWorld("Skybattle") == null) {
            skybattle.world = Bukkit.getWorld("world");
        }
        else {
            skybattle.world = Bukkit.getWorld("Skybattle");
        }

//        skybattle.resetBorder();

        SkybattleGame();
        BSABM();
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
                                if (scoreboardManager.playerList.size() >= tgttos.playerAmount) {
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


    }
    public void createScoreboard(Participant player) {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
        Scoreboard board = manager.getNewScoreboard();
        Integer timeLeft=120;

        Team red = board.registerNewTeam(teamNames[0]+player.ign);
        Team yellow = board.registerNewTeam(teamNames[1]+player.ign);
        Team green = board.registerNewTeam(teamNames[2]+player.ign);
        Team blue = board.registerNewTeam(teamNames[3]+player.ign);
        Team purple = board.registerNewTeam(teamNames[4]+player.ign);
        Team pink = board.registerNewTeam(teamNames[5]+player.ign);

        red.setColor(ChatColor.RED);
        yellow.setColor(ChatColor.YELLOW);
        green.setColor(ChatColor.GREEN);
        blue.setColor(ChatColor.BLUE);
        purple.setColor(ChatColor.DARK_PURPLE);
        pink.setColor(ChatColor.LIGHT_PURPLE);

        //TGTTOS
        Objective sgScoreboard = board.registerNewObjective("sg", "dummy", ChatColor.BOLD+""+ChatColor.YELLOW+"MCC");
        Score sgGameNum = sgScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Game "+gameRound+"/8:"+ChatColor.WHITE+" TGTTOSAWAF");
        sgGameNum.setScore(15);
        Score sgMap = sgScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Map: "+ChatColor.WHITE+"BCA");
        sgMap.setScore(14);
        Score sgEvent = sgScoreboard.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Next Event: ");
        sgEvent.setScore(13);
        Score sgEventName = sgScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Starting: "+ChatColor.WHITE+getFormattedTime(60));
        sgEventName.setScore(12);
        Score sgSpace1 = sgScoreboard.getScore(ChatColor.RESET.toString());
        sgSpace1.setScore(11);
        Score sgGameCoins = sgScoreboard.getScore(ChatColor.AQUA+"Game Coins:");
        sgGameCoins.setScore(10);
        Score sgRed = sgScoreboard.getScore(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+"0");
        sgRed.setScore(9);
        Score sgYellow = sgScoreboard.getScore(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+"0");
        sgYellow.setScore(8);
        Score sgGreen = sgScoreboard.getScore(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+"0");
        sgGreen.setScore(7);
        Score sgBlue = sgScoreboard.getScore(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+"0");
        sgBlue.setScore(6);
        Score sgPurple = sgScoreboard.getScore(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+"0");
        sgPurple.setScore(5);
        Score sgPink = sgScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+"0");
        sgPink.setScore(4);
        Score sgSpace2 = sgScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
        sgSpace2.setScore(3);
        Score sgPlayersAlive = sgScoreboard.getScore(ChatColor.GREEN+"Players Alive: "+ChatColor.WHITE+"0");
        sgPlayersAlive.setScore(2);
        Score sgTeamsAlive = sgScoreboard.getScore(ChatColor.GREEN+"Teams Alive: "+ChatColor.WHITE+"0");
        sgTeamsAlive.setScore(1);

        // Skybattle
        Objective skybattleScoreboard = board.registerNewObjective("Skybattle", "dummy", ChatColor.BOLD+""+ChatColor.YELLOW+"MCC");
        Score skybattleGameNum = skybattleScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Game "+gameRound+"/8:"+ChatColor.WHITE+" Skybattle");
        skybattleGameNum.setScore(15);
        Score skybattleMap = skybattleScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Map: "+ChatColor.WHITE+"Skybattle");
        skybattleMap.setScore(14);
        // TODO?
        Score skybattleRound = skybattleScoreboard.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ChatColor.WHITE+(skybattle.roundNum+1)+"/3");
        skybattleRound.setScore(13);
        Score skybattleTimeLeft = skybattleScoreboard.getScore(ChatColor.BOLD+""+ChatColor.RED + "Time left: "+ChatColor.WHITE+((int) Math.floor(timeLeft/60))+":"+timeLeft%60);
        skybattleTimeLeft.setScore(12);
        Score skybattleSpace = skybattleScoreboard.getScore(ChatColor.RESET.toString());
        skybattleSpace.setScore(11);
        Score skybattleGameCoins = skybattleScoreboard.getScore(ChatColor.AQUA+"Game Coins:");
        skybattleGameCoins.setScore(10);
        Score skybattleRed = skybattleScoreboard.getScore(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+"0");
        skybattleRed.setScore(9);
        Score skybattleYellow = skybattleScoreboard.getScore(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+"0");
        skybattleYellow.setScore(8);
        Score skybattleGreen = skybattleScoreboard.getScore(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+"0");
        skybattleGreen.setScore(7);
        Score skybattleBlue = skybattleScoreboard.getScore(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+"0");
        skybattleBlue.setScore(6);
        Score skybattlePurple = skybattleScoreboard.getScore(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+"0");
        skybattlePurple.setScore(5);
        Score skybattlePink = skybattleScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+"0");
        skybattlePink.setScore(4);
        Score skybattleSpace2 = skybattleScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
        skybattleSpace2.setScore(3);
        Score skybattleCoins = skybattleScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");
        skybattleCoins.setScore(2);
    }

    public String getFormattedTime(int seconds) {
        return seconds / 60 +":"+String.format("%02d", seconds%60);
    }

    public void scoreBoardss() {
        String[] teamList = {"Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets"};
        for (String game : teamList) {
            teamRoundScores.put(game, 0);
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                Integer[] tempArray = new Integer[] {0,0,0,0,0,0};
                for (int i =0; i < teamList.length; i++) {
                    tempArray[i] = (Integer) teamManager.roundScores.get(teamList[i]);
                }
                Arrays.sort(tempArray, Collections.reverseOrder());
                for (Participant p : players.participants) {
                    switch(game.stage) {
                        case "Lobby":
                            break;
                        case "TGTTOS":
                            if (roundScores.get(p.ign) != null) {
                                if (p.roundCoins != ((int) roundScores.get(p.ign))) {
                                    scoreboards.get(p.ign).resetScores(ChatColor.YELLOW + "Your Coins: " + ChatColor.WHITE + roundScores.get(p.ign));
                                    scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.YELLOW + "Your Coins: " + ChatColor.WHITE + p.roundCoins).setScore(2);
                                    roundScores.put(p.ign, p.roundCoins);
                                }
                            }
                            if (tgttos.timeLeft >= 0) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD+""+ChatColor.RED + "Time left: "+ChatColor.WHITE+((int) Math.floor(time.get(p.player.getUniqueId())/60))+":"+time.get(p.player.getUniqueId())%60);
                                scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.BOLD+""+ChatColor.RED +"Time left: "+ChatColor.WHITE+((int) Math.floor(tgttos.timeLeft/60))+":"+tgttos.timeLeft%60).setScore(12);
                                time.put(p.player.getUniqueId(), tgttos.timeLeft);
                            }
                            if (roundNums.get(p.player.getUniqueId()) != tgttos.roundNum) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.GREEN + "Round: " + ChatColor.WHITE + (roundNums.get(p.player.getUniqueId()) + 1) + "/7");
                                scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.BOLD + "" + ChatColor.GREEN + "Round: " + ChatColor.WHITE + (tgttos.roundNum + 1) + "/7").setScore(13);
                                roundNums.put(p.player.getUniqueId(), tgttos.roundNum);
                            }
                            if (maps.get(p.player.getUniqueId()) != tgttos.mapOrder[tgttos.roundNum]) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.AQUA + "Map: " + ChatColor.WHITE + maps.get(p.player.getUniqueId()));
                                scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.BOLD + "" + ChatColor.AQUA + "Map: " + ChatColor.WHITE + tgttos.mapOrder[tgttos.gameOrder[tgttos.roundNum]]).setScore(14);
                                maps.put(p.player.getUniqueId(), tgttos.mapOrder[tgttos.gameOrder[tgttos.roundNum]]);
                            }
                            if (!(tempArray.equals(previousStandings.get(p.player.getUniqueId())))) {
                                for (int i = 0; i < teamManager.teamNames.size(); i++) {
                                    int score = findIndexInNumberArr(tempArray, (Integer) teamManager.roundScores.get(teamList[i]));
                                    switch (teamManager.teamNames.get(i)) {
                                        case "RedRabbits":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.RED + "Red Rabbits: " + ChatColor.WHITE + teamRoundScores.get("Red Rabbits"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.RED + "Red Rabbits: " + ChatColor.WHITE + teamManager.roundScores.get("Red Rabbits")).setScore(9 - score);
                                            break;
                                        case "YellowYaks":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.YELLOW + "Yellow Yaks: " + ChatColor.WHITE + teamRoundScores.get("Yellow Yaks"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.YELLOW + "Yellow Yaks: " + ChatColor.WHITE + teamManager.roundScores.get("Yellow Yaks")).setScore(9 - score);
                                            break;
                                        case "GreenGuardians":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.GREEN + "Green Guardians: " + ChatColor.WHITE + teamRoundScores.get("Green Guardians"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.GREEN + "Green Guardians: " + ChatColor.WHITE + teamManager.roundScores.get("Green Guardians")).setScore(9 - score);
                                            break;
                                        case "BlueBats":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.BLUE + "Blue Bats: " + ChatColor.WHITE + teamRoundScores.get("Blue Bats"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.BLUE + "Blue Bats: " + ChatColor.WHITE + teamManager.roundScores.get("Blue Bats")).setScore(9 - score);
                                            break;
                                        case "PurplePandas":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.DARK_PURPLE + "Purple Pandas: " + ChatColor.WHITE + teamRoundScores.get("Purple Pandas"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.DARK_PURPLE + "Purple Pandas: " + ChatColor.WHITE + teamManager.roundScores.get("Purple Pandas")).setScore(9 - score);
                                            break;
                                        case "PinkPiglets":
                                            scoreboards.get(p.ign).getObjective("tgttos").getScoreboard().resetScores(ChatColor.LIGHT_PURPLE + "Pink Piglets: " + ChatColor.WHITE + teamRoundScores.get("Pink Piglets"));
                                            scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.LIGHT_PURPLE + "Pink Piglets: " + ChatColor.WHITE + teamManager.roundScores.get("Pink Piglets")).setScore(9 - score);
                                            break;
                                    }
                                }
                                previousStandings.put(p.player.getUniqueId(), tempArray);
                            }
                            break;
                        case "SG":
                            if (!Objects.equals(maps.get(p.player.getUniqueId()), sg.eventName) || time.get(p.player.getUniqueId()) != sg.relaventEventTimer) {
                                scoreboards.get(p.ign).resetScores(ChatColor.LIGHT_PURPLE+maps.get(p.player.getUniqueId())+": "+ChatColor.WHITE+getFormattedTime(time.get(p.player.getUniqueId())));
                                scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.LIGHT_PURPLE+sg.eventName+": "+ChatColor.WHITE+getFormattedTime(sg.relaventEventTimer)).setScore(12);
                                maps.put(p.player.getUniqueId(), sg.eventName);
                                time.put(p.player.getUniqueId(), sg.relaventEventTimer);
                            }
                            if (teamsAlive.get(p.player.getUniqueId()) != sg.teamsDead) {
                                scoreboards.get(p.ign).resetScores(ChatColor.GREEN+"Teams Alive: "+ChatColor.WHITE+teamsAlive.get(p.player.getUniqueId()));
                                scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.GREEN+"Teams Alive: "+ChatColor.WHITE+sg.teamsDead).setScore(1);
                                teamsAlive.put(p.player.getUniqueId(), sg.teamsDead);
                            }
                            if (playersAlive.get(p.player.getUniqueId()) != sg.playersDead) {
                                scoreboards.get(p.ign).resetScores(ChatColor.GREEN + "Players Alive: " + ChatColor.WHITE + playersAlive.get(p.player.getUniqueId()));
                                scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.GREEN + "Players Alive: " + ChatColor.WHITE + sg.playersDead).setScore(2);
                                playersAlive.put(p.player.getUniqueId(), sg.teamsDead);
                            }

                            if (!(tempArray.equals(previousStandings.get(p.player.getUniqueId())))) {
                                for (int i = 0; i < teamManager.teamNames.size(); i++) {
                                    int score = findIndexInNumberArr(tempArray, (Integer) teamManager.roundScores.get(teamList[i]));
                                    switch (teamManager.teamNames.get(i)) {
                                        case "RedRabbits":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+teamRoundScores.get("Red Rabbits"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+teamManager.roundScores.get("Red Rabbits")).setScore(9-score);
                                            break;
                                        case "YellowYaks":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+teamRoundScores.get("Yellow Yaks"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+teamManager.roundScores.get("Yellow Yaks")).setScore(9-score);
                                            break;
                                        case "GreenGuardians":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+teamRoundScores.get("Green Guardians"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+teamManager.roundScores.get("Green Guardians")).setScore(9-score);
                                            break;
                                        case "BlueBats":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+teamRoundScores.get("Blue Bats"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+teamManager.roundScores.get("Blue Bats")).setScore(9-score);
                                            break;
                                        case "PurplePandas":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+teamRoundScores.get("Purple Pandas"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+teamManager.roundScores.get("Purple Pandas")).setScore(9-score);
                                            break;
                                        case "PinkPiglets":
                                            scoreboards.get(p.ign).getObjective("sg").getScoreboard().resetScores(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+teamRoundScores.get("Pink Piglets"));
                                            scoreboards.get(p.ign).getObjective("sg").getScore(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+teamManager.roundScores.get("Pink Piglets")).setScore(9-score);
                                            break;
                                    }
                                }
                                previousStandings.put(p.player.getUniqueId(), tempArray);
                            }
                            break;
                        case "Skybattle":
                            if (roundScores.get(p.ign) != null) {
                                if (p.roundCoins != ((int) roundScores.get(p.ign))) {
                                    scoreboards.get(p.ign).resetScores(ChatColor.YELLOW + "Your Coins: " + ChatColor.WHITE + roundScores.get(p.ign));
                                    scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.YELLOW + "Your Coins: " + ChatColor.WHITE + p.roundCoins).setScore(2);
                                    roundScores.put(p.ign, p.roundCoins);
                                }
                            }
                            if (skybattle.timeLeft > 0 && skybattle.getState().equals("PLAYING")) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.RED + "Time left: " + ChatColor.WHITE + ((int) Math.floor(time.get(p.player.getUniqueId()) / 60)) + ":" + time.get(p.player.getUniqueId()) % 60);
                                scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.BOLD + "" + ChatColor.RED + "Time left: " + ChatColor.WHITE + ((int) Math.floor(skybattle.timeLeft / 60)) + ":" + skybattle.timeLeft % 60).setScore(12);
                                time.put(p.player.getUniqueId(), skybattle.timeLeft);

                                if (skybattle.timeLeft % 40 == 0 && skybattle.timeLeft != 240 && skybattle.timeLeft >= 60) {
                                    skybattle.border.setSize(skybattle.border.getSize() * 0.75, 15);
                                    p.player.sendMessage(ChatColor.DARK_RED+"> Border is Shrinking!");
                                    p.player.sendTitle(" ", ChatColor.RED+"Border shrinking!", 0, 20, 10);
                                } else if ((skybattle.timeLeft - 10) % 40 == 0) {
                                    p.player.sendMessage(ChatColor.RED+"> Border shrinking in 10 seconds!");
                                } else if (skybattle.timeLeft == 60) {
                                    skybattle.border.setSize(5, 60);
                                    p.player.sendMessage(ChatColor.DARK_RED+"> Border will continue shrinking!");
                                } else if (skybattle.timeLeft == 70) {
                                    p.player.sendMessage(ChatColor.RED+"> Final shrink in 10 seconds!");
                                }

                            } else if (skybattle.timeLeft > 0 && skybattle.getState().equals("STARTING")) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.RED + "Starting in: " + ChatColor.WHITE + ((int) Math.floor(time.get(p.player.getUniqueId()) / 60)) + ":" + time.get(p.player.getUniqueId()) % 60);
                                scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.BOLD + "" + ChatColor.RED + "Starting in: " + ChatColor.WHITE + ((int) Math.floor(skybattle.timeLeft / 60)) + ":" + skybattle.timeLeft % 60).setScore(12);
                                time.put(p.player.getUniqueId(), skybattle.timeLeft);
                                p.player.sendTitle("Starting in:", "> " + skybattle.timeLeft + " <", 0, 20, 0);
                            } else if (skybattle.timeLeft == 0 && skybattle.getState().equals("PLAYING") && skybattle.roundNum < 3) {
                                skybattle.nextRound();
                                // round ending todo
                            } else if (skybattle.timeLeft == 0 && skybattle.getState().equals("STARTING")) {
                                skybattle.setState("PLAYING");
                                //todo remove "starting in 0:1"
                                skybattle.timeLeft = 240;
                            } else if (skybattle.timeLeft == 0 && skybattle.getState().equals("PLAYING") && skybattle.roundNum >= 3) {
                                skybattle.resetMap();
                                skybattle.resetBorder();
                                // ending todo
                            }

                            if (roundNums.get(p.player.getUniqueId()) != skybattle.roundNum) {
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.GREEN + "Round: " + ChatColor.WHITE + (roundNums.get(p.player.getUniqueId()) + 1) + "/3");
                                scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.BOLD + "" + ChatColor.GREEN + "Round: " + ChatColor.WHITE + (skybattle.roundNum + 1) + "/3").setScore(13);
                                roundNums.put(p.player.getUniqueId(), skybattle.roundNum);
                            }
                            if (!(tempArray.equals(previousStandings.get(p.player.getUniqueId())))) {
                                for (int i = 0; i < teamManager.teamNames.size(); i++) {
                                    int score = findIndexInNumberArr(tempArray, (Integer) teamManager.roundScores.get(teamList[i]));
                                    switch (teamManager.teamNames.get(i)) {
                                        case "RedRabbits":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.RED + "Red Rabbits: " + ChatColor.WHITE + teamRoundScores.get("Red Rabbits"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.RED + "Red Rabbits: " + ChatColor.WHITE + teamManager.roundScores.get("Red Rabbits")).setScore(9 - score);
                                            break;
                                        case "YellowYaks":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.YELLOW + "Yellow Yaks: " + ChatColor.WHITE + teamRoundScores.get("Yellow Yaks"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.YELLOW + "Yellow Yaks: " + ChatColor.WHITE + teamManager.roundScores.get("Yellow Yaks")).setScore(9 - score);
                                            break;
                                        case "GreenGuardians":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.GREEN + "Green Guardians: " + ChatColor.WHITE + teamRoundScores.get("Green Guardians"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.GREEN + "Green Guardians: " + ChatColor.WHITE + teamManager.roundScores.get("Green Guardians")).setScore(9 - score);
                                            break;
                                        case "BlueBats":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.BLUE + "Blue Bats: " + ChatColor.WHITE + teamRoundScores.get("Blue Bats"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.BLUE + "Blue Bats: " + ChatColor.WHITE + teamManager.roundScores.get("Blue Bats")).setScore(9 - score);
                                            break;
                                        case "PurplePandas":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.DARK_PURPLE + "Purple Pandas: " + ChatColor.WHITE + teamRoundScores.get("Purple Pandas"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.DARK_PURPLE + "Purple Pandas: " + ChatColor.WHITE + teamManager.roundScores.get("Purple Pandas")).setScore(9 - score);
                                            break;
                                        case "PinkPiglets":
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScoreboard().resetScores(ChatColor.LIGHT_PURPLE + "Pink Piglets: " + ChatColor.WHITE + teamRoundScores.get("Pink Piglets"));
                                            scoreboards.get(p.ign).getObjective("Skybattle").getScore(ChatColor.LIGHT_PURPLE + "Pink Piglets: " + ChatColor.WHITE + teamManager.roundScores.get("Pink Piglets")).setScore(9 - score);
                                            break;
                                    }
                                }
                            }

                            for (int mapX = -225; mapX <= -87; mapX++) {
                                for (int mapZ = -325; mapZ <= -207; mapZ++) {
                                    skybattle.world.spawnParticle(Particle.ASH , mapX, skybattle.borderHeight, mapZ, 1);
                                }
                            }

                            break;
                        //case "Game":
                    }
                }

                switch (game.stage) {
                    case "TGTTOS":
                        tgttos.timeLeft--;
                        break;
                    case "SG":
                        sg.updateEventTimer();
                        break;
                    case "Skybattle":
                        if (skybattle.timeLeft <= 75) { skybattle.borderHeight -= 0.226666667; }
                        skybattle.timeLeft--;
                        break;
                }
                for (String i : teamList) {
                    teamRoundScores.put(i, teamManager.roundScores.get(i));
                }
            }
        }, 0, 20);
    }

    public int findIndexInNumberArr(Integer[] arr, Integer value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return 0;
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
