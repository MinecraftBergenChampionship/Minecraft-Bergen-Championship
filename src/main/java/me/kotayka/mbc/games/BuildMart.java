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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BuildMart extends Game {
    public AbstractBuildMartMap map = new BuildMartMap(this);
    public List<BuildMartPlayer> buildMartPlayers = new ArrayList<BuildMartPlayer>();
    // TODO this goes for a lot of things but these are preferably private
    public BuildMartTeam red = new BuildMartTeam(MBC.getInstance().red, new Location(map.getWorld(), -107, 1, 150, -90, 0));
    public BuildMartTeam yellow = new BuildMartTeam(MBC.getInstance().yellow, new Location(map.getWorld(), -68, 1, 150, -90, 0));
    public BuildMartTeam green = new BuildMartTeam(MBC.getInstance().green, new Location(map.getWorld(), -28, 1, 150, -90, 0));
    public BuildMartTeam blue = new BuildMartTeam(MBC.getInstance().blue, new Location(map.getWorld(), 11, 1, 150, -90, 0));
    public BuildMartTeam purple = new BuildMartTeam(MBC.getInstance().purple, new Location(map.getWorld(), 50, 1, 150, -90, 0));
    public BuildMartTeam pink = new BuildMartTeam(MBC.getInstance().pink, new Location(map.getWorld(), 89, 1, 150, -90, 0));
    private BuildMartTeam[] teams = {red, yellow, green, blue, purple, pink};

    //private final List<Build> GAME_ORDER = new ArrayList<>(); // Randomly ordered builds
    private final List<Build> EASY_BUILDS = new ArrayList<>();
    private final List<Build> MEDIUM_BUILDS = new ArrayList<>();
    private final List<Build> HARD_BUILDS = new ArrayList<>();
    public final World BUILD_WORLD = Bukkit.getWorld("bsabmMaps");
    public static final int NUM_PLOTS_PER_TEAM = 3;
    // Scoring; points are per player
    public static final int COMPLETION_POINTS_EASY = 4;
    public static final int COMPLETION_POINTS_MEDIUM = 8;
    public static final int COMPLETION_POINTS_HARD = 12;
    public static final int PLACEMENT_POINTS = 1;
    public static final int MOST_BUILD_POINTS = 16;

    private final List<Build> EASY_FIRST_BUILDS = new ArrayList<>();
    private final List<Build> MEDIUM_FIRST_BUILDS = new ArrayList<>();
    private final List<Build> HARD_FIRST_BUILDS = new ArrayList<>();

    private Map<Participant, Material> hotbarSelector = new HashMap<Participant, Material>();

    public BuildMart() {
        super("BuildMart", new String[] {
                "⑮ Replicate as many builds as possible in the time you have!\n\n" + 
                "⑮ The resources you'll need are below you. You will be able to fly around with an elytra!",
                "⑮ Harder builds will be worth more points, but may take longer to complete.\n\n" +
                "⑮ Strategize with your team to do as many builds as fast as you can!",
                "⑮ Some builds may require crafting or smelting, so be wary!\n" +
                "⑮ Chests are also provided to help your team organize.\n" +
                "⑮ Don't forget to use " + ChatColor.BOLD + "/checkbuild" + ChatColor.RESET + " if you're stumped!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑮ +4 points **per player** for completing an easy build\n" +
                        "⑮ +8 points **per player** for completing a medium build\n" +
                        "⑮ +12 points **per player** for completing a hard build\n" +
                        "⑮ +1 points **per player** for each team beat when completing a build\n" +
                        "⑮ Max +3 points **per player** for each build partially completed at game end\n" +
                        "⑮ +16 points **per player** on the team with the most builds completed of any team\n" 
        });


        //where the builds which are first are initialized. change these based off of event

        //halloween
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -276, 185, 2))); //graveyard
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -285, 185, 2))); //skeleton face
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -294, 185, 2))); //nether star
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -114, 185, 2))); // wither skeleton face
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -258, 185, -28))); //ghost
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -267, 185, -28))); //witch cauldron
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -276, 185, -28))); //shark
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -285, 185, -28))); //zombie
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -33, 185, -58))); //murder
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -177, 185, -58))); //pumpkin
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -186, 185, -58))); //dungeon
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -195, 185, -58))); //black cat

        //christmas
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -321, 185, 2))); //reindeer
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -330, 185, 2))); //baby penguins
        //EASY_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -339, 185, 2))); //candy canes
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -186, 185, -28))); // milk and cookies
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -177, 185, -28))); // christmas tree
        //MEDIUM_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -303, 185, -28))); // snowman
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -15, 185, -58))); // chimney
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -204, 185, -58))); // snow globe
        //HARD_FIRST_BUILDS.add(new Build(new Location(BUILD_WORLD, -213, 185, -58))); // sleigh

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

        deleteOldNames();

        // make all item frames fixed
        for (ItemFrame i : map.getWorld().getEntitiesByClass(ItemFrame.class)) {
            i.setFixed(true);
        }

        map.openPortals(false);
        loadBuilds();

        map.loadTeamPlots(teams);
        map.loadBreakAreas();

        placeBuilds();
        map.resetBlockInventories();
        map.resetBreakAreas();

        setGameState(GameState.TUTORIAL);
        setTimer(30);
    }

    @Override
    public void onRestart() {
        for (BuildMartTeam t : teams) {
            for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
                // remove names from example plots
                t.getPlots()[i][0].removeNames();
            }
        }
    }

    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "You have 60 seconds to review locations of blocks on the map!\n");
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SPECTATOR);
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
                setGameState(GameState.STARTING);
                timeRemaining = 90;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                map.openPortals(true);
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                setGameState(GameState.ACTIVE);
                timeRemaining = 720;
            } else {
                if (timeRemaining == 30) {
                    Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                    for (BuildMartPlayer bmp : buildMartPlayers) {
                        bmp.spawn();
                    }
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getPlayer().setGameMode(GameMode.ADVENTURE);
                    }
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS, 1, 1);
                    }
                }
                if (timeRemaining == 40) {
                    Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "10 seconds of review remain!\n");
                }
                startingCountdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                gameOverGraphics();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS);
                }
                for (Participant p : MBC.getInstance().getPlayers()) {
                    flightEffects(p); // sets gamemode adventure
                }

                getLogger().log(ChatColor.BOLD+"Team completion bonuses: ");
                for (BuildMartTeam t : teams) {
                    getLogger().log(t.getTeam().teamNameFormat() + ": ");
                    for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
                        // remove names from example plots
                        t.getPlots()[i][0].removeNames();

                        // get completion bonuses for replica plots
                        BuildPlot plot = t.getPlots()[i][1];
                        Build build = plot.getBuild();
                        double percent = plot.getPercentCompletion();

                        int BUILD_COMPLETION_POINTS;
                        switch (plot.getID()) {
                            case 0 -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_EASY;
                            case 1 -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_MEDIUM;
                            default -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_HARD;
                        }
                        int points = (int) (BUILD_COMPLETION_POINTS * t.getTeam().teamPlayers.size() * percent);

                        getLogger().log(String.format("%.2f%% of %s%s%s", percent, ChatColor.BOLD, build.getName(), ChatColor.RESET));

                        for (Participant p : t.getTeam().teamPlayers) {
                            p.getPlayer().sendMessage(
                                    String.format("%sYour team completed %.2f%% of %s%s%s%s and earned %.2f points!",
                                    ChatColor.GREEN, (percent*100), ChatColor.RESET, ChatColor.BOLD, build.getName(),
                                    ChatColor.GREEN, (points*MBC.getInstance().multiplier))
                            );
                            p.addCurrentScore(points / t.getTeam().teamPlayers.size());
                        }

                        
                    }

                    if (mostBuilds(t)) {
                        for (Participant p : t.getTeam().teamPlayers) {
                            p.getPlayer().sendMessage
                                (ChatColor.GREEN + "Your team finished with the most builds completed!" + MBC.scoreFormatter(MOST_BUILD_POINTS));
                            p.addCurrentScore(MOST_BUILD_POINTS);
                        }
                    }

                }
                setGameState(GameState.END_GAME);
                timeRemaining = 23;
            }
            else if (timeRemaining == 250 || timeRemaining == 500) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS);
                    p.playSound(p, Sound.MUSIC_DISC_MALL, SoundCategory.RECORDS, 1, 1);
                }
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
            //buildMartPlayer.respawn();
            p.getPlayer().teleport(map.INTRO_LOC);
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().setAllowFlight(true);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
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

    public boolean mostBuilds(BuildMartTeam b) {
        int buildsCompletedB = b.getBuildsCompleted();
        for (BuildMartTeam t : teams) {
            int buildsCompletedT = t.getBuildsCompleted();
            if (buildsCompletedB < buildsCompletedT) {
                return false;
            }
        }
        return true;
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

        // Search all plots to check if block broken was in a build plot.
        BuildMartTeam t = getTeam(Participant.getParticipant(e.getPlayer()));
        for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
            BuildPlot plot = t.getPlots()[i][1];
            if (!(plot.inBuildPlot(b.getLocation()))) continue;
            breakBlock(e.getPlayer(), b);
            if ((plot.getBuild().checkBuildBreak(plot.getMIDPOINT(), b.getLocation()))) {
                completeBuild(t, plot);
            }
            return;
        }

        if (m.toString().endsWith("FAN") || m.toString().endsWith("CORAL")) {
            // TODO: This is a map dependent, temporary fix.
            // not that we'll have more maps anyway
            if (e.getBlock().getY() < 20) {
                e.setCancelled(true);
                return;
            }

            map.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(m));
            e.setCancelled(true);
        } else if (m.toString().contains("POTTED")){
            // see above
            if (e.getBlock().getY() < 20) return;

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
                breakBlock(e.getPlayer(), b);
                area.breakBlock();
                return;
            }
        } else {
            BreakArea area = breakAreas.get(0);
            if (!(area.inArea(e.getBlock()))) {
                e.setCancelled(true);
                return;
            }
            breakBlock(e.getPlayer(), b);
            area.breakBlock();
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

        int BUILD_COMPLETION_POINTS;
        int BUILD_PLACEMENT_POINTS = PLACEMENT_POINTS;
        switch (plot.getID()) {
            case 0 -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_EASY;
            case 1 -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_MEDIUM;
            default -> BUILD_COMPLETION_POINTS = COMPLETION_POINTS_HARD;
        }

        Location fwLoc = new Location(map.getWorld(), plot.getMIDPOINT().getX(), plot.getMIDPOINT().getY()+3, plot.getMIDPOINT().getZ());
        MBC.spawnFirework(fwLoc, team.getTeam().getColor());
        int id = plot.getID();
        team.incrementBuildsCompleted(id);
        for (Participant p : team.getTeam().teamPlayers) {
            MBC.spawnFirework(p);
            p.addCurrentScoreNoDisplay(BUILD_COMPLETION_POINTS);
            p.addCurrentScoreNoDisplay(BUILD_PLACEMENT_POINTS * (MBC.getInstance().getValidTeams().size() - placement));
            createLine(3, ChatColor.GREEN.toString()+ChatColor.BOLD+"Builds Completed: " + ChatColor.RESET+team.getBuildsCompleted(), p);
        }

        ChatColor color = switch (plot.getID()) {
            case 0 -> ChatColor.GREEN;
            case 1 -> ChatColor.YELLOW;
            default -> ChatColor.RED;
        };
        String s =
                String.format("%s completed [%s%s%s%s] in %s%s%s place!", team.getTeam().teamNameFormat(), color, ChatColor.BOLD, 
                        plot.getBuild().getName().trim(), ChatColor.RESET, getColorStringFromPlacement(placement), getPlace(placement), ChatColor.RESET);

        if (MBC.getInstance().logStats()) {
            getLogger().log(s);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (team.getTeam().teamPlayers.contains(Participant.getParticipant(p))) {
                p.sendMessage(s + MBC.scoreFormatter(BUILD_COMPLETION_POINTS + BUILD_PLACEMENT_POINTS * (MBC.getInstance().getValidTeams().size() - placement)));
            }
            else {
                p.sendMessage(s);
            }
        }

        // Generate next build based on which build was just completed
        Build nextBuild = switch (id) {
            case 0 -> EASY_BUILDS.get(team.getEasyBuildsCompleted());
            case 1 -> MEDIUM_BUILDS.get(team.getMediumBuildsCompleted());
            default -> HARD_BUILDS.get(team.getHardBuildsCompleted());
        };
        BuildPlot example = team.getPlots()[id][0];
        team.getPlots()[id][1].setAir();
        plot.setBuild(nextBuild);
        example.setBuild(nextBuild);
        // setBuild will take care of placing layers/builds
    }

    /**
     * Loads all builds from bsabmMaps world into their respective ArrayLists.
     * Each list is then shuffled.
     */
    public void loadBuilds() {
        Location FIRST_EASY_BUILD = new Location(BUILD_WORLD, 11, 185, 2);
        Location FIRST_MEDIUM_BUILD = new Location(BUILD_WORLD, 11, 185, -28);
        Location FIRST_HARD_BUILD = new Location(BUILD_WORLD, 11, 185, -58);
        EASY_BUILDS.add(new Build(FIRST_EASY_BUILD));
        MEDIUM_BUILDS.add(new Build(FIRST_MEDIUM_BUILD));
        HARD_BUILDS.add(new Build(FIRST_HARD_BUILD));

        Block easyBlock = BUILD_WORLD.getBlockAt(new Location(BUILD_WORLD, -6, 185, 2));
        Block mediumBlock = BUILD_WORLD.getBlockAt(new Location(BUILD_WORLD, -6, 185, -28));
        Block hardBlock = BUILD_WORLD.getBlockAt(new Location(BUILD_WORLD, -6, 185, -58));

        // load easy builds
        while (easyBlock.getType() == Material.DIAMOND_BLOCK) {
            EASY_BUILDS.add(new Build(easyBlock.getLocation()));
            easyBlock = BUILD_WORLD.getBlockAt(easyBlock.getX() - 9, easyBlock.getY(), easyBlock.getZ());
        }

        // load medium builds
        while (mediumBlock.getType() == Material.DIAMOND_BLOCK) {
            MEDIUM_BUILDS.add(new Build(mediumBlock.getLocation()));
            mediumBlock = BUILD_WORLD.getBlockAt(mediumBlock.getX() - 9, mediumBlock.getY(), mediumBlock.getZ());
        }

        // load hard builds
        while (hardBlock.getType() == Material.DIAMOND_BLOCK) {
            HARD_BUILDS.add(new Build(hardBlock.getLocation()));
            hardBlock = BUILD_WORLD.getBlockAt(hardBlock.getX() - 9, hardBlock.getY(), hardBlock.getZ());
        }

        Collections.shuffle(EASY_BUILDS);
        Collections.shuffle(MEDIUM_BUILDS);
        Collections.shuffle(HARD_BUILDS);

        Collections.shuffle(EASY_FIRST_BUILDS);
        Collections.shuffle(MEDIUM_FIRST_BUILDS);
        Collections.shuffle(HARD_FIRST_BUILDS);

        //load first easy builds at the start
        for (Build b : EASY_FIRST_BUILDS) {
            EASY_BUILDS.remove(b);
            EASY_BUILDS.add(0, b);
        }

        //load first medium builds at the start
        for (Build b : MEDIUM_FIRST_BUILDS) {
            MEDIUM_BUILDS.remove(b);
            MEDIUM_BUILDS.add(0, b);
        }

        //load first hard builds at the start
        for (Build b : HARD_FIRST_BUILDS) {
            HARD_BUILDS.remove(b);
            HARD_BUILDS.add(0, b);
        }

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
        if (getState().equals(GameState.STARTING) && e.getPlayer().getGameMode().equals(GameMode.SPECTATOR) && timeRemaining > 25) {
            if (e.getPlayer().getX() > 120 || e.getPlayer().getX() < -120 || e.getPlayer().getZ() > 120 || e.getPlayer().getZ() < -120) e.getPlayer().teleport(new Location(map.getWorld(), 0, 100, 0));
        }
        Participant p = Participant.getParticipant(e.getPlayer());
        if (hotbarSelector.containsKey(p)) {
            if (e.getPlayer().getTargetBlock(null, 5).getType() != hotbarSelector.get(p)) {
                hotbarSelector.put(p, e.getPlayer().getTargetBlock(null, 5).getType());
                if (e.getPlayer().getTargetBlock(null, 5).getType() != Material.AIR) {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+ createActionBarString(String.valueOf(hotbarSelector.get(p)))));
                }
                else {
                    e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                }
            }
        }
        else {
            hotbarSelector.put(p, e.getPlayer().getTargetBlock(null, 5).getType());
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN+ "" + ChatColor.BOLD+ createActionBarString(String.valueOf(hotbarSelector.get(p)))));
        }
    }

    // Check build when stripping logs
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if ((e.getClickedBlock().getType().toString().endsWith("LOG") && e.getPlayer().getInventory().getItemInMainHand().getType().toString().endsWith("AXE"))
            || e.getClickedBlock().getType().toString().startsWith("POTTED")) {
            Block b = e.getClickedBlock();
            BuildMartTeam t = getTeam(Participant.getParticipant(e.getPlayer()));
            for (int i = 0; i < NUM_PLOTS_PER_TEAM; i++) {
                BuildPlot plot = t.getPlots()[i][1];
                if (!(plot.inBuildPlot(b.getLocation()))) continue;
                if ((plot.getBuild().checkBuild(plot.getMIDPOINT()))) {
                    completeBuild(t, plot);
                }
                return;
            }
        }
    }

    @EventHandler
    public void EnterPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().getLocation().getWorld().equals(map.getWorld())) return;

        ItemStack i = e.getItemDrop().getItemStack();
        if (i.getType().equals(Material.DIAMOND_PICKAXE) || i.getType().equals(Material.DIAMOND_SHOVEL) || i.getType().equals(Material.DIAMOND_AXE)
         || i.getType().equals(Material.DIAMOND_HOE) || i.getType().equals(Material.ELYTRA)) {
            e.setCancelled(true);
        }
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

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        BuildMartPlayer p = getBuildMartPlayer(e.getPlayer());
        if (p == null) return; // new login; doesn't matter
        p.setPlayer(e.getPlayer());
    }

    public void deleteOldNames() {
        for (ArmorStand a : map.getWorld().getEntitiesByClass(ArmorStand.class)) {
            a.remove();
        }
    }

    public List<Build> getOrder(int id) {
        if (id < 0 || id > 2) return null;

        return switch (id) {
            case 0 -> EASY_BUILDS;
            case 1 -> MEDIUM_BUILDS;
            default -> HARD_BUILDS;
        };
    }

}