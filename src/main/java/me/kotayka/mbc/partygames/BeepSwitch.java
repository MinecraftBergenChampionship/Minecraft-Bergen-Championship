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
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.PartyGame;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.gamePlayers.SpleefPlayer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BeepSwitch extends PartyGame {
    // Maps of levels by name
    private List<BeepTestLevel> easyLevels = null;
    private List<BeepTestLevel> mediumLevels = null;
    private List<BeepTestLevel> hardLevels = null;
    private List<BeepTestLevel> extremeLevels = null;

    private final org.bukkit.World world = Bukkit.getWorld("Party");
    private final Location SPAWN = new Location(Bukkit.getWorld("Party"), -5000, 101, -4999, 180, 0);

    private final Location[] easyCheckpointLocations = {
        new Location(Bukkit.getWorld("Party"), -5020, 101, -5019, 180, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -5051, 180, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -5083, 180, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -5115, 180, 0)
    };
    private final Location[] mediumCheckpointLocations = {
        new Location(Bukkit.getWorld("Party"), -5020, 101, -4979, 0, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -4947, 0, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -4915, 0, 0),
        new Location(Bukkit.getWorld("Party"), -5020, 101, -4883, 0, 0)
    };
    private final Location[] hardCheckpointLocations = {
        new Location(Bukkit.getWorld("Party"), -4980, 101, -5019, 180, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -5051, 180, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -5083, 180, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -5115, 180, 0)
    };
    private final Location[] extremeCheckpointLocations = {
        new Location(Bukkit.getWorld("Party"), -4980, 101, -4979, 0, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -4947, 0, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -4915, 0, 0),
        new Location(Bukkit.getWorld("Party"), -4980, 101, -4883, 0, 0)
    };

    private final int[] mediumExtremeCompleteZ = {-4951, -4919, -4887, -4855};
    private final int[] easyHardCompleteZ = {-5047, -5079, -5111, -5143};

    private boolean gameOver = false;

    private Map<MBCTeam, List<Player>> randomTeamList = new HashMap<>();
    private Map<MBCTeam, int[]> progress = new HashMap<>();

    private List<List<BeepTestLevel>> beepTestMaps = new ArrayList<>();

    public long roundTime = 16;
    public int rounds;
    public int roundNum = 0;
    private boolean ended = false;

    private final int deathY = 97;

    public final int STAGE_POINTS = 5;
    public final int EASY_POINTS = 1;
    public final int MEDIUM_POINTS = 3;
    public final int HARD_POINTS = 5;
    public final int EXTREME_POINTS = 7;

    // game instance
    private static BeepSwitch instance = null;

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new BeepSwitch();
        }
        return instance;
    }

    private BeepSwitch() {
        super("BeepSwitch", new String[] {
                "⑰ Use your parkour skills and your teamwork to complete as many beep test courses as you can!",
                "⑰ There are four seperate paths, each for a difficulty of level you can complete.\n\n" +  
                "⑰ Complete courses for points, and complete 4 courses to complete the whole path and get more points!",
                "⑰ However, only one player on your team will be running the parkour at a time.\n\n" + 
                "⑰ This player switches every 16 seconds. Communication is key to completing the most paths possible.",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +1 point for each easy level completed\n" +
                        "⑰ +3 points for each medium level completed\n" +
                        "⑰ +5 points for each hard level completed\n" +
                        "⑰ +7 points for each extreme level completed\n" +
                        "⑰ +5 points to each member of a team for completing a path of difficulty"
        });

        loadCourses();
    }

    @Override
    public void start() {
        super.start();

        teamsAlive.addAll(getValidTeams());

        world().setTime(18000);
        
        Barriers(true);

        int maxRounds = 0;
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            maxRounds = Math.max(maxRounds, t.getPlayers().size());
        }
        rounds = maxRounds*4;

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
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_13, SoundCategory.RECORDS);
        }
        if (MBC.getInstance().party == null) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                p.addCurrentScoreToTotal();
            }
            if (MBC.getInstance().gameNum != 6) {
                MBC.getInstance().updatePlacings();
            }
            
            returnToLobby();
        } else {
            // start next game
            setupNext();
        }
        logger.logStats();
    }

    @Override
    public void onRestart() {

    }

    @Override
    public void loadPlayers() {
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().teleport(SPAWN);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            playersAlive.add(p);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
    }

    @Override
    public void events() {
        switch (getState()) {
            case TUTORIAL:
                MBC.getInstance().showAllPlayers();
                if (timeRemaining == 0) {
                    setGameState(GameState.STARTING);
                    for (MBCTeam t : MBC.getInstance().getValidTeams()) {
                        randomTeamOrder(t);
                        initializeProgress(t);
                    }
                    setTimer(70);
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
                break;
            case STARTING:
                startingCountdown();
                mapCreator("Beep Switch", "Grassy311");
                if (timeRemaining == 69) {
                    Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "You will soon be given your placement of when you will be parkouring...\n");
                }
                if (timeRemaining == 65) {
                    for (Participant p : MBC.getInstance().getPlayers()) displaySpot(p.getPlayer());
                }
                if (timeRemaining == 60) { 
                    placeCourses();
                    Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "You have 45 seconds to review the beep courses on the map!\n");
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().setGameMode(GameMode.SPECTATOR);
                        p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                    Barriers(false);
                }
                if (timeRemaining == 25) {
                    Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "10 seconds of review remain!\n");
                }
                if (timeRemaining == 15) {
                    Barriers(true);
                    List<Player> currentPlayers = currentPlayers();
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        if (currentPlayers.contains(p.getPlayer())) p.getPlayer().setGameMode(GameMode.ADVENTURE);
                        p.getPlayer().teleport(SPAWN);
                        p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                }
                if (timeRemaining == 0) {
                    //MBC.getInstance().hideAllPlayers();
                    if (rounds > roundNum) {
                        Barriers(false);
                        setGameState(GameState.ACTIVE);
                        setTimer(16);
                    }
                    else {
                        timeRemaining = 5;
                        setGameState(GameState.END_ROUND);
                    }
                }
                if (timeRemaining == 2) {
                    roundsLeftDisplay();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS, .75f,1);
                    }
                    List<Player> currentPlayers = currentPlayers();
                    for (Player p : currentPlayers) {
                        MBC.getInstance().hideAllPlayers();
                        pathTeleportItems(p);
                    }
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0) {
                    Map<MBCTeam, Vector> velocityTransfer = new HashMap<>();
                    Map<MBCTeam, Location> locationTransfer = new HashMap<>();
                    List<Player> currentPlayers = currentPlayers();
                    for (Player p : currentPlayers) {
                        Participant part = Participant.getParticipant(p);
                        velocityTransfer.put(part.getTeam(), p.getVelocity());
                        locationTransfer.put(part.getTeam(), p.getLocation());
                        p.getPlayer().getInventory().remove(Material.RED_DYE);
                        p.getPlayer().getInventory().remove(Material.YELLOW_DYE);
                        p.getPlayer().getInventory().remove(Material.LIME_DYE);
                        p.getPlayer().getInventory().remove(Material.BLUE_DYE);
                        p.setGameMode(GameMode.SPECTATOR);
                        MBC.getInstance().showAllPlayers();
                    }

                    roundNum++;
                    roundsLeftDisplay();
                    if (rounds == roundNum && !gameOver) {
                        timeRemaining = 5;
                        setGameState(GameState.END_ROUND);
                    }
                    else {
                        timeRemaining = 16;
                        currentPlayers = currentPlayers();
                        for (Player p : currentPlayers) {
                            Participant part = Participant.getParticipant(p);
                            
                            p.teleport(locationTransfer.get(part.getTeam()));

                            Vector old = velocityTransfer.get(part.getTeam());
                            Vector current = p.getVelocity();
                            
                            current.setX(old.getX());
                            current.setY(old.getY());
                            current.setZ(old.getZ());

                            p.setVelocity(current);
                            
                            pathTeleportItems(p);
                            p.setGameMode(GameMode.ADVENTURE);
                            MBC.getInstance().hideAllPlayers();
                        }
                    }
                    
                }
                else if (timeRemaining == 5) {
                    if (futurePlayers() != null) {
                        List<Player> futurePlayers = futurePlayers();
                        for (Player p : futurePlayers) {
                            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You will be parkouring in 5 seconds!");
                        }
                    }
                    else {
                        Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "The game ends in 5 seconds!");
                    }
                }
                break;
            case END_ROUND:
                if (timeRemaining == 4) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.stopSound(Sound.MUSIC_DISC_13, SoundCategory.RECORDS);
                    }
                    endEvents();
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

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        Player p = e.getPlayer();
        Participant par = Participant.getParticipant(p);

        currentCourseDisplay(par.getTeam());

        if (!currentPlayers().contains(e.getPlayer())) return;
        
        if (e.getPlayer().getY() <= deathY) {
            respawn(sectionLoc(e.getPlayer().getLocation()), Participant.getParticipant(e.getPlayer()));
        }

        if (e.getPlayer().getZ() >= mediumExtremeCompleteZ[0] || e.getPlayer().getZ() <= easyHardCompleteZ[0]) {
            checkComplete(sectionLoc(e.getPlayer().getLocation()), e.getPlayer());
        }
    }

    /**
     * Checks if player p has completed a course on path path
     */
    private void checkComplete(int path, Player p) {
        int[] teamProgress = progress.get(Participant.getParticipant(p).getTeam());
        switch(path) {
            case 0,2:
                if (p.getZ() <= easyHardCompleteZ[0]) {
                    if (teamProgress[path] == 0) {
                        complete(path, Participant.getParticipant(p));
                    }
                    else if (p.getZ() <= easyHardCompleteZ[1]) {
                        if (teamProgress[path] == 1) {
                            complete(path, Participant.getParticipant(p));
                        }
                        else if (p.getZ() <= easyHardCompleteZ[2]) {
                            if (teamProgress[path] == 2) {
                                complete(path, Participant.getParticipant(p));
                            }
                            else if (p.getZ() <= easyHardCompleteZ[3]) {
                                if (teamProgress[path] == 3) {
                                    complete(path, Participant.getParticipant(p));
                                }
                            }
                        }
                    }
                }
                break;
            case 1,3:
                if (p.getZ() >= mediumExtremeCompleteZ[0]) {
                    if (teamProgress[path] == 0) {
                        complete(path, Participant.getParticipant(p));
                    }
                    else if (p.getZ() >= mediumExtremeCompleteZ[1]) {
                        if (teamProgress[path] == 1) {
                            complete(path, Participant.getParticipant(p));
                        }
                        else if (p.getZ() >= mediumExtremeCompleteZ[2]) {
                            if (teamProgress[path] == 2) {
                                complete(path, Participant.getParticipant(p));
                            }
                            else if (p.getZ() >= mediumExtremeCompleteZ[3]) {
                                if (teamProgress[path] == 3) {
                                    complete(path, Participant.getParticipant(p));
                                }
                            }
                        }
                    }
                }
                break;
                
        }
    }

    /*
     * Display the amount of cycles left on the scoreboard.
     */
    private void roundsLeftDisplay() {
        ChatColor color = null;
        if (roundNum < rounds/4) {
            color = ChatColor.AQUA;
        } else if (roundNum < rounds/2) {
            color = ChatColor.GREEN;
        } else if (roundNum < 3*rounds/4) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }

        createLineAll(21, ChatColor.BOLD + "Cycles Left: " + ChatColor.RESET + "" + color + (rounds - roundNum));
    }

    /*
     * Display the course current on on the scoreboard.
     */
    private void currentCourseDisplay(MBCTeam t) {
        List<Player> currentPlayers = currentPlayers();
        Player player = null;
        for (Player p : currentPlayers) {
            if (Participant.getParticipant(p).getTeam().equals(t)) {
                player = p;
            }
        }
        if (player == null) return;

        int path = sectionLoc(player.getLocation())+1;
        int level = progress.get(t)[sectionLoc(player.getLocation())]+1;
        ChatColor color = ChatColor.WHITE;

        switch(path){
            case 1: 
                color = ChatColor.BLUE;
                break;
            case 2: 
                color = ChatColor.GREEN;
                break;
            case 3: 
                color = ChatColor.YELLOW;
                break;
            case 4: 
                color = ChatColor.RED;
                break;
        }

        for (Participant p : t.getPlayers())
        createLine(22, Participant.getParticipant(player).getFormattedName() + ChatColor.BOLD + " at: " + ChatColor.RESET + color + path + "-" + level, p);
    }

    /**
     * Runs if a participant p has completed 
     */
    private void complete(int path, Participant p) {
        int[] teamProgress = progress.get(p.getTeam());

        MBC.spawnFirework(p.getPlayer().getLocation(), p.getTeam().getColor());
        p.getPlayer().playSound(p.getPlayer(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 0.5f, 1);

        String courseComplete = p.getFormattedName() + " has completed " + (path+1) + "-" + (teamProgress[path]+1) + ": " + 
                            ChatColor.BOLD + "" + ChatColor.GOLD + beepTestMaps.get(path).get(teamProgress[path]).getName().trim() + "!";
        Bukkit.broadcastMessage(courseComplete);
        logger.log(courseComplete);

        int points = 0;
        switch(path){
            case 0: points = EASY_POINTS; break;
            case 1: points = MEDIUM_POINTS; break;
            case 2: points = HARD_POINTS; break;
            case 3: points = EXTREME_POINTS; break;
        }

        p.addCurrentScore(points);
        p.getPlayer().sendMessage(ChatColor.RED + "You completed a course!" + MBC.scoreFormatter(points));

        if(teamProgress[path] == 3) {
            String name = "";
            switch(path){
                case 0: 
                    name = "Easy"; 
                    p.getPlayer().getInventory().remove(Material.BLUE_DYE);   
                    break;
                case 1: 
                    name = "Medium"; 
                    p.getPlayer().getInventory().remove(Material.LIME_DYE);
                    break;
                case 2: 
                    name = "Hard"; 
                    p.getPlayer().getInventory().remove(Material.YELLOW_DYE);
                    break;
                case 3: 
                    name = "Extreme"; 
                    p.getPlayer().getInventory().remove(Material.RED_DYE);
                    break;
            }
            String pathCompleteMessage = p.getTeam().getChatColor() + "" + ChatColor.BOLD + "The " + p.getTeam().teamNameFormat() + ChatColor.BOLD + 
                        " have completed the " + ChatColor.GOLD + "" + ChatColor.BOLD + name + ChatColor.BOLD + " path!";
            Bukkit.broadcastMessage(pathCompleteMessage);
            logger.log(pathCompleteMessage);

            p.getPlayer().teleport(SPAWN);
            for (Participant part : p.getTeam().getPlayers()) {
                part.addCurrentScore(STAGE_POINTS);
                part.getPlayer().sendMessage(ChatColor.RED + "Your team completed a path!" + MBC.scoreFormatter(STAGE_POINTS));
            }
        
        }

        teamProgress[path]++;
    }

    /**
     * Respawns player p after death on path path.
     */
    private void respawn(int path, Participant p) {
        int[] teamProgress = progress.get(p.getTeam());
        Location respawn;
        switch(path) {
            case 0: respawn = easyCheckpointLocations[teamProgress[0]]; break;
            case 1: respawn = mediumCheckpointLocations[teamProgress[1]]; break;
            case 2: respawn = hardCheckpointLocations[teamProgress[2]]; break;
            case 3: respawn = extremeCheckpointLocations[teamProgress[3]]; break;
            default: respawn = SPAWN;
        }
        
        p.getPlayer().sendMessage("You fell!");
        p.getPlayer().setVelocity(new Vector(0, 0, 0));
        p.getPlayer().teleport(respawn);
    }

    /**
     * Loads all beep courses.
     */
    private void loadCourses() {
        easyLevels = BeepTestLevelLoader.loadEasyLevels();
        mediumLevels = BeepTestLevelLoader.loadRegularLevels();
        hardLevels = BeepTestLevelLoader.loadMediumLevels();
        extremeLevels = BeepTestLevelLoader.loadHardLevels();
    }

    /**
     * Returns 0 if in Easy Section, 1 if in Medium Section, 2 if in Hard Section, 3 if in Extreme Section
     */
    private int sectionLoc(Location loc) {
        if(loc.getX() > -5000) {
            if (loc.getZ() > -5000) return 3;
            else return 2;
        }
        else {
            if (loc.getZ() > -5000) return 1;
            else return 0;
        }
    }

    /**
     * For MBCTeam t, creates a random order of their players. Stores random order in randomTeamList
     */
    public void randomTeamOrder(MBCTeam t) {
        List<Participant> players = t.getPlayers();
        ArrayList<Player> randList = new ArrayList<>();
        for (int i = 0; i < t.getPlayers().size(); i++) {
            int rand = (int)(Math.random()*players.size());
            randList.add(players.get(rand).getPlayer());
            players.remove(rand);
        }
        randomTeamList.put(t, randList);
    }

    /**
     * For MBCTeam t, puts a 0 0 0 0 array. Updates upon progress
     */
    public void initializeProgress(MBCTeam t) {
        int[] starting = {0,0,0,0};
        progress.put(t, starting);
    }

    /**
     * Displays to player p where they are in order and for which team
     */
    public void displaySpot(Player p) {
        Participant part = Participant.getParticipant(p);
        MBC.spawnFirework(p.getLocation(), part.getTeam().getColor());

        if (!randomTeamList.containsKey(part.getTeam())) return;
        List<Player> playerList = randomTeamList.get(part.getTeam());
        int spot = playerList.indexOf(p);

        p.sendMessage(ChatColor.BOLD + "You will be parkouring " + getPlace(spot+1) + " for the " + part.getTeam().teamNameFormat() + ChatColor.BOLD + "!");
        p.sendTitle(ChatColor.BOLD + "You are " + getPlace(spot+1) + "!", part.getTeam().teamNameFormat(), 0, 60, 20);
    }

    /**
     * Given current round num, returns a list of players who should currently be running the course.
     */
    public List<Player> currentPlayers() {
        if (roundNum < 0) return null;
        List<Player> currentPlayers = new ArrayList<>();
        for (List<Player> randList : randomTeamList.values()) {
            currentPlayers.add(randList.get(roundNum%randList.size()));
        }
        return currentPlayers;
    }

    /**
     * Given current round num, returns a list of players who should currently be running the course.
     */
    public List<Player> futurePlayers() {
        if (roundNum == rounds-1) return null;
        List<Player> currentPlayers = new ArrayList<>();
        for (List<Player> randList : randomTeamList.values()) {
            currentPlayers.add(randList.get((roundNum+1)%randList.size()));
        }
        return currentPlayers;
    }

    /**
     * Places all beep courses on map.
     */
    public void placeCourses() {
        //easy
        Collections.shuffle(easyLevels);
        placeEasyHardCourse(easyLevels.get(0), -5033, 97, -5022);
        placeEasyHardCourse(easyLevels.get(1), -5033, 97, -5054);
        placeEasyHardCourse(easyLevels.get(2), -5033, 97, -5086);
        placeEasyHardCourse(easyLevels.get(3), -5033, 97, -5118);
        List<BeepTestLevel> easy = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            easy.add(easyLevels.get(i));
        }
        beepTestMaps.add(easy);

        //medium
        Collections.shuffle(mediumLevels);
        placeMediumExtremeCourse(mediumLevels.get(0), -5033, 97, -4952);
        placeMediumExtremeCourse(mediumLevels.get(1), -5033, 97, -4920);
        placeMediumExtremeCourse(mediumLevels.get(2), -5033, 97, -4888);
        placeMediumExtremeCourse(mediumLevels.get(3), -5033, 97, -4856);
        List<BeepTestLevel> medium = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            medium.add(mediumLevels.get(i));
        }
        beepTestMaps.add(medium);

        //hard
        Collections.shuffle(hardLevels);
        placeEasyHardCourse(hardLevels.get(0), -4991, 97, -5022);
        placeEasyHardCourse(hardLevels.get(1), -4991, 97, -5054);
        placeEasyHardCourse(hardLevels.get(2), -4991, 97, -5086);
        placeEasyHardCourse(hardLevels.get(3), -4991, 97, -5118);
        List<BeepTestLevel> hard = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            hard.add(hardLevels.get(i));
        }
        beepTestMaps.add(hard);

        //extreme
        Collections.shuffle(extremeLevels);
        placeMediumExtremeCourse(extremeLevels.get(0), -4991, 97, -4952);
        placeMediumExtremeCourse(extremeLevels.get(1), -4991, 97, -4920);
        placeMediumExtremeCourse(extremeLevels.get(2), -4991, 97, -4888);
        placeMediumExtremeCourse(extremeLevels.get(3), -4991, 97, -4856);
        List<BeepTestLevel> extreme = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            extreme.add(extremeLevels.get(i));
        }
        beepTestMaps.add(extreme);

    }

    /**
     * Spawns player p on path path when using teleporting items.
     */
    private void checkpoint(int path, Participant p) {
        int[] teamProgress = progress.get(p.getTeam());
        Location respawn;
        switch(path) {
            case 0: respawn = easyCheckpointLocations[teamProgress[0]]; break;
            case 1: respawn = mediumCheckpointLocations[teamProgress[1]]; break;
            case 2: respawn = hardCheckpointLocations[teamProgress[2]]; break;
            case 3: respawn = extremeCheckpointLocations[teamProgress[3]]; break;
            default: respawn = SPAWN;
        }
        
        p.getPlayer().sendMessage("Teleporting you to path...");
        p.getPlayer().teleport(respawn);
        p.getPlayer().setVelocity(new Vector(0, 0, 0));

        p.getPlayer().getInventory().remove(Material.RED_DYE);
        p.getPlayer().getInventory().remove(Material.YELLOW_DYE);
        p.getPlayer().getInventory().remove(Material.LIME_DYE);
        p.getPlayer().getInventory().remove(Material.BLUE_DYE);

        for (Participant player : p.getTeam().getPlayers()) {
            player.getPlayer().teleport(respawn);
        }

        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { pathTeleportItems(p.getPlayer());}
          }, 60L);
    }

    /**
     * Gives player p all path teleport items.
     */
    private void pathTeleportItems(Player p) {

        if (!currentPlayers().contains(p)) return;
        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta redMeta = redDye.getItemMeta();
        redMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.RED + "Extreme Path");
        redDye.setItemMeta(redMeta);
        ItemStack yellowDye = new ItemStack(Material.YELLOW_DYE);
        ItemMeta yellowMeta = redDye.getItemMeta();
        yellowMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "Hard Path");
        yellowDye.setItemMeta(yellowMeta);
        ItemStack limeDye = new ItemStack(Material.LIME_DYE);
        ItemMeta limeMeta = limeDye.getItemMeta();
        limeMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + "Medium Path");
        limeDye.setItemMeta(limeMeta);
        ItemStack blueDye = new ItemStack(Material.BLUE_DYE);
        ItemMeta blueMeta = blueDye.getItemMeta();
        blueMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.BLUE + "Easy Path");
        blueDye.setItemMeta(blueMeta);

        int[] teamProgress = progress.get(Participant.getParticipant(p).getTeam());

        if (teamProgress[0] < 4) p.getInventory().addItem(blueDye);
        if (teamProgress[1] < 4) p.getInventory().addItem(limeDye);
        if (teamProgress[2] < 4) p.getInventory().addItem(yellowDye);
        if (teamProgress[3] < 4) p.getInventory().addItem(redDye);
    }

    /**
     * Places either an easy or hard beep test level currentLevel in a proper zone specified by int x, int y, and int z (south west corner)
     */
    public void placeEasyHardCourse(BeepTestLevel currentLevel, int x, int y, int z) {
        EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("Party")));
        ForwardExtentCopy copy = new ForwardExtentCopy(BukkitAdapter.adapt(Bukkit.getWorld("Party")), currentLevel.getRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteFrom()), editSession, BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("Party"), x, y, z)));
        try {
        Operations.complete(copy);
        editSession.close();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
        
    }

    /**
     * Places either a medium or extreme beep test level currentLevel in a proper zone specified by int x, int y, and int z (south west corner)
     */
    public void placeMediumExtremeCourse(BeepTestLevel currentLevel, int x, int y, int z) {
        EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("Party")));
        ForwardExtentCopy copy = new ForwardExtentCopy(BukkitAdapter.adapt(Bukkit.getWorld("Party")), currentLevel.getReversedRegion(), BukkitAdapter.asBlockVector(currentLevel.getPasteReversed()), editSession, BukkitAdapter.asBlockVector(new Location(Bukkit.getWorld("Party"), x, y, z)));
        try {
        Operations.complete(copy);
        editSession.close();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    /**
     * Put or remove barriers for all 4 paths
     *
     * @param b Boolean for state of barriers: true = Barriers, false = Air
     */
    private void Barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        for (int y = 100; y <= 120; y++) {
            for (int x = -5041; x <= -4959; x++) {
                for (int z = -5021; z <= -4977; z++) {
                    if ((x == -5041 || x == -4959 || z == -5021 || z == -4977)) {
                        if (world().getBlockAt(x, y, z).getType().equals(Material.AIR) || world().getBlockAt(x, y, z).getType().equals(Material.BARRIER)) world().getBlockAt(x, y, z).setType(m);
                    } 
                    
                }
            }
        }
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) {
            e.getPlayer().teleport(SPAWN);
            return;
        }
        if (e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) {
            if (getState().equals(GameState.ACTIVE)) {
                
            }

        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) return;
        if (!getState().equals(GameState.ACTIVE)) return;

        Participant p = Participant.getParticipant(e.getPlayer());

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

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {

            
            Participant p = Participant.getParticipant(e.getPlayer());
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.RED_DYE) checkpoint(3, p);
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.YELLOW_DYE) checkpoint(2, p);
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.LIME_DYE) checkpoint(1, p);
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.BLUE_DYE) checkpoint(0, p);
            
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Material> trapdoorList = Set.of(Material.OAK_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
                                        Material.ACACIA_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
                                        Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR);
            if(trapdoorList.contains(e.getClickedBlock().getType())) e.setCancelled(true);
        }
    }
}