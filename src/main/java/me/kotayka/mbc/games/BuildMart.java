package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.bsabmMaps.*;
import me.kotayka.mbc.gamePlayers.BuildMartPlayer;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildMart extends Game {
    public AbstractBuildMartMap map = new BuildMartMap(this);
    public List<BuildMartPlayer> buildMartPlayers = new ArrayList<BuildMartPlayer>();
    public BuildMartTeam red = new BuildMartTeam(MBC.getInstance().red, new Location(map.getWorld(), -107, 1, 150, -90, 0));
    public BuildMartTeam yellow = new BuildMartTeam(MBC.getInstance().yellow, new Location(map.getWorld(), -68, 1, 150, -90, 0));
    public BuildMartTeam green = new BuildMartTeam(MBC.getInstance().green, new Location(map.getWorld(), -28, 1, 150, -90, 0));
    public BuildMartTeam blue = new BuildMartTeam(MBC.getInstance().blue, new Location(map.getWorld(), 11, 1, 150, -90, 0));
    public BuildMartTeam purple = new BuildMartTeam(MBC.getInstance().purple, new Location(map.getWorld(), 50, 1, 150, -90, 0));
    public BuildMartTeam pink = new BuildMartTeam(MBC.getInstance().pink, new Location(map.getWorld(), 89, 1, 150, -90, 0));
    public BuildMartTeam[] teams = {red, yellow, green, blue, purple, pink};
    private final List<Build> GAME_ORDER = new ArrayList<>(); // Randomly ordered builds
    private final Location FIRST_BUILD_MIDDLE = new Location(Bukkit.getWorld("bsabmMaps"), -6, 185, 2);
    public final World BUILD_WORLD = Bukkit.getWorld("bsabmMaps");
    public static final int NUM_PLOTS_PER_TEAM = 3;
    public static final int BUILD_COMPLETION_POINTS = 3; // points are per player
    public static final int BUILD_PLACEMENT_POINTS = 3; // points are per player

    public BuildMart() {
        super("BuildMart");
    }

    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET + ChatColor.RESET.toString(), p);
        createLine(3, ChatColor.GREEN.toString()+ChatColor.BOLD+"Builds Completed: " + ChatColor.RESET+"0");

        updateInGameTeamScoreboard();
        //displayTeamCurrentScore(p.getTeam());
        //updatePlayerCurrentScoreDisplay(p);
    }

    public void start() {
        super.start();

        map.openPortals(false);
        map.resetBlockInventories();
        loadBuilds();

        map.loadTeamPlots(teams);
        map.loadBreakAreas();

        placeBuilds();

        setGameState(GameState.STARTING);
        setTimer(30);
    }

    public void events() {
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                map.openPortals(true);
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                setGameState(GameState.ACTIVE);
                timeRemaining = 720;
            } else {
                startingCountdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                gameOverGraphics();
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.ADVENTURE);
                    flightEffects(p);
                }
                setGameState(GameState.END_GAME);
                timeRemaining = 37;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            teamGameEndEvents();
        }
    }

    public void loadPlayers() {
        ItemStack[] items = getItems();
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().setAllowFlight(true);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            BuildMartPlayer buildMartPlayer = new BuildMartPlayer(p, this);
            buildMartPlayers.add(buildMartPlayer);
            buildMartPlayer.respawn();
            for (ItemStack i : items) {
                if (i.getType().equals(Material.ELYTRA)) {
                    p.getPlayer().getInventory().setChestplate(i);
                } else {
                    p.getPlayer().getInventory().addItem(i);
                }
            }
        }
    }

    public void placeBuilds() {
        for (BuildMartTeam t : teams) {
            for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
                t.getPlots()[i][0].placeBuild();
                t.getPlots()[i][1].placeFirstLayer();
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        if (!(e.getBlock().getLocation().getWorld().equals(map.getWorld()))) return;
        if (e.getBlock().getLocation().getBlockY() == map.FIRST_LAYER_Y) {
            // note: this is assuming the plots will never be on the same level as ANY break area
            e.setCancelled(true);
            return;
        }

        Block b = e.getBlock();

        BuildMartTeam t = getTeam(Participant.getParticipant(e.getPlayer()));
        for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
            BuildPlot plot = t.getPlots()[i][1];
            if (!(plot.inBuildPlot(b.getLocation()))) continue;
            if ((plot.getBuild().checkBuild(plot.getMIDPOINT()))) {
                completeBuild(t, plot);
            }
            map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(b.getType()));
            return;
        }

        List<BreakArea> breakAreas = map.BreakAreas().get(e.getBlock().getType());
        if (breakAreas == null) {
            Bukkit.broadcastMessage("No break areas!"); e.setCancelled(true); return; }

        if (breakAreas.size() > 1) {
            for (BreakArea area : breakAreas) {
                // assuming no break areas overlap, which would be pointless
                if (!(area.inArea(b.getLocation()))) continue;
                if (area.lastBlock()) {
                    area.Replace();
                }
                map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(b.getType()));
                return;
            }
        } else {
            BreakArea area = breakAreas.get(0);
            if (!(area.inArea(e.getBlock()))) {
                e.setCancelled(true);
                return;
            }
            if (area.lastBlock()) {
                area.Replace();
            }
            map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(b.getType()));
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent e) {
        if (!(e.getBlock().getLocation().getWorld().equals(map.getWorld()))) return;

        BuildMartTeam t = getTeam(Participant.getParticipant(e.getPlayer()));
        for (int i = 0; i < 3; i++) {
            BuildPlot plot = t.getPlots()[i][1];
            if (!(plot.inBuildPlot(e.getBlock().getLocation()))) continue;
            if (!(plot.getBuild().checkBuild(plot.getMIDPOINT()))) return;

            completeBuild(t, plot);
            return;
        }
        e.setCancelled(true);
    }

    public void completeBuild(BuildMartTeam team, BuildPlot plot) {
        // Firework & scoring
        Location fwLoc = new Location(map.getWorld(), plot.getMIDPOINT().getX(), plot.getMIDPOINT().getY()+3, plot.getMIDPOINT().getZ());
        MBC.spawnFirework(fwLoc, team.getTeam().getColor());
        for (Participant p : team.getTeam().teamPlayers) {
            MBC.spawnFirework(p);
            p.addCurrentScoreNoDisplay(BUILD_COMPLETION_POINTS);
        }

        // Next Build
        team.incrementBuildsCompleted();
        int id = plot.getID();
        Build nextBuild = GAME_ORDER.get((NUM_PLOTS_PER_TEAM+team.getBuildsCompleted()) % GAME_ORDER.size());
        BuildPlot example = team.getPlots()[id][0];
        team.getPlots()[id][1].setAir();
        plot.setBuild(nextBuild);
        example.setBuild(nextBuild);
        // setBuild will take care of placing layers/builds
    }

    /**
     * Load all possible builds
     */
    public void loadBuilds() {
        Block b = BUILD_WORLD.getBlockAt(FIRST_BUILD_MIDDLE);

        Build build = new Build(new Location(BUILD_WORLD, 11, 185, 2));
        GAME_ORDER.add(build);

        while (b.getType() == Material.DIAMOND_BLOCK) {
            GAME_ORDER.add(new Build(b.getLocation()));

            b = BUILD_WORLD.getBlockAt(b.getX() - 9, b.getY(), b.getZ());
        }
        Collections.shuffle(GAME_ORDER);

        Bukkit.broadcastMessage("[Debug] GAME_ORDER.size() == " + GAME_ORDER.size());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        map.onMove(e);
    }

    @EventHandler
    public void EnterPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void CoralFade(BlockFadeEvent e) {
        if (e.getBlock().getType().toString().endsWith("FAN") || e.getBlock().getType().toString().endsWith("CORAL"))
            e.setCancelled(true);
    }

    // Get items for players
    private ItemStack[] getItems() {
        ItemStack pick = new ItemStack(Material.DIAMOND_PICKAXE);

        ItemStack silk = new ItemStack(Material.DIAMOND_PICKAXE);
        silk.addEnchantment(Enchantment.SILK_TOUCH, 1);

        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);

        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);

        ItemStack hoe = new ItemStack(Material.DIAMOND_HOE);

        ItemStack elytra = new ItemStack(Material.ELYTRA);

        ItemStack[] items = {pick, silk, axe, shovel, hoe, elytra};

        for (ItemStack i : items) {
            ItemMeta meta = i.getItemMeta();
            meta.setUnbreakable(true);
            i.setItemMeta(meta);
        }

        return items;
    }

    public BuildMartTeam getTeam(Participant p) {
        return switch (p.getTeam().getChatColor()) {
            case RED -> red;
            case YELLOW -> yellow;
            case GREEN -> green;
            case BLUE -> blue;
            case DARK_PURPLE -> purple;
            case LIGHT_PURPLE -> pink;
            default -> null;
        };
    }

    public BuildMartPlayer getBuildMartPlayer(Player p) {
        for (BuildMartPlayer x : buildMartPlayers) {
            if (p.getName().equals(x.getPlayer().getName())) {
                return x;
            }
        }
        return null;
    }

    public List<Build> getOrder() {
        return GAME_ORDER;
    }

}