package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.Biomes;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
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
    public List<AceRacePlayer> aceRacePlayerList = new ArrayList<>();
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
        super(1, "Ace Race");
    }

    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.AQUA + "" + ChatColor.BOLD + "Game: "+MBC.getInstance().gameNum+"/6:" + ChatColor.WHITE + " Ace Race", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void start() {
        super.start();
        setGameState(GameState.TUTORIAL);
        setTimer(10); //debug
        //setTimer(180);

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Starting Practice Time!");

        //loadPlayers();
        //createScoreboard();

        for (AceRacePlayer p : aceRacePlayerList) {
            p.getPlayer().sendTitle(ChatColor.GOLD+""+ChatColor.BOLD+"Practice Starting!", "", 20, 60, 20);
        }
    }

    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 60) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.RED + "One minute left of practice!");
            } else if (timeRemaining <= 0) {
                Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.YELLOW + "Practice Over!");
                for (AceRacePlayer p : aceRacePlayerList) {
                    p.getPlayer().sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Practice Over!", "", 0, 60, 20);
                }
                setGameState(GameState.END_ROUND);
                timeRemaining = 5;
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining <= 0) {
                map.setBarriers(true);
                for (AceRacePlayer p : aceRacePlayerList) {
                    p.getPlayer().teleport(new Location(map.getWorld(), 2, 26, 150, 90, 0));
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
                timeRemaining = 600;
                startingTime = System.currentTimeMillis();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 30) {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "30 seconds remaining!");
                // TODO: play overtime music (or if there are like 2 players still racing: tbd)
            } else if (timeRemaining <= 0) {
                for (AceRacePlayer p : aceRacePlayerList) {
                    gameOverGraphics(p);
                    if (!(p.getPlayer().getGameMode().equals(GameMode.SPECTATOR))) {
                        winEffects(p.getParticipant()); // this is just for the effects. players not in spectator by the end of the round have lost.
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
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 10, false, false)); // can probably use scoreboard teams but until then
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().teleport(new Location(map.getWorld(), 1, 26, 150, 90, 0));

            aceRacePlayerList.add(new AceRacePlayer(p, this));
        }
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (MBC.getInstance().getGameID() != this.gameID) return;

        if (map.checkDeath(e.getPlayer().getLocation())) {
            AceRacePlayer player = ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer()));
            int checkpoint = player.checkpoint;
            e.getPlayer().teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength-1 : checkpoint-1));
            e.getPlayer().setFireTicks(0);
        }

        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).setCheckpoint();
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
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).setCheckpoint();
        }
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
}
