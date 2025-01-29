package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.iDrgCity;
import me.kotayka.mbc.gameMaps.aceRaceMap.semoiB;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

public class AceRace extends Game {
    // Change this to determine played map
    //public AceRaceMap map = new Biomes();
    public AceRaceMap map = new iDrgCity();
    public static World world = Bukkit.getWorld("AceRace");;
    public Map<UUID, AceRacePlayer> aceRacePlayerMap = new HashMap<UUID, AceRacePlayer>();
    public short[] finishedPlayersByLap = {0, 0, 0};
    public long startingTime;

    // keep track of top 5 fastest
    public SortedMap<Long, List<String>> fastestLaps = new TreeMap<Long, List<String>>();

    // SCORING VARIABLES
    public static final int FINISH_RACE_POINTS = 12;           // points for finishing the race
    public static final int PLACEMENT_LAP_POINTS = 1;         // points for placement for first laps
    public static final int LAP_COMPLETION_POINTS = 1;
    public static final int PLACEMENT_FINAL_LAP_POINTS = 3;   // points for placement for last lap
    public static final int[] PLACEMENT_BONUSES = {20, 15, 15, 10, 10, 5, 5, 5, 5, 5}; // points for Top 10 finishers
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

        //for (Player p : Bukkit.getOnlinePlayers()) {
            //p.teleport(map.getIntroLocation());
        //}

        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.getPlayer().teleport(map.getIntroLocation());
            p.reset();
        }

        setTimer(TUTORIAL_TIME);
    }

    @Override
    public void onRestart() {
        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.reset();
        }
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
                //Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.GOLD + "" + ChatColor.BOLD + "Starting Practice Time!");
                finishedIntro = true;
            } else if (timeRemaining == 60) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "One minute left of practice!");
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining <= 0) {
                map.setBarriers(true);
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    p.getPlayer().teleport(map.getIntroLocation());
                    //p.getPlayer().teleport(new Location(map.getWorld(), 2, 26, 150, 90, 0));
                    p.checkpoint = 0;
                }
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
                if (timeRemaining == 11) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, SoundCategory.RECORDS, 0.75f, 1);
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                map.setBarriers(false);
                //setPVP(true);
                timeRemaining = 720;
                startingTime = System.currentTimeMillis();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.RECORDS, 0.75f, 1);
                    p.playSound(p, Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1, 1);
                }
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "30 seconds remaining!");
                // TODO: play overtime music (or if there are like 2 players still racing: tbd)
            } else if (timeRemaining <= 0) {
                gameOverGraphics();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_11, SoundCategory.RECORDS);
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
                    p.stopSound(Sound.MUSIC_DISC_11, SoundCategory.RECORDS);
                    p.playSound(p, Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1, 1);
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
            //p.getInventory().addItem(trident);
            p.getInventory().addItem(redDye);
            p.getInventory().addItem(yellowDye);
            p.getInventory().addItem(limeDye);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 1, false, false));
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            //p.getPlayer().teleport(new Location(map.getWorld(), 1, 26, 150, 90, 0));

            aceRacePlayerMap.put(p.getPlayer().getUniqueId(), new AceRacePlayer(p, this));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR && map.checkDeath(e.getPlayer().getLocation())) {
            e.getPlayer().teleport(map.respawns.getFirst());
            return;
        }

        // experimental: making players disappear for others if they are within 5 blocks
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
                        checker.addHiddenPlayer(mover);
                        player.hidePlayer(mover);
                        mover.hidePlayer(player);
                    }
                    else if(checker.checkHiddenPlayer(mover) && Math.sqrt(diffX*diffX + diffY*diffY + diffZ*diffZ) <= 8) {
                        player.hidePlayer(mover);
                        mover.hidePlayer(player);
                    }
                    else {
                        checker.removeHiddenPlayer(mover);
                        mover.showPlayer(player);
                        player.showPlayer(mover);
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


        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            //Location l = p.getLocation();
            //l.setPitch(-30);
            //Vector d = l.getDirection();
            //p.setVelocity(d.multiply(4));
            //p.setVelocity(new Vector(p.getVelocity().getX(), 1.65, p.getVelocity().getZ()));
            player.setCheckpoint();
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            //Location l = p.getLocation();
            //l.setPitch(-30);
            //Vector d = l.getDirection();
            //p.setVelocity(d.multiply(2.5));
            //p.setVelocity(new Vector(p.getVelocity().getX(), 1.25, p.getVelocity().getZ()));
            player.setCheckpoint();
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            player.setCheckpoint();
            //p.setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            //p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
            return;
        }
        if (e.getTo().getBlock().getType().toString().toLowerCase().contains("carpet")) {
            player.setCheckpoint();
        }
    }

    public AceRacePlayer getGamePlayer(Player p) {
        return aceRacePlayerMap.get(p.getUniqueId());
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
        
        //Bukkit.broadcastMessage("[Debug] fastestLaps.keySet().size() == " + fastestLaps.keySet().size());
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
        if (i.equals(Material.LEATHER_BOOTS)) e.setCancelled(true);
    }

       @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {

            AceRacePlayer p = getGamePlayer(e.getPlayer());
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.RED_DYE 
            || e.getPlayer().getInventory().getItemInMainHand().getType() == Material.YELLOW_DYE 
            || e.getPlayer().getInventory().getItemInMainHand().getType() == Material.LIME_DYE) {
                int time = p.cooldownTimer;
                if (time < timeRemaining) {
                    p.getPlayer().sendMessage(ChatColor.RED + "Please wait a moment before using an item again!");
                    e.setCancelled(true);
                    return;
                }
                else {
                    p.cooldownTimer = timeRemaining - 2;
                }
            }

            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.RED_DYE) firstCheckpoint(e.getPlayer());
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.YELLOW_DYE) lastCheckpoint(e.getPlayer());
            if (e.getPlayer().getInventory().getItemInMainHand().getType() == Material.LIME_DYE) nextCheckpoint(e.getPlayer());
            
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Set<Material> trapdoorList = Set.of(Material.OAK_TRAPDOOR, Material.DARK_OAK_TRAPDOOR, Material.SPRUCE_TRAPDOOR, Material.BIRCH_TRAPDOOR,
                                        Material.ACACIA_TRAPDOOR, Material.CHERRY_TRAPDOOR, Material.MANGROVE_TRAPDOOR, Material.JUNGLE_TRAPDOOR,
                                        Material.CRIMSON_TRAPDOOR, Material.WARPED_TRAPDOOR);
            if(trapdoorList.contains(e.getClickedBlock().getType())) e.setCancelled(true);
        }
    }

   @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        AceRacePlayer p = getGamePlayer(e.getPlayer());
        if (p == null) return; // new login; doesn't matter
        p.setPlayer(e.getPlayer());
    }
}
