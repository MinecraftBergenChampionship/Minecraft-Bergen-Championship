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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeepTest extends PartyGame {
    // Maps of levels by name
    private List<BeepTestLevel> easyLevels = null;
    private List<BeepTestLevel> regularLevels = null;
    private List<BeepTestLevel> mediumLevels = null;
    private List<BeepTestLevel> hardLevels = null;
    private int rounds = 0;
    private boolean oppositeSide = false;
    private Set<Player> fallenPlayers = new HashSet<>();

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

    // game instance
    private static BeepTest instance = null;

    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new BeepTest();
            return new BeepTest();
        } else {
            return instance;
        }
    }

    private BeepTest() {
        super("BeepTest", new String[] {
                "⑰ The fitness gram " + ChatColor.BOLD + "Beep Test" + ChatColor.RESET + " is a multistage parkour capacity test that progressively gets more difficult as it continues.",
                "⑰ Jump from one side of the map to the other to complete the courses as fast as you can.\n\n" +  
                "⑰ The parkour difficulty starts easy, but gets more difficult each time the arena changes color.",
                "⑰ The third time you fail the parkour course, or do not complete it in time, you are eliminated.\n\n" + 
                "⑰ You earn points for each stage and each difficulty you complete.",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +1 point for each easy level completed\n" +
                        "⑰ +2 points for each medium level completed\n" +
                        "⑰ +3 points for each hard level completed\n" +
                        "⑰ +4 points for each extreme level completed\n" +
                        "⑰ +4 points for completing a color of difficulty\n"
        });

        loadCourses();

    }

    @Override
    public void start() {
        super.start();

        world().setTime(18000);

        loadInitialArena();
        Barriers(true);
        resetGround();

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    @Override
    public void onRestart() {

    }

    @Override
    public void loadPlayers() {
        //MBC.getInstance().hideAllPlayers();
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(SPAWN);
            p.addPotionEffect(MBC.SATURATION);
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            Participant par = Participant.getParticipant(p);
            if (par != null) {
                p.getInventory().setBoots(par.getTeam().getColoredLeatherArmor(leatherBoots));
                par.board.getTeam(par.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            }
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
                startingCountdown();
                if (timeRemaining == 0) {
                    //MBC.getInstance().hideAllPlayers();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS,1,1); // temp?
                    }
                    Barriers(false);
                    newGround();
                    nextRound();
                    roundDisplay();
                    setGameState(GameState.ACTIVE);
                    setTimer(13);
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0) {
                    Bukkit.broadcastMessage("rounds == " + rounds);
                    if (rounds > 14) {
                        // there are no more rounds. game is over
                        Bukkit.broadcastMessage("The game is over!");
                        gameOver();
                    } else {
                        // clear level, move onto next level
                        rounds++;
                        newGround();
                        respawn();
                        nextRound();
                        roundDisplay();
                    }
                    int timeForLevel;
                    if (rounds < 4) {
                        timeForLevel = 10;
                    } else if (rounds < 8) {
                        timeForLevel = 15;
                    } else if (rounds < 12) {
                        timeForLevel = 20;
                    } else {
                        timeForLevel = 25;
                    }
                    setTimer(timeForLevel+1);
                }
                break;
            case END_GAME:
        }

    }

    @Override
    public void createScoreboard(Participant p) {
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
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.ADVENTURE) return;

        if (p.getLocation().getY() < -59 && fallenPlayers.add(p)) {
            p.sendMessage(ChatColor.RED + "You fell!");
        }
    }

    /*
     * Respawn all players that fell in the previous round to the appropriate location.
     */
    private void respawn() {
        if (fallenPlayers.size() == 0) return;
        for (Player p : fallenPlayers) {
            if (oppositeSide) {
                p.teleport(SPAWN);
            } else {
                p.teleport(OPPOSITE_SPAWN);
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

    /**
     * Spawns the course for the next round on the appropriate side.
     */
    private void nextRound() {
        BeepTestLevel lvl = chooseLevel();
        if (oppositeSide) {
            oppositeSide = false;
            EditSession editSession = WorldEdit.getInstance().newEditSession(WorldEditWorld);
            ForwardExtentCopy copy = new ForwardExtentCopy(WorldEditWorld, lvl.getReversedRegion(), BukkitAdapter.asBlockVector(lvl.getPasteReversed()), editSession, BukkitAdapter.asBlockVector(courseToPrimary));
            try {
                Operations.complete(copy);
                editSession.close();
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
        } else {
            oppositeSide = true;
            EditSession editSession = WorldEdit.getInstance().newEditSession(WorldEditWorld);
            ForwardExtentCopy copy = new ForwardExtentCopy(WorldEditWorld, lvl.getRegion(), BukkitAdapter.asBlockVector(lvl.getPasteFrom()), editSession, BukkitAdapter.asBlockVector(courseToPrimary));
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

    /**
     * Randomly chooses a level from the appropriate category based on rounds.
     *
     * @return The randomly selected level.
     */
    private BeepTestLevel chooseLevel() {
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
        BeepTestLevel lvl = chooseFrom.get(rand);
        chooseFrom.remove(rand);
        return lvl;
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
}