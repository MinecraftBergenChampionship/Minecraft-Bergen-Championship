package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.Biomes;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.teams.Spectator;
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
    public AceRaceMap map = new Biomes();
    public static World world = Bukkit.getWorld("AceRace");;
    public Map<UUID, AceRacePlayer> aceRacePlayerMap = new HashMap<UUID, AceRacePlayer>();
    public short[] finishedPlayersByLap = {0, 0, 0};
    public long startingTime;

    // keep track of top 5 fastest
    public SortedMap<Long, List<String>> fastestLaps = new TreeMap<Long, List<String>>();

    // SCORING VARIABLES
    public static final int FINISH_RACE_POINTS = 8;           // points for finishing the race
    public static final int PLACEMENT_LAP_POINTS = 1;         // points for placement for first laps
    public static final int PLACEMENT_FINAL_LAP_POINTS = 4;   // points for placement for last lap
    public static final int[] PLACEMENT_BONUSES = {25, 15, 15, 10, 10, 5, 5, 5}; // points for Top 8 finishers

    public AceRace() {
        super("Ace Race");
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

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting Practice Time!");

        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.reset();
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getPlayer().sendTitle(ChatColor.GOLD+""+ChatColor.BOLD+"Practice Starting!", "", 20, 60, 20);
        }
        setTimer(180);
    }

    @Override
    public void onRestart() {
        for (AceRacePlayer p : aceRacePlayerMap.values()) {
            p.reset();
        }
    }

    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 60) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "One minute left of practice!");
            } else if (timeRemaining <= 0) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + "Practice Over!");
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    p.getPlayer().setVelocity(new Vector(0,0,0));
                    p.getPlayer().removePotionEffect(PotionEffectType.SPEED);
                    p.getPlayer().sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Practice Over!", "", 0, 60, 20);
                    createLine(6, ChatColor.GREEN.toString()+ChatColor.BOLD+"Lap: " + ChatColor.RESET+"1/3", p.getParticipant());
                }
                setGameState(GameState.END_ROUND);
                timeRemaining = 5;
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining <= 0) {
                map.setBarriers(true);
                for (AceRacePlayer p : aceRacePlayerMap.values()) {
                    p.getPlayer().teleport(new Location(map.getWorld(), 2, 26, 150, 90, 0));
                    p.checkpoint = 0;
                }
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
            } else {
                setGameState(GameState.ACTIVE);
                map.setBarriers(false);
                setPVP(true);
                timeRemaining = 600;
                startingTime = System.currentTimeMillis();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "30 seconds remaining!");
                // TODO: play overtime music (or if there are like 2 players still racing: tbd)
            } else if (timeRemaining <= 0) {
                gameOverGraphics();
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
            p.getInventory().addItem(trident);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().teleport(new Location(map.getWorld(), 1, 26, 150, 90, 0));

            aceRacePlayerMap.put(p.getPlayer().getUniqueId(), new AceRacePlayer(p, this));
        }
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (Participant.getParticipant(e.getPlayer()).getTeam() instanceof Spectator) return;

        AceRacePlayer player = getGamePlayer(e.getPlayer());

        if (map.checkDeath(e.getPlayer().getLocation())) {
            int checkpoint = player.checkpoint;
            e.getPlayer().teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength-1 : checkpoint-1));
            e.getPlayer().setFireTicks(0);
        }

        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            player.setCheckpoint();
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
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
