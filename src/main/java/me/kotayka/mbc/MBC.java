package me.kotayka.mbc;

import me.kotayka.mbc.games.*;
import me.kotayka.mbc.teams.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MBC implements Listener {
    // singleton for event
    public static MBC mbc = null;

    // event specifics

    public static final int MAX_PLAYERS_PER_TEAM = 4;
    public static final int MAX_TEAMS = 6;
    public static final int MAX_PLAYERS = MAX_PLAYERS_PER_TEAM * MAX_TEAMS;
    public static final int GAME_COUNT = 6;

    public List<Participant> players = new ArrayList<>(16);

    public Red red = new Red();
    public Yellow yellow = new Yellow();
    public Green green = new Green();
    public Blue blue = new Blue();
    public Purple purple = new Purple();
    public Pink pink = new Pink();
    public Spectator spectator = new Spectator();

    public List<MBCTeam> teams = new ArrayList<>(Arrays.asList(red, yellow, green, blue, purple, pink, spectator));
    public List<String> teamNamesFull = new ArrayList<>(Arrays.asList("Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets", "Spectator"));
    public static List<String> teamNames = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets", "Spectator"));
    public ScoreboardManager manager =  Bukkit.getScoreboardManager();

    public int gameID = 0;
    public Game currentGame;
    public int gameNum = 0;

    public Plugin plugin;
    public Lobby lobby = new Lobby();
    public AceRace aceRace = null;
    public TGTTOS tgttos = null;
    public BSABM bsabm = null;
    public Skybattle skybattle = null;
    public SurvivalGames sg = null;

    public static final List<String> gameNameList = new ArrayList<>(Arrays.asList("AceRace","TGTTOS","BSABM","Skybattle", "SurvivalGames"));
    public final List<Game> gameList = new ArrayList<Game>(6);

    // Define Special Blocks
    // NOTE: ALWAYS USE `getBlock().getRelative(BlockFace.DOWN)` or equivalent
    // ALSO NOTE: may move to more general class since these are likely ubiquitous
    public static final Material SPEED_PAD = Material.OBSERVER;
    public static final Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public static final Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public static final Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;
    public double multiplier = 1;


    private MBC(Plugin plugin) {
        this.plugin = plugin;
        currentGame = lobby;
    }

    // ensure singular instance to remove static overuse
    public static MBC getInstance(Plugin plugin) {
        if (mbc == null) {
            mbc = new MBC(plugin);
        }
        return mbc;
    }

    // ensure singular instance to remove static overuse
    public static MBC getInstance() {
        return Objects.requireNonNull(mbc);
    }

    public Game gameInstance(int gameNum) {
        switch(gameNameList.get(gameNum)) {
            case "AceRace":
                if (aceRace == null) {
                    aceRace = new AceRace();
                    this.gameID = 1;
                }
                return aceRace;
            case "TGTTOS":
                if (tgttos == null) {
                    tgttos = new TGTTOS();
                    this.gameID = 2;
                }
                return tgttos;
            case "BSABM":
                if (bsabm == null) {
                    bsabm = new BSABM();
                    this.gameID = 3;
                }
                return bsabm;
            case "Skybattle":
                if (skybattle == null) {
                    skybattle = new Skybattle();
                    this.gameID = 4;
                }
                return skybattle;
            case "SurvivalGames":
                if (sg == null) {
                    sg = new SurvivalGames();
                    this.gameID = 5;
                }
                return sg;
            default:
                return lobby;
        }
    }


    private MBC() {}

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

    /**
     * @return List of all non-spectator teams with at least one player
     */
    public static List<MBCTeam> getValidTeams() {
        List<MBCTeam> newTeams = new ArrayList<>();
        for (int i = 0; i < MBC.teamNames.size(); i++) {
            if (!Objects.equals(MBC.getInstance().teams.get(i).fullName, "Spectator") && MBC.getInstance().teams.get(i).teamPlayers.size() > 0) {
                newTeams.add(MBC.getInstance().teams.get(i));
            }
        }
        return newTeams;
    }

    /**
     * Handles formatting player messages
     * @param e PlayerChatEvent
     */
    @EventHandler
    public void onPlayerChat(PlayerChatEvent e) {
        String msg = e.getMessage();
        for (Participant p : players) {
            if (e.getPlayer() == p.getPlayer()) {
                e.setFormat(p.getFormattedName() + ": " + msg);
                break;
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        }
    }

    /**
     * Prevent players from taking damage from fireworks
     * Until this ever becomes a feature in a game
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        // players or chickens should never get damaged by fireworks
        if (e.getDamager() instanceof Firework) {
            e.setCancelled(true);
        }
    }

    public int getGameID() {
        return this.gameID;
    }

    public void startGame(Game game) {
        gameNum++;
        Bukkit.broadcastMessage(ChatColor.GOLD + game.gameName + ChatColor.WHITE + " has started");
        currentGame = game;
        currentGame.start();
    }

    public void startGame(int game) {
        // prevent another game from starting if a non-lobby game is active
        if (gameID != 0) { // TODO: decision dome may have another gameID, so add that here
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: Another game with MBC.gameID " + gameID + " is in progress!");
            return;
        }

        startGame(gameInstance(game));
    }

    public void cancelEvent(int taskID) {
        if (Bukkit.getScheduler().isCurrentlyRunning(taskID)) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    /**
     * Spawn firework on a given player
     * @param p Participant on which the firework should spawn
     */
    public static void spawnFirework(Participant p) {
        Location l = p.getPlayer().getLocation();
        l.setY(l.getY()+1);
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.addEffect(FireworkEffect.builder().withColor(p.getTeam().getColor()).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    /**
     * Linear search through all players, returns list of
     * players not on Team Spectator, including those in
     * spectator mode.
     * @return List of participants not on Team Spectator.
     */
    public List<Participant> getPlayers() {
        List<Participant> newList = new ArrayList<>();
        for (Participant p : getInstance().players) {
            if (!Objects.equals(p.getTeam().fullName, "Spectator")) {
                newList.add(p);
            }
        }

        return newList;
    }
}
