package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.bsabmMaps.*;
import me.kotayka.mbc.gamePlayers.BuildMartPlayer;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.util.*;

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

    private Map<Participant, Material> hotbarSelector = new HashMap<Participant, Material>();

    public BuildMart() {
        super("BuildMart");
    }

    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET + ChatColor.RESET.toString(), p);
        createLineAll(3, ChatColor.GREEN.toString()+ChatColor.BOLD+"Builds Completed: " + ChatColor.RESET+"0");

        updateInGameTeamScoreboard();
        //displayTeamCurrentScore(p.getTeam());
        //updatePlayerCurrentScoreDisplay(p);
    }

    public void start() {
        super.start();

        map.openPortals(false);
        loadBuilds();

        map.loadTeamPlots(teams);
        map.loadBreakAreas();

        placeBuilds();
        map.resetBlockInventories();
        map.resetBreakAreas();

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
                //timeRemaining = 720; // DEBUG
                timeRemaining = 30;
            } else {
                startingCountdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                gameOverGraphics();
                for (Participant p : MBC.getInstance().getPlayers()) {
                    flightEffects(p); // sets gamemode adventure
                }
                for (BuildMartTeam t : teams) {
                    for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
                        // remove names from example plots
                        t.getPlots()[i][0].removeNames();

                        // get completion bonuses for replica plots
                        BuildPlot plot = t.getPlots()[i][1];
                        Build build = plot.getBuild();
                        double percent = plot.getPercentCompletion();
                        int points = (int) (BUILD_COMPLETION_POINTS * t.getTeam().teamPlayers.size() * percent);
                        for (Participant p : t.getTeam().teamPlayers) {
                            p.getPlayer().sendMessage(
                                    String.format("%sYour team completed %.2f%% of %s%s%s%s and earned %.2f points!",
                                    ChatColor.GREEN, (percent*100), ChatColor.RESET, ChatColor.BOLD, build.getName(),
                                    ChatColor.GREEN, (points*MBC.getInstance().multiplier))
                            );
                            p.addCurrentScore(points / t.getTeam().teamPlayers.size());
                        }
                    }
                }
                setGameState(GameState.END_GAME);
                timeRemaining = 23;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            teamGameEndEvents();
        }
    }

    public void loadPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            BuildMartPlayer buildMartPlayer = new BuildMartPlayer(p, this);
            buildMartPlayers.add(buildMartPlayer);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            buildMartPlayer.respawn();
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().setAllowFlight(true);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
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
        Material m = e.getBlock().getType();

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
            if ((plot.getBuild().checkBuildBreak(plot.getMIDPOINT(), b.getLocation()))) {
                completeBuild(t, plot);
            }
            breakBlock(e.getPlayer(), b);
            return;
        }

        if (m.toString().endsWith("FAN") || m.toString().endsWith("CORAL")) {
            map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(m));
            e.setCancelled(true);
        }
        if (m.toString().contains("POTTED")){
            for (ItemStack i : e.getBlock().getDrops()) {
                map.getWorld().dropItemNaturally(b.getLocation(), i);
            }
        }

        List<BreakArea> breakAreas = map.BreakAreas().get(e.getBlock().getType());
        if (breakAreas == null) { e.setCancelled(true); return; }

        if (breakAreas.size() > 1) {
            for (BreakArea area : breakAreas) {
                // assuming no break areas overlap, which would be pointless
                if (!(area.inArea(b.getLocation()))) continue;
                if (area.lastBlock()) {
                    area.Replace();
                }
                breakBlock(e.getPlayer(), b);
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
            breakBlock(e.getPlayer(), b);
            return;
        }
        e.setCancelled(true);
    }

    private void breakBlock(Player p, Block b) {
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getEnchantments().size() > 0 && tool.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
            map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(b.getType()));
            return;
        }

        Collection<ItemStack> drops = b.getDrops(tool);
        for (ItemStack i : drops) {
            map.getWorld().dropItemNaturally(b.getLocation(), i);
        }
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
        int placement = 1;
        for (BuildMartTeam t : teams) {
            if (!team.getTeam().getTeamName().equals(t.getTeam().getTeamName()) && t.getCompletions().get(plot.getBuild()) != null) {
                placement++;
            }
        }
        team.getCompletions().put(plot.getBuild(), placement);

        Location fwLoc = new Location(map.getWorld(), plot.getMIDPOINT().getX(), plot.getMIDPOINT().getY()+3, plot.getMIDPOINT().getZ());
        MBC.spawnFirework(fwLoc, team.getTeam().getColor());
        team.incrementBuildsCompleted();
        for (Participant p : team.getTeam().teamPlayers) {
            MBC.spawnFirework(p);
            p.addCurrentScoreNoDisplay(BUILD_COMPLETION_POINTS);
            p.addCurrentScoreNoDisplay(BUILD_PLACEMENT_POINTS * (MBC.getInstance().getValidTeams().size() - placement));
            createLine(3, ChatColor.GREEN.toString()+ChatColor.BOLD+"Builds Completed: " + ChatColor.RESET+team.getBuildsCompleted(), p);
        }

        Bukkit.broadcastMessage(
                String.format("%s completed [%s%s%s] in %s%s%s place!", team.getTeam().teamNameFormat(), ChatColor.BOLD, plot.getBuild().getName(),
                               ChatColor.RESET, getColorStringFromPlacement(placement), getPlace(placement), ChatColor.RESET)
        );

        // Next Build
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

        //Bukkit.broadcastMessage("[Debug] GAME_ORDER.size() == " + GAME_ORDER.size());
    }

    public StringBuilder createActionBarString (String material) {
        String[] strs = material.split("_");
        StringBuilder finalString = new StringBuilder();
        for (String str : strs) {
            str = str.toLowerCase();
            finalString.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
            finalString.append(" ");
        }
        return finalString;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!(e.getPlayer().getLocation().getWorld().equals(map.getWorld()))) return;
        map.onMove(e);
        Participant p = Participant.getParticipant(e.getPlayer());
        if (hotbarSelector.containsKey(p)) {
            if (e.getPlayer().getTargetBlock(null, 5).getType() != hotbarSelector.get(p)) {
                hotbarSelector.put(p, e.getPlayer().getTargetBlock(null, 5).getType());
                if (e.getPlayer().getTargetBlock(null, 5).getType() != Material.AIR) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(p))))));
                }
                else {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                }
            }
        }
        else {
            hotbarSelector.put(p, e.getPlayer().getTargetBlock(null, 5).getType());
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+String.valueOf(createActionBarString(String.valueOf(hotbarSelector.get(p))))));
        }
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
    public static ItemStack[] getItemsForBuildMart() {
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
            if (p.getUniqueId().equals(x.getPlayer().getUniqueId())) {
                return x;
            }
        }
        return null;
    }

    public List<Build> getOrder() {
        return GAME_ORDER;
    }

}