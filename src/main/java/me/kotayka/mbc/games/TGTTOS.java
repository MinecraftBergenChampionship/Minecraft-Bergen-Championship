package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.tgttosMap.TGTTOSMap;
import me.kotayka.mbc.gameMaps.tgttosMap.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TGTTOS extends Game {
    private int roundNum = 0;
    private static final int MAX_ROUNDS = 6;
    private TGTTOSMap map = null;
    private List<TGTTOSMap> maps = new ArrayList<>(
            Arrays.asList(new Pit(), new Meatball(), new Walls(),
                          new Cliffs(), new Glide(), new Skydive(),
                          new Boats()
            ));

    private List<Participant> finishedParticipants;
    private String[] deathMessages = new String[39];
    private static final ItemStack SHEARS = new ItemStack(Material.SHEARS);

    public TGTTOS() {
        super(2, "TGTTOS");
    }

    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.AQUA + "" + ChatColor.BOLD + "Game: "+ MBC.getInstance().gameNum+"/6:" + ChatColor.WHITE + " TGTTOS", p);

        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void events() {
        if (getState().equals(GameState.STARTING)) {
           if (timeRemaining == 0) {
               for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
               }
               map.Barriers(false);
               setGameState(GameState.ACTIVE);
               timeRemaining = 120;
           } else {
               Countdown();
           }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                for (Participant p : MBC.getInstance().getPlayers()) {
                    if (!finishedParticipants.contains(p)) {
                        winEffects(p); // just for the flying
                        p.getPlayer().sendMessage(ChatColor.RED+"You didn't finish in time!");
                    }
                }

                if (roundNum == MAX_ROUNDS) {
                    setGameState(GameState.END_GAME);
                    timeRemaining = 37;
                } else {
                    setGameState(GameState.END_ROUND);
                    timeRemaining = 5;
                }
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                startRound();
            } else if (timeRemaining == 5) {
                roundOverGraphics();
            }
        } else if (getState().equals(GameState.END_GAME)) {
            gameEndEvents();
        }
    }

    public void start() {
        super.start();

        ItemMeta meta = SHEARS.getItemMeta();
        meta.setUnbreakable(true);
        SHEARS.setItemMeta(meta);

        setDeathMessages();
        //setGameState(GameState.TUTORIAL);
        setGameState(GameState.STARTING);

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 10, false, false));
        }

        startRound();
    }

    /**
     * Moved from startRound()
     * repurpose loadPlayers() however is best needed for tgttos
     */
    public void loadPlayers() {
        if (map == null) {
            return;
        }
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            p.getPlayer().setVelocity(new Vector(0,0,0));
            p.getPlayer().teleport(map.getSpawnLocation());

            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }

            if (p.getPlayer().hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }

            if (map instanceof Meatball) {
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            }

            if (map.getItems() == null) continue;

            Bukkit.broadcastMessage("Map items: " + map.getItems().toString());
            for (ItemStack i : map.getItems()) {
                if (i.getType().equals(Material.WHITE_WOOL)) {
                    ItemStack wool = p.getTeam().getColoredWool();
                    wool.setAmount(64);
                    p.getInventory().addItem(wool);
                    p.getInventory().addItem(new ItemStack(Material.SHEARS));
                }
            }
        }
    }

    /**
     * Resets variables and map for next round
     * If at maximum rounds, ends the game
     */
    public void startRound() {
        if (roundNum == MAX_ROUNDS) {
            setGameState(GameState.END_GAME);
            timeRemaining = 37;
            return;
        } else {
            roundNum++;
        }

        finishedParticipants = new ArrayList<>(MBC.getInstance().players.size());
        TGTTOSMap newMap = maps.get((int) (Math.random()*maps.size()));
        map = newMap;
        maps.remove(newMap);
        map.Barriers(true);

        createLine(23, ChatColor.AQUA + "" + ChatColor.BOLD + "Round: "+ roundNum+"/6: " + ChatColor.WHITE + map.getName());

        if (map != null) {
            loadPlayers();
        }
        map.getWorld().spawnEntity(map.getEndLocation(), EntityType.CHICKEN);

        setGameState(GameState.STARTING);
        setTimer(20);
    }

    private void setDeathMessages() {
        try {
            FileReader fr = new FileReader("tgttos_death_messages.txt");
            BufferedReader br = new BufferedReader(fr);
            int i = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                deathMessages[i] = line;
                i++;
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            Bukkit.broadcastMessage(ChatColor.RED+"Error: " + e.getMessage());
        }
    }

    private void printDeathMessage(Participant p) {
        int rand = (int) (Math.random() * deathMessages.length);
        Bukkit.broadcastMessage(ChatColor.GRAY+deathMessages[rand].replace("{player}", p.getFormattedName() + ChatColor.GRAY));
    }

    private void Countdown() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (timeRemaining <= 10 && timeRemaining > 3) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">"+timeRemaining+"<", 0,20,0);
            } else if (timeRemaining == 3) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">"+ChatColor.RED+""+ChatColor.BOLD+ timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 2) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">"+ChatColor.YELLOW+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            } else if (timeRemaining == 1) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">"+ChatColor.GREEN+""+ChatColor.BOLD + timeRemaining+ChatColor.WHITE+""+ChatColor.BOLD+"<", 0,20,0);
            }
        }
    }

    /**
     * Custom Shears for unbreaking
     * @return unbreakable shears
     */
    public static ItemStack getShears() {
        return SHEARS;
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;
        if (map == null) return;

        if (e.getPlayer().getLocation().getY() < map.getDeathY()) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().teleport(map.getSpawnLocation());
            printDeathMessage(Participant.getParticipant(e.getPlayer()));
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!isGameActive()) return;

        if (!(event.getBlock().getType().toString().endsWith("WOOL"))) {
            event.setCancelled(true);
        }

        event.setDropItems(false);
    }

    public void chickenClick(Participant p, Entity chicken) {
        p.addRoundScore(MBC.getInstance().getPlayers().size()-finishedParticipants.size());
        finishedParticipants.add(p);
        String place = getPlace(finishedParticipants.size());
        chicken.remove();
        Bukkit.broadcastMessage(p.getTeam().getChatColor()+p.getPlayerName()+ChatColor.WHITE+" finished in "+ChatColor.AQUA+place+"!");
        MBC.spawnFirework(p);
        p.getPlayer().setGameMode(GameMode.SPECTATOR);
        p.getPlayer().sendMessage(ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GREEN+" place!");

        if (finishedParticipants.size() == MBC.getInstance().getPlayers().size()) {
            setGameState(GameState.END_ROUND);
            timeRemaining = 5;
        }
    }

    @EventHandler
    public void chickenLeftClick(EntityDamageByEntityEvent event) {
        if (!isGameActive()) return;

        if (event.getEntity() instanceof Chicken && event.getDamager() instanceof Player) {
            if (((Player) event.getDamager()).getGameMode() != GameMode.SURVIVAL)
                event.setCancelled(true);
            else
                chickenClick(Participant.getParticipant((Player) event.getDamager()), event.getEntity());
        }
    }

    @EventHandler
    public void chickenRightClick(PlayerInteractEntityEvent event) {
        if (!isGameActive()) return;

        if (event.getRightClicked() instanceof Chicken) {
            chickenClick(Participant.getParticipant(event.getPlayer()), event.getRightClicked());
        }
    }

    @EventHandler
    public void boatExit(VehicleExitEvent event) {
        if (!isGameActive()) return;

        if (event.getVehicle() instanceof Boat && event.getExited() instanceof Player) {
            Boat boat = (Boat) event.getVehicle();
            boat.remove();

            ItemStack boatItem = new ItemStack(Material.OAK_BOAT);
            Player p = (Player) event.getExited();
            p.getInventory().addItem(boatItem);
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (!isGameActive()) return;

        if (event.getBlock().getType().toString().endsWith("WOOL")) {
            ItemStack i = new ItemStack(event.getItemInHand());
            i.setAmount(1);
            MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, () -> event.getPlayer().getInventory().addItem(i), 20);
        }
    }
}
