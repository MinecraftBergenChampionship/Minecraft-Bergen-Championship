package me.kotayka.mbc.games.acerace;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.QueakiesGoldMine;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.HashSet;

public class AceRace extends Game {
    public AceRaceMap map = new QueakiesGoldMine();
    public static World world = Bukkit.getWorld("AceRace");
    public Map<UUID, AceRacePlayer> aceRacePlayerMap = new HashMap<>();
    public short[] finishedPlayersByLap = {0, 0, 0};
    public AceRacePlayer[][] lapOne;
    public AceRacePlayer[][] lapTwo;
    public AceRacePlayer[][] lapThree;
    public ArrayList<AceRacePlayer> currentPlacements = new ArrayList<>();
    public long startingTime;

    private final List<Minecart> activeMinecarts = new ArrayList<>();
    private final Map<UUID, Long> minecartSpawnTimes = new HashMap<>();
    private BukkitRunnable minecartSpawnTask;
    private BukkitRunnable minecartTickTask;

    private static final double MINECART_SPEED = 0.4;
    private static final long GRACE_PERIOD_MS = 500;
    private static final List<Location> TRACK_SPAWN_LOCATIONS = new ArrayList<>();
    private final Map<Location, Long> spawnLocationCooldowns = new HashMap<>();

    public SortedMap<Long, List<String>> fastestLaps = new TreeMap<>();

    public static final int FINISH_RACE_POINTS_18 = 12;
    public static final int FINISH_RACE_POINTS_24 = 18;
    public static final int FINISH_RACE_POINTS = FINISH_RACE_POINTS_18;

    public static final int PLACEMENT_LAP_POINTS_18 = 1;
    public static final int PLACEMENT_LAP_POINTS_24 = 1;
    public static final int PLACEMENT_LAP_POINTS = PLACEMENT_LAP_POINTS_18;

    public static final int LAP_COMPLETION_POINTS_18 = 1;
    public static final int LAP_COMPLETION_POINTS_24 = 2;
    public static final int LAP_COMPLETION_POINTS = LAP_COMPLETION_POINTS_18;

    public static final int PLACEMENT_FINAL_LAP_POINTS_18 = 3;
    public static final int PLACEMENT_FINAL_LAP_POINTS_24 = 3;
    public static final int PLACEMENT_FINAL_LAP_POINTS = PLACEMENT_FINAL_LAP_POINTS_18;

    public static final int[] PLACEMENT_BONUSES = {20, 15, 15, 10, 10, 5, 5, 5, 5, 5};
    public static final int TUTORIAL_TIME = 240;

    private boolean finishedIntro = false;

    public AceRace() {
        super("Ace Race",
                new String[] {
                        "⑭ Complete the race as fast as you can!\n\n" +
                                "⑭ The " + ChatColor.BOLD + "practice time" + ChatColor.RESET + " has started.",
                        "⑭ Red jump pads will boost you, orange jump pads will launch you higher, and green pads will give you a jump boost.\n\n" +
                                "⑭ Orange tiles with arrows will give you a speed boost.",
                        "⑭ Hold right click with a trident to get a boost in water.\n" +
                                "⑭ Soar with an elytra by pressing space midair!\n" +
                                "⑭ Checkpoints will be given across the map.",
                        ChatColor.BOLD + "Scoring:\n" + ChatColor.RESET +
                                "⑭ +1 point for completing a lap\n" +
                                "⑭ +1 point for every player beaten on a lap\n" +
                                "⑭ +12 points for finishing the course\n" +
                                "⑭ +3 points for every player beaten on the final lap\n" +
                                "⑭ Top 8 Bonuses- 1st:+20, 2nd,3rd:+15, 4th,5th:+10, 6th-10th:+5"
                });

        World aceRaceWorld = Bukkit.getWorld("acerace");
        stopMinecartSystem();
        if (aceRaceWorld != null) {
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1074.30, 108.00, 910.68));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1076.30, 107.00, 912.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1079.30, 106.00, 915.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1080.30, 106.00, 918.70));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1082.30, 106.00, 920.68));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1081.30, 106.00, 922.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1081.30, 105.00, 924.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1084.30, 105.00, 926.70));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1085.62, 106.00, 928.70));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1090.30, 104.00, 932.48));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1089.30, 103.00, 934.70));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1094.30, 103.00, 939.60));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1097.30, 102.00, 946.54));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1099.30, 101.00, 948.56));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1098.30, 102.00, 952.36));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1116.70, 101.00, 946.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1114.70, 101.00, 941.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1106.70, 103.00, 931.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1105.70, 103.00, 930.10));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1104.70, 104.00, 928.55));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1102.70, 104.00, 925.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1098.70, 104.00, 922.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1095.70, 105.00, 918.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1095.70, 105.00, 916.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1090.70, 106.00, 912.30));
            TRACK_SPAWN_LOCATIONS.add(new Location(aceRaceWorld, -1087.70, 106.00, 910.24));
        }
    }

    private void startMinecartSystem() {

        minecartSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!getState().equals(GameState.ACTIVE)) return;
                if (TRACK_SPAWN_LOCATIONS.isEmpty()) return;

                Location spawnLoc = TRACK_SPAWN_LOCATIONS
                        .get(new Random().nextInt(TRACK_SPAWN_LOCATIONS.size()));

                long now = System.currentTimeMillis();

                Long lastSpawn = spawnLocationCooldowns.get(spawnLoc);
                if (lastSpawn != null && (now - lastSpawn) < 500) {
                    return;
                }

                spawnLocationCooldowns.put(spawnLoc, now);

                Minecart cart = spawnLoc.getWorld().spawn(spawnLoc, Minecart.class);

                cart.setMaxSpeed(MINECART_SPEED);
                cart.setSlowWhenEmpty(false);
                cart.setVelocity(spawnLoc.getDirection().multiply(MINECART_SPEED));

                activeMinecarts.add(cart);
                minecartSpawnTimes.put(cart.getUniqueId(), now);
            }
        };

        minecartSpawnTask.runTaskTimer(MBC.getInstance().getPlugin(), 0L, 1L);

        minecartTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!getState().equals(GameState.ACTIVE)) return;

                Set<Minecart> toRemove = new HashSet<>();

                for (Minecart cart : activeMinecarts) {

                    if (toRemove.contains(cart)) continue;

                    if (cart.isDead() || !cart.isValid()) {
                        toRemove.add(cart);
                        minecartSpawnTimes.remove(cart.getUniqueId());
                        continue;
                    }

                    Location loc = cart.getLocation();
                    Block currentBlock = loc.getBlock();

                    if (currentBlock.getType() == Material.BLACK_CONCRETE) {
                        triggerMinecartExplosion(cart, false);
                        cart.remove();
                        toRemove.add(cart);
                        minecartSpawnTimes.remove(cart.getUniqueId());
                        continue;
                    }

                    long spawnTime = minecartSpawnTimes.getOrDefault(cart.getUniqueId(), System.currentTimeMillis());
                    boolean inGracePeriod = (System.currentTimeMillis() - spawnTime) < GRACE_PERIOD_MS;

                    if (inGracePeriod) continue;

                    for (Entity nearby : cart.getNearbyEntities(2, 2, 2)) {

                        if (nearby instanceof Player) {
                            triggerMinecartExplosion(cart, true);
                            cart.remove();
                            toRemove.add(cart);
                            minecartSpawnTimes.remove(cart.getUniqueId());
                            break;
                        }

                        if (nearby instanceof Minecart && nearby != cart) {
                            triggerMinecartExplosion(cart, true);
                            ((Minecart) nearby).remove();

                            toRemove.add((Minecart) nearby);
                            cart.remove();

                            minecartSpawnTimes.remove(nearby.getUniqueId());
                            minecartSpawnTimes.remove(cart.getUniqueId());
                            break;
                        }
                    }
                }

                activeMinecarts.removeAll(toRemove);
            }
        };

        minecartTickTask.runTaskTimer(MBC.getInstance().getPlugin(), 0L, 1L);
    }

    private void triggerMinecartExplosion(Minecart cart, boolean isCollision) {
        Location loc = cart.getLocation();
        World w = loc.getWorld();
        if (w == null) return;

        w.spawnParticle(Particle.EXPLOSION, loc, 3, 0.3, 0.3, 0.3, 0);
        w.spawnParticle(Particle.FLAME, loc, 20, 0.5, 0.5, 0.5, 0.05);
        w.spawnParticle(Particle.SMOKE, loc, 15, 0.4, 0.4, 0.4, 0.02);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);

        if (isCollision) {
            for (Participant p : MBC.getInstance().getPlayers()) {
                Player player = p.getPlayer();
                if (player.getLocation().distance(loc) <= 1.5) {
                    AceRacePlayer acePlayer = getGamePlayer(player);
                    if (acePlayer != null) {
                        int checkpoint = acePlayer.checkpoint;
                        player.teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength - 1 : checkpoint - 1));
                        player.removePotionEffect(PotionEffectType.SPEED);
                        player.setFireTicks(0);
                    }
                }
            }
        }
    }

    private void stopMinecartSystem() {
        if (minecartSpawnTask != null) {
            minecartSpawnTask.cancel();
            minecartSpawnTask = null;
        }
        if (minecartTickTask != null) {
            minecartTickTask.cancel();
            minecartTickTask = null;
        }

        // Remove tracked minecarts
        for (Minecart cart : activeMinecarts) {
            if (cart != null && cart.isValid()) cart.remove();
        }
        activeMinecarts.clear();
        minecartSpawnTimes.clear();

        if (world != null) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Minecart) {
                    entity.remove();
                }
            }
        }
    }

    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(7, " ", p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
        updatePlayerCurrentScoreDisplay(p);
    }

    public void start() {
        super.start();
        setGameState(GameState.TUTORIAL);

        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.getPlayer().teleport(map.getIntroLocation());
            p.reset();
        }
        lapOne = new AceRacePlayer[map.checkpoints.size()][aceRacePlayerMap.size()];
        lapTwo = new AceRacePlayer[map.checkpoints.size()][aceRacePlayerMap.size()];
        lapThree = new AceRacePlayer[map.checkpoints.size()][aceRacePlayerMap.size()];

        setTimer(TUTORIAL_TIME);
    }

    @Override
    public void onRestart() {
        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.reset();
        }
        stopMinecartSystem();
    }

    @EventHandler
    public void onTntExplode(EntityExplodeEvent event) {
        Location center = event.getLocation();
        double radius = 5.0;

        List<Player> players = center.getWorld().getPlayers();

        for (Player p : players) {
            if (p.getLocation().distance(center) <= radius) {
                AceRacePlayer acePlayer = getGamePlayer(p);
                if (acePlayer != null) {
                    int checkpoint = acePlayer.checkpoint;
                    p.teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength - 1 : checkpoint - 1));
                    p.removePotionEffect(PotionEffectType.SPEED);
                    p.setFireTicks(0);
                }
            }
        }

        event.blockList().clear();
    }

    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + "Practice Over!");
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    p.getPlayer().setVelocity(new Vector(0,0,0));
                    p.getPlayer().removePotionEffect(PotionEffectType.SPEED);
                    p.getPlayer().sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Practice Over!", "", 0, 60, 20);
                    p.getPlayer().getInventory().remove(Material.RED_DYE);
                    p.getPlayer().getInventory().remove(Material.YELLOW_DYE);
                    p.getPlayer().getInventory().remove(Material.LIME_DYE);
                    createLine(6, ChatColor.GREEN.toString()+ChatColor.BOLD+"Lap: " + ChatColor.RESET+"1/3", p.getParticipant());
                }
                setGameState(GameState.END_ROUND);
                timeRemaining = 5;
            } else if (!finishedIntro && timeRemaining > 0 && timeRemaining % 7 == 0 && timeRemaining != TUTORIAL_TIME-30) {
                Introduction();
            } else if (!finishedIntro && timeRemaining == TUTORIAL_TIME-30){
                MBC.getInstance().sendMutedMessages();
                finishedIntro = true;
            } else if (timeRemaining == 60) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "One minute left of practice!");
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining <= 0) {
                map.setBarriers(true);
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    p.getPlayer().teleport(map.getIntroLocation());
                    p.checkpoint = 0;
                }
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
                mapCreator(map.mapName, map.creatorName);
                if (timeRemaining == 11) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, "sfx.ace_race_starting", SoundCategory.RECORDS, 0.75f, 1);
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                map.setBarriers(false);
                timeRemaining = 720;
                startingTime = System.currentTimeMillis();
                startMinecartSystem();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, "sfx.started_ring", SoundCategory.RECORDS, 0.75f, 1);
                    p.playSound(p, "igm.ace_race", SoundCategory.RECORDS, 1, 1);
                }
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "30 seconds remaining!");
            } else if (timeRemaining <= 0) {
                stopMinecartSystem();
                gameOverGraphics();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound("igm.ace_race", SoundCategory.RECORDS);
                }
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    if (!(p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))) {
                        flightEffects(p.getParticipant());
                        p.getPlayer().sendMessage(ChatColor.RED + "Better luck next time!");
                    }
                }
                setGameState(GameState.END_GAME);
                timeRemaining = 42;
            }
            else if (timeRemaining % 157 == 92) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound("igm.ace_race", SoundCategory.RECORDS);
                    p.playSound(p, "igm.ace_race", SoundCategory.RECORDS, 1, 1);
                }
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 41) {
                for (Participant p : MBC.getInstance().getPlayers()) {
                    MBC.getInstance().showPlayers(p);
                }
            }
            if (timeRemaining == 40) {
                Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Fastest Laps: ");
            } else if (timeRemaining == 36) {
                topLaps();
            } else if (timeRemaining < 34){
                gameEndEvents();
            }
        }
    }

    public void loadPlayers() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta itemMeta = trident.getItemMeta();
        itemMeta.setUnbreakable(true);
        trident.setItemMeta(itemMeta);
        trident.addEnchantment(Enchantment.RIPTIDE, 1);
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);
        leatherBoots.addEnchantment(Enchantment.DEPTH_STRIDER, 1);

        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta redMeta = redDye.getItemMeta();
        redMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.RED + "Return To Start");
        redDye.setItemMeta(redMeta);
        ItemStack yellowDye = new ItemStack(Material.YELLOW_DYE);
        ItemMeta yellowMeta = redDye.getItemMeta();
        yellowMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.YELLOW + "Last Checkpoint");
        yellowDye.setItemMeta(yellowMeta);
        ItemStack limeDye = new ItemStack(Material.LIME_DYE);
        ItemMeta limeMeta = limeDye.getItemMeta();
        limeMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.GREEN + "Next Checkpoint");
        limeDye.setItemMeta(limeMeta);

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            p.getInventory().addItem(redDye);
            p.getInventory().addItem(yellowDye);
            p.getInventory().addItem(limeDye);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 1, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 1, false, false));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

            aceRacePlayerMap.put(p.getPlayer().getUniqueId(), new AceRacePlayer(p, this));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR && map.checkDeath(e.getPlayer().getLocation())) {
            e.getPlayer().teleport(map.respawns.getFirst());
            return;
        }

        if (e.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            Player mover = e.getPlayer();
            for (Participant p : MBC.getInstance().getPlayers()) {
                Player player = p.getPlayer();
                AceRacePlayer checker = getGamePlayer(player);
                if (mover != player && player.getGameMode() != GameMode.SPECTATOR && (getState().equals(GameState.TUTORIAL) || getState().equals(GameState.ACTIVE))) {
                    double diffX = player.getX() - mover.getX();
                    double diffY = player.getY() - mover.getY();
                    double diffZ = player.getZ() - mover.getZ();
                    if (Math.sqrt(diffX*diffX + diffY*diffY + diffZ*diffZ) <= 5) {
                    }
                    else if(checker.checkHiddenPlayer(mover) && Math.sqrt(diffX*diffX + diffY*diffY + diffZ*diffZ) <= 10) {
                    }
                    else {
                    }
                }
            }
        }

        Player p = e.getPlayer();
        AceRacePlayer player = getGamePlayer(p);
        if (player == null) return;

        if (map.checkDeath(p.getLocation())) {
            int checkpoint = player.checkpoint;
            p.teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength-1 : checkpoint-1));
            p.removePotionEffect(PotionEffectType.SPEED);
            p.setFireTicks(0);
        }

        // Attempt to set a checkpoint when a player lands on a special block.
        Material landingBlock = e.getTo().getBlock().getRelative(BlockFace.DOWN).getType();
        if (landingBlock == MBC.MEGA_BOOST_PAD || landingBlock == MBC.JUMP_PAD || landingBlock == MBC.BOOST_PAD) {
            player.setCheckpoint(false);
            return;
        }

        // Attempt to set a checkpoint whenever a carpet block is reached.
        // If powerups are enabled, provide a powerup on black carpet.
        Material block = e.getTo().getBlock().getType();
        if (block.toString().toLowerCase().contains("carpet")) {
            // For simplicity, provide a powerup whenever a black carpet checkpoint is reached.
            // TODO: player.setCheckpoint() should return a boolean indicating whether it was successful, then a powerup should be given here.
            //       Powerups should also not be distributed on Lap completion.
            player.setCheckpoint(map.powerups && block == Material.BLACK_CARPET && getState() == GameState.ACTIVE);
        }
    }

    public AceRacePlayer getGamePlayer(Player p) {
        return aceRacePlayerMap.get(p.getUniqueId());
    }

    public int checkpointPlacement(AceRacePlayer p, int lap, int checkpoint) {
        switch(lap) {
            case 1:
                for (int i = 0; i < lapOne.length; i++) {
                    if (lapOne[checkpoint][i] == null) {
                        lapOne[checkpoint][i] = p;
                        return (i+1);
                    }
                }
                return -1;
            case 2:
                for (int i = 0; i < lapTwo.length; i++) {
                    if (lapTwo[checkpoint][i] == null) {
                        lapTwo[checkpoint][i] = p;
                        return (i+1);
                    }
                }
                return -1;
            case 3:
                for (int i = 0; i < lapThree.length; i++) {
                    if (lapThree[checkpoint][i] == null) {
                        lapThree[checkpoint][i] = p;
                        return (i+1);
                    }
                }
                return -1;
            default:
                return -1;
        }
    }

    public void lastCheckpoint(Player p) {
        AceRacePlayer player = getGamePlayer(p);
        int checkpoint = player.checkpoint;
        player.checkpointSetter(checkpoint-1);
        p.teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength-1 : checkpoint-1));
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFireTicks(0);
    }

    public void nextCheckpoint(Player p) {
        AceRacePlayer player = getGamePlayer(p);
        int checkpoint = player.checkpoint;
        player.checkpointSetter(checkpoint+1);
        p.teleport(map.getRespawns().get(checkpoint));
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFireTicks(0);
    }

    public void firstCheckpoint(Player p) {
        AceRacePlayer player = getGamePlayer(p);
        player.checkpointSetter(0);
        p.teleport(map.getRespawns().get(0));
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setFireTicks(0);
    }

    public void topLaps() {
        StringBuilder topFive = new StringBuilder();
        int counter = 0;

        for (Long l : fastestLaps.keySet()) {
            for (int i = 0; i < fastestLaps.get(l).size(); i++) {
                topFive.append(String.format((counter+1) + ". %-18s %-9s\n", fastestLaps.get(l).get(i), new SimpleDateFormat("m:ss.S").format(new Date(l))));
            }
            counter++;
        }
        Bukkit.broadcastMessage(topFive.toString());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().getLocation().getWorld().equals(map.getWorld())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Material i = e.getCurrentItem().getType();
        if (i == null) e.setCancelled(true);
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            AceRacePlayer p = getGamePlayer(e.getPlayer());
            Material playerItem = e.getPlayer().getInventory().getItemInMainHand().getType();
            if (playerItem == Material.RED_DYE || playerItem == Material.YELLOW_DYE || playerItem == Material.LIME_DYE) {
                int time = p.cooldownTimer;
                if (time < timeRemaining) {
                    p.getPlayer().sendMessage(ChatColor.RED + "Please wait a moment before using an item again!");
                    e.setCancelled(true);
                    return;
                } else {
                    p.cooldownTimer = timeRemaining - 2;
                }
                if (playerItem == Material.RED_DYE) firstCheckpoint(e.getPlayer());
                else if (playerItem == Material.YELLOW_DYE) lastCheckpoint(e.getPlayer());
                else nextCheckpoint(e.getPlayer());
            } else {
                // in all other cases, consider a powerup to be used.
                // PowerupHandler.usePowerup() will determine appropriate effects if a powerup is used.
                // if a powerup is not used, no additional effects will occur, and the event will not be cancelled.
                PowerupHandler.usePowerup(p, playerItem, e.getAction() == Action.RIGHT_CLICK_BLOCK);
            }
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Material> trapdoorList = Set.of(Material.OAK_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
                    Material.ACACIA_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
                    Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR);
            if(trapdoorList.contains(e.getClickedBlock().getType())) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        Entity hitEntity = event.getHitEntity();
        if (!(hitEntity instanceof Player victim)) return;

        ProjectileSource shooterSource = snowball.getShooter();

        String attackerName = "Unknown";
        if (shooterSource instanceof Player attacker) {
            attackerName = attacker.getName();
        }

        victim.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                100,
                0,
                false,
                true,
                true
        ));

        victim.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                100,
                2,
                false,
                true,
                true
        ));

        victim.sendMessage("You were hit by" + attackerName + "!");
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        AceRacePlayer p = getGamePlayer(e.getPlayer());
        if (p == null) return;
        p.setPlayer(e.getPlayer());
    }
}