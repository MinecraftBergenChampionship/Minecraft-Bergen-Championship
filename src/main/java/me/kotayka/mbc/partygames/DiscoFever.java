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

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.*;


public class DiscoFever extends PartyGame {
    private int discoID = -1; // ID for event that applies randomized block pattern to all the air blocks in the Disco Region
    private int bossBarID = -1; // ID for event that constantly updates the boss bar time display.
    private long delay = 80; // Delay for disco event. Decreases every 5 rounds.
    private int rounds = 0;
    private int counter = -1;
    private BossBar bossBar;

    // Region boundaries for disco fever. Incrementally moved after each round.
    private Location discoPrimary = new Location(world(), 409, 0, 418);
    private Location discoSecondary = new Location(world(), 390, 0, 402);
    private Location backPrimary = new Location(world(), 409, 0, 401);
    private Location backSecondary = new Location(world(), 390, 0, 398);

    private Map<ColorType, Material> palette = new HashMap<>();
    private final Material EMTPY_BLOCK = Material.LIGHT_GRAY_STAINED_GLASS;
    private Material safe = null;
    private ColorType lastColor = null;
    private int DEATH_Y = -10; // y-level at which the game considers the player as eliminated
    private boolean deletedSpawn = false;
    // WorldEdit
    private Region disco = null;
    private Region back = null;
    private Pattern randomPattern = null;
    private EditSession editSession = null;

    public Map<ColorType, Material> woodMap = Map.of(ColorType.RED, Material.MANGROVE_PLANKS, ColorType.YELLOW, 
                                                        Material.BIRCH_PLANKS, ColorType.GREEN, Material.LIME_TERRACOTTA, 
                                                        ColorType.BLUE, Material.WARPED_PLANKS, ColorType.PINK, Material.CRIMSON_PLANKS);
    public Map<ColorType, Material> concreteMap = Map.of(ColorType.RED, Material.RED_CONCRETE, ColorType.YELLOW, 
                                                        Material.YELLOW_CONCRETE, ColorType.GREEN, Material.LIME_CONCRETE, 
                                                        ColorType.BLUE, Material.BLUE_CONCRETE, ColorType.PINK, Material.PURPLE_CONCRETE);
    public Map<ColorType, Material> oreMap = Map.of(ColorType.RED, Material.REDSTONE_BLOCK, ColorType.YELLOW, 
                                                        Material.GOLD_BLOCK, ColorType.GREEN, Material.EMERALD_BLOCK, 
                                                        ColorType.BLUE, Material.LAPIS_BLOCK, ColorType.PINK, Material.AMETHYST_BLOCK);
    public Map<ColorType, Material> woolMap = Map.of(ColorType.RED, Material.RED_WOOL, ColorType.YELLOW, 
                                                        Material.YELLOW_WOOL, ColorType.GREEN, Material.LIME_WOOL, 
                                                        ColorType.BLUE, Material.BLUE_WOOL, ColorType.PINK, Material.PURPLE_WOOL);
    public Map<ColorType, Material> miscMapOne = Map.of(ColorType.RED, Material.RED_MUSHROOM_BLOCK, ColorType.YELLOW, 
                                                        Material.HONEYCOMB_BLOCK, ColorType.GREEN, Material.MELON, 
                                                        ColorType.BLUE, Material.WARPED_WART_BLOCK, ColorType.PINK, Material.PURPUR_BLOCK);
    public Map<ColorType, Material> miscMapTwo = Map.of(ColorType.RED, Material.BRICKS, ColorType.YELLOW, 
                                                        Material.SPONGE, ColorType.GREEN, Material.MOSS_BLOCK, 
                                                        ColorType.BLUE, Material.PRISMARINE_BRICKS, ColorType.PINK, Material.STRIPPED_CHERRY_WOOD);

    public Map<ColorType, Material> randomizedBlocks = new HashMap<ColorType, Material>();

    public final int STAGE_POINTS = 2;
    public final int SURVIVAL_POINTS = 1;
    public final int WIN_POINTS = 10;
    public final Location SPAWN = new Location(Bukkit.getWorld("Party"), 400, 1.5, 400, 0, 0);

    // game instance
    private static DiscoFever instance = null;

    /**
     * Get the instance for this disco fever.
     */
    public static PartyGame getInstance() {
        if (instance == null) {
            instance = new DiscoFever();
        }
        return instance;
    }
    private DiscoFever() {
        super("DiscoFever", new String[] {
                "⑰ Go as fast as you can towards the block in your hand before the timer runs out, and the rest of the blocks disappear!",
                "⑰ The platform moves as time goes on, so make sure to keep moving forwards.\n\n" +
                "⑰ The timer will get faster as the game goes on!",
                "⑰ You'll get points for each player you outlive, along with points for every few rounds you survive.\n\n" +
                "⑰ Players recieve a bonus for reaching the end of the course (and getting to the monkey!!).",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑰ +2 points for surviving a speed increase\n" +
                        "⑰ +1 point for each player outlived\n" +
                        "⑰ +10 points for reaching the end of the course\n"
        });
    }

    @Override
    public void start() {
        super.start();

        world().setTime(18000);

        initializePalette();
        initializeRegions();
        Barriers(true);
        setSpawnArea(true);

        setGameState(GameState.TUTORIAL);
        randomPattern = generatePattern();

        setTimer(38);
    }

    @Override
    public void onRestart() {
        setSpawnArea(true);
    }

    @Override
    public void loadPlayers() {
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        if (!teamsAlive.isEmpty()) {
            teamsAlive.clear();
        }
        teamsAlive.addAll(getValidTeams());

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setFlying(false);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(MBC.SATURATION);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            playersAlive.add(p);
            p.getPlayer().teleport(SPAWN);
        }
        updatePlayersAliveScoreboard();
    }

    @Override
    public void events() {
        switch (getState()) {
            case TUTORIAL:
                if (timeRemaining == 0) {
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().teleport(SPAWN);
                        playerClear(p.getPlayer());
                    }
                    Barriers(true);
                    endDisco();
                    setGameState(GameState.END_ROUND);
                    rounds = 0;
                    counter = 0;
                    setTimer(7);
                } else if (timeRemaining == 36) {
                    Disco();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.getPlayer().sendTitle(ChatColor.GOLD + "" + ChatColor.BOLD + "Practice Starting!", "", 20, 60, 20);
                    }
                    Barriers(false);
                    MBC.getInstance().hideAllPlayers();
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
                break;
            case END_ROUND:
                if (timeRemaining == 0) {
                    delay = 80;
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        MBC.getInstance().showPlayers(p);
                    }
                    initializeRegions();
                    setGameState(GameState.STARTING);
                    setTimer(20);
                } else if (timeRemaining == 6) {
                    Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Practice Over!");
                } else if (timeRemaining == 5) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "The game will begin shortly...");
                }
                break;
            case STARTING:
                startingCountdown();
                if (timeRemaining == 10) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_MELLOHI, SoundCategory.RECORDS,1,1);
                    }
                }

                if (timeRemaining == 0) {
                    MBC.getInstance().hideAllPlayers();

                    Barriers(false);
                    setGameState(GameState.ACTIVE);
                    Disco();
                    setTimer(237);
                }
                break;
            case ACTIVE:
                if (timeRemaining == 0 || discoPrimary.getZ() > 674) {
                    endDisco();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.stopSound(Sound.MUSIC_DISC_MELLOHI, SoundCategory.RECORDS);
                    }
                    setGameState(GameState.END_GAME);
                    setTimer(7);
                }
                break;
            case END_GAME:
                if (timeRemaining == 0) {
                     if (MBC.getInstance().party == null) {
                        for (Participant p : MBC.getInstance().getPlayers()) {
                            p.addCurrentScoreToTotal();
                        }
                        MBC.getInstance().updatePlacings();
                        logger.logStats();
                        returnToLobby();
                    } else {
                        // start next game
                        setupNext();
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

    private Pattern generatePattern() {
        RandomPattern pattern = new RandomPattern();

        for (Material m : palette.values()) {
            pattern.add(BukkitAdapter.adapt(m.createBlockData()), 12.5);
        }
        pattern.add(BukkitAdapter.adapt(EMTPY_BLOCK.createBlockData()), 50);
        return pattern;
    }

    @Override
    public void endEvents() {
        logger.logStats();
        timeRemaining = 1;
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
        // TODO: move boss bar portion elsewhere
        if (discoPrimary.getZ() > 674) {
            endDisco();
            timeRemaining = 0;
            return;
        }
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAll();
        }
        bossBar = Bukkit.createBossBar(ChatColor.RED + "" + ChatColor.BOLD + "TIME", BarColor.RED, BarStyle.SOLID);
        bossBar.setVisible(true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(p);
            double displayDelay = ((double)delay)/20.0;
            createLine(18, "" + ChatColor.BOLD + ChatColor.AQUA + "Current Delay: " + ChatColor.RESET + displayDelay, Participant.getParticipant(p));
            createLine(17, ChatColor.RESET.toString(), Participant.getParticipant(p));
        }
        discoID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().getPlugin(), () -> {
            // Decrease delay between applying floor pattern and removing the blocks
            if (counter == 5 && rounds % 5 == 0 && delay > 10) {
                MBC.getInstance().cancelEvent(discoID);
                if (bossBar != null) {
                    bossBar.removeAll();
                    bossBar.setVisible(false);
                }
                if (delay > 20) {
                    delay -= 10;
                } else if (delay == 12) {
                    delay -= 2;
                }
                else {
                    delay -= 4;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (playersAlive.contains(Participant.getParticipant(p))) {
                        Participant.getParticipant(p).addCurrentScore(STAGE_POINTS);
                        p.sendMessage(ChatColor.YELLOW+"Things are speeding up!" + MBC.scoreFormatter(STAGE_POINTS));
                    }
                    else {
                        p.sendMessage(ChatColor.YELLOW+"Things are speeding up!");
                    }
                }
                logger.log("Things are speeding up! Delay = " + (delay/20.0));
                counter = 0;
                // Cancel task then call Disco() again to reinitialize it
                Disco();
                return;
            }

            // update BossBar
            // TODO can possibly be improved
            new BukkitRunnable() {
                double tmp = delay;
                @Override
                public void run() {
                    if (tmp < 0) {
                        cancel();
                        bossBar.setColor(BarColor.GREEN);
                        bossBar.setTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "TIME");
                        refillBar();
                    } else {
                        // these next two lines should go whenever it switches from red to green;
                        // tried under bossBar = Bukkit.create() ... but didn't work in my limited testing.
                        // this works fine, but is more computationally expensive, although I'm not sure if it is significant
                        bossBar.setColor(BarColor.RED);
                        bossBar.setTitle(ChatColor.RED + "" + ChatColor.BOLD + "TIME");
                        bossBar.setProgress(tmp / delay);
                        tmp--;
                    }
                }
            }.runTaskTimer(MBC.getInstance().getPlugin(), 0, 1);


            // Apply randomized palette to the region
            editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world()));
            try {
                editSession.setBlocks(disco, randomPattern);
                if (backPrimary.getZ() > 402) {
                    editSession.setBlocks(back, BlockTypes.LIGHT_GRAY_CONCRETE.getDefaultState());
                    if (!deletedSpawn) {
                        setSpawnArea(false);
                        deletedSpawn = true;
                    }
                }
                editSession.close();
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }

            // Choose safe block
            ColorType[] colors = palette.keySet().stream()
                    .filter(color -> !color.equals(lastColor))
                    .toArray(ColorType[]::new);
            int rand = (int) (Math.random() * colors.length);
            lastColor = colors[rand];
            safe = palette.get(colors[rand]);
            showSafeBlock(safe);
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
                    if (backPrimary.getZ() > 402) {
                        for (BlockVector3 block : back) {
                            Block b = world().getBlockAt(block.x(), block.y(), block.z());
                            b.setType(Material.AIR);
                        }
                    }

                    if (getState().equals(GameState.ACTIVE)) {
                        if (counter != 5) {
                            counter++;
                        }
                        rounds++;
                        incrementRegions();
                    }
                    MBC.getInstance().cancelEvent(bossBarID);
                }
            };
            removeFloor.runTaskLater(MBC.getInstance().getPlugin(), delay);
        }, 0, delay+40);
    }

    private void refillBar() {
        new BukkitRunnable() {
            int tmp = 0;
            @Override
            public void run() {
                tmp++;
                if ((tmp / 40.0) == 1) {
                    cancel();
                    return;
                }
                bossBar.setProgress(tmp / 40.0);
            }
        }.runTaskTimer(MBC.getInstance().getPlugin(), 0, 1);
    }

    /**
     * Fills the hotbar of each player with the safe block.
     *
     * @param m The material which will not be destroyed on the dance floor.
     */
    private void showSafeBlock(Material m) {
        ItemStack item = new ItemStack(m);
        for (Participant p : playersAlive) {
            p.getPlayer().getInventory().setItem(0, item);
            p.getPlayer().getInventory().setItem(1, item);
            p.getPlayer().getInventory().setItem(2, item);
            p.getPlayer().getInventory().setItem(3, item);
            p.getPlayer().getInventory().setItem(4, item);
            p.getPlayer().getInventory().setItem(5, item);
            p.getPlayer().getInventory().setItem(6, item);
            p.getPlayer().getInventory().setItem(7, item);
            p.getPlayer().getInventory().setItem(8, item);
            p.getPlayer().getInventory().setItem(9, item);
            p.getPlayer().getInventory().setItem(40, item);
        }
    }

    private void playerClear(Player p) {
        p.getInventory().setItem(0, null);
        p.getInventory().setItem(1, null);
        p.getInventory().setItem(2, null);
        p.getInventory().setItem(3, null);
        p.getInventory().setItem(4, null);
        p.getInventory().setItem(5, null);
        p.getInventory().setItem(6, null);
        p.getInventory().setItem(7, null);
        p.getInventory().setItem(8, null);
        p.getInventory().setItem(9, null);
        p.getInventory().setItem(40, null);
    }

    /**
     * Increments each region by 4
     */
    private void incrementRegions() {
        discoPrimary.setZ(discoPrimary.getZ()+4);
        discoSecondary.setZ(discoSecondary.getZ()+4);
        backPrimary.setZ(backPrimary.getZ()+4);
        backSecondary.setZ(backSecondary.getZ()+4);
        disco = new CuboidRegion(BukkitAdapter.asBlockVector(discoPrimary), BukkitAdapter.asBlockVector(discoSecondary));
        back = new CuboidRegion(BukkitAdapter.asBlockVector(backPrimary), BukkitAdapter.asBlockVector(backSecondary));
    }


    /**
     * Cancels the task represented by discoID,
     * ending the Disco Fever game.
     */
    private void endDisco() {
        if (discoID != -1) {
            MBC.getInstance().cancelEvent(discoID);
            MBC.getInstance().cancelEvent(bossBarID);
            discoID = -1;
            bossBarID = -1;
        }
        if (bossBar != null) {
            bossBar.setVisible(false);
            bossBar.removeAll();
        }

        for (BlockVector3 block : disco) {
            Block b = world().getBlockAt(block.x(), block.y(), block.z());
            b.setType(Material.AIR);
        }
        if (backPrimary.getZ() > 402) {
            for (BlockVector3 block : back) {
                Block b = world().getBlockAt(block.x(), block.y(), block.z());
                b.setType(Material.AIR);
            }
        }

        if (getState() != GameState.TUTORIAL) {
            roundWinners(WIN_POINTS, SURVIVAL_POINTS);

            gameOver();
        }
    }

    /**
     * Determine which blocks to use as a color palette such that:
     * - No two blocks have the same ColorType
     * - No two blocks have the same TextureType
     */
    private void initializePalette() {

        List<ColorType> colorList = new ArrayList<>();
        colorList.add(ColorType.RED);
        colorList.add(ColorType.YELLOW);
        colorList.add(ColorType.GREEN);
        colorList.add(ColorType.BLUE);
        colorList.add(ColorType.PINK);

        Collections.shuffle(colorList);
        // place 1 is wood, place 2 is concrete, place 3 is ores, place 4 is wool, place 5 is one of the two 

        for (int i = 0; i < colorList.size(); i++) {
            ColorType c = colorList.get(i);
            Material m;
            switch (i) {
                case 0 -> {
                    palette.put(c, woodMap.get(c));
                }
                case 1 -> {
                    palette.put(c, concreteMap.get(c));
                }
                case 2 -> {
                    palette.put(c, oreMap.get(c));
                }
                case 3 -> {
                    palette.put(c, woolMap.get(c));
                }
                case 4 -> {
                    if (Math.random()*2 == 1) {
                        palette.put(c, miscMapOne.get(c));
                    } else {
                        palette.put(c, miscMapTwo.get(c));
                    }
                }
            }
            // TODO: this randomization sucks someone should make it better
        }
    }

    /**
     * Define initial regions for both disco section and falling section, as well as the Edit Session.
     * Does not perform randomization of blocks, but should fill with appropriate
     * starter blocks.
     */
    private void initializeRegions() {
        discoPrimary = new Location(world(), 409, 0, 418);
        discoSecondary = new Location(world(), 390, 0, 402);
        backPrimary = new Location(world(), 409, 0, 401);
        backSecondary = new Location(world(), 390, 0, 398);
        disco = new CuboidRegion(BukkitAdapter.asBlockVector(discoPrimary), BukkitAdapter.asBlockVector(discoSecondary));
        back = new CuboidRegion(BukkitAdapter.asBlockVector(backPrimary), BukkitAdapter.asBlockVector(backSecondary));
        editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world()));
        try {
            editSession.setBlocks(disco, BlockTypes.LIGHT_GRAY_STAINED_GLASS.getDefaultState());
            //editSession.setBlocks(back, BlockTypes.LIGHT_GRAY_CONCRETE.getDefaultState());
            editSession.close();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR && e.getTo().getY() < -100)  {
            e.getPlayer().teleport(SPAWN);
            return;
        }

        if (e.getPlayer().getGameMode() != GameMode.ADVENTURE) return;
        if (e.getTo().getY() > DEATH_Y) return;

        Player p = e.getPlayer();
        if (getState().equals(GameState.TUTORIAL) || getState().equals(GameState.END_ROUND)) {
            p.teleport(SPAWN);
            p.sendMessage(ChatColor.RED+"You fell!");
            return;
        }

        if (getState().equals(GameState.ACTIVE)) {
            p.setGameMode(GameMode.SPECTATOR);
            playerClear(p);
            p.sendTitle(" ", ChatColor.RED + "You died!", 0, 60, 20);
            Participant part = Participant.getParticipant(p);
            if (part == null) return;
            updatePlayersAlive(part);
            for (Player person : Bukkit.getOnlinePlayers()) {
                if (playersAlive.contains(Participant.getParticipant(person))) {
                    Participant.getParticipant(person).addCurrentScore(SURVIVAL_POINTS);
                    person.sendMessage(part.getFormattedName() + " was eliminated!" + MBC.scoreFormatter(SURVIVAL_POINTS));
                }
                else {
                    person.sendMessage(part.getFormattedName() + " was eliminated!");
                }
                person.playSound(person, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,1,1);
            }
            logger.log(p.getName() + " fell into the void at delay " + delay);
            MBC.getInstance().showPlayers(part);
            updatePlayersAliveScoreboard();
            // other things later
        }
    }


    /**
     * Prevent players from throwing items.
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
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

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.ADVENTURE) {
            playerClear(player);
            Participant part = Participant.getParticipant(player);
            updatePlayersAlive(part);
            for (Participant i : playersAlive) {
                i.addCurrentScore(SURVIVAL_POINTS);
            }
            updatePlayersAliveScoreboard();
        }
    }

    /**
     * Place or remove spawn area;
     * b = True -> Place Spawn
     * b = False -> Remove spawn
     */
    public void setSpawnArea(boolean b) {
        if (!b) {
            for (int y = 0; y < 4; y++) {
                for (int x = 409; x >= 390; x--) {
                    for (int z = 398; z <= 401; z++) {
                        world().getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        } else {
            for (int y = 0; y < 4; y++) {
                for (int x = 409; x >= 390; x--) {
                    for (int z = 398; z <= 401; z++) {
                        world().getBlockAt(x, y, z).setType(world().getBlockAt(x, y, z-398).getType());
                    }
                }
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