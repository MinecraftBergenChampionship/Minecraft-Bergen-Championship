package me.kotayka.mbc;

import me.kotayka.mbc.games.AceRace;
import me.kotayka.mbc.games.Lobby;
import me.kotayka.mbc.games.Skybattle;
import me.kotayka.mbc.games.TGTTOS;
import me.kotayka.mbc.teams.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MBC implements Listener {
    // event specifics

    // not sure if we should do this until we resolve the "static or not" stuff
    /*
    public final short MAX_PLAYERS_PER_TEAM = 4;
    public final short MAX_TEAMS = 6;
    public final short MAX_PLAYERS = MAX_PLAYERS_PER_TEAM * MAX_TEAMS;
     */

    public static List<Participant> players = new ArrayList<>(16);
    public static Red red = new Red();
    public static Yellow yellow = new Yellow();
    public static Green green = new Green();
    public static Blue blue = new Blue();
    public static Purple purple = new Purple();
    public static Pink pink = new Pink();
    public static Spectator spectator = new Spectator();

    public static List<Team> teams = new ArrayList<>(Arrays.asList(red, yellow, green, blue, purple, pink, spectator));
    public static List<String> teamNamesFull = new ArrayList<>(Arrays.asList("Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets", "Spectator"));
    public static List<String> teamNames = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets", "Spectator"));
    public static ScoreboardManager manager =  Bukkit.getScoreboardManager();

    public static int gameID = 0;
    public static Game currentGame;
    public static int gameNum = -1;

    public static Plugin plugin;
    public static Lobby lobby = new Lobby();
    public static AceRace aceRace = new AceRace();
    public static TGTTOS tgttos = new TGTTOS();
    public static Skybattle skybattle = new Skybattle();

    public static List<String> gameNameList = new ArrayList<>(Arrays.asList("AceRace","TGTTOS", "Skybattle"));
    public static List<Game> gameList = new ArrayList<>(Arrays.asList(aceRace,tgttos, skybattle));

    // Define Special Blocks
    // NOTE: ALWAYS USE `getBlock().getRelative(BlockFace.DOWN)` or equivalent
    // ALSO NOTE: may move to more general class since these are likely ubiquitous
    public final static Material SPEED_PAD = Material.OBSERVER;
    public final static Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public final static Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public final static Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;


    public static int multiplier = 1;

    public MBC(Plugin plugin) {
        MBC.plugin = plugin;
        for (Player p : Bukkit.getOnlinePlayers()) {
            players.add(new Participant(p));
        }
        startGame(lobby);
    }

    public MBC() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Participant.contains(event.getPlayer())) {
            players.add(new Participant(event.getPlayer()));
        }

        Participant p = Participant.getParticipant(event.getPlayer());

        if (p.objective == null || !Objects.equals(p.gameObjective, currentGame.gameName)) {
            currentGame.createScoreboard(p);
            p.gameObjective = currentGame.gameName;
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equalsIgnoreCase("Flying is not enabled on this server"))
            e.setCancelled(true);
    }

    public static int getGameID() {
        return gameID;
    }

    public static void startGame(Game game) {
        gameID = gameList.indexOf(game)+1;
        gameNum++;
        Bukkit.broadcastMessage(ChatColor.GOLD + game.gameName + ChatColor.WHITE + " has started");
        currentGame = game;
        currentGame.start();
    }

    public static void startGame(int game) {
        startGame(gameList.get(game));
    }

    public static void cancelEvent(int taskID) {
        if (Bukkit.getScheduler().isCurrentlyRunning(taskID)) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public static List<Participant> getIngamePlayer() {
        List<Participant> newList = new ArrayList<>();
        for (Participant p : players) {
            if (!Objects.equals(p.getTeam().fullName, "Spectator")) {
                newList.add(p);
            }
        }

        return newList;
    }
}
