package me.kotayka.mbc.partygames;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.PartyGame;
import me.kotayka.mbc.gamePlayers.SpleefPlayer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeepTest extends PartyGame {
    // Maps of levels by name
    private BeepTestLevel currentLevel = null;
    private List<BeepTestLevel> easyLevels = null;
    private List<BeepTestLevel> regularLevels = null;
    private List<BeepTestLevel> mediumLevels = null;
    private List<BeepTestLevel> hardLevels = null;
    private int rounds = 0;
    private boolean oppositeSide = false;
    private Set<Participant> fallenPlayers = new HashSet<>();

    private final Location OPPOSITE_SPAWN = new Location(Bukkit.getWorld("Party"), -522, -55, -490);
    // arena regions
    private final Location copyArenaPrimary = new Location(Bukkit.getWorld("Party"), -65, -63, 60);
    private final Location copyArenaSecondary = new Location(Bukkit.getWorld("Party"), -109, -37, 112);
    private final Location arenaPrimary = new Location(Bukkit.getWorld("Party"),-505, -63, -492);
    private final Location arenaSecondary = new Location(Bukkit.getWorld("Party"),-536, -56, -492);
    private final Location courseToPrimary = new Location(Bukkit.getWorld("Party"), -534, -59, -462);
    private final Location courseToSecondary = new Location(Bukkit.getWorld("Party"), -510, -59, -486);
    private final BlockVector3 arenaFrom = new BlockVector3(-65, -63, 60);
    private final BlockVector3 arenaTo = new BlockVector3(-500, -63, -500);
    private final World WorldEditWorld = BukkitAdapter.adapt(Bukkit.getWorld("Party"));

    private final org.bukkit.World world = Bukkit.getWorld("Party");
    private final Location SPAWN = new Location(Bukkit.getWorld("Party"), -522, -55, -458, 180, 0);

    private List<Participant> completedPlayers = new ArrayList<>();
    private List<Participant> alivePlayers = new ArrayList<>();
    public long roundTime;
    private boolean ended = false;

    public final int STAGE_POINTS = 6;
    public final int EASY_POINTS = 1;
    public final int MEDIUM_POINTS = 2;
    public final int HARD_POINTS = 3;
    public final int EXTREME_POINTS = 4;
    public int CURRENT_POINTS = EASY_POINTS;

    public final int REGULAR_Z = -486;
    public final int OPPOSITE_Z = -461;
    //to test: might not work, but i found a glitch beforehand

    // game instance
    private static BeepTest instance = null;

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new BeepTest();
        }
        return instance;
    }

    private BeepTest() {
        super("BeepTest", new String[] {
                "⑰ The fitness gram " + ChatColor.BOLD + "Beep Test" + ChatColor.RESET + " is a multistage parkour\n" + 
                " capacity test that progressively gets more difficult as it continues.",
                "⑰ Jump from one side of the map to the other to complete the courses as fast as you can.\n\n" +  
                "⑰ The parkour difficulty starts easy, but gets more difficult each time the arena changes color.",
                "⑰ The third time you fail the parkour course, or do not complete it in time, you are eliminated.\n\n" + 
                "⑰ You earn points for each stage and each difficulty you complete.",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +1 point for each easy level completed\n" +
                        "⑰ +2 points for each medium level completed\n" +
                        "⑰ +3 points for each hard level completed\n" +
                        "⑰ +4 points for each extreme level completed\n" +
                        "⑰ +6 points for completing a color of difficulty"
        });

        loadCourses();

    }

    @Override
    public void start() {
        super.start();

        teamsAlive.addAll(getValidTeams());

        world().setTime(18000);

        loadInitialArena();
        Barriers(true);
        resetGround();

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    @Override
    public void endEvents() {
        if (ended) {
            return;
        }
        ended = true;
        MBC.getInstance().showAllPlayers();
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().stopSound(Sound.MUSIC_DISC_13);
            p.getPlayer().setMaxHealth(20);
            p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
        }
        roundWinners(STAGE_POINTS, 0);
        if (MBC.getInstance().party == null) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                p.addCurrentScoreToTotal();
            }
            MBC.getInstance().updatePlacings();
            logger.logStats();
            returnToLobby();
        } else {
            // start next game
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.stopSound(Sound.MUSIC_DISC_13, SoundCategory.RECORDS);
            }
            setupNext();
        }
        logger.logStats();
    }

    @Override
    public void onRestart() {

    }

    @Override
    public void loadPlayers() {
        MBC.getInstance().hideAllPlayers();
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        alivePlayers.clear();
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().teleport(SPAWN);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            alivePlayers.add(p);
            playersAlive.add(p);
            p.getPlayer().setMaxHealth(6);
            p.getPlayer().setHealth(p.getPlayer().getMaxHealth());
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }

    @Override
    public void events() {
        switch (getState()) {
            case TUTORIAL:
                if (timeRemaining == 0) {
                    setGameState(GameState.STARTING);
                    setTimer(15);
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
                break;
            case STARTING:
                startingCountdown(Sound.ITEM_GOAT_HORN_SOUND_1);
                if (timeRemaining == 9) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_7, SoundCategory.RECORDS, 1, 1);
                    }
                }
                if (timeRemaining == 0) {
                    //MBC.getInstance().hideAllPlayers();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.BLOCKS, 0.75f, 1);
                        p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS, .75f,1); // temp?
                    }
                    Barriers(false);
                    newGround();
                    nextRound();
                    roundDisplay();
                    roundTime = System.currentTimeMillis();
                    setGameState(GameState.ACTIVE);
                    setTimer(13);
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0) {
                    if (rounds > 14) {
                        removeHealth();
                        endEvents();
                        return;
                    } else {
                        // clear level, move onto next level
                        MBC.getInstance().hideAllPlayers();
                        rounds++;
                        newGround();
                        nextRound();
                        removeHealth();
                        stagePoints();
                        roundDisplay();

                        // for any players that are in the course but completed the level (??)
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (inLevel(p)) {
                                if (oppositeSide) {
                                    p.teleport(SPAWN);
                                } else {
                                    p.teleport(OPPOSITE_SPAWN);
                                }
                            }
                        }

                        fallenPlayers.clear();
                        completedPlayers.clear();
                        roundTime = System.currentTimeMillis();
                        int timeForLevel;
                        if (rounds < 4) {
                            timeForLevel = 15;
                            CURRENT_POINTS = EASY_POINTS;
                        } else if (rounds < 8) {
                            timeForLevel = 15;
                            CURRENT_POINTS = MEDIUM_POINTS;
                        } else if (rounds < 12) {
                            timeForLevel = 20;
                            CURRENT_POINTS = HARD_POINTS;
                        } else {
                            timeForLevel = 25;
                            CURRENT_POINTS = EXTREME_POINTS;
                        }
                        if (rounds % 4 == 0 && rounds != 0) {
                            for (Participant p : playersAlive) {
                                p.getPlayer().sendMessage(ChatColor.GREEN + "You've moved onto the next stage!");
                                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            }
                        }
                        if (!ended) {
                            setTimer(timeForLevel+1);
                        }
                    }
                }
                break;
        }

    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(25,String.format("%s%sGame %d/6: %s%s", ChatColor.AQUA, ChatColor.BOLD, MBC.getInstance().gameNum, ChatColor.WHITE, "Party (" + name()) + ")", p);
        createLine(15, String.format("%sGame Coins: %s(x%s%.1f%s)", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, MBC.getInstance().multiplier, ChatColor.RESET), p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
    }

    /*
     * Display the amount of rounds on the scoreboard.
     */
    private void roundDisplay() {
        ChatColor color = null;
        if (rounds < 4) {
            color = ChatColor.AQUA;
        } else if (rounds < 8) {
            color = ChatColor.GREEN;
        } else if (rounds < 12) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }

        String round = ((rounds / 4) + 1) + "-" + ((rounds % 4)+1);
        createLineAll(21, ChatColor.BOLD + "Current Round: " + ChatColor.RESET + "" + color + round);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        Player p = e.getPlayer();
        Participant par = Participant.getParticipant(p);

        if (e.getTo().getY() < -100) {
            p.teleport(SPAWN);
            return;
        }

        if (p.getGameMode() != GameMode.ADVENTURE) return;

        if (!completedPlayers.contains(par)) {
            completedCourse(e);
        }

        if (completedPlayers.contains(par) && !oppositeSide && p.getZ() <= REGULAR_Z) {
            p.teleport(new Location(Bukkit.getWorld("Party"), -522, -55, -458, 0, 0));
            p.setVelocity(new Vector(0, 0, 0));
            return;
        }
        if (completedPlayers.contains(par) && oppositeSide && p.getZ() >= OPPOSITE_Z) {
            p.teleport(new Location(Bukkit.getWorld("Party"), -522, -55, -490, 180, 0));
            p.setVelocity(new Vector(0, 0, 0));
            return;
        }

        if (p.getLocation().getY() < -59 && !fallenPlayers.contains(par)) {
            // teleport players who fell back up
            if (completedPlayers.contains(par)) {
                p.setVelocity(new Vector(0, 0, 0));
                if (oppositeSide) {
                    p.teleport(SPAWN);
                    
                }
                else {
                    p.teleport(OPPOSITE_SPAWN);
                }
                
                return;
            }

            fallenPlayers.add(par);
            p.setVelocity(new Vector(0, 0, 0));
            p.sendMessage(ChatColor.RED + "You fell!");
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(p);
            }

            if (p.getPlayer().getHealth() <= 2) {
                logger.log(par.getFormattedName() + " lost their last life on " + currentLevel.getName());
            } else {
                logger.log(par.getFormattedName() + " fell on " + currentLevel.getName());
            }
        }
    }

    /*
     * Respawn all players that fell in the previous round to the appropriate location.
     */
    private void respawn() {
        if (fallenPlayers.size() == 0) return;
        for (Participant p : fallenPlayers) {
            if (oppositeSide) {
                p.getPlayer().teleport(SPAWN);
            } else {
                p.getPlayer().teleport(OPPOSITE_SPAWN);
            }
        }
        fallenPlayers.clear();
    }

    private void loadCourses() {
        easyLevels = BeepTestLevelLoader.loadEasyLevels();
        regularLevels = BeepTestLevelLoader.loadRegularLevels();
        mediumLevels = BeepTestLevelLoader.loadMediumLevels();
        hardLevels = BeepTestLevelLoader.loadHardLevels();
    }

    public void removeHealth() {
        List<Participant> toEliminate = new ArrayList<>();
        for (Participant p : alivePlayers) {
            if (completedPlayers.contains(p) && !fallenPlayers.contains(p)) continue;
            logger.log(p.getPlayerName() + " didn't complete this level!");
            Player pl = p.getPlayer();
            if (pl.getHealth() <= 2) {
                toEliminate.add(p);
            } else {
                pl.setHealth(pl.getHealth()-2);
                if (!oppositeSide) {
                    pl.teleport(OPPOSITE_SPAWN);
                }
                else {
                    pl.teleport(SPAWN);
                }
            }
        }

        for (Participant p : toEliminate) {
            eliminatePlayer(p);
        }
    }

    public void stagePoints() {
        if (rounds % 4 == 0) {
            for (Participant p : alivePlayers) {
                p.addCurrentScore(STAGE_POINTS);
                p.getPlayer().sendMessage(ChatColor.GREEN + "You completed stage #" + (rounds / 4) + "!" + MBC.scoreFormatter(STAGE_POINTS));
            }
        }
    }

    public void eliminatePlayer(Participant p) {
        if (getState().equals(GameState.ACTIVE)) {
            p.getPlayer().setGameMode(GameMode.SPECTATOR);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 1, false, false));
            alivePlayers.remove(p);
            p.getPlayer().sendTitle(" ", ChatColor.RED + "You died!", 0, 60, 20);
            updatePlayersAlive(p);
            Bukkit.broadcastMessage(p.getFormattedName() + " was eliminated!");
            MBC.getInstance().showPlayers(p);
            updatePlayersAliveScoreboard();
        }
    }

    public void completedCourse(PlayerMoveEvent e) {
        Player pl = e.getPlayer();
        Participant p = Participant.getParticipant(pl);
        if (oppositeSide) {
            if (e.getTo().getZ() < REGULAR_Z) {
                completedPlayers.add(p);
                long currentTime = System.currentTimeMillis() - roundTime;
                String formattedTime = new SimpleDateFormat("ss.S").format(new Date(currentTime));
                pl.sendMessage(ChatColor.GREEN + "You completed " + currentLevel.getName().trim() + " in " + formattedTime + " seconds!" + MBC.scoreFormatter(CURRENT_POINTS));
                logger.log(p.getFormattedName() + " completed " + currentLevel.getName().trim() + " in " + formattedTime + "seconds!");
                if (completedPlayers.size() == 1) {
                    Bukkit.broadcastMessage(p.getFormattedName() + ChatColor.WHITE + " completed " + ChatColor.BOLD + ChatColor.GOLD + currentLevel.getName().trim() + ChatColor.RESET + " first, in " + formattedTime + " seconds!");
                }
                p.addCurrentScore(CURRENT_POINTS);
            }
        } else {
            if (e.getTo().getZ() > OPPOSITE_Z) {
                long currentTime = System.currentTimeMillis() - roundTime;
                String formattedTime = new SimpleDateFormat("ss.S").format(new Date(currentTime));
                pl.sendMessage(ChatColor.GREEN + "You completed " + currentLevel.getName().trim() + " in " + formattedTime + " seconds!" + MBC.scoreFormatter(CURRENT_POINTS));
                logger.log(p.getFormattedName() + " completed " + currentLevel.getName().trim() + " in " + formattedTime + " seconds!");
                if (completedPlayers.isEmpty()) {
                    Bukkit.broadcastMessage(p.getFormattedName() + ChatColor.WHITE + " completed " + ChatColor.BOLD + ChatColor.GOLD + currentLevel.getName().trim() + ChatColor.RESET + " first, in " + formattedTime + " seconds!");
                }
                completedPlayers.add(p);
                p.addCurrentScore(CURRENT_POINTS);
            }
        }
    }

    /**
     * Spawns the course for the next round on the appropriate side.
     */
    private void nextRound() {
        chooseLevel();
        if (oppositeSide) {
            oppositeSide = false;
            EditSession editSession = WorldEdit.getInstance().newEditSession(WorldEditWorld);
            ForwardExtentCopy copy = new ForwardExtentCopy(WorldEditWorld, currentLevel.getReversedRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteReversed()), editSession, BukkitAdapter.asBlockVector(courseToPrimary));
            try {
                Operations.complete(copy);
                editSession.close();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            oppositeSide = true;
            EditSession editSession = WorldEdit.getInstance().newEditSession(WorldEditWorld);
            ForwardExtentCopy copy = new ForwardExtentCopy(WorldEditWorld, currentLevel.getRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteFrom()), editSession, BukkitAdapter.asBlockVector(courseToPrimary));
            try {
                Operations.complete(copy);
                editSession.close();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        }
    }

    private void newGround() {
        int y = -63;
        int x;
        int z;
        if (rounds < 4) {x = -560;} else if (rounds < 8) {x = -596;} else if (rounds < 12) {x = -632;} else {x = -668;}
        if (oppositeSide) {z = -387;} else {z = -425;}
        for (int groundX = x; groundX >= x - 34; groundX--) {
            for (int groundY = y; groundY <= y + 7; groundY++) {
                for (int groundZ = z; groundZ <= z + 36; groundZ++) {
                    int mapX = groundX - x + (-505);
                    int mapY = groundY - y + (-63);
                    int mapZ = groundZ - z + (-492);

                    Block groundBlock = world.getBlockAt(groundX, groundY, groundZ);
                    Block mapBlock = world.getBlockAt(mapX, mapY, mapZ);

                    if (groundBlock.getType() != mapBlock.getType()) {
                        if ((mapY < -60 || mapX > -507 || mapX < -537)) {
                            mapBlock.setType(groundBlock.getType());
                            mapBlock.setBlockData(groundBlock.getBlockData());
                        } else if (mapBlock.getType() != Material.AIR) {
                            mapBlock.setType(groundBlock.getType());
                            mapBlock.setBlockData(groundBlock.getBlockData());
                        }
                    }
                }
            }
        }
    }

    private void resetGround() {
        int y = -63;
        int x = -524;
        int z = -425;
        for (int groundX = x; groundX >= x - 34; groundX--) {
            for (int groundY = y; groundY <= y + 7; groundY++) {
                for (int groundZ = z; groundZ <= z + 36; groundZ++) {
                    int mapX = groundX - x + (-505);
                    int mapY = groundY - y + (-63);
                    int mapZ = groundZ - z + (-492);

                    Block groundBlock = world.getBlockAt(groundX, groundY, groundZ);
                    Block mapBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (!(groundBlock.getType().name().equals(mapBlock.getType().name()))) {
                        mapBlock.setType(groundBlock.getType());
                        mapBlock.setBlockData(groundBlock.getBlockData());
                    }
                }
            }
        }
    }

    private boolean inLevel(Player p) {
        if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return false;
        return p.getLocation().getZ() > -486 && p.getLocation().getZ() < -462;
    }

    /**
     * Randomly chooses a level from the appropriate category based on rounds.
     *
     * @return The randomly selected level.
     */
    private void chooseLevel() {
        List<BeepTestLevel> chooseFrom = null;
        if (rounds < 4) {
            chooseFrom = easyLevels;
        } else if (rounds < 8) {
            chooseFrom = regularLevels;
        } else if (rounds < 12) {
            chooseFrom = mediumLevels;
        } else {
            chooseFrom = hardLevels;
        }

        // select random level
        int rand = (int) (Math.random() * chooseFrom.size());
        currentLevel = chooseFrom.get(rand);
        chooseFrom.remove(rand);
    }


    /**
     * Put or remove barriers on the first side of the course
     *
     * @param b Boolean for state of barriers: true = Barriers, false = Air
     */
    private void Barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        for (int y = -55; y <= -52; y++) {
            for (int x = -536; x <= -508; x++) {
                world().getBlockAt(x, y, -461).setType(m);
            }
        }
    }

    /**
     * Loads the default arena using WorldEditAPI.
     */
    private void loadInitialArena() {
        CuboidRegion arena = new CuboidRegion(BukkitAdapter.asBlockVector(copyArenaPrimary), BukkitAdapter.asBlockVector(copyArenaSecondary));
        EditSession editSession = WorldEdit.getInstance().newEditSession(WorldEditWorld);
        ForwardExtentCopy copy = new ForwardExtentCopy(WorldEditWorld, arena, arenaFrom, editSession, arenaTo);
        try {
            Operations.complete(copy);
            editSession.close();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) {
            e.getPlayer().teleport(SPAWN);
            return;
        }
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            if (getState().equals(GameState.ACTIVE)) {
                p.getPlayer().setGameMode(GameMode.SPECTATOR);
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 1, false, false));
                p.getPlayer().sendTitle(" ", ChatColor.RED + "You died!", 0, 60, 20);
                MBC.getInstance().showPlayers(p);
            }

        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;
        if (!getState().equals(GameState.ACTIVE)) return;

        Participant p = Participant.getParticipant(e.getPlayer());
        if (!alivePlayers.contains(p)) return;
        alivePlayers.remove(p);
        updatePlayersAlive(p);
        updatePlayersAliveScoreboard();

        for (Player play : Bukkit.getOnlinePlayers()) {
            play.sendMessage(p.getFormattedName() + " disconnected!");
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
   }

   @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }
}