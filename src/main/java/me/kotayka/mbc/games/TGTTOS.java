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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;
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
                    new Cliffs(), new Elytra(), //new Skydive(),
                    new Boats() //, new Glide()
            ));

    private List<Participant> finishedParticipants;
    private String[] deathMessages = new String[52];
    private List<Location> placedBlocks = new ArrayList<Location>(20);
    private boolean firstTeamBonus = false;  // determine whether or not a full team has completed yet
    private boolean secondTeamBonus = false;

    // Scoring
    public static int PLACEMENT_POINTS = 1; // awarded multiplied by the amount of players who havent finished yet
    public static int COMPLETION_POINTS = 1; // awarded for completing the course
    public static int FIRST_TEAM_BONUS = 4; // awarded per player on team
    public static int SECOND_TEAM_BONUS = 2; // awarded per player on team
    public static int TOP_THREE_BONUS = 5; // bonus for placing top 3

    public TGTTOS() {
        super("TGTTOS", new String[] {
                "Why did the MBC Player cross the road? To get to the other side!!!",
                "To get to the other side, or TGTTOS, is a game about, well, getting to the other side of a map.",
                "Make sure to look up, down, left and right; the finish could basically be anywhere.",
                "Also make sure to look at the items in your inventory, as these could be the key to finishing ahead of the competition if used right.",
                "Once you do get to the end of the map, punch or right click a chicken to finish (I hope you're reading, Jeremy).",
                "Points are counted for however many people you finish ahead of, and extra points are given to the first full team to finish the course.",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "- +1 point for completing the course\n" +
                        "- +1 point for every player outplaced\n" +
                        "- +4 points for each player on the first full team to finish a course\n" +
                        "- +2 points for each player on the second full team to finish a course\n" +
                        "- +5 bonus points for placing Top 3 in a course\n"
        });
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
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
                }
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
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
            if (timeRemaining == 36) {
                gameOverGraphics();
            } else if (timeRemaining == 0) {
                removePlacedBlocks();
            }
            gameEndEvents();
        }
    }

    public void start() {
        super.start();

        setDeathMessages();

        startFirstRound();

        setGameState(GameState.TUTORIAL);

        setTimer(53);
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
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            if (p.getTeam().equals(MBC.getInstance().spectator)) {
                p.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
            if (p.getPlayer().hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            if (map instanceof Meatball) {
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            }
        }
    }

    private void startFirstRound() {
        roundNum++;

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

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            map.getWorld().spawnEntity(map.getEndLocation(), EntityType.CHICKEN);

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

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().getInventory().clear();
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            map.getWorld().spawnEntity(map.getEndLocation(), EntityType.CHICKEN);

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

        if (e.getPlayer().getLocation().getY() < map.getDeathY()) {
            if (e.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                e.getPlayer().teleport(map.getSpawnLocation());
                return;
            }
            if (map instanceof Boats) {
                e.getPlayer().getInventory().addItem(new ItemStack(Material.OAK_BOAT));
            }
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
                    teammate.getPlayer().sendMessage(ChatColor.GREEN+"Your team finished first and earned a " + (FIRST_TEAM_BONUS*MBC.getInstance().multiplier*p.getTeam().getPlayers().size()) + " point bonus!");
                }
                firstTeamBonus = true;
            } else {
                Bukkit.broadcastMessage(p.getTeam().teamNameFormat() + ChatColor.GREEN + "" + ChatColor.BOLD + " was the second full team to finish!");
                for (Participant teammate : p.getTeam().getPlayers()) {
                    teammate.addCurrentScore(SECOND_TEAM_BONUS);
                    teammate.getPlayer().sendMessage(ChatColor.GREEN+"Your team finished second and earned a " + (SECOND_TEAM_BONUS*MBC.getInstance().multiplier*p.getTeam().getPlayers().size()) + " point bonus!");
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

        if (event.getRightClicked() instanceof Chicken && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            chickenClick(Participant.getParticipant(event.getPlayer()), event.getRightClicked());
        }
    }

    @EventHandler
    public void boatExit(VehicleExitEvent e) {
        if (!isGameActive()) return;
        if (!(e.getVehicle() instanceof Boat)) return;
        if (!(e.getExited() instanceof Player)) return;

        Boat boat = (Boat) e.getVehicle();
        if (boat.getPassengers().size() > 1) {
            for (Entity en : boat.getPassengers()) {
                Location l = en.getLocation().add(0, 1, 0);
                en.teleport(l);

                // give boat to all players
                if (en instanceof Player) {
                    ((Player) en).getInventory().addItem(new ItemStack(Material.OAK_BOAT));
                }
            }
        } else {
            ((Player) e.getExited()).getInventory().addItem(new ItemStack(Material.OAK_BOAT));
        }
        boat.remove();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the interaction is placing a boat or throwing meatball
        if (event.getItem() != null && (event.getItem().getType() == Material.OAK_BOAT || event.getItem().getType() == Material.SNOWBALL) && !getState().equals(GameState.ACTIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (!isGameActive()) {
            e.setCancelled(true);
        }

        Player p = e.getPlayer();

        if (e.getBlock().getType().toString().endsWith("WOOL")) {
            // if block was placed too close to spawn, don't place it (only for Meatball+Skydive for now)
            if ((map instanceof Meatball || map instanceof Skydive) && e.getBlock().getLocation().distanceSquared(map.getSpawnLocation()) < 9) {
                p.sendMessage(ChatColor.RED+"Move further away from spawn before building!");
                e.setCancelled(true);
                return;
            }

            // add to placed blocks
            placedBlocks.add(e.getBlock().getLocation());
            // if block was wool, give appropriate amount back
            String wool = e.getBlock().getType().toString();
            // check item slot
            if (e.getHand() == EquipmentSlot.HAND) {
                int index = p.getInventory().getHeldItemSlot();
                int amt = Objects.requireNonNull(p.getInventory().getItem(index)).getAmount();
                p.getInventory().setItem(index, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
                return;
            }
            if (e.getHand() == EquipmentSlot.OFF_HAND) {
                int amt = Objects.requireNonNull(p.getInventory().getItem(40)).getAmount();
                p.getInventory().setItem(40, new ItemStack(Objects.requireNonNull(Material.getMaterial(wool)), amt));
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
