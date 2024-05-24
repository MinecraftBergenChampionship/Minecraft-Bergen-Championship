package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.semoiB;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.text.SimpleDateFormat;
import java.util.*;

public class AceRace extends Game {
    // Change this to determine played map
    //public AceRaceMap map = new Biomes();
    public AceRaceMap map = new semoiB();
    public static World world = Bukkit.getWorld("AceRace");;
    public Map<UUID, AceRacePlayer> aceRacePlayerMap = new HashMap<UUID, AceRacePlayer>();
    public short[] finishedPlayersByLap = {0, 0, 0};
    public long startingTime;

    // keep track of top 5 fastest
    public SortedMap<Long, List<String>> fastestLaps = new TreeMap<Long, List<String>>();

    // SCORING VARIABLES
    public static final int FINISH_RACE_POINTS = 8;           // points for finishing the race
    public static final int PLACEMENT_LAP_POINTS = 1;         // points for placement for first laps
    public static final int LAP_COMPLETION_POINTS = 1;
    public static final int PLACEMENT_FINAL_LAP_POINTS = 4;   // points for placement for last lap
    public static final int[] PLACEMENT_BONUSES = {25, 15, 15, 10, 10, 5, 5, 5}; // points for Top 8 finishers
    public static final int TUTORIAL_TIME = 45;

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
                            "⑭ +8 points for finishing the course\n" +
                            "⑭ +4 points for every player beaten on the final lap\n" +
                            "⑭ Top 8 Bonuses- 1st:+25, 2nd,3rd:+15, 4th,5th:+10, 6th-8th:+5"
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
            p.getPlayer().teleport(new Location(map.getWorld(), -2158, 13, -2303, 90, 0));
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
                    createLine(6, ChatColor.GREEN.toString()+ChatColor.BOLD+"Lap: " + ChatColor.RESET+"1/3", p.getParticipant());
                }
                // something breaks like right here. idk why
                setGameState(GameState.END_ROUND);
                timeRemaining = 5;
            } else if (!finishedIntro && timeRemaining > 0 && timeRemaining % 7 == 0) {
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
                    p.getPlayer().teleport(new Location(map.getWorld(), -2158, 13, -2303, 90, 0));
                    //p.getPlayer().teleport(new Location(map.getWorld(), 2, 26, 150, 90, 0));
                    p.checkpoint = 0;
                }
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
                if (timeRemaining == 10) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_11, SoundCategory.RECORDS, 1, 1);
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                map.setBarriers(false);
                //setPVP(true);
                timeRemaining = 720;
                startingTime = System.currentTimeMillis();
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
        } else if (getState().equals(GameState.END_GAME)) {
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

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            //p.getInventory().addItem(trident);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 1, false, false));
            //p.getPlayer().teleport(new Location(map.getWorld(), 1, 26, 150, 90, 0));

            aceRacePlayerMap.put(p.getPlayer().getUniqueId(), new AceRacePlayer(p, this));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR && map.checkDeath(e.getPlayer().getLocation())) {
            e.getPlayer().teleport(map.respawns.get(0));
            return;
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
    public void onReconnect(PlayerJoinEvent e) {
        AceRacePlayer p = getGamePlayer(e.getPlayer());
        if (p == null) return; // new login; doesn't matter
        p.setPlayer(e.getPlayer());
    }
}
