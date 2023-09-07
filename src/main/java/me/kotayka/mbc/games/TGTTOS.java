package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.tgttosMap.TGTTOSMap;
import me.kotayka.mbc.gameMaps.tgttosMap.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
import java.util.*;

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
    private String[] deathMessages = new String[44];
    private List<Location> placedBlocks = new ArrayList<Location>(20);
    private boolean firstTeamBonus = false;  // determine whether or not a full team has completed yet
    private boolean secondTeamBonus = false;

    // Scoring
    public static int PLACEMENT_POINTS = 1; // awarded multiplied by the amount of players who havent finished yet
    public static int COMPLETION_POINTS = 1; // awarded for completing the course
    public static int FIRST_TEAM_BONUS = 5; // awarded per player on team
    public static int SECOND_TEAM_BONUS = 3; // awarded per player on team
    public static int TOP_THREE_BONUS = 5; // bonus for placing top 3

    public TGTTOS() {
        super("TGTTOS");
    }

    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updateInGameTeamScoreboard();
    }

    /**
     * Update scoreboard display on how many players have finished the round
     */
    public void updateFinishedPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            createLine(2, ChatColor.YELLOW + "Finished: " + ChatColor.WHITE + finishedParticipants.size() + "/" + MBC.getInstance().getPlayers().size(), p);
        }
    }

    public void events() {
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                map.Barriers(false);
                setPVP(true);
                setGameState(GameState.ACTIVE);
                timeRemaining = 120;
            } else {
                Countdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining == 0) {
                for (Participant p : MBC.getInstance().getPlayers()) {
                    if (!finishedParticipants.contains(p)) {
                        flightEffects(p);
                        p.getPlayer().sendMessage(ChatColor.RED + "You didn't finish in time!");
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
            } else if (timeRemaining == 4) {
                roundOverGraphics();
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                removePlacedBlocks();
            }
            gameEndEvents();
        }
    }

    public void start() {
        super.start();

        setDeathMessages();
        //setGameState(GameState.TUTORIAL);
        setGameState(GameState.STARTING);

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
        }

        startRound();
    }

    @Override
    public void onRestart() {
        roundNum = 0;
        maps = new ArrayList<>(
                Arrays.asList(new Pit(), new Meatball(), new Walls(),
                        new Cliffs(), new Glide(), new Skydive(), new Boats()
                )
        );
        removePlacedBlocks();
    }

    /**
     * Moved from startRound()
     * repurpose loadPlayers() however is best needed for tgttos
     */
    public void loadPlayers() {
        setPVP(false);
        if (map == null) {
            return;
        }

        getLogger().log(ChatColor.AQUA.toString() + ChatColor.BOLD + "New Map: " + ChatColor.WHITE + map.getName());

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            p.getPlayer().setVelocity(new Vector(0, 0, 0));
            p.getPlayer().teleport(map.getSpawnLocation());
            if (p.getPlayer().hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            if (map instanceof Meatball) {
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            }
        }

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);

            if (p.getPlayer().getAllowFlight()) {
                removeWinEffect(p);
            }

            if (map.getItems() == null) continue;

            for (ItemStack i : map.getItems()) {
                if (i.getType().equals(Material.WHITE_WOOL)) {
                    ItemStack wool = p.getTeam().getColoredWool();
                    wool.setAmount(64);
                    p.getInventory().addItem(wool);
                } else if (i.getType().equals(Material.SHEARS)) {
                    ItemMeta meta = i.getItemMeta();
                    meta.setUnbreakable(true);
                    i.setItemMeta(meta);
                    p.getInventory().addItem(i);
                } else if (i.getType().equals(Material.LEATHER_BOOTS)) {
                    p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(i));
                } else {
                    p.getInventory().addItem(i);
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

        firstTeamBonus = false;
        secondTeamBonus = false;

        finishedParticipants = new ArrayList<>(MBC.getInstance().players.size());
        map = maps.get((int) (Math.random() * maps.size()));
        maps.remove(map);
        map.Barriers(true);

        createLineAll(22, ChatColor.AQUA + "" + ChatColor.BOLD + "Map: " + ChatColor.RESET + map.getName());
        createLineAll(21, ChatColor.GREEN + "Round: " + ChatColor.RESET + roundNum + "/6");
        updateFinishedPlayers();

        if (map != null) {
            loadPlayers();
        }
        for (int i = 0; i < MBC.getInstance().getPlayers().size(); i++) {
            map.getWorld().spawnEntity(map.getEndLocation(), EntityType.CHICKEN);
        }

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
            Bukkit.broadcastMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
    }

    private void printDeathMessage(Participant p) {
        int rand = (int) (Math.random() * deathMessages.length);
        Bukkit.broadcastMessage(ChatColor.GRAY + deathMessages[rand].replace("{player}", p.getFormattedName() + ChatColor.GRAY));
    }

    private void Countdown() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (timeRemaining <= 10 && timeRemaining > 3) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">" + timeRemaining + "<", 0, 20, 0);
            } else if (timeRemaining == 3) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">" + ChatColor.RED + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 2) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">" + ChatColor.YELLOW + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            } else if (timeRemaining == 1) {
                p.sendTitle(ChatColor.AQUA + "Chaos begins in:", ChatColor.BOLD + ">" + ChatColor.GREEN + "" + ChatColor.BOLD + timeRemaining + ChatColor.WHITE + "" + ChatColor.BOLD + "<", 0, 20, 0);
            }
        }
    }

    private void removePlacedBlocks() {
        for (Location l : placedBlocks) {
            if (!l.getBlock().getType().equals(Material.AIR)) {
                l.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;
        if (map == null) return;
        if (!e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;

        if (e.getPlayer().getLocation().getY() < map.getDeathY()) {
            e.getPlayer().setVelocity(new Vector(0, 0, 0));
            e.getPlayer().teleport(map.getSpawnLocation());
            printDeathMessage(Participant.getParticipant(e.getPlayer()));
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!isGameActive()) return;

        if (!(event.getBlock().getType().toString().endsWith("WOOL"))) {
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);

        if (placedBlocks.contains(event.getBlock().getLocation())) placedBlocks.remove(event.getBlock().getLocation());
    }

    private void checkTeamFinish(Participant p) {
        int count = 0;
        for (Participant teammate : p.getTeam().getPlayers()) {
            if (teammate.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
                count++;
        }
        if (count == p.getTeam().getPlayers().size()) {
            if (!firstTeamBonus) {
                Bukkit.broadcastMessage(p.getTeam().teamNameFormat() + ChatColor.GREEN + "" + ChatColor.BOLD + " was the first full team to finish!");
                for (Participant teammate : p.getTeam().getPlayers()) {
                    teammate.addCurrentScore(FIRST_TEAM_BONUS);
                }
                firstTeamBonus = true;
            } else {
                Bukkit.broadcastMessage(p.getTeam().teamNameFormat() + ChatColor.GREEN + "" + ChatColor.BOLD + " was the second full team to finish!");
                for (Participant teammate : p.getTeam().getPlayers()) {
                    teammate.addCurrentScore(SECOND_TEAM_BONUS);
                }
                secondTeamBonus = true;
            }
        }
    }

    public void chickenClick(Participant p, Entity chicken) {
        finishedParticipants.add(p);
        int placement = finishedParticipants.size();
        p.addCurrentScore(PLACEMENT_POINTS * (MBC.getInstance().getPlayers().size() - placement) + COMPLETION_POINTS);
        String place = getPlace(placement);
        if (placement < 4) {
            p.addCurrentScore(TOP_THREE_BONUS);
        }
        chicken.remove();
        String finish = p.getFormattedName() + ChatColor.WHITE + " finished in " + ChatColor.AQUA + place + "!";

        getLogger().log(finish);

        Bukkit.broadcastMessage(finish);

        MBC.spawnFirework(p);
        p.getPlayer().setGameMode(GameMode.SPECTATOR);
        p.getPlayer().sendMessage(ChatColor.GREEN + "You finished in " + ChatColor.AQUA + place + ChatColor.GREEN + " place!");

        // check if all players on a team finished
        if (!firstTeamBonus || !secondTeamBonus)
            checkTeamFinish(p);

        updateFinishedPlayers();

        if (finishedParticipants.size() == MBC.getInstance().getPlayers().size()) {
            if (roundNum != MAX_ROUNDS) {
                setGameState(GameState.END_ROUND);
                timeRemaining = 5;
            } else {
                setGameState(GameState.END_GAME);
                timeRemaining = 37;
            }
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the interaction is placing a boat
        if (event.getItem() != null && event.getItem().getType() == Material.OAK_BOAT && !getState().equals(GameState.ACTIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (!isGameActive()) return;

        Player p = e.getPlayer();

        if (e.getBlock().getType().toString().endsWith("WOOL")) {
            // add to placed blocks
            placedBlocks.add(e.getBlock().getLocation());
            // if block was wool, give appropriate amount back
            String wool = e.getBlock().getType().toString();
            // check item slot
            int index = p.getInventory().getHeldItemSlot();
            if (p.getInventory().getItem(40) != null && p.getInventory().getItem(index) == null) {
                if (Objects.requireNonNull(p.getInventory().getItem(40)).getType().toString().equals(wool)) {
                    int amt;
                    // "some wacky bullshit prevention" - me several months ago
                    if (Objects.requireNonNull(p.getInventory().getItem(40)).getAmount() + 63 > 100) {
                        amt = 64;
                    } else {
                        amt = Objects.requireNonNull(p.getInventory().getItem(40)).getAmount() + 63;
                    }
                    p.getInventory().setItem(40, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
                }
            } else if (p.getInventory().getItem(index).getType().toString().equals(wool)) {
                int amt = p.getInventory().getItem(index).getAmount();
                p.getInventory().setItem(index, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        // deal slight knockback to other players
        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            Player p = (Player) e.getHitEntity();
            snowballHit((Snowball) e.getEntity(), p);
        }
    }
}
