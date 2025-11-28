package me.kotayka.mbc.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.lockdownMaps.Abandoned;
import me.kotayka.mbc.gameMaps.lockdownMaps.LockdownMap;
import me.kotayka.mbc.gamePlayers.LockdownPlayer;

public class Lockdown extends Game {
    public LockdownMap map = new Abandoned(this);
    public Map<UUID, LockdownPlayer> lockdownPlayerMap = new HashMap<>();

    public List<LockdownPlayer> escapedList = new ArrayList<>();

    public HashMap<Participant, Integer> escapeCounter = new HashMap<>();

    public final int PAINTBALL_DAMAGE = 3;
    private Map<UUID, Long> cooldowns = new HashMap<>();
    private Map<UUID, Boolean> canShoot = new HashMap<>();
    public static final long COOLDOWN_TIME = 400; // 0.8 seconds
    private int cooldownTaskID = -1;
    private static final ItemStack HEAL_POTION = new ItemStack(Material.SPLASH_POTION);

    private WorldBorder border = null;

    private final int ORIGINAL_KILL_POINTS_18 = 5;
    private final int ORIGINAL_KILL_POINTS_24 = 7;
    private final int ORIGINAL_KILL_POINTS = ORIGINAL_KILL_POINTS_24;

    private final int TWO_KILL_POINTS_18 = 4;
    private final int TWO_KILL_POINTS_24 = 6;
    private final int TWO_KILL_POINTS = TWO_KILL_POINTS_24;

    private final int FOUR_KILL_POINTS_18 = 3;
    private final int FOUR_KILL_POINTS_24 = 5;
    private final int FOUR_KILL_POINTS = FOUR_KILL_POINTS_24;

    private final int SIX_KILL_POINTS_18 = 3;
    private final int SIX_KILL_POINTS_24 = 5;
    private final int SIX_KILL_POINTS = SIX_KILL_POINTS_24;

    private MBCTeam[][] capturedPoints = new MBCTeam[6][6];
    private MBCTeam[][] originalCapturedPoints = new MBCTeam[6][6];

    private final int FIRST_TEAM_ZONE_POINTS = 1;
    private final int FIRST_ZONE = 5; // for 24 players this is 5
    private final int SECOND_ZONE = 4; // for 24 players this is 4
    private final int THIRD_ZONE = 3; // for 24 players this is 3
    private final int FOURTH_FIFTH_ZONE = 2;
    private final int SIXTH_AND_MORE_ZONE = 1;

    private final int ESCAPE_POINTS_18 = 10;
    private final int ESCAPE_POINTS_24 = 10;
    private int ESCAPE_POINTS = ESCAPE_POINTS_18;

    public Lockdown() {
        super("Lockdown", new String[] {
                "⑲ Make your way to the center of the map, capturing as many zones as possible!\n\n" + 
                "⑲ Escape using the evacuation point in the middle of the map!",
                "⑲ Replace the wool at a capture zone in the center of a room to get points.\n\n" + 
                "⑲ Be careful, other teams want to steal your zones - and PVP is on...",
                "⑲ The border will shrink as time goes on, so make your way to the center.\n\n" +
                "⑲ The center room has 4 zones as well as the evacuation point!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                                "⑲ +1-5 points per player for capturing a zone, decreasing after each captured\n" +
                                "⑲ +1 point per player for capturing a zone first\n" +
                                "⑲ +10 points for escaping at the evacuation point\n" +
                                "⑲ +5-8 points per kill, decreasing after each kill\n"
        });
    }
    private int roundNum = 1;

    @Override
    public void createScoreboard(Participant p) {
        createLine(22, ChatColor.GREEN+""+ChatColor.BOLD+"Round: " + ChatColor.RESET+roundNum+"/3", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        if (lockdownPlayerMap.size() < 1) {
            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+"0", p);
        } else {
            for (LockdownPlayer x : lockdownPlayerMap.values()) {
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+x.kills, p);
            }
        }

        updateInGameTeamScoreboard();
    }

    @Override
    public void start() {
        super.start();

        setGameState(GameState.TUTORIAL);

        setTimer(30);
    }

    public void loadPlayers() {
        setPVP(false);
        nameTagVisibility(true);
        if (lockdownPlayerMap != null) {
            for (LockdownPlayer p : lockdownPlayerMap.values()) {
                p.lastDamager = null;
            }
        }
        if (border == null) {
            border = map.getWorld().getWorldBorder();
        }

        escapedList = new ArrayList<>();

        border.setCenter(64, 64);
        border.setSize(232);
        border.setDamageAmount(0.5);
        border.setDamageBuffer(0);
        map.addBarriers();
        ESCAPE_POINTS = ESCAPE_POINTS_24;

        for (int i = 0; i < capturedPoints.length; i++) {
            for (int j = 0; j < capturedPoints[i].length; j++) {
                capturedPoints[i][j] = null;
                originalCapturedPoints[i][j] = null;
            }
        }
        if (roundNum == 1)
            teamsAlive.addAll(getValidTeams());
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setFlying(false);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setHealth(20);
            

            p.getPlayer().removePotionEffect(PotionEffectType.JUMP_BOOST);
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.POISON);
            p.getPlayer().removePotionEffect(PotionEffectType.RESISTANCE);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 255, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 255, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 255, false, false));
            if (roundNum == 1) {
                lockdownPlayerMap.put(p.getPlayer().getUniqueId(), new LockdownPlayer(p));
                playersAlive.add(p);
            } else {
                resetAliveLists();
            }
            // reset scoreboard & variables after each round
            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+(Objects.requireNonNull(lockdownPlayerMap.get(p.getPlayer().getUniqueId()))).kills, p);
        }
        map.spawnPlayers(roundNum);
        updatePlayersAliveScoreboard();
    }
    

    /**
     * 
     */
    public void resetMaps() {
        if (lockdownPlayerMap != null) {
            for (LockdownPlayer p : lockdownPlayerMap.values()) {
                p.lastDamager = null;
            }
        }
        
    }

    @Override
    public void onRestart() {
        cooldowns.clear();
        canShoot.clear();
        lockdownPlayerMap.clear();

        if (cooldownTaskID != -1) {
            Bukkit.getScheduler().cancelTask(cooldownTaskID);
        }

        roundNum = 1;
        resetPlayers();
    }
    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                setGameState(GameState.STARTING);
                timeRemaining = 30;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                if (roundNum == 1) mapCreator(map.mapName, map.creatorName);
                startingCountdown();
                if (timeRemaining == 25) sendSpawns();
                if (timeRemaining == 20) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_PRECIPICE, SoundCategory.RECORDS, 1, 1);
                        p.getInventory().clear();
                    }
                    for (LockdownPlayer p : lockdownPlayerMap.values()) {
                        switch (roundNum) {
                            //case(-1):
                                //giveAxeKit(p);
                                //p.getPlayer().sendTitle(ChatColor.BOLD+"Kit: " + ChatColor.GREEN+"" + ChatColor.BOLD+"AXES AND BOWS", "", 20, 60, 20);
                                //p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, SoundCategory.BLOCKS, 1, 1);
                                //break;
                            case(2):
                                giveTridentKit(p);
                                p.getPlayer().sendTitle(ChatColor.BOLD+"Kit: " + ChatColor.AQUA+"" + ChatColor.BOLD+"TRIDENTS", "", 20, 60, 20);
                                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.BLOCKS, 1, 1);
                                break;
                            case(3):
                                givePaintballKit(p);
                                p.getPlayer().sendTitle(ChatColor.BOLD+"Kit: " + ChatColor.LIGHT_PURPLE+"" + ChatColor.BOLD+"PAINTBALL", "", 20, 60, 20);
                                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 1, 1);
                                paintballTask(p);
                                break;
                            case(1):
                            default:
                                giveSwordKit(p);
                                p.getPlayer().sendTitle(ChatColor.BOLD+"Kit: " + ChatColor.RED+"" + ChatColor.BOLD+"SWORDS AND CROSSBOWS", "", 20, 60, 20);
                                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WARDEN_DEATH, SoundCategory.BLOCKS, 1, 1);
                                break;
                        }
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                map.removeBarriers();
                repeatingSneakEvent();
                setPVP(true);
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().setInvulnerable(false);
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);

                    if (roundNum != 3) {
                        p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                        p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                    }
                }
                updatePlayersAliveScoreboard();
                timeRemaining = 300;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            repeatingSneakEvent();
            if (timeRemaining == 235) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Border shrinking in 10 seconds! All outer rooms will soon be closed!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING IN 10 SECONDS!", "", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
                }
            }
            if (timeRemaining == 225) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Border shrinking!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING!", "", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                }
                border.setSize(154, 45);
            }
            if (timeRemaining == 130) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Border shrinking in 10 seconds! All rooms except the middle will soon be closed!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING IN 10 SECONDS!", "", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
                }
            }
            if (timeRemaining == 120) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Border shrinking!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING!", "", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                }
                border.setSize(76, 45);
            }
            if (timeRemaining == 55) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Border shrinking! Use the evacuation zone to escape!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING IN 10 SECONDS!", "", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, 1, 1);
                }
            }
            if (timeRemaining == 45) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Final border closing!");
                for (LockdownPlayer p : lockdownPlayerMap.values()) {
                    p.getPlayer().sendTitle(ChatColor.RED+"BORDER SHRINKING!", "Escape if you can!", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                }
                border.setSize(6, 45);
            }
            if (timeRemaining == 0) {
                if (cooldownTaskID != -1) {
                    Bukkit.getScheduler().cancelTask(cooldownTaskID);
                }
                sendEscapees();
                escapeCounter.clear();
                for (Participant p : playersAlive) {
                    p.getPlayer().sendMessage(ChatColor.RED + "You got locked in! You earn no escape points.");
                    p.getPlayer().getInventory().clear();
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1);
                }
                woolPoints();
                if (roundNum < 3) {
                    roundOverGraphics();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.stopSound(Sound.MUSIC_DISC_PRECIPICE, SoundCategory.RECORDS);
                        p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
                    }
                    timeRemaining = 10;
                    setGameState(GameState.END_ROUND);
                } else {
                    gameOverGraphics();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.stopSound(Sound.MUSIC_DISC_PRECIPICE, SoundCategory.RECORDS);
                        p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
                    }
                    timeRemaining = 40;
                    setGameState(GameState.END_GAME);
                }
            }

        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 5) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                }
            }
            if (timeRemaining == 1) {
                roundNum++;
                map.resetMap();
                loadPlayers();
                createLineAll(22, ChatColor.GREEN+""+ChatColor.BOLD+"Round: " + ChatColor.RESET+roundNum+"/3");
                timeRemaining = 30;
                setGameState(GameState.STARTING);
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 36) {
                nameTagVisibility(true);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                }
            } 
            if (timeRemaining <= 35) {
                gameEndEvents();
            }
        }
    }

    public void paintballTask(LockdownPlayer p) {
        canShoot.put(p.getPlayer().getUniqueId(), true);
        cooldownTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().getPlugin(), () -> {
            Iterator<Map.Entry<UUID, Long>> iterator = cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                long storedTime = entry.getValue();

                if (System.currentTimeMillis() - storedTime >= COOLDOWN_TIME) {
                    canShoot.put(entry.getKey(), true);
                    iterator.remove();
                }
            }
        }, 20, 1);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!isGameActive()) return;
        if (roundNum != 3) return;

        event.setCancelled(true);
    }

    /*
     * Given LockdownPlayer p, return the correct amount of points they earn for killing a player. Will decrease as # of kills increases
     */
    public int killPoints(LockdownPlayer p) {
        switch(p.kills) {
            case (0):
                return ORIGINAL_KILL_POINTS;
            case (1):
            case (2):
                return TWO_KILL_POINTS;
            case (3):
            case (4):
                return FOUR_KILL_POINTS;
            default:
                return SIX_KILL_POINTS;
        }
    }

    /**
     * Given location l, returns the middle wool block. Returns null if not part of wool station
     */
    public Location checkLocation(Location l) {
        for (Location potential : map.getWoolLocations()) {
            for (int x = ((int)potential.getBlockX())-1; x <= ((int)potential.getBlockX())+1; x++) {
                for (int z = ((int)potential.getBlockZ())-1; z <= ((int)potential.getBlockZ())+1; z++) {
                    if (l.equals(new Location(map.getWorld(), x, (int)potential.getBlockY(), z))) {
                        return potential;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given center block location l, check to see if it is filled by color wool. returns true if so, false if not
     */
    public boolean filled(Location l) {
        Block checkWool = l.getBlock();
        
        for (int x = ((int)l.getBlockX())-1; x <= ((int)l.getBlockX())+1; x++) {
            for (int z = ((int)l.getBlockZ())-1; z <= ((int)l.getBlockZ())+1; z++) {
                if (!checkWool.getType().equals(map.getWorld().getBlockAt(x, l.getBlockY(), z).getType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Runs at the end of each round. Checks each location to see if it is captured. Gives points if so.
     */
    public void woolPoints() {
        HashMap<MBCTeam, Integer> woolPoints = new HashMap<>();
        HashMap<MBCTeam, Integer> capturedZones = new HashMap<>();

        String map = "\n" +ChatColor.GOLD + ""+ ChatColor.BOLD + "Map:\n\n";
        for (int i = 0; i < capturedPoints.length; i++) {
            for (int j = 0; j < capturedPoints[i].length; j++) {
                if (capturedPoints[i][j] != null) {
                    MBCTeam t = capturedPoints[i][j];
                    map = map+ t.getChatColor()+""+ChatColor.BOLD+"■";
                    if (!woolPoints.containsKey(t)) woolPoints.put(t, 0);
                    if (!capturedZones.containsKey(t)) capturedZones.put(t, 0);

                    int pointsEarned = 0;
                    switch(capturedZones.get(t)) {
                        case 0: 
                            pointsEarned = FIRST_ZONE; 
                            break;
                        case 1: 
                            pointsEarned = SECOND_ZONE;
                            break;
                        case 2: 
                            pointsEarned = THIRD_ZONE;
                            break;
                        case 3:
                        case 4:
                            pointsEarned = FOURTH_FIFTH_ZONE;
                            break;
                        default: 
                            pointsEarned = SIXTH_AND_MORE_ZONE;
                            break;
                    }
                    
                    woolPoints.replace(t, woolPoints.get(t)+pointsEarned);
                    capturedZones.replace(t, capturedZones.get(t)+1);

                }
                else {
                    map = map+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                }
            }
            map = map + "\n";
        }

        String message = "\n" + ChatColor.BOLD + "Wool Points:\n";
        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            if (!woolPoints.containsKey(t)) woolPoints.put(t, 0);
            message = message + t.teamNameFormat() + ChatColor.BOLD + ": " + (woolPoints.get(t)*t.getPlayers().size()) + " wool points\n";
            for (Participant p : t.getPlayers()) {
                p.addCurrentScore(woolPoints.get(t));
                p.getPlayer().sendMessage(ChatColor.GREEN + "Your team earned points for capturing wool points!" + MBC.scoreFormatter(woolPoints.get(t)));
                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.BLOCKS, 1, 1);
            }
        }
        logger.log(message);
        

        final String finalMap = map;
        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { sendMap(finalMap);}
          }, 100L);
    }

    public void sendEscapees() {
        String survivorDisplay = ChatColor.YELLOW + "" + ChatColor.BOLD + "\nEscaped: " + ChatColor.RESET + "\n\n";

        Map<MBCTeam, String> teamEscapes = new HashMap<>();

        for (LockdownPlayer p : escapedList) {
            MBCTeam m = p.getParticipant().getTeam();
            if (!teamEscapes.containsKey(m)) {
                teamEscapes.put(m, p.getParticipant().getFormattedName() + ", ");
            }
            else {
                String survival = teamEscapes.get(m);
                survival += p.getParticipant().getFormattedName() + ", ";
                teamEscapes.replace(m, survival);
            }
        }
        
        MBCTeam[] teamList = {MBCTeam.getTeam("red"), MBCTeam.getTeam("yellow"), MBCTeam.getTeam("green"), 
                                MBCTeam.getTeam("blue"), MBCTeam.getTeam("purple"), MBCTeam.getTeam("pink")};
        
        for (int i = 0; i < teamList.length; i++) {
            String escapees = teamEscapes.get(teamList[i]);
            if (escapees != null) {
                survivorDisplay += escapees.substring(0, escapees.length()-2) + ChatColor.RESET + "\n";
            }
        }

        logger.log(survivorDisplay);
        final String escapedPlayers = survivorDisplay;
        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() { sendMap(escapedPlayers);}
          }, 40L);
        
    }

    /*
     * Sends spawn locations at beginning of each round.
     */
    public void sendSpawns() {
        MBCTeam[] spawnLocations = map.teamSpawnLocations();
        String spawns = "\n" +ChatColor.GOLD + ""+ ChatColor.BOLD + "Spawn Locations:\n\n";
        
        for (int i = 0; i < capturedPoints.length; i++) {
            for (int j = 0; j < capturedPoints[i].length; j++) {
                //(0,2)
                // (0,5)
                // (3,5)
                // (5,3)
                // (5,0)
                // (2,0)
                if (i == 0 && j == 2) {
                    MBCTeam t = spawnLocations[0];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else if (i == 0 && j == 5) {
                    MBCTeam t = spawnLocations[1];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else if (i == 3 && j == 5) {
                    MBCTeam t = spawnLocations[2];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else if (i == 5 && j == 3) {
                    MBCTeam t = spawnLocations[3];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else if (i == 5 && j == 0) {
                    MBCTeam t = spawnLocations[4];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else if (i == 2 && j == 0) {
                    MBCTeam t = spawnLocations[5];
                    if (t == null) spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                    else spawns = spawns+t.getChatColor() +"" + ChatColor.BOLD +"■";
                }
                else {
                    spawns = spawns+ChatColor.WHITE +""+ChatColor.BOLD+ "■";
                }
            }
            spawns = spawns + "\n";
        }

        Bukkit.broadcastMessage(spawns);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    /*
     * Sends the map in chat.
     */
    public void sendMap(String s) {
        Bukkit.broadcastMessage(s);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p, Sound.ENTITY_CHICKEN_EGG, 1, 1);
        }
    }

    /*
     * checks to see if player p is outside world border.
     */
    public boolean isOutsideOfBorder(Player p) {
        Location loc = p.getLocation();
        double x = loc.getX();
        double z = loc.getZ();
        double size = border.getSize();
        return ((x > size || (-x) > size) || (z > size || (-z) > size));
    }


    /**
     * Check to see if a block is being placed in a wool station, what color it is, and which station its in.
     */
    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (!isGameActive()) return;
        if (checkWool(e.getBlock().getLocation())) {
            Player p = e.getPlayer();
            String wool = e.getBlock().getType().toString();
            // check item slot
            if (e.getHand() == EquipmentSlot.HAND) {
                int index = p.getInventory().getHeldItemSlot();
                int amt = Objects.requireNonNull(p.getInventory().getItem(index)).getAmount();
                p.getInventory().setItem(index, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
            }
            else if (e.getHand() == EquipmentSlot.OFF_HAND) {
                int amt = Objects.requireNonNull(p.getInventory().getItem(40)).getAmount();
                p.getInventory().setItem(40, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
            }

            Location l = checkLocation(e.getBlock().getLocation());
            if (l == null) return;
            if (filled(l)) {
                MBCTeam t = Participant.getParticipant(p).getTeam();
                capturePoint(l, t);
            } else {
            }
        }
        else {
            e.setCancelled(true);
            return;
        }

    }

    @EventHandler
    public void sneakEvent(PlayerToggleSneakEvent e) {
        if (!isGameActive()) return;
        
        if (e.isSneaking()) {
            Player p = e.getPlayer();
            if (p.getGameMode().equals(GameMode.SPECTATOR)) return;
            Location l = p.getLocation();
            int x = (l.getBlockX());
            int z = (l.getBlockZ());

            Block check = new Location(map.getWorld(), x, 1, z).getBlock();
            if (check.getType().equals(Material.WAXED_EXPOSED_COPPER_BULB) && p.getY() < 4.5) {
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You are now escaping!" + ChatColor.RESET + "" + ChatColor.RED + 
                                                " Hold shift and wait 5 seconds.");
                p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 7, false, false));
                escapeCounter.put(Participant.getParticipant(p), 4);
            }
            
        }
        else {
            Player p = e.getPlayer();
            if (p.hasPotionEffect(PotionEffectType.LEVITATION) && escapeCounter.containsKey(Participant.getParticipant(p))) {
                removeEscapee(Participant.getParticipant(p));
            }
            
        }

    }

    /*
     * Repeating event. Will recur every second for entirety of game. If player is in escapecounter for 5 seconds, will "escape".
     */
    public void repeatingSneakEvent() {
        if (!isGameActive()) return;

        ArrayList<Participant> toRemove = new ArrayList<>();

        for (Participant p : escapeCounter.keySet()) {
            if (p == null) continue;
            int time = escapeCounter.get(p);
            if (time == 0) {
                playerEscape(p);
                toRemove.add(p);
            }
            else {
                escapeCounter.replace(p, time-1);
                p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1, 1);
                if (time == 1) p.getPlayer().sendTitle(ChatColor.RED+"Hold shift...", "1 second left...", 0, 0, 20);
                else p.getPlayer().sendTitle(ChatColor.RED+"Hold shift...", time + " seconds left...", 0, 0, 20);
            }
        }

        for (Participant p : toRemove) {
            escapeCounter.remove(p);
        }

        toRemove.clear();
    }

    /*
     * Player p escapes! Give points, change playersalive.
     */
    public void playerEscape(Participant p) {
        p.getPlayer().playSound(p.getPlayer(), "sfx.kill_coins", SoundCategory.BLOCKS, 1, 1);
        p.addCurrentScore(ESCAPE_POINTS);
        p.getPlayer().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "You Escaped!" + MBC.getInstance().scoreFormatter(ESCAPE_POINTS));
        Bukkit.broadcastMessage(p.getFormattedName() + " escaped the Lockdown!");
        updatePlayersAlive(p);
        logger.log(p.getFormattedName() + " escaped the Lockdown!");
        p.getPlayer().setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        LockdownPlayer lp = lockdownPlayerMap.get(p.getPlayer().getUniqueId());
        if (lp!=null) escapedList.add(lp);
    }

    /*
     * Player p fails to escape. Remove from escapeCounter, remove levitation.
     */
    public void removeEscapee(Participant p) {
        p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1, 1);
        p.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Escape Cancelled!");
        p.getPlayer().removePotionEffect(PotionEffectType.LEVITATION);
        escapeCounter.remove(p);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) return;
        if (!isGameActive()) return;

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Material> trapdoorList = Set.of(Material.OAK_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
                                        Material.ACACIA_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
                                        Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR);
            if(trapdoorList.contains(e.getClickedBlock().getType())) e.setCancelled(true);
        }

        // handle shooting paintballs
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = e.getPlayer();
            Material heldItem = player.getInventory().getItemInMainHand().getType();
            if (heldItem == Material.DIAMOND_HORSE_ARMOR) {
                shootPaintball(player);
            } else if ((heldItem == Material.SPLASH_POTION && e.getHand() == EquipmentSlot.HAND) || (player.getInventory().getItemInOffHand().getType() == Material.SPLASH_POTION  && e.getHand() == EquipmentSlot.OFF_HAND)) {
                LockdownPlayer lockdownPlayer = lockdownPlayerMap.get(player.getUniqueId());
                if (lockdownPlayer != null) handlePotionStatus(lockdownPlayer);
            }
        }
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
        if (e.getOffHandItem() != null && e.getOffHandItem().getType() == Material.SPLASH_POTION) {
            e.setCancelled(true);
        }
        if (e.getOffHandItem() != null && e.getMainHandItem().getType() == Material.SPLASH_POTION) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getCursor() != null && e.getCursor().getType() == Material.SPLASH_POTION) {
            if (e.getSlot() == 40) { // Slot 40 is the offhand slot
                e.setCancelled(true);
            }
    }   
    }

    /**
     * Spawns a snowball projectile from `shooter`
     * {@link #onPlayerInteract} calls this function on valid shot attempt.
     * @param shooter The player who is shooting.
     */
    private void shootPaintball(Player shooter) {
        if (canShoot.containsKey(shooter.getUniqueId()) && canShoot.get(shooter.getUniqueId())) {
            Snowball proj = shooter.launchProjectile(Snowball.class);
            proj.setVelocity(proj.getVelocity().multiply(1.25));
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 2);

            canShoot.put(shooter.getUniqueId(), false);
            cooldowns.put(shooter.getUniqueId(), System.currentTimeMillis());
        } else {
            shooter.playSound(shooter.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
        }
    }

    /**
     * Schedules a task to give player a potion if they do not have one and are in the correct GameMode.
     * {@link #onPlayerInteract} calls this function on valid potion throw
     * @param player Player who threw the potion
     */
    private void handlePotionStatus(LockdownPlayer player) {
       player.setPotion(false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (player.getPlayer().getGameMode() == GameMode.SURVIVAL && isGameActive()) {
                    player.setPotion(true);
                    player.getPlayer().getInventory().addItem(HEAL_POTION);
                }
            }
        }, 600);
    }

    /**
     * Overrides checklastteam function from game. Game cannot end from only 1 team remaining.
     */
    @Override
    public void checkLastTeam(MBCTeam t) {
        if (checkTeamEliminated(t)) {
            teamsAlive.remove(t);
            t.announceTeamOut();
        }
    }

    /**
     * Check to see if a block is being broken in a wool station, what color it was, and which station its in.
     */
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        if (!isGameActive()) return;
        if (checkWool(e.getBlock().getLocation())) {
            Player p = e.getPlayer();
            Location l = checkLocation(e.getBlock().getLocation());
            if (l == null) return;
            if (capturedPoints[map.rowOfLocation(l)][map.columnOfLocation(l)] != null) {
                lostPoint(l, capturedPoints[map.rowOfLocation(l)][map.columnOfLocation(l)]);
            }
        }
        else {
            e.setCancelled(true);
            return;
        }
    }

    /*
     * MBCTeam t has captured the point at middle location l. Note this and make message in chat.
     */
    public void capturePoint(Location l, MBCTeam t) {
        int row = map.rowOfLocation(l);
        int column = map.columnOfLocation(l);

        boolean notYetCaptured = false;

        if (originalCapturedPoints[row][column] == null) {
            notYetCaptured = true;
        }

        for (Participant p : t.getPlayers()) {
            if (!p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) MBC.spawnFirework(p);

            if (notYetCaptured) {
                p.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your team captured a zone!" + MBC.getInstance().scoreFormatter(FIRST_TEAM_ZONE_POINTS));
                p.addCurrentScore(FIRST_TEAM_ZONE_POINTS);
                originalCapturedPoints[row][column] = t;
            }
            else {
                p.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Your team captured a zone!");
            }
        }

        capturedPoints[row][column] = t;

        
    }

    /*
     * MBCTeam t has lost the point at middle location l. Note this and make message in chat.
     */
    public void lostPoint(Location l, MBCTeam t) {
        int row = map.rowOfLocation(l);
        int column = map.columnOfLocation(l);

        capturedPoints[row][column] = null;

        for (Participant p : t.getPlayers()) {
            p.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Another team is breaking one of your zones...");
            p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_ALLAY_DEATH, SoundCategory.BLOCKS, 0.5f, 1);
        }
    }

    /**
     * Check location l to see if its the middle of the map.
     */
    public boolean checkMiddle(Location l) {
        if (map.getWorld().getBlockAt((int)l.getBlockX(), 1, (int)l.getBlockZ()).getType().equals(Material.WAXED_EXPOSED_COPPER_BULB)) return true;
        return false;
    }

    /**
     * Check location l to see if its at a wool station.
     */
    public boolean checkWool(Location l) {
        for (Location potential : map.getWoolLocations()) {
            for (int x = ((int)potential.getBlockX())-1; x <= ((int)potential.getBlockX())+1; x++) {
                for (int z = ((int)potential.getBlockZ())-1; z <= ((int)potential.getBlockZ())+1; z++) {
                    if (l.equals(new Location(map.getWorld(), x, (int)potential.getBlockY(), z))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Gives LockdownPlayer p the axe kit.
     */
    public static void giveAxeKit(LockdownPlayer p) {
        if (p == null) {return;}

        ItemStack stoneAxe = new ItemStack(Material.STONE_AXE);
        ItemMeta stoneMeta = stoneAxe.getItemMeta();
        stoneMeta.setUnbreakable(true);
        stoneAxe.setItemMeta(stoneMeta);

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setUnbreakable(true);
        bow.setItemMeta(bowMeta);

        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 8);
        ItemStack wool = p.getParticipant().getTeam().getColoredWool();
        wool.setAmount(64);

        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shears.getItemMeta();
        shearsMeta.setUnbreakable(true);
        shears.setItemMeta(shearsMeta);

        ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
        ItemMeta helmetMeta = ironHelmet.getItemMeta();
        helmetMeta.setUnbreakable(true);
        ironHelmet.setItemMeta(helmetMeta);

        ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);
        ItemMeta bootsMeta = ironBoots.getItemMeta();
        bootsMeta.setUnbreakable(true);
        ironBoots.setItemMeta(bootsMeta);

        ItemStack leatherChestplate = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemMeta chestplateMeta = leatherChestplate.getItemMeta();
        chestplateMeta.setUnbreakable(true);
        leatherChestplate.setItemMeta(chestplateMeta);

        ItemStack ironPants = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta pantsMeta = ironPants.getItemMeta();
        pantsMeta.setUnbreakable(true);
        ironPants.setItemMeta(pantsMeta);

        p.getPlayer().getInventory().addItem(stoneAxe);
        p.getPlayer().getInventory().addItem(bow);
        p.getPlayer().getInventory().addItem(steak);
        p.getPlayer().getInventory().addItem(wool);
        p.getPlayer().getInventory().addItem(shears);
        p.getPlayer().getInventory().addItem(arrow);

        p.getPlayer().getInventory().setHelmet(ironHelmet);
        p.getPlayer().getInventory().setChestplate(leatherChestplate);
        p.getPlayer().getInventory().setLeggings(ironPants);
        p.getPlayer().getInventory().setBoots(ironBoots);
    }

    /**
     * Gives LockdownPlayer p the sword kit (normal one).
     */
    public static void giveSwordKit(LockdownPlayer p) {
        if (p == null) {
            return;
        }

        ItemStack stoneSword = new ItemStack(Material.STONE_SWORD);
        ItemMeta stoneMeta = stoneSword.getItemMeta();
        stoneMeta.setUnbreakable(true);
        stoneSword.setItemMeta(stoneMeta);

        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        ItemMeta crossbowMeta = crossbow.getItemMeta();
        crossbowMeta.setUnbreakable(true);
        crossbow.setItemMeta(crossbowMeta);

        //ItemStack map = new ItemStack(Material.FILLED_MAP);
        //MapMeta mapMeta = (MapMeta)map.getItemMeta();
        //mapMeta.setMapId(116);
        //MapView mapView = mapMeta.getMapView();
        //mapView.setTrackingPosition(false);
        //mapMeta.setMapView(mapView);
        //map.setItemMeta(mapMeta);

        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 8);
        ItemStack wool = p.getParticipant().getTeam().getColoredWool();
        wool.setAmount(64);

        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shears.getItemMeta();
        shearsMeta.setUnbreakable(true);
        shears.setItemMeta(shearsMeta);

        ItemStack diamondHelmet = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta helmetMeta = diamondHelmet.getItemMeta();
        helmetMeta.setUnbreakable(true);
        diamondHelmet.setItemMeta(helmetMeta);

        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta bootsMeta = diamondBoots.getItemMeta();
        bootsMeta.setUnbreakable(true);
        diamondBoots.setItemMeta(bootsMeta);

        ItemStack leatherChestplate = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemMeta chestplateMeta = leatherChestplate.getItemMeta();
        chestplateMeta.setUnbreakable(true);
        leatherChestplate.setItemMeta(chestplateMeta);

        ItemStack ironPants = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta pantsMeta = ironPants.getItemMeta();
        pantsMeta.setUnbreakable(true);
        ironPants.setItemMeta(pantsMeta);

        p.getPlayer().getInventory().addItem(stoneSword);
        p.getPlayer().getInventory().addItem(crossbow);
        p.getPlayer().getInventory().addItem(steak);
        p.getPlayer().getInventory().addItem(wool);
        p.getPlayer().getInventory().addItem(shears);
        p.getPlayer().getInventory().addItem(arrow);
        //p.getPlayer().getInventory().addItem(map);

        p.getPlayer().getInventory().setHelmet(diamondHelmet);
        p.getPlayer().getInventory().setChestplate(leatherChestplate);
        p.getPlayer().getInventory().setLeggings(ironPants);
        p.getPlayer().getInventory().setBoots(diamondBoots);
    }

    /**
     * Gives LockdownPlayer p the trident kit.
     */
    public static void giveTridentKit(LockdownPlayer p) {
        if (p == null) {return;}

        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta tridentMeta = trident.getItemMeta();
        tridentMeta.setUnbreakable(true);
        trident.setItemMeta(tridentMeta);

        ItemStack goldAxe = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta axeMeta = goldAxe.getItemMeta();
        axeMeta.setUnbreakable(true);
        goldAxe.setItemMeta(axeMeta);

        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 8);
        ItemStack wool = p.getParticipant().getTeam().getColoredWool();
        wool.setAmount(64);

        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shears.getItemMeta();
        shearsMeta.setUnbreakable(true);
        shears.setItemMeta(shearsMeta);

        ItemStack turtleHelmet = new ItemStack(Material.TURTLE_HELMET);
        ItemMeta helmetMeta = turtleHelmet.getItemMeta();
        helmetMeta.setUnbreakable(true);
        turtleHelmet.setItemMeta(helmetMeta);

        ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);
        ItemMeta bootsMeta = ironBoots.getItemMeta();
        bootsMeta.setUnbreakable(true);
        ironBoots.setItemMeta(bootsMeta);

        ItemStack leatherChestplate = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemMeta chestplateMeta = leatherChestplate.getItemMeta();
        chestplateMeta.setUnbreakable(true);
        leatherChestplate.setItemMeta(chestplateMeta);

        ItemStack diamondPants = new ItemStack(Material.DIAMOND_LEGGINGS);
        ItemMeta pantsMeta = diamondPants.getItemMeta();
        pantsMeta.setUnbreakable(true);
        diamondPants.setItemMeta(pantsMeta);

        p.getPlayer().getInventory().addItem(trident);
        p.getPlayer().getInventory().addItem(goldAxe);
        p.getPlayer().getInventory().addItem(steak);
        p.getPlayer().getInventory().addItem(wool);
        p.getPlayer().getInventory().addItem(shears);

        p.getPlayer().getInventory().setHelmet(turtleHelmet);
        p.getPlayer().getInventory().setChestplate(leatherChestplate);
        p.getPlayer().getInventory().setLeggings(diamondPants);
        p.getPlayer().getInventory().setBoots(ironBoots);
    }

     /**
     * Returns the trident from the Trident Kit.
     */
    public static ItemStack giveTrident() {

        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta tridentMeta = trident.getItemMeta();
        tridentMeta.setUnbreakable(true);
        trident.setItemMeta(tridentMeta);

        return trident;
    }

    /**
     * Gives LockdownPlayer p the paintball kit.
     */
    public static void givePaintballKit(LockdownPlayer p) {
        if (p == null) {return;}

        ItemStack paintballGun = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
        ItemStack steak = new ItemStack(Material.COOKED_BEEF, 8);
        ItemStack wool = p.getParticipant().getTeam().getColoredWool();
        wool.setAmount(64);
        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shears.getItemMeta();
        shearsMeta.setUnbreakable(true);
        shears.setItemMeta(shearsMeta);

        PotionMeta potionMeta = (PotionMeta) HEAL_POTION.getItemMeta();
        potionMeta.setBasePotionType(PotionType.HEALING);
        ArrayList<String> potionLore = new ArrayList();
        potionLore.add(ChatColor.LIGHT_PURPLE + "Gives large amount of health on splash!");
        potionMeta.setLore(potionLore);
        potionMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Potion of Healing II");
        potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true);
        HEAL_POTION.setItemMeta(potionMeta);

        ItemStack helm = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_HELMET));
        ItemStack chest = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemStack legs = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_LEGGINGS));
        ItemStack boots = p.getParticipant().getTeam().getColoredLeatherArmor(new ItemStack(Material.LEATHER_BOOTS));

        ItemMeta helmMeta = helm.getItemMeta();
        ItemMeta chestMeta = helm.getItemMeta();
        ItemMeta legsMeta = helm.getItemMeta();
        ItemMeta bootsMeta = helm.getItemMeta();
        helmMeta.setUnbreakable(true);
        chestMeta.setUnbreakable(true);
        legsMeta.setUnbreakable(true);
        bootsMeta.setUnbreakable(true);

        p.getPlayer().getInventory().addItem(paintballGun);
        p.getPlayer().getInventory().addItem(wool);
        p.getPlayer().getInventory().addItem(shears);
        p.getPlayer().getInventory().addItem(HEAL_POTION);

        p.getPlayer().getInventory().setHelmet(helm);
        p.getPlayer().getInventory().setChestplate(chest);
        p.getPlayer().getInventory().setLeggings(legs);
        p.getPlayer().getInventory().setBoots(boots);
    }

    /**
     * Check to see if Player p is in the middle of the map.
     */
    public boolean checkMiddle(Player p) {
        return checkMiddle(p.getLocation());
    }

    /**
     * Track damage for appropriate kill credit
     */
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!isGameActive()) return;
        if (!((e.getEntity()) instanceof Player)) return;

        LockdownPlayer player = lockdownPlayerMap.get(e.getEntity().getUniqueId());
        
        if (player == null) return;

        if (e.getDamager() instanceof Player) {
            Participant damager = Participant.getParticipant((Player) e.getDamager());
            if (damager.getTeam().equals(player.getParticipant().getTeam())) return;
        }

        // for any general attack
        if (e.getDamager() instanceof Player) {
            player.lastDamager = (Player) e.getDamager();
        }
        Participant damaged = Participant.getParticipant((Player) e.getEntity());

        DamageType d = e.getDamageSource().getDamageType();

        if(escapeCounter.containsKey(damaged)) {
            removeEscapee(damaged);
        }
    }

    /**
     * Track projectile damage for appropriate kill credit
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        // TODO this isn't registering for some reason
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (e.getHitBlock() != null) {
                arrow.remove();
                return;
            }
        }

        if (e.getEntity() instanceof Trident) {
            Trident trident = (Trident) e.getEntity();
            if (!isGameActive()) {
                trident.remove();
                e.setCancelled(true);
                MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
                    @Override
                    public void run() { ((Player) trident.getShooter()).getInventory().addItem(giveTrident());}
                  }, 20L);
                return;
            }
            if (!(trident.getShooter() instanceof Player)) {
                trident.remove();
                e.setCancelled(true);
                return;
            }
            if (e.getHitBlock() != null) {
                trident.remove();
                ((Player) trident.getShooter()).playSound(((Player) trident.getShooter()), Sound.ITEM_TRIDENT_HIT_GROUND, 1, 1);
                MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
                    @Override
                    public void run() { ((Player) trident.getShooter()).getInventory().addItem(giveTrident());}
                  }, 20L);
                return;
            }
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                trident.remove();
                MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
                    @Override
                    public void run() { ((Player) trident.getShooter()).getInventory().addItem(giveTrident());}
                  }, 20L);
            }
        }

        if (!isGameActive()) return;
        if (e.getHitEntity() == null) return;
        if (!(e.getEntity().getShooter() instanceof Player) || !(e.getHitEntity() instanceof Player)) return;

        // register paintball damage
        LockdownPlayer player = lockdownPlayerMap.get(e.getHitEntity().getUniqueId());
        Participant damager = Participant.getParticipant((Player) e.getEntity().getShooter());

        if (e.getEntity() instanceof Snowball) {
            paintballHit(damager, player.getParticipant(), e.getEntity());
        }

        if (player == null || damager == null) return;

        if (player.getParticipant().getTeam().equals(damager.getTeam())) return;

        player.lastDamager = (Player) e.getEntity().getShooter();
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();

        if (shooter instanceof Player) {
            Player player = (Player) shooter;

            ItemStack arrow = new ItemStack(Material.ARROW);
            MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() {
                @Override
                public void run() { player.getInventory().addItem(arrow);}
              }, 60L);
        }
    }

    private void paintballHit(Participant shooter, Participant hit, Projectile projectile) {
        if (shooter.getTeam().getTeamName().equals(hit.getTeam().getTeamName())) return;

        Player hitPlayer = hit.getPlayer();
        Vector velocity = projectile.getVelocity();
        projectile.remove();

        Participant damaged = Participant.getParticipant(hitPlayer);

        shooter.getPlayer().playSound(shooter.getPlayer(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        hit.getPlayer().playSound(hit.getPlayer(), Sound.BLOCK_GLASS_BREAK, 1, 1.5f);

        if(escapeCounter.containsKey(damaged)) {
            removeEscapee(damaged);
        }

        if (hitPlayer.getHealth() - PAINTBALL_DAMAGE <= 0) {
            paintballDeath(lockdownPlayerMap.get(shooter.getPlayer().getUniqueId()), lockdownPlayerMap.get(hit.getPlayer().getUniqueId()));
        } else {
            hitPlayer.damage(PAINTBALL_DAMAGE);
            lockdownPlayerMap.get(hit.getPlayer().getUniqueId()).lastDamager = shooter.getPlayer();
        }
        hitPlayer.setVelocity(new Vector(velocity.getX() * 0.1, 0.15, velocity.getZ() * 0.1));
    }

    /**
     * Give kill credit to last damager ONLY if nobody else had hit them between
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!isGameActive()) return;
        LockdownPlayer player = lockdownPlayerMap.get(e.getEntity().getUniqueId());
        if (player == null) return;

        LockdownPlayer killer = lockdownPlayerMap.get(e.getEntity().getUniqueId());
        if (player.lastDamager != null) {
            killer = lockdownPlayerMap.get(player.lastDamager.getUniqueId());
        }

        EntityDamageEvent damageEvent = e.getPlayer().getLastDamageCause();
        if (damageEvent == null) {
            if (killer == null) {
                e.setDeathMessage(player.getParticipant().getFormattedName() + " died mysteriously!");
            } else {
                e.setDeathMessage(player.getParticipant().getFormattedName() + " mysteriously died to " + killer.getParticipant().getFormattedName());
                killer.getParticipant().addCurrentScore(killPoints(killer));
                killer.kills++;
                createLine(2, ChatColor.YELLOW + "" + ChatColor.BOLD + "Your kills: " + ChatColor.RESET + killer.kills, killer.getParticipant());
            }
        } else {
            skybattleDeathGraphics(e, damageEvent.getCause());
        }
        updatePlayersAlive(player.getParticipant());

        if (killer!=null && !killer.equals(player)) {
            killer.getPlayer().playSound(killer.getPlayer(), "sfx.kill_coins", SoundCategory.BLOCKS, 0.5f, 1);
        }

        e.getPlayer().getInventory().clear();
        e.setCancelled(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(e.getDeathMessage());
        }

        getLogger().log(e.getDeathMessage());

        updatePlayersAliveScoreboard();

        if (playersAlive.size() == 0) {
            timeRemaining = 1;
        }

    }

    private void paintballDeath(LockdownPlayer killer, LockdownPlayer victim) {
        String deathMessage = victim.getParticipant().getFormattedName() + " was shot by " + killer.getParticipant().getFormattedName();
        if (!killer.equals(victim)) {
            killer.getPlayer().playSound(killer.getPlayer(), "sfx.kill_coins", SoundCategory.BLOCKS, 0.5f, 1);
        }

        int killPoints = killPoints(killer);
        killer.kills++;

        createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+killer.kills, killer.getParticipant());
        killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayer().getName() + "!" + MBC.scoreFormatter(killPoints));
        killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
        killer.getParticipant().addCurrentScore(killPoints);

        MBC.spawnFirework(victim.getParticipant());
        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        victim.getPlayer().getInventory().clear();
        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(deathMessage);
        }

        getLogger().log(deathMessage);

        updatePlayersAlive(victim.getParticipant());

        updatePlayersAliveScoreboard();

        if (playersAlive.isEmpty()) {
            timeRemaining = 1;
        }
    }

    /**
     * If true, will enable name tags to be visible. If false, will enable name tags to be invisible.
     */
    private void nameTagVisibility(boolean b) {
        for (Participant p : MBC.getInstance().getPlayers()) {
            Team.OptionStatus o;
            if(b) o = Team.OptionStatus.ALWAYS;
            else o = Team.OptionStatus.NEVER;
            for (MBCTeam m : MBC.getInstance().getValidTeams()) {
                p.board.getTeam(m.getTeamFullName()).setOption(Team.Option.NAME_TAG_VISIBILITY, o);
            }
        }
    }

    /**
     * Removes player from playersAlive list
     * Updates display for players alive
     * Checks if last team is remaining
     * @param p Participant to be removed
     */
    @Override
    public void updatePlayersAlive(Participant p) {
        playersAlive.remove(p);
        checkLastTeam(p.getTeam());
        updatePlayersAliveScoreboard();
        if (playersAlive.size() == 0) {
        timeRemaining = 1;
        }
    }

    /**
     * Tests to see if a player moves, is infected, and is within 2 blocks of any other player. If so, infects them too.
     */
    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;
        if (map == null) return;

        Player p = e.getPlayer();
        Location l = p.getLocation();
        int x = (l.getBlockX());
        int z = (l.getBlockZ());
        Block check = new Location(map.getWorld(), x, 1, z).getBlock();
        if (check.getType() != Material.WAXED_EXPOSED_COPPER_BULB && escapeCounter.containsKey(Participant.getParticipant(p))) {
            removeEscapee(Participant.getParticipant(p));
        }

        if(isOutsideOfBorder(p)) {

        }
        
    }

    /**
     * Explicitly handles deaths where the player dies indirectly to combat or to
     * custom damage (border or void).
     * Checks for last team remaining.
     * @param e Event thrown when a player dies
     */
    public void skybattleDeathGraphics(PlayerDeathEvent e, EntityDamageEvent.DamageCause damageCause) {
        LockdownPlayer victim = lockdownPlayerMap.get(e.getPlayer().getUniqueId());
        String deathMessage = e.getDeathMessage();

        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        MBC.spawnFirework(victim.getParticipant());
        deathMessage = deathMessage.replace(victim.getPlayer().getName(), victim.getParticipant().getFormattedName());

        if (victim.lastDamager != null && !lockdownPlayerMap.get(victim.lastDamager.getUniqueId()).equals(victim)) {
            
            LockdownPlayer killer = lockdownPlayerMap.get(victim.lastDamager.getUniqueId());
            int killPoints = killPoints(killer);
            killer.kills++;

            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+killer.kills, killer.getParticipant());
            
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayer().getName() + "!" + MBC.scoreFormatter(killPoints));
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            killer.getParticipant().addCurrentScore(killPoints);

            switch (damageCause) {
                case ENTITY_ATTACK:
                case ENTITY_SWEEP_ATTACK:
                    deathMessage = victim.getParticipant().getFormattedName() + " was slain by " + killer.getParticipant().getFormattedName();
                    break;
                case PROJECTILE:
                    deathMessage = victim.getParticipant().getFormattedName() + " was shot by " + killer.getParticipant().getFormattedName();
                    break;
                case CUSTOM:
                    deathMessage = victim.getParticipant().getFormattedName() + " was killed in the border whilst fighting " + killer.getParticipant().getFormattedName();
                    break;
                case ENTITY_EXPLOSION:
                case BLOCK_EXPLOSION:
                    deathMessage = victim.getParticipant().getFormattedName() + " was blown up by " + killer.getParticipant().getFormattedName();
                    break;
                case FALL:
                    deathMessage = victim.getParticipant().getFormattedName() + " hit the ground too hard whilst trying to escape from " + killer.getParticipant().getFormattedName();
                    break;
                case SUFFOCATION:
                case FALLING_BLOCK:
                    deathMessage += " whilst fighting " + killer.getParticipant().getFormattedName();
                    break;
                case WORLD_BORDER:
                    deathMessage = victim.getParticipant().getFormattedName() + " was killed in the border fighting " + killer.getParticipant().getFormattedName();
                    break;
                default:
                    deathMessage = victim.getParticipant().getFormattedName() + " has died to " + killer.getParticipant().getFormattedName();
                    break;
            }
        } else if (victim.getPlayer().getKiller() != null) {
            LockdownPlayer killer = lockdownPlayerMap.get(victim.getPlayer().getKiller().getUniqueId());
            int killPoints = killPoints(killer);
            killer.kills++;

            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+killer.kills, killer.getParticipant());
            
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayer().getName() + "!" + MBC.scoreFormatter(killPoints));
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            killer.getParticipant().addCurrentScore(killPoints);

            if (!(deathMessage.contains(" " + killer.getPlayer().getName()))) {
                deathMessage += " whilst fighting " + killer.getParticipant().getFormattedName();
            }
        } else {
            // if no killer, the player killed themselves
            if (damageCause.equals(EntityDamageEvent.DamageCause.CUSTOM)) {
                deathMessage = victim.getParticipant().getFormattedName() + " was killed by border damage";
            }
        }

        e.setDeathMessage(deathMessage);

    }

    /*
     * Act as if player is not alive on disconnect.
     */
    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            LockdownPlayer p = lockdownPlayerMap.get(e.getPlayer().getUniqueId());
            updatePlayersAlive(p.getParticipant());


            for (Player play : Bukkit.getOnlinePlayers()) {
                play.sendMessage(p.getParticipant().getFormattedName() + " disconnected!");
            }

            if (p.lastDamager != null) {
                LockdownPlayer killer = lockdownPlayerMap.get(p.lastDamager.getPlayer().getUniqueId());
                if (killer == null) return;
                int killPoints = killPoints(killer);
                killer.getParticipant().addCurrentScore(killPoints);
                killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + p.getParticipant().getPlayerName() + "!" + MBC.scoreFormatter(killPoints));
                killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + p.getParticipant().getFormattedName(), 0, 60, 20);
                killer.kills++;
            }
        }
    }

    /*
     * On reconnect, reset lastdamager and set spectator.
     */
    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        LockdownPlayer p = lockdownPlayerMap.get(e.getPlayer().getUniqueId());
        if (p == null) return;
        p.lastDamager = null;
        p.setPlayer(e.getPlayer());

        // if log back in during paused/starting, manually teleport them
        if (!(getState().equals(GameState.PAUSED)) && !(getState().equals(GameState.STARTING))) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().teleport(map.getCenter());
        }
    }

    /**
     * Ensures nothing is dropped.
     */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().getLocation().getWorld().equals(map.getWorld())) return;
        e.setCancelled(true);
   }

    /*
     * Resets all players to 0 kills and no last damager.
     */
    public void resetPlayers() {
        for (LockdownPlayer p : lockdownPlayerMap.values()) {
            p.kills = 0;
            p.lastDamager = null;
        }
    }
}

