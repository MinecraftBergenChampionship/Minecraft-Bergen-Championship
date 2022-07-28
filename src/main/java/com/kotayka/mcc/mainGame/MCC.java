package com.kotayka.mcc.mainGame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
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
    public int gameRound = 0;

//  Managers
    private final Players players = new Players(this);
    private final NPCManager npcManager = new NPCManager(this, players);
    public teamManager teamManager;

//  Games
    private final TGTTOS tgttos = new TGTTOS(players, npcManager, this);
    private final Skybattle skybattle = new Skybattle(players, plugin, this);

//  Game Manager
    // temporarily commented out
    // private final Game game = new Game(tgttos, this);
    private final Game game = new Game(skybattle, this);

//  Scoreboard
    public Map roundScores = new HashMap();
    public Map teamRoundScores = new HashMap();

    @Override
    public void onEnable() {
        manager = Bukkit.getScoreboardManager();
        scoreBoards();
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
        //getCommand("ping").setExecutor(new ping());
        // temp
        /*
        loadMaps();
        TGTTOSGame();
         */


        // startEvent();

        // Temp
        if (Bukkit.getWorld("Skybattle") == null) {
            skybattle.world = Bukkit.getWorld("world");
        }
        else {
            skybattle.world = Bukkit.getWorld("Skybattle");
        }

        skybattle.resetBorder();

        SkybattleGame();
    }

    /*

    public void startEvent() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            switch(game.stage) {
                case "Skybattle":

            }
        }, 20, 20);
    }
     */
    public void loadMaps() {
        tgttos.loadMaps();
    }

    public void TGTTOSGame() {
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
                                for (Participant p : players.participants) {
                                    if (event.getPlayer().getName().equals(p.ign)) {
                                        p.roundCoins+=tgttos.playerPoints;
                                        Bukkit.broadcastMessage(p.toString());
                                        teamManager.roundScores.put(p.fullName, ((int) teamManager.roundScores.get(p.fullName))+tgttos.playerPoints);
                                    }
                                }
                                Bukkit.broadcastMessage(ChatColor.GOLD+event.getPlayer().getName()+ChatColor.GRAY+ " finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
                                event.getPlayer().sendMessage(ChatColor.WHITE+"[+"+String.valueOf(tgttos.playerPoints)+"] "+ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GRAY+" place!");
                                tgttos.playerPoints--;
                                if (tgttos.playerPoints <= 0) {
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

        //Lobby
        Objective lobby = board.registerNewObjective("lobby", "dummy", ChatColor.BOLD+""+ChatColor.YELLOW+"MCC");
        Score lobbyEventBegins = lobby.getScore(ChatColor.BOLD+""+ChatColor.RED + "Event begins in:");
        lobbyEventBegins.setScore(9);
        Score lobbyTimer = lobby.getScore(ChatColor.WHITE + "Waiting...");
        lobbyTimer.setScore(8);
        Score lobbySpace1 = lobby.getScore(ChatColor.RESET.toString());
        lobbySpace1.setScore(7);
        Score lobbyPlayers = lobby.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Players: " + ChatColor.WHITE +String.format("%d/16",Bukkit.getOnlinePlayers().size()));
        lobbyPlayers.setScore(6);
        Score lobbySpace2 = lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
        lobbySpace2.setScore(5);
        Score lobbyTeamTitle = lobby.getScore(ChatColor.BOLD+""+ChatColor.WHITE + "Your Team:");
        lobbyTeamTitle.setScore(4);
        Score lobbyTeam = lobby.getScore(ChatColor.AQUA + "none");
        lobbyTeam.setScore(3);
        Score lobbySpace3 = lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString());
        lobbySpace3.setScore(2);
        Score lobbyEventCoins = lobby.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Event Coins: " + ChatColor.WHITE+"0");
        lobbyEventCoins.setScore(1);
        Score lobbyTeamCoins = lobby.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Team Coins: " + ChatColor.WHITE+"0");
        lobbyTeamCoins.setScore(0);

        //TGTTOS
        Objective tgttosScoreboard = board.registerNewObjective("tgttos", "dummy", ChatColor.BOLD+""+ChatColor.YELLOW+"MCC");
        Score tgttosGameNum = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Game "+gameRound+"/8:"+ChatColor.WHITE+" TGTTOSAWAF");
        tgttosGameNum.setScore(15);
        Score tgttosMap = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Map: "+ChatColor.WHITE+tgttos.mapOrder[tgttos.gameOrder[tgttos.roundNum]]);
        tgttosMap.setScore(14);
        Score tgttosRoundNum = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ChatColor.WHITE+(tgttos.roundNum+1)+"/7");
        tgttosRoundNum.setScore(13);
        Score tgttosTimeLeft = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.RED + "Time left: "+ChatColor.WHITE+((int) Math.floor(timeLeft/60))+":"+timeLeft%60);
        tgttosTimeLeft.setScore(12);
        Score tgttosSpace1 = tgttosScoreboard.getScore(ChatColor.RESET.toString());
        tgttosSpace1.setScore(11);
        Score tgttosGameCoins = tgttosScoreboard.getScore(ChatColor.AQUA+"Game Coins:");
        tgttosGameCoins.setScore(10);
        Score tgttosRed = tgttosScoreboard.getScore(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+"0");
        tgttosRed.setScore(9);
        Score tgttosYellow = tgttosScoreboard.getScore(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+"0");
        tgttosYellow.setScore(8);
        Score tgttosGreen = tgttosScoreboard.getScore(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+"0");
        tgttosGreen.setScore(7);
        Score tgttosBlue = tgttosScoreboard.getScore(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+"0");
        tgttosBlue.setScore(6);
        Score tgttosPurple = tgttosScoreboard.getScore(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+"0");
        tgttosPurple.setScore(5);
        Score tgttosPink = tgttosScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+"0");
        tgttosPink.setScore(4);
        Score tgttosSpace2 = tgttosScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
        tgttosSpace2.setScore(3);
        Score tgttosCoins = tgttosScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");
        tgttosCoins.setScore(2);

        // Skybattle
        Objective skybattleScoreboard = board.registerNewObjective("Skybattle", "dummy", ChatColor.BOLD+""+ChatColor.YELLOW+"MCC");
        Score skybattleGameNum = skybattleScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Game "+gameRound+"/8:"+ChatColor.WHITE+" Skybattle");
        skybattleGameNum.setScore(15);
        Score skybattleMap = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.AQUA + "Map: "+ChatColor.WHITE+"Skybattle");
        skybattleMap.setScore(14);
        // TODO?
        Score skybattleRound = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ChatColor.WHITE+(skybattle.roundNum+1)+"/3");
        skybattleRound.setScore(13);
        Score skybattleTimeLeft = tgttosScoreboard.getScore(ChatColor.BOLD+""+ChatColor.RED + "Time left: "+ChatColor.WHITE+((int) Math.floor(timeLeft/60))+":"+timeLeft%60);
        skybattleTimeLeft.setScore(12);
        Score skybattleSpace = tgttosScoreboard.getScore(ChatColor.RESET.toString());
        skybattleSpace.setScore(11);
        Score skybattleGameCoins = tgttosScoreboard.getScore(ChatColor.AQUA+"Game Coins:");
        skybattleGameCoins.setScore(10);
        Score skybattleRed = tgttosScoreboard.getScore(ChatColor.RED+"Red Rabbits: "+ChatColor.WHITE+"0");
        skybattleRed.setScore(9);
        Score skybattleYellow = tgttosScoreboard.getScore(ChatColor.YELLOW+"Yellow Yaks: "+ChatColor.WHITE+"0");
        skybattleYellow.setScore(8);
        Score skybattleGreen = tgttosScoreboard.getScore(ChatColor.GREEN+"Green Guardians: "+ChatColor.WHITE+"0");
        skybattleGreen.setScore(7);
        Score skybattleBlue = tgttosScoreboard.getScore(ChatColor.BLUE+"Blue Bats: "+ChatColor.WHITE+"0");
        skybattleBlue.setScore(6);
        Score skybattlePurple = tgttosScoreboard.getScore(ChatColor.DARK_PURPLE+"Purple Pandas: "+ChatColor.WHITE+"0");
        skybattlePurple.setScore(5);
        Score skybattlePink = tgttosScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Pink Piglets: "+ChatColor.WHITE+"0");
        skybattlePink.setScore(4);
        Score skybattleSpace2 = tgttosScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString());
        skybattleSpace2.setScore(3);
        Score skybattleCoins = tgttosScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");
        skybattleCoins.setScore(2);

        scoreboards.put(player.ign, board);
        lobby.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.player.setScoreboard(board);
    }

    public void changeScoreboard(String scoreboardName) {
        for (Participant p : players.participants) {
            switch(scoreboardName) {
                case "Lobby":
                    scoreboards.get(p.ign).getObjective("lobby").setDisplaySlot(DisplaySlot.SIDEBAR);
                    break;
                case "TGTTOS":
                    scoreboards.get(p.ign).getObjective("tgttos").setDisplaySlot(DisplaySlot.SIDEBAR);
                    break;
                case "Skybattle":
                    scoreboards.get(p.ign).getObjective("Skybattle").setDisplaySlot(DisplaySlot.SIDEBAR);
            }
        }
    }

    public void scoreBoards() {
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
                                scoreboards.get(p.ign).resetScores(ChatColor.BOLD + "" + ChatColor.RED + "Time left: " + ChatColor.WHITE + ((int) Math.floor(time.get(p.player.getUniqueId()) / 60)) + ":" + time.get(p.player.getUniqueId()) % 60);
                                scoreboards.get(p.ign).getObjective("tgttos").getScore(ChatColor.BOLD + "" + ChatColor.RED + "Time left: " + ChatColor.WHITE + ((int) Math.floor(tgttos.timeLeft / 60)) + ":" + tgttos.timeLeft % 60).setScore(12);
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
        String[] teamNamesFull = {"Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets"};

        for (String team : teamNamesFull) {
            teamRoundScores.put(team, 0);
        }

        List<String> tn = new ArrayList<>(Arrays.asList(teamNames));
        teamManager = new teamManager(players.participants, tn, this);

        for (String team : teamNamesFull) {
            teamManager.roundScores.put(team, 0);
        }
    }

    public void createTeams(Participant player) {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};

        Scoreboard board = scoreboards.get(player.ign);

        Team red = board.registerNewTeam(teamNames[0]);
        Team yellow = board.registerNewTeam(teamNames[1]);
        Team green = board.registerNewTeam(teamNames[2]);
        Team blue = board.registerNewTeam(teamNames[3]);
        Team purple = board.registerNewTeam(teamNames[4]);
        Team pink = board.registerNewTeam(teamNames[5]);

        red.setColor(ChatColor.RED);
        yellow.setColor(ChatColor.YELLOW);
        green.setColor(ChatColor.GREEN);
        blue.setColor(ChatColor.BLUE);
        purple.setColor(ChatColor.DARK_PURPLE);
        pink.setColor(ChatColor.LIGHT_PURPLE);

        red.setPrefix("Ⓡ ");
        yellow.setPrefix("Ⓨ ");
        green.setPrefix("Ⓖ ");
        blue.setPrefix("Ⓑ ");
        purple.setPrefix("Ⓤ ");
        pink.setPrefix("Ⓟ ");

        Team[] teamss = {red,yellow,green,blue,purple,pink};
        teams.put(player.ign, teamss);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
