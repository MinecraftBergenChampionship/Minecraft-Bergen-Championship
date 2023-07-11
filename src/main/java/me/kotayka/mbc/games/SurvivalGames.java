package me.kotayka.mbc.games;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.sgMaps.BCA;
import me.kotayka.mbc.gameMaps.sgMaps.SurvivalGamesMap;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

public class SurvivalGames extends Game {
    private final SurvivalGamesMap map = new BCA();
    private List<SurvivalGamesItem> items;
    private List<SurvivalGamesItem> supply_items;

    // This file must be in /home/MBCAdmin/MBC/ and re-added each time an update is made
    private final File CHEST_FILE = new File("survival_games_items.json");
    private final File SUPPLY_FILE = new File("supply_crate_items.json");
    private SurvivalGamesEvent event = SurvivalGamesEvent.GRACE_OVER;
    private final List<Location> chestLocations = new ArrayList<Location>(50);
    private final List<SupplyCrate> crates = new ArrayList<SupplyCrate>(3);
    private Map<Player, Integer> playerKills = new HashMap<>();
    private boolean dropLocation = false;
    private int crateNum = 0;

    // SCORING
    public final int KILL_POINTS = 45;
    public final int SURVIVAL_POINTS = 3;
    public final int WIN_POINTS = 45;

    public SurvivalGames() {
        super("SurvivalGames");

        try {
            readItems();
        } catch(IOException | ParseException e) {
            Bukkit.broadcastMessage(ChatColor.YELLOW+ e.getMessage());
            Bukkit.broadcastMessage(ChatColor.RED+"Unable to parse " + CHEST_FILE.getAbsolutePath() + " or " + SUPPLY_FILE.getAbsolutePath());
        }
    }

    private void readItems() throws IOException, ParseException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<SurvivalGamesItem>>() {}.getType();
        Reader reader = new FileReader(CHEST_FILE);
        items = gson.fromJson(reader, listType);
        reader = new FileReader(SUPPLY_FILE);
        supply_items = gson.fromJson(reader, listType);
        reader.close();
    }

    @Override
    public void loadPlayers() {
        teamsAlive.addAll(getValidTeams());
        map.setBarriers(true);
        setPVP(false);
        for (Participant p : MBC.getInstance().getPlayers()) {
            playersAlive.add(p);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().getInventory().clear();
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 10, false, false));
            map.spawnPlayers();
        }
        regenChest();
    }

    @Override
    public void start() {
        super.start();

        // setGameState(TUTORIAL);
        setGameState(GameState.STARTING);

        setTimer(30);
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(24, ChatColor.AQUA + "" + ChatColor.BOLD + "Game "+ MBC.getInstance().gameNum+"/6:" + ChatColor.WHITE + " Survival Games", p);
        createLine(21, ChatColor.AQUA+""+ChatColor.BOLD+"Map: " + ChatColor.RESET+ map.mapName);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);
        updatePlayersAliveScoreboard();
        createLine(0, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+"0");

        updateInGameTeamScoreboard();
    }

    @Override
    public void events() {
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
            } else {
                map.setBarriers(false);
                for (GamePlayer p : gamePlayers) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                setGameState(GameState.ACTIVE);
                Bukkit.broadcastMessage(ChatColor.RED+"Grace ends in 1 minute!");
                timeRemaining = 720;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            /*
             * Event timeline:
             *  12 mins: Game Starts
             *  11 mins: Grace Ends
             *  10 mins: First supply crate announced; border begins to move
             *   9 mins: First supply crate spawns
             *   8 mins: 1 minute to chest refill, 2nd supply crate announced
             *   7 mins: Chest Refill, 2nd supply crate spawns, 3rd supply crate announced
             *   6 mins: Last supply crate lands
             */
            if (crates.size() > 0) { crateParticles(); }

            if (timeRemaining == 0) {
                if (teamsAlive.size() > 1) {
                    Bukkit.broadcastMessage(ChatColor.RED+"Border shrinking!");
                    map.Overtime();
                    setGameState(GameState.OVERTIME);
                    timeRemaining = 45;
                } else {
                    setGameState(GameState.END_GAME);
                    gameOverGraphics();
                    roundWinners(WIN_POINTS);
                    timeRemaining = 37;
                }
            }

            if (timeRemaining == 660) {
                event = SurvivalGamesEvent.SUPPLY_CRATE;
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                }
                setPVP(true);
                Bukkit.broadcastMessage(ChatColor.DARK_RED+"Grace period is now over.");
                map.startBorder();
                Bukkit.broadcastMessage(ChatColor.RED+"Border will continue to shrink!");
            } else if (timeRemaining == 600) {
                crateLocation();
            } else if (timeRemaining == 540) {
                spawnSupplyCrate();
            } else if (timeRemaining == 480) {
                event = SurvivalGamesEvent.CHEST_REFILL;
                crateLocation();
                Bukkit.broadcastMessage(ChatColor.RED+""+ChatColor.BOLD+"Chests will refill in one minute!");
            } else if (timeRemaining == 420) {
                spawnSupplyCrate();
                Bukkit.broadcastMessage(ChatColor.RED+""+ChatColor.BOLD+"Chests have been refilled!");
                regenChest();
                event = SurvivalGamesEvent.SUPPLY_CRATE;
                crateLocation();
            } else if (timeRemaining == 360) {
                spawnSupplyCrate();
                event = SurvivalGamesEvent.DEATHMATCH;
            }
            UpdateEvent();
        } else if (getState().equals(GameState.OVERTIME)) {
            if (timeRemaining == 0) {
                setGameState(GameState.END_GAME);
                gameOverGraphics();
                roundWinners(WIN_POINTS);
                timeRemaining = 37;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 35) {
                map.resetMap();
            }
            gameEndEvents();
        }
    }

    /**
     * Updates player scoreboard for each upcoming event using SurvivalGamesEnum
     */
    private void UpdateEvent() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            // display coordinates of last unopened supply drop separately
            if (dropLocation && crates.size() > 0) {
                Location l = crates.get(crateNum).getLocation();
                createLine(22, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Supply drop: " + ChatColor.RESET + "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")", p);
            } else {
                if (event.equals(SurvivalGamesEvent.SUPPLY_CRATE) || event.equals(SurvivalGamesEvent.CHEST_REFILL) && crates.size() > 0) {
                    dropLocation = true;
                }
            }

            switch (event) {
                case GRACE_OVER ->
                        createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Grace ends in: " + ChatColor.RESET + getFormattedTime(timeRemaining - 660), p);
                case CHEST_REFILL ->
                        createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chest refill: " + ChatColor.RESET + getFormattedTime(timeRemaining - 420));
                case DEATHMATCH ->
                        createLine(23, ChatColor.RED + "" + ChatColor.BOLD + "Deathmatch: " + ChatColor.WHITE + "Active");
                case SUPPLY_CRATE -> {
                    // hard coded times; the 2nd supply drop coincides with chest refill
                    int nextTime = (timeRemaining > 540) ? timeRemaining - 540 : timeRemaining - 360;
                    createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Next supply crate: " + ChatColor.RESET + getFormattedTime(nextTime));
                }
            }
        }
    }

    /**
     * Generate location of supply crate
     */
    public void crateLocation() {
        int index = (int) (Math.random()*chestLocations.size());

        Location l = (chestLocations.get(index));
        chestLocations.remove(l);
        dropLocation = true;
        crates.add(new SupplyCrate(l, false));
        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Supply crate spawning at " + ChatColor.RESET+"("+l.getX()+", "+l.getY()+", "+l.getZ()+")");
    }

    /**
     * Given the location of the crate is predetermined, replace the block
     * with the crate block and generate loot
     * @see SurvivalGames crateLocation()
     */
    public void spawnSupplyCrate() {
        // delete this once it is final
        double totalWeight = 0;
        for (SurvivalGamesItem item : supply_items) {
            totalWeight += item.getWeight();
        }
        Bukkit.broadcastMessage("[Debug] Total Supply Weight == " + totalWeight);

        Location l = crates.get(crateNum).getLocation();
        l.getBlock().setType(Material.BLACK_SHULKER_BOX);
        ShulkerBox crate = (ShulkerBox) map.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()).getState();

        int chestItems = (int) (Math.random()*6+5);
        for (int b = 0; b < chestItems; b++) {
            // Now choose a random item.
            int idx = 0;
            for (double r = Math.random() * totalWeight; idx < supply_items.size() - 1; ++idx) {
                r -= supply_items.get(idx).getWeight();
                if (r <= 0.0) break;
            }

            crate.getInventory().setItem((int) (Math.random()*27), supply_items.get(idx).getItem());
        }
    }

    /**
     * Spawn particles at super chest spawning location
     */
    private void crateParticles() {
        for (SupplyCrate crate : crates) {
            if (!crate.beenOpened()) {
                Location l = crate.getLocation();
                map.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, l.getX() + Math.random()*0.5-0.5, l.getBlockY() + 1, l.getZ() + Math.random()*0.5-0.5, 5);
                map.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, l.getX() + Math.random()*0.5-0.5, l.getBlockY() + Math.random(), l.getZ() + Math.random()*0.5-0.5, 5);
            }
        }
    }

    /**
     * Regenerates the loot within every chest in the map.
     * If empty, updates list of eligible Super Chests.
     */
    public void regenChest() {
        // delete when final.
        double totalWeight = 0;
        for (SurvivalGamesItem item : items) {
            totalWeight += item.getWeight();
        }
        Bukkit.broadcastMessage("[Debug] Total Weight == " + totalWeight);

        Random rand = new Random();
        Chunk[] c = map.getWorld().getLoadedChunks();
        for (Chunk chunk : c) {//loop through loaded chunks
            for (int x = 0; x < chunk.getTileEntities().length; x++) {//loop through tile entities within loaded chunks
                if (chunk.getTileEntities()[x] instanceof Chest) {
                    Chest chest = (Chest) chunk.getTileEntities()[x];

                    if (crates.size() < 1 && map.checkChest(chest)) {
                        chestLocations.add(chest.getLocation());
                    }

                    chest.getInventory().clear();
                    int chestItems = rand.nextInt(2) + 5;
                    for (int b = 0; b < chestItems; b++) {
                        // Now choose a random item w/weight
                        int idx = 0;
                        for (double r = Math.random() * totalWeight; idx < items.size() - 1; ++idx) {
                            r -= items.get(idx).getWeight();
                            if (r <= 0.0) break;
                        }

                        chest.getInventory().setItem(rand.nextInt(27), items.get(idx).getItem());
                    }
                }
            }
        }
    }

    /**
     * Handles event where player eats stew; may account for
     * other things if other custom items are added.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(e.getAction().isRightClick())) return;
        Player p = e.getPlayer();


        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.BLACK_SHULKER_BOX)) {
            crates.get(crateNum).setOpened(true);
            dropLocation = false;
            crateNum++;
            return;
        }

        if (p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW && p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW) {
            return;
        }

        boolean mainHand = p.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW;
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, true));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        map.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 3, Material.DIRT.createBlockData());

        if (mainHand) {
            p.getInventory().setItemInMainHand(null);
        } else {
            p.getInventory().setItemInOffHand(null);
        }
    }

    /**
     * Death events
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Participant killer = Participant.getParticipant(e.getPlayer().getKiller());
        if (killer != null) {
            killer.addCurrentScore(KILL_POINTS);
            if (playerKills.get(e.getPlayer().getKiller()) == null) {
                playerKills.put(e.getPlayer().getKiller(), 1);
                createLine(0, ChatColor.YELLOW+""+ChatColor.BOLD+"Your Kills: "+ChatColor.RESET+"1", killer);
            } else {
                int kills = playerKills.get(e.getPlayer().getKiller());
                playerKills.put(e.getPlayer().getKiller(), kills++);
                createLine(0, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+kills, killer);
            }
        }

        deathEffectsWithHealth(e);
        // may require testing due to concurrency
        for (Participant p : playersAlive) {
            p.addCurrentScore(SURVIVAL_POINTS);
        }
    }

    /**
     * TODO: better standardization across maps
     * For now: prevent blocks from being broken if they are not glass blocks
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // this solution might be temporary, not sure if other maps or other blocks are necessary to add.
        String brokenBlock = e.getBlock().getType().toString();
        if (!brokenBlock.endsWith("GLASS"))  e.setCancelled(true);

        map.brokenBlocks.put(e.getBlock().getLocation(), e.getBlock().getType());
    }

    /**
     * Prevent item frame rotation
     */
    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) e.setCancelled(true);
    }

    /**
     * Reset all transformed crates
     */
    public void resetCrates() {
        for (SupplyCrate crate : crates) {
            crate.getLocation().getBlock().setType(Material.CHEST);
        }
    }
}

class SurvivalGamesItem {
    private Material material;
    private int stack_max;
    private double weight;

    public SurvivalGamesItem() {}

    public ItemStack getItem() {
        return new ItemStack(material, (int) (Math.random() * stack_max) +1);
    }
    public double getWeight() { return weight; }
}

class SupplyCrate {
    private final Location LOCATION;
    private boolean opened;
    public SupplyCrate(Location loc, boolean opened) {
        LOCATION = loc;
        this.opened = opened;
    }

    public Location getLocation() { return LOCATION; }
    public boolean beenOpened() { return opened; }
    public void setOpened(boolean b) { opened = b; }
}

enum SurvivalGamesEvent {
    GRACE_OVER,
    SUPPLY_CRATE,
    CHEST_REFILL,
    DEATHMATCH
}