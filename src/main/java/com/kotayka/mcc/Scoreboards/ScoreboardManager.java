package com.kotayka.mcc.Scoreboards;

import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public class ScoreboardManager {
    private final Players playersObject;

    List<String> teamFullList = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"));
    public List<String> teamList = new ArrayList<>();
    Map<String, String> teamNameFull = new HashMap<>();
    public Map<ScoreboardPlayer, ScoreboardTeam> playerTeams = new HashMap<>();
    Map<String, String> teamIcons = new HashMap<>();
    public List<ScoreboardTeam> scoreboardTeamList = new ArrayList<>();

    public Map<UUID, ScoreboardPlayer> players = new HashMap<>();
    public Map<String, ScoreboardPlayer> playersWithNames = new HashMap<>();
    public List<ScoreboardPlayer> playerList = new ArrayList<>();
    public int timer;
    public Map<String, ScoreboardTeam> teams = new HashMap<>();
    public Map<String, Integer> teamAmount = new HashMap<>();
    public Map<String, ChatColor> teamColors = new HashMap<>();
    org.bukkit.scoreboard.ScoreboardManager manager =  Bukkit.getScoreboardManager();


    // Game Start Variables
    public List<String> teamsReadyList = new ArrayList<>();

    //Reset Variables
    Map<String, Integer> teamAmountFinished = new HashMap<>();
    Boolean teamBonus = true;

    private final Plugin plugin;
    private final MCC mcc;

    ScoreboardTeamManager tabColor;

    final int[] taskId = new int[]{-1,-1};
    int statTime=0;

    public ScoreboardManager(Players players, Plugin plugin, MCC mcc) {
        this.playersObject = players;
        this.plugin = plugin;
        this.mcc = mcc;

        tabColor = new ScoreboardTeamManager(mcc, this);
    }

    public void initilizeVars() {

        teamColors.put("RedRabbits", ChatColor.RED);
        teamColors.put("YellowYaks", ChatColor.YELLOW);
        teamColors.put("GreenGuardians", ChatColor.GREEN);
        teamColors.put("BlueBats", ChatColor.BLUE);
        teamColors.put("PurplePandas", ChatColor.DARK_PURPLE);
        teamColors.put("PinkPiglets", ChatColor.LIGHT_PURPLE);

        teamNameFull.put("RedRabbits","Red Rabbits");
        teamNameFull.put("YellowYaks","Yellow Yaks");
        teamNameFull.put("GreenGuardians","Green Guardians");
        teamNameFull.put("BlueBats","Blue Bats");
        teamNameFull.put("PurplePandas","Purple Pandas");
        teamNameFull.put("PinkPiglets","Pink Piglets");

        teamIcons.put("RedRabbits","Ⓡ");
        teamIcons.put("YellowYaks","Ⓨ");
        teamIcons.put("GreenGuardians","Ⓖ");
        teamIcons.put("BlueBats","Ⓑ");
        teamIcons.put("PurplePandas","Ⓤ");
        teamIcons.put("PinkPiglets","Ⓟ");

        teamAmount.put("RedRabbits",0);
        teamAmount.put("YellowYaks",0);
        teamAmount.put("GreenGuardians",0);
        teamAmount.put("BlueBats",0);
        teamAmount.put("PurplePandas",0);
        teamAmount.put("PinkPiglets",0);

        teamAmountFinished.put("RedRabbits",0);
        teamAmountFinished.put("YellowYaks",0);
        teamAmountFinished.put("GreenGuardians",0);
        teamAmountFinished.put("BlueBats",0);
        teamAmountFinished.put("PurplePandas",0);
        teamAmountFinished.put("PinkPiglets",0);
    }

    public void addPlayer(Participant p) {
        if (!teamList.contains(p.team)) {
            teamList.add(p.team);
            ScoreboardTeam t = new ScoreboardTeam(p.team);
            teams.put(p.team, t);
            scoreboardTeamList.add(t);
        }
        ScoreboardPlayer scoreboardPlayer = new ScoreboardPlayer(manager.getNewScoreboard(), p);
        p.player.setScoreboard(scoreboardPlayer.board);
        players.put(p.player.getUniqueId(), scoreboardPlayer);
        playerList.add(scoreboardPlayer);
        teamAmount.put(p.team, teamAmount.get(p.team)+1);
        createTeams(scoreboardPlayer);
        playerTeams.put(scoreboardPlayer, teams.get(scoreboardPlayer.player.team));
        playersWithNames.put(p.ign, scoreboardPlayer);
        createLobbyBoard(scoreboardPlayer);

        addPlayerToReadyTeam(scoreboardPlayer);

        for (ScoreboardPlayer player : playerList) {
            GenerateTeamsRound(player.currentObj, player);
        }

        tabColor.newPlayer(p.team, p.player);
    }

    public void start() {
        initilizeVars();
    }

    public void removeTeam(String teamName) {
        for (ScoreboardPlayer p : playerList) {
            Objective obj = p.currentObj;
            obj.getScoreboard().resetScores(teamColors.get(teamName)+teamName);
            teamsReadyList.remove(teamName);
        }
    }

    public void addTeam(String teamName) {
        for (ScoreboardPlayer p : playerList) {
            Objective obj = p.currentObj;
            obj.getScore(teamColors.get(teamName)+teamName).setScore(20);
            teamsReadyList.add(teamName);
        }
    }

    public void clearTeams() {
        for (ScoreboardPlayer p : playerList) {
            Objective obj = p.currentObj;
            for (String teamName : teamsReadyList) {
                obj.getScoreboard().resetScores(teamColors.get(teamName)+teamName);
            }
        }
    }

    public void addPlayerToReadyTeam(ScoreboardPlayer p) {
        Objective obj = p.currentObj;
        for (String teamName : teamsReadyList) {
            obj.getScore(teamColors.get(teamName)+teamName).setScore(20);
        }
    }

    public void GenerateTeamsRound(Objective obj, ScoreboardPlayer p) {
        for (Object[] objects : p.teamLines) {
            if ((int) objects[1] != -1) {
                obj.getScoreboard().resetScores((String) objects[0]);
            }
        }

        List<Integer> teamScores = new ArrayList<>();
        for (ScoreboardTeam team : scoreboardTeamList) {
            teamScores.add(team.roundScore);
        }
        Collections.sort(teamScores, Collections.reverseOrder());
        List<String> t = new ArrayList<>(teamList);
        for (int i = 0; i < teamScores.size(); i++) {
            for (ScoreboardTeam team : scoreboardTeamList) {
                if (team.roundScore==teamScores.get(i) && t.contains(team.teamName)) {
                    obj.getScore(teamIcons.get(team.teamName)+teamColors.get(team.teamName)+" "+teamNameFull.get(team.teamName)+ChatColor.WHITE+": "+teamScores.get(i)).setScore(14-i);
                    int index = teamFullList.indexOf(team.teamName);
                    p.teamLines[index][0] = teamIcons.get(team.teamName)+teamColors.get(team.teamName)+" "+teamNameFull.get(team.teamName)+ChatColor.WHITE+": "+teamScores.get(i);
                    p.teamLines[index][1] = 14-i;
                    t.remove(team.teamName);
                }
            }
        }

        if (p.lines.get(obj).containsKey(2)) {
            obj.getScoreboard().resetScores(p.lines.get(obj).get(2));
        }

        obj.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+p.roundScore).setScore(2);
        p.lines.get(obj).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+p.roundScore);
    }

    public void GenerateTeamsGame(Objective obj, ScoreboardPlayer p) {
        for (Object[] objects : p.teamLines) {
            if ((int) objects[1] != -1) {
                obj.getScoreboard().resetScores((String) objects[0]);
            }
        }

        List<Integer> teamScores = new ArrayList<>();
        for (ScoreboardTeam team : scoreboardTeamList) {
            teamScores.add(team.gameScore);
        }
        Collections.sort(teamScores, Collections.reverseOrder());
        List<String> t = new ArrayList<>(teamList);
        for (int i = 0; i < teamScores.size(); i++) {
            for (ScoreboardTeam team : scoreboardTeamList) {
                if (team.gameScore==teamScores.get(i) && t.contains(team.teamName)) {
                    obj.getScore(teamIcons.get(team.teamName)+teamColors.get(team.teamName)+" "+teamNameFull.get(team.teamName)+ChatColor.WHITE+": "+teamScores.get(i)).setScore(14-i);
                    int index = teamFullList.indexOf(team.teamName);
                    p.teamLines[index][0] = teamIcons.get(team.teamName)+teamColors.get(team.teamName)+" "+teamNameFull.get(team.teamName)+ChatColor.WHITE+": "+teamScores.get(i);
                    p.teamLines[index][1] = 14-i;
                    t.remove(team.teamName);

                    if (team.teamName == p.player.team) {
                        if (p.lines.get(obj).containsKey(2)) {
                            obj.getScoreboard().resetScores(p.lines.get(obj).get(2));
                        }

                        obj.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+p.gameScore).setScore(2);
                        p.lines.get(obj).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+p.roundScore);
                    }
                }
            }
        }
    }

    public void resetPlayerRounds(Objective obj, ScoreboardPlayer p) {
        if (p.lines.get(obj).containsKey(1)) {
            obj.getScoreboard().resetScores(p.lines.get(obj).get(1));
        }

        obj.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.roundScore).setScore(1);
        p.lines.get(obj).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+p.roundScore);
    }

    public void createLobbyBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective lobby = player.board.registerNewObjective("lobby", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("Lobby",lobby);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(lobby, lines);

        lobby.getScore(ChatColor.RED+""+ChatColor.BOLD + "Event begins in:").setScore(22);
        lobby.getScore(ChatColor.GREEN + "Teams ready").setScore(21);
        lobby.getScore(ChatColor.RESET.toString()).setScore(19);
        lobby.getScore(ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:").setScore(18);
        lobby.getScore(teamColors.get(player.player.team)+player.player.fullName).setScore(17);
        lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(16);
        lobby.getScore(ChatColor.GREEN+"Game Scores").setScore(15);
        lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        lobby.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore).setScore(1);

        GenerateTeamsGame(lobby, player);

        player.lines.get(lobby).put(21, ChatColor.RED+""+ChatColor.BOLD + "Event begins in:");
        player.lines.get(lobby).put(20, ChatColor.GREEN + "Teams ready");
        player.lines.get(lobby).put(19, ChatColor.RESET.toString());
        player.lines.get(lobby).put(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:");
        player.lines.get(lobby).put(17, teamColors.get(player.player.team)+player.player.fullName);
        player.lines.get(lobby).put(16, ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(lobby).put(15, ChatColor.GREEN+"Game Scores");
        player.lines.get(lobby).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(lobby).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore);

        player.currentObj = lobby;
        lobby.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createMidLobbyBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective lobby = player.board.registerNewObjective("lobby", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("Lobby",lobby);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(lobby, lines);

        lobby.getScore(ChatColor.RED+""+ChatColor.BOLD + "Next Round begins in:").setScore(22);
        lobby.getScore(ChatColor.GREEN + "Teams ready").setScore(21);
        lobby.getScore(ChatColor.RESET.toString()).setScore(19);
        lobby.getScore(ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:").setScore(18);
        lobby.getScore(teamColors.get(player.player.team)+player.player.fullName).setScore(17);
        lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(16);
        lobby.getScore(ChatColor.GREEN+"Game Scores").setScore(15);
        lobby.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        lobby.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore).setScore(1);

        GenerateTeamsGame(lobby, player);

        player.lines.get(lobby).put(21, ChatColor.RED+""+ChatColor.BOLD + "Next Round begins in:");
        player.lines.get(lobby).put(20, ChatColor.GREEN + "Teams ready");
        player.lines.get(lobby).put(19, ChatColor.RESET.toString());
        player.lines.get(lobby).put(18, ChatColor.GREEN+""+ChatColor.BOLD + "Your Team:");
        player.lines.get(lobby).put(17, teamColors.get(player.player.team)+player.player.fullName);
        player.lines.get(lobby).put(16, ChatColor.RESET.toString()+ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(lobby).put(15, ChatColor.GREEN+"Game Scores");
        player.lines.get(lobby).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(lobby).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore);

        player.currentObj = lobby;
        lobby.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createSkybattleBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }

        Objective skybattleScoreboard = player.board.registerNewObjective("Skybattle", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("Skybattle", skybattleScoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(skybattleScoreboard, lines);

        skybattleScoreboard.getScore(ChatColor.AQUA +""+ChatColor.BOLD+ "Game 0/6:"+ChatColor.WHITE+" Skybattle").setScore(23);
        skybattleScoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Map: "+ChatColor.WHITE+"Skybattle").setScore(22);
        skybattleScoreboard.getScore(ChatColor.GREEN+""+ChatColor.BOLD + "Round: "+ ChatColor.WHITE+mcc.skybattle.roundNum+"/3").setScore(21);
        skybattleScoreboard.getScore(ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        skybattleScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(19);
        skybattleScoreboard.getScore(ChatColor.GREEN+"Game Scores").setScore(15);
        skybattleScoreboard.getScore(ChatColor.RESET.toString()).setScore(4);
        skybattleScoreboard.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0").setScore(2);
        skybattleScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0").setScore(1);

        GenerateTeamsRound(skybattleScoreboard, player);

        player.lines.get(skybattleScoreboard).put(23, ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" Skybattle");
        player.lines.get(skybattleScoreboard).put(22, ChatColor.AQUA+""+ChatColor.BOLD + "Map: "+ChatColor.WHITE+"Skybattle");
        player.lines.get(skybattleScoreboard).put(21, ChatColor.GREEN+""+ChatColor.BOLD + "Round: "+ ChatColor.WHITE+mcc.skybattle.roundNum+"/3");
        player.lines.get(skybattleScoreboard).put(20, ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(skybattleScoreboard).put(15, ChatColor.GREEN+"Game Scores");
        player.lines.get(skybattleScoreboard).put(4, ChatColor.RESET.toString());
        player.lines.get(skybattleScoreboard).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0");
        player.lines.get(skybattleScoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");

        player.currentObj = skybattleScoreboard;
        skybattleScoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createTGTTOSBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective tgttosScoreboard = player.board.registerNewObjective("tgttos", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("tgttos",tgttosScoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(tgttosScoreboard, lines);

        tgttosScoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" TGTTOS").setScore(23);
        tgttosScoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Map: "+ChatColor.WHITE+mcc.tgttos.mapOrder[mcc.tgttos.gameOrder[mcc.tgttos.roundNum]]).setScore(22);
        tgttosScoreboard.getScore(ChatColor.GREEN +""+ChatColor.BOLD+ "Round: "+ChatColor.WHITE+"1/7").setScore(21);
        tgttosScoreboard.getScore(ChatColor.RED +""+ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        tgttosScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(19);
        tgttosScoreboard.getScore(ChatColor.GREEN+"Game Scores").setScore(15);
        tgttosScoreboard.getScore(ChatColor.RESET.toString()).setScore(4);
        tgttosScoreboard.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0").setScore(2);
        tgttosScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0").setScore(1);

        GenerateTeamsRound(tgttosScoreboard, player);

        player.lines.get(tgttosScoreboard).put(23, ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" TGTTOS");
        player.lines.get(tgttosScoreboard).put(22, ChatColor.AQUA+""+ChatColor.BOLD + "Map: "+ChatColor.WHITE+mcc.tgttos.mapOrder[mcc.tgttos.gameOrder[mcc.tgttos.roundNum]]);
        player.lines.get(tgttosScoreboard).put(21, ChatColor.GREEN+""+ChatColor.BOLD + "Round: "+ChatColor.WHITE+"1/7");
        player.lines.get(tgttosScoreboard).put(20, ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(tgttosScoreboard).put(15, ChatColor.GREEN+"Game Scores");
        player.lines.get(tgttosScoreboard).put(4, ChatColor.RESET.toString());
        player.lines.get(tgttosScoreboard).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0");
        player.lines.get(tgttosScoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");

        player.currentObj = tgttosScoreboard;
        tgttosScoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createAceRaceBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective aceRacesScoreboard = player.board.registerNewObjective("aceRace", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("aceRace",aceRacesScoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(aceRacesScoreboard, lines);

        aceRacesScoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" Ace Race").setScore(23);
        aceRacesScoreboard.getScore(ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        aceRacesScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(19);
        aceRacesScoreboard.getScore(ChatColor.GREEN+"Game Scores").setScore(15);
        aceRacesScoreboard.getScore(ChatColor.RESET.toString()).setScore(4);
        aceRacesScoreboard.getScore(ChatColor.LIGHT_PURPLE+"Completed Laps: "+ChatColor.WHITE+"0/3").setScore(3);
        aceRacesScoreboard.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0").setScore(2);
        aceRacesScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0").setScore(1);

        GenerateTeamsRound(aceRacesScoreboard, player);

        player.lines.get(aceRacesScoreboard).put(23, ChatColor.AQUA +""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" TGTTOS");
        player.lines.get(aceRacesScoreboard).put(20, ChatColor.RED +""+ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(aceRacesScoreboard).put(15, ChatColor.GREEN+"Game Scores");
        player.lines.get(aceRacesScoreboard).put(4, ChatColor.RESET.toString());
        player.lines.get(aceRacesScoreboard).put(3, ChatColor.LIGHT_PURPLE+"Completed Laps: "+ChatColor.WHITE+"0/3");
        player.lines.get(aceRacesScoreboard).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0");
        player.lines.get(aceRacesScoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");

        player.currentObj = aceRacesScoreboard;
        aceRacesScoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void endGame() {
        for (String t : teamList) {
            teams.get(t).gameScore+=teams.get(t).roundScore;
            teams.get(t).roundScore=0;
        }
        for (ScoreboardPlayer p : playerList) {
            p.gameScore+=p.roundScore;
            p.roundScore=0;
        }
        resetVars();
    }

    public void createSGBoard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective sgScoreboard = player.board.registerNewObjective("SG", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("SG",sgScoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(sgScoreboard, lines);

        sgScoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" Survival Games").setScore(23);
        sgScoreboard.getScore(ChatColor.AQUA +""+ ChatColor.BOLD+ "Map: "+ChatColor.WHITE+"BCA").setScore(22);
        sgScoreboard.getScore(ChatColor.GREEN+""+ChatColor.BOLD + "Next Event: "+ChatColor.LIGHT_PURPLE+"Starting").setScore(21);
        sgScoreboard.getScore(ChatColor.RED +""+ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        sgScoreboard.getScore(ChatColor.RESET.toString()).setScore(19);
        sgScoreboard.getScore(ChatColor.AQUA+"Game Coins:").setScore(15);
        sgScoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        sgScoreboard.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0").setScore(2);
        sgScoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0").setScore(1);

        GenerateTeamsRound(sgScoreboard, player);

        player.lines.get(sgScoreboard).put(23, ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" Survival Games");
        player.lines.get(sgScoreboard).put(22, ChatColor.AQUA+""+ChatColor.BOLD + "Map: "+ChatColor.WHITE+"BCA");
        player.lines.get(sgScoreboard).put(21, ChatColor.GREEN+""+ ChatColor.BOLD+ "Next Event: "+ChatColor.LIGHT_PURPLE+"Starting");
        player.lines.get(sgScoreboard).put(20, ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(sgScoreboard).put(19, ChatColor.RESET.toString());
        player.lines.get(sgScoreboard).put(15, ChatColor.AQUA+"Game Coins:");
        player.lines.get(sgScoreboard).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(sgScoreboard).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0");
        player.lines.get(sgScoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");

        player.currentObj = sgScoreboard;
        sgScoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createBSABMScoreboard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective scoreboard = player.board.registerNewObjective("BuildMart", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("BuildMart",scoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(scoreboard, lines);

        scoreboard.getScore(ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" Build Mart").setScore(23);
        scoreboard.getScore(ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        scoreboard.getScore(ChatColor.RESET.toString()).setScore(19);
        scoreboard.getScore(ChatColor.AQUA+"Game Coins:").setScore(15);
        scoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        scoreboard.getScore(ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0").setScore(2);
        scoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0").setScore(1);

        GenerateTeamsRound(scoreboard, player);

        player.lines.get(scoreboard).put(23, ChatColor.AQUA+""+ChatColor.BOLD + "Game 0/6:"+ChatColor.WHITE+" BuildMart");
        player.lines.get(scoreboard).put(20, ChatColor.RED +""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(scoreboard).put(19, ChatColor.RESET.toString());
        player.lines.get(scoreboard).put(15, ChatColor.AQUA+"Game Coins:");
        player.lines.get(scoreboard).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(scoreboard).put(2, ChatColor.GREEN+"Team Coins: "+ChatColor.WHITE+"0");
        player.lines.get(scoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+"0");

        player.currentObj = scoreboard;
        scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createDecisionDome(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective scoreboard = player.board.registerNewObjective("DecisionDome", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("DecisionDome",scoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(scoreboard, lines);

        scoreboard.getScore(ChatColor.GREEN+"Event: "+ChatColor.LIGHT_PURPLE+"Voting!").setScore(21);
        scoreboard.getScore(ChatColor.RED+""+ ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        scoreboard.getScore(ChatColor.RESET.toString()).setScore(19);
        scoreboard.getScore(ChatColor.AQUA+"Game Coins:").setScore(15);
        scoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        scoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+ player.gameScore).setScore(1);

        GenerateTeamsGame(scoreboard, player);

        player.lines.get(scoreboard).put(21, ChatColor.GREEN+"Event: "+ChatColor.LIGHT_PURPLE+"Voting!");
        player.lines.get(scoreboard).put(20, ChatColor.RED +""+ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(scoreboard).put(19, ChatColor.RESET.toString());
        player.lines.get(scoreboard).put(15, ChatColor.AQUA+"Game Coins:");
        player.lines.get(scoreboard).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(scoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore);

        player.currentObj = scoreboard;
        scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createInstructionScoreboard(ScoreboardPlayer player) {
        if (player.currentObj != null) {
            player.currentObj.unregister();
        }
        Objective scoreboard = player.board.registerNewObjective("Instructions", "dummy", ChatColor.YELLOW+""+ChatColor.BOLD+"MBC");
        player.objectiveMap.put("Instructions",scoreboard);
        Map<Integer, String> lines = new HashMap<>();
        player.lines.put(scoreboard, lines);

        scoreboard.getScore(ChatColor.GREEN+"Game Starting Soon!").setScore(21);
        scoreboard.getScore(ChatColor.RED+""+ ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0").setScore(20);
        scoreboard.getScore(ChatColor.RESET.toString()+ChatColor.RESET.toString()).setScore(3);
        scoreboard.getScore(ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+ player.gameScore).setScore(1);

        GenerateTeamsGame(scoreboard, player);

        player.lines.get(scoreboard).put(21, ChatColor.GREEN+"Game Starting Soon!");
        player.lines.get(scoreboard).put(20, ChatColor.RED+""+ ChatColor.BOLD+ "Time left: "+ChatColor.WHITE+"0:0");
        player.lines.get(scoreboard).put(3, ChatColor.RESET.toString()+ChatColor.RESET.toString());
        player.lines.get(scoreboard).put(1, ChatColor.YELLOW+"Your Coins: "+ChatColor.WHITE+player.gameScore);

        player.currentObj = scoreboard;
        scoreboard.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void createTeams(ScoreboardPlayer player) {
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};

        Scoreboard board = player.board;

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

        red.setPrefix(ChatColor.WHITE+"Ⓡ ");
        yellow.setPrefix(ChatColor.WHITE+"Ⓨ ");
        green.setPrefix(ChatColor.WHITE+"Ⓖ ");
        blue.setPrefix(ChatColor.WHITE+"Ⓑ ");
        purple.setPrefix(ChatColor.WHITE+"Ⓤ ");
        pink.setPrefix(ChatColor.WHITE+"Ⓟ ");

        player.teams = new Team[]{red, yellow, green, blue, purple, pink};
    }

    public void addPlayersToTeam() {
        for (ScoreboardPlayer p : playerList) {
            for (ScoreboardPlayer p2 : playerList) {
                p.teams[teamList.indexOf(playerTeams.get(p2).teamName)].addEntry(p2.player.ign);
            }
        }
    }

    public void addScore(ScoreboardPlayer player, int amount) {
        player.roundScore = player.roundScore+amount;
        playerTeams.get(player).roundScore+=amount;

        resetPlayerRounds(player.currentObj, player);
        for (ScoreboardPlayer p : playerList) {
            GenerateTeamsRound(p.currentObj, p);
        }
    }

    public void addTeamScore(String team, int amount) {
        Bukkit.broadcastMessage(team+" scored");
        for (ScoreboardPlayer p : playerList) {
            if (playerTeams.get(p).teamName.equals(team)) {
                p.roundScore = p.roundScore+amount;
                playerTeams.get(p).roundScore+=amount;

                resetPlayerRounds(p.currentObj, p);

                }
            }
        for (ScoreboardPlayer p : playerList) {
            GenerateTeamsRound(p.currentObj, p);
        }
    }


    public void placementPoints(ScoreboardPlayer player, int amount, int playersFinished) {
        addScore(player, (playerList.size()-playersFinished)*amount);
    }

    public void teamFinish(ScoreboardPlayer player, int amount) {
        teamAmountFinished.put(player.player.team, teamAmountFinished.get(player.player.team)+1);
        if (teamBonus) {
            for (String t : teamList) {
                if (teamAmountFinished.get(t) == teamAmount.get(t)) {
                    teamBonus=false;
                    for (ScoreboardPlayer p : playerList) {
                        if (playerTeams.get(p).teamName.equals(t)) {
                            addScore(p, amount);
                        }
                    }
                    Bukkit.broadcastMessage(teamIcons.get(t)+ teamColors.get(t)+ " " + ChatColor.BOLD+teamNameFull.get(t)+ChatColor.WHITE + "" + ChatColor.BOLD+" finished first.");
                }
            }
        }
    }

    public void lastOneStanding(ScoreboardPlayer player, String game) {
        teamAmountFinished.put(player.player.team, teamAmountFinished.get(player.player.team)+1);
        int teamsDead = 0;
        for (String t : teamList) {
            if (teamAmountFinished.get(t) == teamAmount.get(t)) {
                teamsDead++;
            }
        }
        Bukkit.broadcastMessage("teamsDead == " + teamsDead);
        Bukkit.broadcastMessage("teamList.size()-1 == " + (teamList.size()-1));
        if (teamsDead >= teamList.size()-1) {
            switch (game) {
                case "SG":
                    mcc.sg.endGame();
                    break;
                case "Skybattle":
                    timer = 0;
                    break;
            }
        }
    }

    public void changeLine(int line, String value) {
        for (ScoreboardPlayer p : playerList) {
            Objective obj = p.currentObj;
            if (p.lines.get(obj).containsKey(line)) {
                obj.getScoreboard().resetScores(p.lines.get(obj).get(line));
            }

            obj.getScore(value).setScore(line);
            p.lines.get(obj).put(line, value);
        }
    }

    public void changePlayerLine(int line, String value, ScoreboardPlayer p) {
        Objective obj = p.currentObj;
        if (p.lines.get(obj).containsKey(line)) {
            obj.getScoreboard().resetScores(p.lines.get(obj).get(line));
        }

        obj.getScore(value).setScore(line);
        p.lines.get(obj).put(line, value);
    }

    public String getFormattedTime(int seconds) {
        return String.format("%02d", seconds/60) +":"+String.format("%02d", seconds%60);
    }

    public void gameTimerEnded(String game) {
        switch (game) {
            case "TGTTOS":
                mcc.tgttos.nextRound();
                break;
            case "SG":
                mcc.sg.events();
                break;
            case "Skybattle":
                mcc.skybattle.timeEndEvents();
                break;
            case "BuildMart":
                mcc.game.endGame();
                break;
            case "AceRace":
                mcc.aceRace.endGame();
                break;
            case "DD":
                mcc.decisionDome.timerEnds();
                break;
            case "DDOne":
                mcc.decisionDome.voteCounterEnds();
                break;
            case "DDTwo":
                mcc.decisionDome.addLevitation();
                break;
            case "DDThree":
                mcc.decisionDome.nextGame();
                break;
            case "Lobby":
                mcc.decisionDome.start();
                break;
            case "SGEnd":
                mcc.sg.completeEndGame();
                break;
            case "game":
                mcc.game.instructions.timerEnds();
                break;
        }
    }

    public void startTimerForGame(int time, String game) {
        timer = time;
        if (taskId[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskId[0]);
        }
        taskId[0] = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    for (ScoreboardPlayer p : playerList) {
                        Objective obj = p.currentObj;
                        if (p.lines.get(obj).containsKey(20)) {
                            obj.getScoreboard().resetScores(p.lines.get(obj).get(20));
                        }
                        String value = ChatColor.RED+""+ChatColor.BOLD + "Time left: "+ChatColor.WHITE+getFormattedTime(timer);
                        obj.getScore(value).setScore(20);
                        p.lines.get(obj).put(20, value);

                        // Special Cases:
                        if (mcc.game.stage.equals("Skybattle")) {
                            mcc.skybattle.timedEventsHandler(timer, p);
                        }
                    }
                    // Events that shouldn't loop for each player
                    if (mcc.game.stage.equals("Skybattle")) {
                        mcc.skybattle.specialEvents(timer);
                    }
                }
                else if (timer == 0) {
                    for (ScoreboardPlayer p : playerList) {
                        Objective obj = p.currentObj;
                        if (p.lines.get(obj).containsKey(20)) {
                            obj.getScoreboard().resetScores(p.lines.get(obj).get(20));
                        }
                        String value = ChatColor.RED + "Waiting...";;
                        obj.getScore(value).setScore(20);
                        p.lines.get(obj).put(20, value);
                    }
                    gameTimerEnded(game);
                    // teleport back to spawn if game is over (this is only for skybattle right now)
                    if (mcc.gameIsOver) {
                        mcc.game.endGame();
                        timer = -1;
                    }
                }
                timer--;
            }

        }, 20L, 20L);
    }

    public void resetVars() {
        teamAmountFinished = new HashMap<>();
        teamAmountFinished.put("RedRabbits",0);
        teamAmountFinished.put("YellowYaks",0);
        teamAmountFinished.put("GreenGuardians",0);
        teamAmountFinished.put("BlueBats",0);
        teamAmountFinished.put("PurplePandas",0);
        teamAmountFinished.put("PinkPiglets",0);
        timer = 0;
        teamBonus = true;
    }
}
