package me.kotayka.mbc.partygames;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.PartyGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class DiscoFever extends PartyGame {
    private int discoID = -1; // ID for event that applies randomized block pattern to all the air blocks in the Disco Region
    private long delay = 100; // Delay for disco event. Decreases every 5 rounds.
    private int rounds = 0;

    // Region boundaries for disco fever. Incrementally moved after each round.
    private Location discoPrimary = new Location(world(), 409, 0, 418);
    private Location discoSecondary = new Location(world(), 390, 0, 407);
    private Location backPrimary = new Location(world(), 409, 0, 406);
    private Location backSecondary = new Location(world(), 409, 0, 403);

    private Map<ColorType, Material> palette = new HashMap<>();
    private final Material EMTPY_BLOCK = Material.LIGHT_GRAY_STAINED_GLASS;
    private Material safe = null;
    private ColorType lastColor = null;

    // WorldEdit
    private Region disco = null;
    private Region back = null;
    private Pattern randomPattern = null;
    private EditSession editSession = null;

    /**
     * Get the instance for this disco fever.
     */
    public static PartyGame getInstance() {
        if (PartyGameFactory.getPartyGame("Disco Fever") != null) {
            return PartyGameFactory.getPartyGame("Disco Fever");
        }
        return new DiscoFever();
    }
    private DiscoFever() {
        super("Disco Fever", new Location(Bukkit.getWorld("Party"), 400, 1, 400,0,0), new String[] {
                "text 1",
                "text 2",
                "text 3",
                "scoring"
        });
    }

    @Override
    public void start() {
        super.start();

        initializePalette();
        initializeRegions();

        setGameState(GameState.TUTORIAL);

        setTimer(37);
    }

    @Override
    public void loadPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setInvulnerable(false);
            p.setFlying(false);
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.addPotionEffect(MBC.SATURATION);
            p.teleport(SPAWN);
        }
    }

    @Override
    public void events() {
        switch(getState()) {
            case TUTORIAL:
                if (timeRemaining == 0) {
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().teleport(SPAWN);
                    }
                    setGameState(GameState.STARTING);
                    setTimer(20);
                } else if (timeRemaining == 36) {
                    randomPattern = generatePattern();
                    // TODO: start playing music
                    Barriers(false);
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
        }
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
    }

    private Pattern generatePattern() {
        RandomPattern pattern = new RandomPattern();

        for (Material m : palette.values()) {
            pattern.add(BukkitAdapter.adapt(m.createBlockData()), 12.5);
        }
        pattern.add(BukkitAdapter.adapt(EMTPY_BLOCK.createBlockData()), 50);
        return pattern;
    }


    /**
     * Initiates the Bukkit Runnable task that comprises Disco Fever.
     * This task:
     * - Applies the random palette to the floor, replacing all air blocks.
     * - Generates the new palette
     * - After a delay, removes all blocks except the randomly chosen block.
     * - Updates the region, including updating the editSession instance and both the "back" region and disco region.
     * - Repeats the cycle. The delay between the first event and the second decreases over several iterations.
     *      - The delay may never reach less than 0.5 seconds.
     * - Can only be canceled with the "endDisco()" method within this class
     */
    private void Disco() {
        // Cancel event whenever delay decreases
        if (discoID != -1) {
            MBC.getInstance().cancelEvent(discoID);
            // Decrease delay between applying floor pattern and removing the blocks
            if (rounds % 6 == 0 && delay > 0.5) {
                if (delay > 1) {
                    delay -= 0.5;
                } else {
                    delay -= 0.2;
                }
            }
        }

        discoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().getPlugin(), () -> {
            // Apply randomized palette to the region
            for (BlockVector3 block : disco) {
                randomPattern.applyBlock(block);
            }

            // Choose safe block
            ColorType[] colors = palette.keySet().stream()
                    .filter(color -> !color.equals(lastColor))
                    .toArray(ColorType[]::new);
            int rand = (int) (Math.random() * colors.length);
            lastColor = colors[rand];
            safe = palette.get(colors[rand]);

            BukkitRunnable removeFloor = new BukkitRunnable() {
                @Override
                public void run() {
                    for (BlockVector3 block : disco) {
                        Block b = world().getBlockAt(block.x(), block.y(), block.z());
                        Material mat = b.getType();
                        if (mat != safe) {
                            b.setType(Material.AIR);
                        }
                    }
                }
            };
            removeFloor.runTaskLater(MBC.getInstance().getPlugin(), delay);
        }, 0, delay+40);
    }

    /**
     * Cancels the task represented by discoID,
     * ending the Disco Fever game.
     */
    private void endDisco() {
        if (discoID != -1) {
            MBC.getInstance().cancelEvent(discoID);
        }
    }

    /**
     * Determine which blocks to use as a color palette such that:
     * - No two blocks have the same ColorType
     * - No two blocks have the same TextureType
     */
    private void initializePalette() {
        // TODO: Randomization
        palette.put(ColorType.RED, Material.ACACIA_PLANKS);
        palette.put(ColorType.BLUE, Material.LAPIS_BLOCK);
        palette.put(ColorType.GREEN, Material.EMERALD_BLOCK);
        palette.put(ColorType.PINK, Material.PURPLE_WOOL);
        palette.put(ColorType.YELLOW, Material.YELLOW_CONCRETE);
    }

    /**
     * Define initial regions for both disco section and falling section, as well as the Edit Session.
     * Does not perform randomization of blocks, but should fill with appropriate
     * starter blocks.
     */
    private void initializeRegions() {
        disco = new CuboidRegion(BukkitAdapter.asBlockVector(discoPrimary), BukkitAdapter.asBlockVector(discoSecondary));
        //back = new CuboidRegion(BukkitAdapter.asBlockVector(backPrimary), BukkitAdapter.asBlockVector(backSecondary));
        editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world()));
        try {
            editSession.setBlocks(disco, BlockTypes.LIGHT_GRAY_STAINED_GLASS.getDefaultState());
            //editSession.setBlocks(back, BlockTypes.LIGHT_GRAY_CONCRETE.getDefaultState());
        } catch (MaxChangedBlocksException e) {
           e.printStackTrace();
        }
    }


    /**
     * Determine whether or not to put up barrier walls for start of game.
     *
     * @param b Boolean for state of barriers: true = Barriers, false = Air
     */
    public void Barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;

        for (int y = 1; y <= 3; y++) {
            for (int x = 390; x <= 409; x++) {
                world().getBlockAt(x, y, 402).setType(m);
            }
        }
    }
}

/**
 * DiscoColor is an enum that represents the different color blocks
 * that can appear in the disco fever minigame.
 */
enum ColorType {
    YELLOW,
    BLUE,
    RED,
    PINK,
    GREEN,
    EMPTY
}

/**
 * DiscoTexture is an enum that represents the different textures
 * of blocks that can appear in the disco fever minigame.
 */
enum TextureType {
    PLANKS,
    ORE_BLOCK,
    WOOL,
    CONCRETE,
    MISC
}

/**
 * DiscoBlock represents a block that can appear during the Disco Fever Party Game.
 * Each block has a TextureType, a ColorType, and a Material.
 */
class DiscoBlock {
    Material material;
    ColorType color;
    TextureType texture;

    public DiscoBlock(Material m, ColorType c, TextureType t) {
        this.material = m;
        this.color = c;
        this.texture = t;
    }

}