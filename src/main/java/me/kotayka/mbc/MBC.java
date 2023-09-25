package me.kotayka.mbc;

import me.kotayka.mbc.NPCs.NPCManager;
import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.comparators.TotalIndividualComparator;
import me.kotayka.mbc.games.*;
import me.kotayka.mbc.teams.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class MBC implements Listener {
    // singleton for event
    public static MBC mbc = null;
    private boolean enable_stat_logging = false;

    // event specifics
    public static final int MAX_PLAYERS_PER_TEAM = 4;
    public static final int MAX_TEAMS = 6;
    public static final int MAX_PLAYERS = MAX_PLAYERS_PER_TEAM * MAX_TEAMS;
    public static final int GAME_COUNT = 6;

    /**
     * `players` represents all current active players.
     * Players are added to the list when they join a team, if that team is not Spectator.
     * Players are removed from the list when they log off.
     */
    public List<Participant> players = new ArrayList<>(16); // every player
    public List<Participant> participants = new ArrayList<>(16); // every player + spectators
    //public TreeSet<Participant> individual = new TreeSet<>(Participant.rawTotalScoreComparator);
    //public TreeSet<MBCTeam> teamScores = new TreeSet<>(new TeamScoreSorter());

    public Red red = new Red();
    public Yellow yellow = new Yellow();
    public Green green = new Green();
    public Blue blue = new Blue();
    public Purple purple = new Purple();
    public Pink pink = new Pink();
    public Spectator spectator = new Spectator();

    public List<MBCTeam> teams = new ArrayList<>(Arrays.asList(red, yellow, green, blue, purple, pink, spectator));
    //public List<MBCTeam> validTeams = new ArrayList<>();
    //public List<String> teamNamesFull = new ArrayList<>(Arrays.asList("Red Rabbits", "Yellow Yaks", "Green Guardians", "Blue Bats", "Purple Pandas", "Pink Piglets", "Spectator"));
    public static List<String> teamNames = new ArrayList<>(Arrays.asList("RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets", "Spectator"));
    public ScoreboardManager manager =  Bukkit.getScoreboardManager();
    public final Scoreboard board = manager.getNewScoreboard();

    private Minigame currentGame;
    public int gameNum = 1; // REVERT THIS AFTER TEST

    public Plugin plugin;
    public final Lobby lobby = new Lobby();
    public DecisionDome decisionDome = null;
    public AceRace aceRace = null;
    public TGTTOS tgttos = null;
    public BuildMart bsabm = null;
    public Skybattle skybattle = null;
    public SurvivalGames sg = null;
    public Spleef spleef = null;
    public Dodgebolt dodgebolt = null;
    public boolean finalGame = false;

    public static final List<String> gameNameList = new ArrayList<>(Arrays.asList("DecisionDome","AceRace","TGTTOS","BuildMart","Skybattle", "SurvivalGames", "Spleef","Dodgebolt"));
    public final List<Game> gameList = new ArrayList<Game>(6);

    // Define Special Blocks
    // NOTE: ALWAYS USE `getBlock().getRelative(BlockFace.DOWN)` or equivalent
    public static final Material SPEED_PAD = Material.OBSERVER;
    public static final Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public static final Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public static final Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;
    public double multiplier = 1;

    public static NPCManager npcManager;

    private MBC(Plugin plugin) {
        this.plugin = plugin;
        currentGame = lobby;
        npcManager = new NPCManager((JavaPlugin) plugin);
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

    public Minigame gameInstance(int gameNum) {
        switch(gameNameList.get(gameNum)) {
            case "DecisionDome":
                if (decisionDome == null) {
                    Bukkit.broadcastMessage("Making new decisiondome!");
                    decisionDome = new DecisionDome(gameNum > 1); // NOTE: if we get > 8 games available per event, this must change.
                    // NOTE NOTE: this is kind of redundant now
                }
                return decisionDome;
            case "AceRace":
                if (aceRace == null) {
                    aceRace = new AceRace();
                }
                return aceRace;
            case "TGTTOS":
                if (tgttos == null) {
                    tgttos = new TGTTOS();
                }
                return tgttos;
            case "BuildMart":
                if (bsabm == null) {
                    bsabm = new BuildMart();
                }
                return bsabm;
            case "Skybattle":
                if (skybattle == null) {
                    skybattle = new Skybattle();
                }
                return skybattle;
            case "SurvivalGames":
                if (sg == null) {
                    sg = new SurvivalGames();
                }
                return sg;
            case "Spleef":
                if (spleef == null) {
                    spleef = new Spleef();
                }
                return spleef;
            case "Dodgebolt":
                if (dodgebolt == null) {
                    if (getValidTeams().size() < 2) {
                        dodgebolt = new Dodgebolt();
                    } else {
                        // TODO: this is a sin against man
                        List<MBCTeam> temp = getValidTeams();
                        temp.sort(new TeamScoreSorter());
                        dodgebolt = new Dodgebolt(temp.get(temp.size()-1), temp.get(temp.size()-2));
                    }
                }
                return dodgebolt;
            default:
                return lobby;
        }
    }

    // todo: this is kinda lazy and using enums would probably be way better if possible down the line
    public Minigame gameInstance(String gameName) {
        return gameInstance(gameNameList.indexOf(gameName));
    }

    public Game getGame(String name) {
        return game(gameNameList.indexOf(name));
    }

    private Game game(int num) {
        switch (gameNameList.get(num)) {
            case "AceRace":
                return aceRace;
            case "TGTTOS":
                return tgttos;
            case "SurvivalGames":
                return sg;
            case "Skybattle":
                return skybattle;
            case "BuildMart":
                return bsabm;
            case "Spleef":
                return spleef;
            default:
                return null;
        }
    }

    /**
     * Access to current event activity
     * @return Minigame currentGame
     */
    public Minigame getMinigame() {
        return currentGame;
    }

    /**
     * For use exclusively if there is no minigame active
     * @return Game currentGame
     */
    public Game getGame() {
        if (!(currentGame instanceof Game)) {
            System.err.print("Tried to access Game when no game was active!");
            return null;
        }
        return (Game) currentGame;
    }

    public void incrementMultiplier() {
        /*
         * Game 1: 1x
         * Game 2, 3: 1.5x
         * Game 4, 5: 2x
         * Game 6: 2.5x
         */
        if (gameNum % 2 == 0) {
            Bukkit.broadcastMessage("The point multiplier has increased from " + multiplier + " to " + (multiplier+0.5) + "!");
            multiplier+=0.5;
        }
    }

    public void setCurrentGame(Minigame game) {
       currentGame = game;
    }

    private MBC() {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Participant.contains(event.getPlayer())) { // new login this instance
            Participant newPlayer = new Participant(event.getPlayer());
            players.add(newPlayer);

            if (newPlayer.objective == null || !Objects.equals(newPlayer.gameObjective, currentGame.gameName)) {
                currentGame.createScoreboard(newPlayer);
                newPlayer.gameObjective = currentGame.gameName;
            }
        } else { // relog
            Participant p = Participant.getParticipant(event.getPlayer());
            p.setPlayer(event.getPlayer());
            Scoreboard newBoard = manager.getNewScoreboard();
            p.board = newBoard;
            p.getPlayer().setScoreboard(newBoard);
            p.changeTeam(p.getTeam());
            p.setupScoreboardTeams();
            p.gameObjective = currentGame.gameName;
            currentGame.newObjective(p);
            currentGame.createScoreboard(p);
            if (currentGame instanceof Game) {
                currentGame.createLineAll(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, currentGame.gameName));
                currentGame.createLineAll(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET));
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());

        // no need to pause for non-full games
        if (currentGame instanceof Game) {
            switch (currentGame.getState()) {
                case TUTORIAL:
                case END_ROUND:
                    ((Game) currentGame).disconnect = true;
                    Bukkit.broadcastMessage("[Debug] disconnect during transition state!");
                    break;
                case STARTING:
                    currentGame.Pause();
                    break;
            }
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
    public List<MBCTeam> getValidTeams() {
        List<MBCTeam> newTeams = new ArrayList<>();
        for (MBCTeam t : teams) {
            if (!(t instanceof Spectator) && t.teamPlayers.size() > 0) {
                newTeams.add(t);
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
            if (e.getPlayer().getUniqueId().equals(p.getPlayer().getUniqueId())) {
                msg = msg.replace("%", "%%");
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

        if (!(currentGame instanceof Game)) return;
        if (!getGame().PVP() && e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    public void startGame(Minigame game) {
        if (currentGame instanceof Game) {
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: " + currentGame.gameName + " is in progress!");
            return;
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + game.gameName + ChatColor.WHITE + " has started");
        currentGame = game;
        currentGame.start();
    }

    public void startGame(int game) {
        // prevent another game from starting if a non-lobby game is active
        if (currentGame instanceof Game) {
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: " + currentGame.gameName + " is in progress!");
            return;
        }

        startGame(gameInstance(game));
    }

    public void startGame(String gameName) {
        gameName = gameName.replaceAll("\\s", "");
        startGame(gameInstance(gameName));
    }

    public void cancelEvent(int taskID) {
        // for some reason this was not cancelling the event so i commented it out for now, probably worth another look in the future
        //if (Bukkit.getScheduler().isCurrentlyRunning(taskID)) {
        Bukkit.getScheduler().cancelTask(taskID);
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
     * Spawn Firework at a given location with a given color
     * @param l Location to spawn the firework
     * @param c Color for the firework to have
     */
    public static void spawnFirework(Location l, Color c) {
        org.bukkit.entity.Firework fw = (org.bukkit.entity.Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder().withColor(c).build());
        fw.setFireworkMeta(fwm);
        fw.detonate();
    }

    /**
     * ArrayList of all Participants not on Team Spectator.
     * @return Copy of list of current players without spectators.
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

    public void updatePlacings() {
        int i = 1;
        //int lastScore = -1;

        List<Participant> individual = getPlayers();
        individual.sort(new TotalIndividualComparator());

        // TODO: using an auto sort ds maybe a set would be better
        for (Participant p : individual) {
            //if (p.getRawTotalScore() == lastScore) {
                //p.setPlacement(i);
            //} else {
            p.setPlacement(i++);
                //lastScore = p.getRawTotalScore();
            //}
        }
    }

    public void getPlacementInfo(Player sender) {
        Participant p = Participant.getParticipant(sender);

        if (p == null) return;

        if (p.getTeam() instanceof Spectator) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"You are Spectating, and do not have a score!");
            return;
        }

        if (currentGame instanceof Game) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"Your scores will update after the game has finished!");
            return;
        }

        int placement = p.getPlacement();
        if (placement < 0) {
            sender.sendMessage(ChatColor.GREEN+"[placement] " + ChatColor.RESET+"The event has not started yet!");
            return;
        }

        StringBuilder msg = new StringBuilder(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=\n"+ChatColor.RESET);
        msg.append(ChatColor.YELLOW+"Your placement: " + Game.getColorStringFromPlacement(placement) + Game.getPlace(placement) + ChatColor.RESET + "\n");
        msg.append(ChatColor.YELLOW+"Your score: " + ChatColor.RESET+ p.getRawTotalScore()+"\n");
        if (placement != 1) {
            List<Participant> aheadPlayers = Participant.getParticipant(placement-1);
            msg.append("The player one place above you has " + (aheadPlayers.get(0).getRawTotalScore() - p.getRawTotalScore()) + " more coins.\n");
        }
        msg.append(ChatColor.AQUA+"+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=");
        sender.sendMessage(msg.toString());
    }

    public void getTopIndividual(Player sender) {
        List<Participant> individual = getPlayers();
        individual.sort(new TotalIndividualComparator());
        int placement;
        StringBuilder msg = new StringBuilder(ChatColor.AQUA.toString()+ChatColor.BOLD+"Player scores: \n"+ChatColor.RESET);
        for (Participant p : individual) {
            placement = p.getPlacement();
            if (placement < 9) {
                msg.append(placement).append(". ").append(p.getFormattedName()).append(": ").append(p.getRawTotalScore()).append("\n");
            } else {
                break;
            }
        }
        sender.sendMessage(msg.toString());
    }

    public static void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void setGameNum(int num) {
        if (finalGame && num != 6) {
            finalGame = false;
        } else if (num == 6) {
            finalGame = true;
        }
        gameNum = num;
    }

    public void setLogStats(boolean b) { enable_stat_logging = b; }
    public boolean logStats() { return enable_stat_logging; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }
    public static String statDirectory() { return "stat_archive"+File.separator+"MBC1";}
    /**
     * ArrayList of all Participants including Spectators.
     * @return Copy of list of current players with spectators.
     */
    public List<Participant> getPlayersAndSpectators() {
        return new ArrayList<>(getInstance().participants);
    }
}
