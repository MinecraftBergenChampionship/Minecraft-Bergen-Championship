package me.kotayka.mbc.games;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.sgMaps.JesuscraftTwo;
import me.kotayka.mbc.gameMaps.sgMaps.SurvivalGamesMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

public class SurvivalGames extends Game {
    private final SurvivalGamesMap map = new JesuscraftTwo();
    private WorldBorder border = null;
    private List<SurvivalGamesItem> items;

    // This file must be in /home/MBCAdmin/MBC/ and re-added each time an update is made
    private final File CHEST_FILE = new File("survival_games_items.json");
    private final File SUPPLY_FILE = new File("supply_crate_items.json");
    private SurvivalGamesEvent event = SurvivalGamesEvent.GRACE_OVER;
    private Map<Player, Integer> playerKills = new HashMap<>();
    private Map<MBCTeam, Integer> teamPlacements = new HashMap<>();
    private int deadTeams = 0; // just to avoid sync issues w/teamsAlive.size()
    private boolean firstRound = true;
    private Map<Player, Double> playerDamage = new HashMap<>();

    private Map<MBCTeam, Horcrux> horcruxMap = new HashMap<>();
    private List<Horcrux> horcruxList = new ArrayList<>();

    // Enchantment
    private final GUIItem[] guiItems = setupGUIItems();

    private BossBar bossBar;

    // SCORING
    public final int KILL_POINTS_INITIAL = 15;
    public int killPoints = KILL_POINTS_INITIAL;
    public final int SURVIVAL_POINTS = 1;
    // Shared amongst each team: 12, 10, 9, 7, 6, 5 points for each player
    public final int[] TEAM_BONUSES_4 = {48, 40, 36, 28, 24, 20};
    public final int[] TEAM_BONUSES_3 = {36, 30, 27, 21, 18, 15};
    private double totalDamage = 0;
    // public final int WIN_POINTS = 36; // shared amongst all remaining players

    public SurvivalGames() {
        super("SurvivalGames", new String[] {
                "⑬ " + ChatColor.BOLD + "In a world..." + ChatColor.RESET + "spawn in with nothing, collect items through chests, emerge the last team standing!\n\n" + 
                "⑬ There will be a short grace period.",
                "⑬ Purchase items at the enchant table, drag and click books to enchant!\n\n" + 
                "⑬ Additionally, watch out for the border, " + ChatColor.BOLD + " you will not be able to break glass or open doors behind the border.",
                "⑬ Live as long as you can, and place your teams horcrux for one revive!\n\n" +
                "⑬ There are two rounds, so remember who killed you...",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑬ +5-15 points for eliminations\n" +
                        "⑬ +150 points split based on % of damage dealt\n" +
                        "⑬ +1 points for every player outlived\n" +
                        "⑬ Team Bonuses (split amongst team):\n" +
                        "     ⑬ 1st: +12 points, 2nd: +10 points, 3rd: +9 points\n" +
                        "     ⑬ 4th: +7 points, 5th: +6 points, 6th: +5 points"
        });

        for (MBCTeam t : getValidTeams()) {
            teamPlacements.put(t, getValidTeams().size());
        }

        try {
            readItems();
        } catch(IOException | ParseException e) {
            Bukkit.broadcastMessage(ChatColor.YELLOW+ e.getMessage());
            Bukkit.broadcastMessage(ChatColor.RED+"Unable to parse " + CHEST_FILE.getAbsolutePath() + " or " + SUPPLY_FILE.getAbsolutePath());
        }
    }

    private void readItems() throws IOException, ParseException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<SurvivalGamesItem>>() {}.getType();
        Reader reader = new FileReader(CHEST_FILE);
        items = gson.fromJson(reader, listType);
        reader.close();
    }

    @Override
    public void loadPlayers() {
        for (Horcrux h : horcruxList) {
            h.inUse = false;
            h.placed = false;
            h.used = false;
            if (h.armorStand != null) {
                h.armorStand.remove();
                h.armorStand = null;
            }
        }
        totalDamage = 0;

        if (bossBar != null) {
            bossBar.setVisible(false);
        }

        setPVP(false);
        deadTeams = 0;
        killPoints = KILL_POINTS_INITIAL;
        // possibly redundant
        if (!teamsAlive.isEmpty()) {
            teamsAlive.clear();
        }
        teamsAlive.addAll(getValidTeams());
        map.setBarriers(true);

        map.spawnPlayers();
        if (!playersAlive.isEmpty()) {
            playersAlive.clear();
        }
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
            playersAlive.add(p);
            playerDamage.put(p.getPlayer(), 0.0);
            p.getPlayer().getInventory().clear();
            ItemStack endCrystal = new ItemStack(Material.END_CRYSTAL);

            ItemMeta meta = endCrystal.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("Horcrux");
                endCrystal.setItemMeta(meta);
            }

            p.getInventory().setItem(8, endCrystal);
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 10, false, false));
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
            if (map.type.equals("Elytra")) {
                p.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
            }
        }
        updatePlayersAliveScoreboard();
        regenChest();
    }

    @Override
    public void start() {
        super.start();

        for (MBCTeam t : getValidTeams()) {
            Horcrux h = new Horcrux(t);
            horcruxMap.put(t, h);
            horcruxList.add(h);
        }

        for (ItemFrame i : map.getWorld().getEntitiesByClass(ItemFrame.class)) {
            i.setFixed(true);
        }

         setGameState(GameState.TUTORIAL);
        //setGameState(GameState.STARTING);

        setTimer(30);
    }

    @Override
    public void onRestart() {
        teamPlacements.clear();
        playerKills.clear();
        //crateNum = 0;
        deadTeams = 0;
        killPoints = KILL_POINTS_INITIAL;
        if (bossBar != null) {
            bossBar.setVisible(false);
        }

        for (Horcrux h : horcruxList) {
            h.inUse = false;
            h.placed = false;
            h.used = false;

            if (h.armorStand != null) {
                h.armorStand.remove();
                h.armorStand = null;
            }
        }

        for (Participant p : MBC.getInstance().getPlayers()) {
            playerDamage.put(p.getPlayer(), 0.0);
        }

        map.resetMap();
    }

    @Override
    public void createScoreboard(Participant p) {
        //createLineAll(21, ChatColor.AQUA+""+ChatColor.BOLD+"Map: " + ChatColor.RESET+ map.mapName);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        updatePlayersAliveScoreboard();
        createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+"0", p);

        updateInGameTeamScoreboard();
    }

    @Override
    public void events() {
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                MBC.getInstance().sendMutedMessages();
                Bukkit.broadcastMessage("\n" + MBC.MBC_STRING_PREFIX + "The game is starting!\n");
                setGameState(GameState.STARTING);
                timeRemaining = 15;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
                if (timeRemaining == 10) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS, 1, 1);
                    }
                }
            } else {
                map.setBarriers(false);
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Grace ends in 30 seconds!");
                bossBar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "GRACE PERIOD", BarColor.PURPLE, BarStyle.SOLID);
                bossBar.setVisible(true);
                for (Participant p : MBC.getInstance().getPlayers()) {
                    bossBar.addPlayer(p.getPlayer());
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                    p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                    p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                }
                setGameState(GameState.ACTIVE);
                timeRemaining = 450;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (map.hasElevationBorder && timeRemaining < 50 && timeRemaining % 2 == 0) {
                map.Border();
            }
            decrementBossBar();
            checkHorcruxes();
            /*
             * Event timeline:
             *  7:30: Game Starts
             *  7:00: Grace Ends, border starts to move; kills worth 10; elytra removed for elytra maps
             *  6:00: 1 minute to chest refill, 1st supply crate announced
             *  5:00: Chest Refill, 1st supply crate drops, 2nd supply crate announced; kills worth 8
             *  4:00: 2nd supply crate drops, 3rd supply crate announced
             *  3:00: Last supply crate lands; kills worth 5
             */
            if (timeRemaining == 0) {
                if (teamsAlive.size() > 1) {
                    Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Border shrinking!");
                    map.Overtime();
                    setGameState(GameState.OVERTIME);
                    if (map.type.equals("Elytra")) {
                        for (Participant p : playersAlive) {
                            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 2, false, false));
                        }
                    }
                    timeRemaining = 45;
                } else {
                    createLineAll(23, ChatColor.RED + "" + ChatColor.BOLD+"Round Over!");
                    damagePoints();
                    for (Participant p : playersAlive) {
                        MBCTeam t = p.getTeam();
                        teamPlacements.put(t, 1);
                    }
                    placementPoints();
                    if (!firstRound) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS);
                        }
                        bossBar.setVisible(false);
                        gameOverGraphics();
                        roundWinners(0, SURVIVAL_POINTS);
                        setGameState(GameState.END_GAME);
                        timeRemaining = 37;
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS);
                        }
                        roundOverGraphics();
                        roundWinners(0, SURVIVAL_POINTS);
                        setGameState(GameState.END_ROUND);
                        bossBar.setVisible(false);
                        firstRound = false;
                        timeRemaining = 10;
                    }
                }
            }

            if (timeRemaining == 420) {
                event = SurvivalGamesEvent.CHEST_REFILL;
                bossBar.removeAll();
                bossBar = Bukkit.createBossBar(ChatColor.RED + "" + ChatColor.BOLD + "MAX KILL POINTS / CHEST REFILL", BarColor.RED, BarStyle.SOLID);
                bossBar.setVisible(true);
                setPVP(true);
                getLogger().log(ChatColor.DARK_RED+"Grace period is now over.");
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED+"Grace period is now over.");
                if (map.type.equals("Elytra")) {
                    Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED+"Elytras have been removed.");
                }
                map.startBorder();
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Border will continue to shrink!");

                // double check elytras are gone
                for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
                    bossBar.addPlayer(p.getPlayer());
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                    if (p.getPlayer().getGameMode() == GameMode.SPECTATOR) continue;
                    p.getPlayer().setInvulnerable(false);
                    if (map.type.equals("Elytra")) {
                        p.getPlayer().getInventory().remove(Material.ELYTRA);
                        if (p.getPlayer().getInventory().getChestplate() == null) continue;
                        if (p.getPlayer().getInventory().getChestplate().getType().equals(Material.ELYTRA)) {
                            p.getPlayer().getInventory().setChestplate(new ItemStack(Material.AIR));
                        }
                    }
                }
            } else if (timeRemaining == 360) {
                event = SurvivalGamesEvent.CHEST_REFILL;
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+""+ChatColor.BOLD+"Chests will refill in one minute!");
                //crateLocation();
            } else if (timeRemaining == 300) {
                //spawnSupplyCrate();
                killPoints -= 5;
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED + "" + ChatColor.BOLD + "Kill points are decreasing! (15 -> 10)");
                bossBar.removeAll();
                bossBar = Bukkit.createBossBar(ChatColor.RED + "" + ChatColor.BOLD + "KILL POINTS DECREASE", BarColor.RED, BarStyle.SOLID);
                bossBar.setVisible(true);
                for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
                    bossBar.addPlayer(p.getPlayer());
                    p.getPlayer().sendTitle("", ChatColor.RED+"Chests refilled!", 20, 60, 20);
                    p.getPlayer().playSound(p.getPlayer(), Sound.BLOCK_CHEST_OPEN, 1, 1);
                }
                regenChest();
                getLogger().log(ChatColor.RED+""+ChatColor.BOLD+"Chests have been refilled!");
                Bukkit.broadcastMessage(ChatColor.RED+""+ChatColor.BOLD+"Chests have been refilled!");
                event = SurvivalGamesEvent.DEATHMATCH;
            } else if (timeRemaining == 180) {
                killPoints -= 5;
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED.toString() + ChatColor.BOLD + "Kill points are decreasing! (10 -> 5)");
                bossBar.removeAll();
                bossBar.setVisible(false);
            } else if (timeRemaining == 50) {
                if (map.hasElevationBorder) {
                    Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED.toString() + ChatColor.BOLD + "The border is rising!");
                }
            }
            UpdateEvent();
        } else if (getState().equals(GameState.OVERTIME)) {
            if (map.hasElevationBorder && timeRemaining % 2 == 0) {
                map.Border();
            }
            checkHorcruxes();

            if (timeRemaining == 0) {
                createLineAll(23, ChatColor.RED + "" + ChatColor.BOLD+"Round Over!");
                for (Participant p : playersAlive) {
                    MBCTeam t = p.getTeam();
                    teamPlacements.put(t, 1);
                }
                damagePoints();
                placementPoints();
                if (!firstRound) {
                    gameOverGraphics();
                    roundWinners(0, SURVIVAL_POINTS);
                    setGameState(GameState.END_GAME);
                    timeRemaining = 37;
                } else {
                    roundOverGraphics();
                    roundWinners(0, SURVIVAL_POINTS);
                    setGameState(GameState.END_ROUND);
                    firstRound = false;
                    timeRemaining = 10;
                }
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 1) {
                for (Horcrux h : horcruxList) {
                    h.inUse = false;
                    h.placed = false;
                    h.used = false;

                    if (h.armorStand != null) {
                        h.armorStand.remove();
                        h.armorStand = null;
                    }
                }
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
                }
                map.resetMap();
                loadPlayers();
                setGameState(GameState.STARTING);
                timeRemaining = 30;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 36) {
                for (Horcrux h : horcruxList) {
                    h.inUse = false;
                    h.placed = false;
                    h.used = false;

                    if (h.armorStand != null) {
                        h.armorStand.remove();
                        h.armorStand = null;
                    }
                }
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().removePotionEffect(PotionEffectType.GLOWING);
                }
                map.resetMap();
            }
            gameEndEvents();
        }
    }

    /**
     * Updates player scoreboard for each upcoming event using SurvivalGamesEnum
     */
    private void UpdateEvent() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            switch (event) {
                case GRACE_OVER ->
                        createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Grace ends in: " + ChatColor.RESET + getFormattedTime(timeRemaining - 420), p);
                case CHEST_REFILL ->
                        createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chest refill: " + ChatColor.RESET + getFormattedTime(timeRemaining - 300), p);
                case DEATHMATCH ->
                        createLine(23, ChatColor.RED + "" + ChatColor.BOLD + "Deathmatch: " + ChatColor.WHITE + "Active", p);
                case SUPPLY_CRATE -> {
                    // hard coded times; the 2nd supply drop coincides with chest refill
                    int nextTime = (timeRemaining > 240) ? timeRemaining - 240 : timeRemaining - 180;
                    createLine(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Next supply crate: " + ChatColor.RESET + getFormattedTime(nextTime), p);
                }
            }
        }
    }


    /**
     * Regenerates the loot within every chest in the map.
     * If empty, updates list of eligible Super Chests.
     */
    public void regenChest() {
        double totalWeight = 115.5;

        Random rand = new Random();
        Chunk[] c = map.getWorld().getLoadedChunks();
        for (Chunk chunk : c) {//loop through loaded chunks
            for (int x = 0; x < chunk.getTileEntities().length; x++) {//loop through tile entities within loaded chunks
                if (chunk.getTileEntities()[x] instanceof Chest) {
                    Chest chest = (Chest) chunk.getTileEntities()[x];

                    chest.getInventory().clear();
                    int chestItems = rand.nextInt(2) + 5;
                    for (int b = 0; b < chestItems; b++) {
                        // Now choose a random item w/weight
                        int idx = 0;
                        for (double r = Math.random() * totalWeight; idx < items.size() - 1; ++idx) {
                            r -= items.get(idx).getWeight();
                            if (r <= 0.0) break;
                        }

                        chest.getInventory().setItem(rand.nextInt(27), items.get(idx).getItem());
                    }
                }
            }
        }
    }

    @EventHandler
    public void fallDamage(EntityDamageEvent e) {
        // cancel fall damage during grace period
        if (e.getEntity() instanceof Player) {
            if (getState().equals(GameState.ACTIVE) && timeRemaining >= 420 && timeRemaining <= 450) {
                if (e.getCause().equals(DamageCause.FALL)) {
                    return;
                }
            }
        }
        
    }

    public void placementPoints() {
        for (MBCTeam t : getValidTeams()) {
            for (Participant p : t.getPlayers()) {
                int placement = teamPlacements.get(t);
                if (t.getPlayers().size() == 4) {
                    p.addCurrentScore(TEAM_BONUSES_4[placement-1] / t.getPlayers().size());
                    p.getPlayer().sendMessage(ChatColor.GREEN + "Your team came in " + getPlace(placement) + "!" + MBC.scoreFormatter((int)(TEAM_BONUSES_4[placement-1] / t.getPlayers().size())));
                } else {
                    p.addCurrentScore(TEAM_BONUSES_3[placement-1] / t.getPlayers().size());
                    p.getPlayer().sendMessage(ChatColor.GREEN + "Your team came in " + getPlace(placement) + "!" + MBC.scoreFormatter((int)(TEAM_BONUSES_3[placement-1] / t.getPlayers().size())));
                }
            }
        }
    }

    // Apply regeneration when eating mushroom stew
    private void eatMushroomStew(Player p) {
        if (p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW && p.getInventory().getItemInMainHand().getType() != Material.MUSHROOM_STEW) {
            return;
        }

        boolean mainHand = p.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW;
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1, false, true));
        p.playSound(p.getLocation(), Sound.BLOCK_GRASS_BREAK, 1, 1);
        map.getWorld().spawnParticle(Particle.BLOCK, p.getEyeLocation(), 5, Material.DIRT.createBlockData());

        if (mainHand) {
            p.getInventory().setItemInMainHand(null);
        } else {
            p.getInventory().setItemInOffHand(null);
        }
    }

    private void damagePoints() {
        int sqrtSum = 0;
        for (Player player : playerDamage.keySet()) {
            sqrtSum += Math.sqrt(playerDamage.get(player));
        }

        for (Player player : playerDamage.keySet()) {
            Participant p = Participant.getParticipant(player);
            double percentage = 100 *(playerDamage.get(player) / totalDamage);
            int points = 0;
            if (percentage != 0) {
                points = (int) (Math.log(Math.pow(150*Math.sqrt(percentage)/sqrtSum, 5.5)/7));
                if (points < 0) {
                    points = 0;
                }
            }
            p.addCurrentScore(points);
            logger.log(String.format("%s dealt %.2f%% of the total damage and earned %d points!", p.getPlayerName(),percentage,points));
            p.getPlayer().sendMessage(String.format("%sYou dealt %.2f%% of the total damage and earned %d points!", ChatColor.GREEN, percentage, points));
        }
    }

    private void removeEndCrystal(Player player) {
        // Remove one end crystal from the player's inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.END_CRYSTAL) {
                item.setAmount(item.getAmount() - 1);
                if (item.getAmount() <= 0) {
                    player.getInventory().remove(item);
                }
                break;
            }
        }
    }

    /**
     * Handles Custom Inventory for Enchanting, Tracking opened loot boxes, and mushroom stew.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(e.getAction().isRightClick())) return;
        Player p = e.getPlayer();

        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.ANVIL)) {
            e.setCancelled(true);
            return;
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW || p.getInventory().getItemInOffHand().getType() == Material.MUSHROOM_STEW) {
            eatMushroomStew(p);
            return;
        }

        // Custom GUI for Enchanting
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.ENCHANTING_TABLE)) {
            e.setCancelled(true);
            setupGUI(e.getPlayer());
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
            Player player = e.getPlayer();
            Participant person = Participant.getParticipant(player);
            Block block = e.getClickedBlock();

            if (block != null && e.getBlockFace() == org.bukkit.block.BlockFace.UP) {
                ItemStack item = e.getItem();
                if (item != null && item.getType() == Material.END_CRYSTAL && getState().equals(GameState.ACTIVE)) {
                    for (Participant participant : MBC.getInstance().players) {
                        if (participant.getTeam().equals(person.getTeam())) {
                            removeEndCrystal(participant.getPlayer());
                        }
                    }

                    Horcrux horcrux = horcruxMap.get(person.getTeam());

                    e.setCancelled(true);

                    if (horcrux.placed) {
                        return;
                    }

                    Location loc = block.getLocation().clone().add(new Vector(0.5, 1, 0.5));

                    horcrux.spawn(loc);
                    horcrux.placed = true;
                }
            }
        }

        // prevent stripping trees :p
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().toString().endsWith("SIGN")) {
            return;
        }
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().toString().endsWith("LOG") && p.getInventory().getItemInMainHand().getType().toString().endsWith("AXE")) {
            e.setCancelled(true);
        }
    }

    public void HandleInteractHorcrux(Player p, ArmorStand a) {
        Horcrux horcrux = Horcrux.getHorcrux(horcruxList, a);
        Participant participant = Participant.getParticipant(p);

        if (participant.getTeam().equals(horcrux.team)) {
            p.sendMessage(ChatColor.RED+"You cannot break your own Horcrux!");
        }
        else if (horcrux.inUse) {
            p.sendMessage(ChatColor.RED+"This Horcrux is currently in use.");
        }
        else {
            MBC.spawnFirework(a.getLocation().clone().add(0.5, 2, 0.5), horcrux.team.getColor());

            logger.log(horcrux.team.getTeamName() + "'s Horcrux was destroyed by " + participant.getPlayerName());
            for (Participant par : MBC.getInstance().players) {
                if (par.getTeam().equals(horcrux.team)) {
                    par.getPlayer().sendMessage(ChatColor.RED+"Your Horcrux has been destroyed by " + participant.getFormattedName() + "!");
                }
            }

            horcrux.used = true;
            a.remove();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ArmorStand) {
            if (e.getDamager() instanceof Arrow) {
                e.setCancelled(true);
                e.getDamager().remove();
                map.getWorld().dropItemNaturally(e.getEntity().getLocation(), new ItemStack(Material.ARROW));
                return;
            }
        }

        if (e.getDamager() instanceof Player && e.getEntity() instanceof ArmorStand) {
            HandleInteractHorcrux((Player) e.getDamager(), (ArmorStand) e.getEntity());
            e.setCancelled(true);
            return;
        }

        if (!(e.getEntity() instanceof Player)) return;
        if (e.getDamager() instanceof Player damager) {
            if (playerDamage.get(damager) == null) {
                return;
            }
            double previous = playerDamage.get(damager);
            Player damaged = (Player) e.getEntity();
            double damage = Math.min(e.getDamage(), damaged.getHealth());
            playerDamage.put((Player) e.getDamager(), previous + damage);
            totalDamage += damage;
            return;
        }

        if (e.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) e.getDamager()).getShooter();
            if (!(shooter instanceof Player)) return;
            if (playerDamage.get(shooter) == null) {
                return;
            }
            Player damaged = (Player) e.getEntity();
            double damage = Math.min(e.getDamage(), damaged.getHealth());
            totalDamage += damage;
            playerDamage.compute((Player) shooter, (k, previous) -> previous + damage);
        }
    }

    /**
     * Death events
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Horcrux horcrux = horcruxMap.get(Participant.getParticipant(victim).getTeam());
        Participant killer = Participant.getParticipant(victim.getKiller());

        // remove the horcruxes
        for (ItemStack i : victim.getInventory()) {
            if (i != null && i.getType().toString().endsWith("CRYSTAL")) {
                victim.getInventory().remove(i);
            }
        }

        if (killer != null) {
            killer.addCurrentScore(killPoints);
            if (playerKills.get(victim.getKiller()) == null) {
                playerKills.put(victim.getKiller(), 1);
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your Kills: "+ChatColor.RESET+"1", killer);
            } else {
                int kills = playerKills.get(victim.getKiller());
                kills++;
                playerKills.put(e.getPlayer().getKiller(), kills);
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+kills, killer);
            }
            deathEffectsWithHealthSG(e, horcrux, e.getPlayer().getLastDamageCause().getCause());
            killer.getPlayer().playSound(killer.getPlayer(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 0.5f, 1);
    
        } else {
            Participant p = Participant.getParticipant(victim);
            if (p == null) return;
            MBC.spawnFirework(p);
            e.setDeathMessage(e.getDeathMessage().replace(e.getPlayer().getName(), p.getFormattedName()));
            if (horcrux.used || !horcrux.placed) {
                updatePlayersAlive(p);
            }
        }

        victim.setGameMode(GameMode.SPECTATOR);
        getLogger().log(e.getDeathMessage());
        victim.getPlayer().removePotionEffect(PotionEffectType.GLOWING);

        for (Player play : Bukkit.getOnlinePlayers()) {
            if (playersAlive.contains(Participant.getParticipant(play))) {
                play.sendMessage(e.getDeathMessage() + MBC.scoreFormatter(SURVIVAL_POINTS));
            }
            else {
                play.sendMessage(e.getDeathMessage());
            }
        }
        
        Participant victimParticipant = Participant.getParticipant(victim);

        if (horcrux.used || !horcrux.placed) {
            int count = 0;
            for (Participant p : victimParticipant.getTeam().teamPlayers) {
                if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                    count++;
                }
            }

            if (count == victimParticipant.getTeam().teamPlayers.size()) {
                teamPlacements.put(victimParticipant.getTeam(), getValidTeams().size() - deadTeams);
                deadTeams++;
            }

        }

        for (ItemStack i : victim.getInventory()) {
            if (i == null) continue;
            map.getWorld().dropItemNaturally(victim.getLocation(), i);
        }

        e.setCancelled(true);

        for (Participant p : playersAlive) {
            if (p != victimParticipant) {
                p.addCurrentScore(SURVIVAL_POINTS);
            }
        }

        if (!horcrux.used && horcrux.placed) {
            Bukkit.broadcastMessage(victimParticipant.getFormattedName()+ChatColor.BOLD+" is being respawned by their team's " + ChatColor.GOLD + "horcrux!");

            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(victim);
                playerHead.setItemMeta(skullMeta);
            }

            // Set the player head on the ArmorStand
            horcrux.armorStand.setHelmet(playerHead);
            horcrux.inUse = true;

            Bukkit.getScheduler().runTaskLater(MBC.getInstance().plugin, new Runnable() {
                @Override
                public void run() {
                    victim.getInventory().clear();
                    victim.teleport(horcrux.location);
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,100, 255, false, true));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION,100, 3, false, false));
                    victim.setInvulnerable(true);
                    victim.setGameMode(GameMode.SURVIVAL);
                    map.getWorld().spawnParticle(Particle.EXPLOSION, horcrux.location, 10);
                    map.getWorld().playSound(horcrux.location, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1, 1);
                    victim.sendMessage(MBC.MBC_STRING_PREFIX + "You have been respawned and are invulnerable! Run!");
                    horcrux.armorStand.remove();

                    // not sure if this is good practice or not.
                    Bukkit.getScheduler().runTaskLater(MBC.getInstance().plugin, new Runnable() {
                        @Override
                        public void run() {
                            victim.setInvulnerable(false);
                            victim.sendMessage(MBC.MBC_STRING_PREFIX + "You are no longer invulnerable.");
                        }
                    }, 100L);
                }
            }, 100L);
        }

        horcrux.used = true;
    }

    public void deathEffectsWithHealthSG(PlayerDeathEvent e, Horcrux horcrux, EntityDamageEvent.DamageCause damageCause) {
        Participant victim = Participant.getParticipant(e.getPlayer());
        Participant killer = Participant.getParticipant(e.getPlayer().getKiller());
        String deathMessage = e.getDeathMessage();

        if (victim == null) return;

        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        MBC.spawnFirework(victim);
        if (damageCause == DamageCause.CUSTOM) {
            if (killer != null) {
                killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!" + MBC.scoreFormatter(killPoints));
                killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
                String health = String.format(" ["+ChatColor.RED+"♥ %.2f"+ChatColor.RESET+"]", killer.getPlayer().getHealth());
                deathMessage = victim.getFormattedName() + " died in the border whilst fighting " + killer.getFormattedName() + health;
            } else {
                deathMessage = victim.getFormattedName() + " died in the border";
            }
        } else {
            deathMessage = deathMessage.replace(victim.getPlayerName(), victim.getFormattedName());
            if (killer != null) {
                killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayerName() + "!" + MBC.scoreFormatter(killPoints));
                killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getFormattedName(), 0, 60, 20);
                String health = String.format(" ["+ChatColor.RED+"♥ %.2f"+ChatColor.RESET+"]", killer.getPlayer().getHealth());
                deathMessage = deathMessage.replace(killer.getPlayerName(), killer.getFormattedName()+health);
            }
        }

        e.setDeathMessage(deathMessage);

        if (horcrux.used || !horcrux.placed) {
            updatePlayersAlive(victim);
        }
    }

    private void decrementBossBar() {
        if (timeRemaining <= 180) return;

        if (timeRemaining > 420) {
            bossBar.setProgress((timeRemaining - 420) / 30.0); // grace period time
            return;
        }

        if (timeRemaining > 300) {
            bossBar.setProgress((timeRemaining - 300) / 120.0); // kills worth 10 points time
            return;
        }

        bossBar.setProgress((timeRemaining - 180) / 120.0);  // kills worth 8 points time
    }

    /**
     * TODO: better standardization across maps
     * For now: prevent blocks from being broken if they are not glass blocks
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getBlock().getLocation().getWorld().equals(map.getWorld())) return;

        // this solution might be temporary, not sure if other maps or other blocks are necessary to add.
        String brokenBlock = e.getBlock().getType().toString();
        if (brokenBlock.contains("GLASS") || e.getBlock().getType().equals(Material.TALL_GRASS) || e.getBlock().getType().equals(Material.FIRE)) {
            map.brokenBlocks.put(e.getBlock().getLocation(), e.getBlock().getType());
            return;
        }

        e.setCancelled(true);
    }

    /**
     * Prevent item frame rotation
     */
    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) e.setCancelled(true);
        if (e.getRightClicked() instanceof ArmorStand) {

            Player player = e.getPlayer();
            ArmorStand armorStand = (ArmorStand) e.getRightClicked();

            HandleInteractHorcrux(player, armorStand);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerEntityInteract(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            Player player = e.getPlayer();
            ArmorStand armorStand = (ArmorStand) e.getRightClicked();

            HandleInteractHorcrux(player, armorStand);
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.getLocation().getY() <= -25) {
            p.teleport(map.Center());
            return;
        }

        if(map.type.equals("Elytra") && getState() == GameState.ACTIVE && p.getGameMode() == GameMode.SURVIVAL && p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            if (p.getInventory().getChestplate() == null) return;
            if (p.getInventory().getChestplate().getType().equals(Material.ELYTRA)) {
                p.getInventory().setChestplate(new ItemStack(Material.AIR));
            }
            p.getInventory().remove(Material.ELYTRA);
        }

    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            Participant p = Participant.getParticipant(e.getPlayer());
            if (p == null) return;

            updatePlayersAlive(p);

            for (Player play : Bukkit.getOnlinePlayers()) {
                if (playersAlive.contains(Participant.getParticipant(play))) {
                    play.sendMessage(p.getFormattedName() + " disconnected!" + MBC.scoreFormatter(SURVIVAL_POINTS));
                }
                else {
                    play.sendMessage(p.getFormattedName() + " disconnected!");
                }
            }
            
            
            for (Participant n : playersAlive) {
                n.addCurrentScore(SURVIVAL_POINTS);
            }
        }
    }

    // NOTE:
    // for this implementation of disconnect handling (present for all pvp games)
    // we may have to restart if someone's internet completely gives out
    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        Participant p = Participant.getParticipant(e.getPlayer());
        if (p == null) {
            e.getPlayer().teleport(map.Center());
            return;
        }
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!e.getPlayer().getLocation().getWorld().equals(map.getWorld())) return;
        Material i = e.getItemDrop().getItemStack().getType();
        if (i == Material.ELYTRA) e.setCancelled(true);
        else return;
   }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("Enchanting")) {
            // handle custom enchants;
            handleEnchantGUI(e);
            return;
        }
        Player p = (Player) e.getWhoClicked();
        ItemStack book = e.getCursor();
        if (book.getType() != Material.ENCHANTED_BOOK) return;
        ItemStack tool = e.getCurrentItem();
        if (tool == null) return;
        if (!tool.getEnchantments().isEmpty()) {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED+"Cannot enchant enchanted item!");
        }

        String toolName = tool.getType().toString();
        boolean armorNotBoots = toolName.contains("CHESTPLATE") || toolName.contains("HELMET") || toolName.contains("LEGGINGS");

        // ALLOWED ENCHANTMENTS:
        // SWORD: SHARPNESS 1 OR KNOCKBACK 1
        // AXE: NONE
        // BOW: POWER; (note: this may be too OP)
        // CROSSBOW: QUICK CHARGE 1 OR MULTISHOT
        // ARMOR: Protection 1
        // BOOTS: Feather Falling 1

        if (!(book.getItemMeta() instanceof EnchantmentStorageMeta)) {
            p.sendMessage("Book has no enchants!");
        }
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        Map<Enchantment, Integer> enchantments = meta.getStoredEnchants();

        if (enchantments.size() > 1) {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED+"Cannot enchant, too many enchantments on book!");
            return;
        }

        Enchantment ench = null;
        for (Enchantment e2 : enchantments.keySet()) {
            ench = e2;
        }

        if (ench == null) return;

        // TODO ? this can probably be rewritten a lot prettier using switches or something else
        if (toolName.contains("SWORD") && (ench.equals(Enchantment.KNOCKBACK) || ench.equals(Enchantment.SHARPNESS))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (tool.getType().equals(Material.CROSSBOW) && (ench.equals(Enchantment.QUICK_CHARGE) || ench.equals(Enchantment.MULTISHOT))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (toolName.contains("BOOTS") && (ench.equals(Enchantment.PROTECTION) || ench.equals(Enchantment.PROTECTION))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (armorNotBoots && ench.equals(Enchantment.PROTECTION)) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (tool.getType().equals(Material.BOW) && ench.equals(Enchantment.POWER)){
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED + "Cannot apply this enchantment to this item!");
            e.setCancelled(true);
        }
        e.setCancelled(true);
    }

    private void setupGUI(Player p) {
        Inventory gui = Bukkit.createInventory(p, 9, "Enchanting");

        for (int i = 0; i < guiItems.length; i++) {
            gui.setItem(i, guiItems[i].item);
        }

        p.openInventory(gui);
    }


    // TODO
    private void handleEnchantGUI(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        Material type = e.getCurrentItem().getType();
        //if (type == null) return;

        Player p = (Player) e.getWhoClicked();
        GUIItem item = null;

        for (GUIItem guiItem : guiItems) {
            if (guiItem.item.getType().equals(type)) {
                item = guiItem;
                break;
            }
        }

        if (item == null) return;

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        int level = p.getLevel();
        if (p.getInventory().firstEmpty() == -1) {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED + "Full inventory!");
        } else if (level < item.cost) {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED + "Not enough levels!");
        } else {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(item.enchantment, 1, true);
            book.setItemMeta(meta);
            p.setLevel(level-item.cost);
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            p.getInventory().addItem(book);
            p.closeInventory();
            //p.updateInventory();
        }
        // prevent taking items (bad)
        e.setCancelled(true);
    }

    private GUIItem[] setupGUIItems() {
        GUIItem[] items = new GUIItem[7];

        ItemStack sharpness = new ItemStack(Material.DIAMOND_SWORD);
        sharpness.addEnchantment(Enchantment.SHARPNESS, 1);
        ItemMeta sharpnessMeta = sharpness.getItemMeta();
        sharpnessMeta.setDisplayName(ChatColor.RED+"Sharpness I");
        sharpness.setItemMeta(sharpnessMeta);
        sharpness.setLore(List.of("Cost: 3 XP"));
        items[0] = new GUIItem(sharpness, Enchantment.SHARPNESS, 3);

        ItemStack knockback = new ItemStack(Material.IRON_SWORD);
        knockback.addEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta knockMeta = knockback.getItemMeta();
        knockMeta.setDisplayName(ChatColor.RED+"Knockback I");
        knockback.setItemMeta(knockMeta);
        knockback.setLore(List.of("Cost: 1 XP"));
        items[1] = new GUIItem(knockback, Enchantment.KNOCKBACK, 1);

        ItemStack protection = new ItemStack(Material.DIAMOND_CHESTPLATE);
        protection.addEnchantment(Enchantment.PROTECTION, 1);
        ItemMeta protMeta = protection.getItemMeta();
        protMeta.setDisplayName(ChatColor.GREEN+"Protection I");
        protMeta.setLore(List.of("Cost: 2 XP"));
        protection.setItemMeta(protMeta);
        items[2] = new GUIItem(protection, Enchantment.PROTECTION, 2);

        ItemStack featherFalling = new ItemStack(Material.IRON_BOOTS);
        featherFalling.addEnchantment(Enchantment.FEATHER_FALLING, 1);
        ItemMeta ffMeta = featherFalling.getItemMeta();
        ffMeta.setDisplayName(ChatColor.GREEN+"Feather Falling I");
        featherFalling.setItemMeta(ffMeta);
        featherFalling.setLore(List.of("Cost: 1 XP"));
        items[3] = new GUIItem(featherFalling, Enchantment.FEATHER_FALLING, 1);

        ItemStack power = new ItemStack(Material.BOW);
        power.addEnchantment(Enchantment.POWER, 1);
        ItemMeta powerMeta = power.getItemMeta();
        powerMeta.setDisplayName(ChatColor.RED+"Power I");
        power.setItemMeta(powerMeta);
        power.setLore(List.of("Cost: 3 XP"));
        items[4] = new GUIItem(power, Enchantment.POWER, 3);

        ItemStack quickCharge = new ItemStack(Material.CROSSBOW);
        quickCharge.addEnchantment(Enchantment.QUICK_CHARGE, 1);
        ItemMeta qcMeta = quickCharge.getItemMeta();
        qcMeta.setDisplayName(ChatColor.BLUE+"Quick Charge I");
        quickCharge.setItemMeta(qcMeta);
        quickCharge.setLore(List.of("Cost: 1 XP"));
        items[5] = new GUIItem(quickCharge, Enchantment.QUICK_CHARGE, 1);

        ItemStack multishot = new ItemStack(Material.ARROW, 3);
        multishot.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
        ItemMeta msMeta = multishot.getItemMeta();
        msMeta.setDisplayName(ChatColor.BLUE+"Multishot");
        multishot.setItemMeta(msMeta);
        multishot.setLore(List.of("Cost: 2 XP"));
        items[6] = new GUIItem(multishot, Enchantment.MULTISHOT, 2);

        return items;
    }

    private void checkHorcruxes() {
        for (Horcrux h : horcruxList) {
            if (border == null) {
                border = map.getWorld().getWorldBorder();
            }

            if (h.armorStand == null || h.used) continue;

            Location loc = h.armorStand.getLocation();
            double size = border.getSize()/2;
            Location center = border.getCenter();

            if (map.hasElevationBorder) {
                if (loc.getY() <= map.borderHeight) {
                    h.inUse = false;
                    h.placed = true;
                    h.used = true;
                    h.armorStand.remove();
                    logger.log(h.team.getTeamName() + "'s Horcrux was destroyed by the border!");
                    for (Participant p : h.team.getPlayers()) {
                        p.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your Horcrux was destroyed by the border!");
                    }
                    return;
                }
            }

            double x = loc.getX() - center.getX(), z = loc.getZ() - center.getZ();
            if ((x > size || (-x) > size) || (z > size || (-z) > size)) {
                h.inUse = false;
                h.placed = true;
                h.used = true;
                h.armorStand.remove();
                logger.log(h.team.getTeamName() + "'s Horcrux was destroyed by the border!");
                for (Participant p : h.team.getPlayers()) {
                    p.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Your Horcrux was destroyed by the border!");
                }
            }
        }
    }
}

class SurvivalGamesItem {
    private Material material;
    private int stack_max;
    private double weight;
    String[] unbreakable = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "SWORD", "AXE", "BOW"};

    public SurvivalGamesItem() {}

    public ItemStack getItem() {
        ItemStack it = new ItemStack(material, (int) (Math.random() * stack_max) +1);
        String s = it.getType().toString();
        for (String value : unbreakable) {
            if (s.contains(value)) {
                ItemMeta meta = it.getItemMeta();
                meta.setUnbreakable(true);
                it.setItemMeta(meta);
                break;
            }
        }
        return it;
    }
    public double getWeight() { return weight; }
}

class SupplyCrate {
    private final Location LOCATION;
    private boolean opened;
    public SupplyCrate(Location loc, boolean opened) {
        LOCATION = loc;
        this.opened = opened;
    }

    public Location getLocation() { return LOCATION; }
    public boolean beenOpened() { return opened; }
    public void setOpened(boolean b) { opened = b; }
}

enum SurvivalGamesEvent {
    GRACE_OVER,
    SUPPLY_CRATE,
    CHEST_REFILL,
    DEATHMATCH
}

class GUIItem {
    public final ItemStack item;
    public final Enchantment enchantment;
    public final int cost;

    public GUIItem(ItemStack item, Enchantment enchantment, int cost) {
        this.item = item;
        this.enchantment = enchantment;
        this.cost = cost;
    }
}

class Horcrux {
    public boolean placed = false;
    public Location location;
    public ArmorStand armorStand;

    public boolean used = false;
    public boolean inUse = false;

    public MBCTeam team;

    public Horcrux(MBCTeam t) {
        team = t;
    }

    public void spawn(Location loc) {
        location = loc;

        armorStand = loc.getWorld().spawn(location, ArmorStand.class);
        armorStand.setArms(true);
        armorStand.setVisible(true);

        ItemStack leatherHelmet = team.getColoredLeatherArmor(new ItemStack(Material.LEATHER_HELMET));
        ItemStack leatherChestplate = team.getColoredLeatherArmor(new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemStack leatherLeggings = team.getColoredLeatherArmor(new ItemStack(Material.LEATHER_LEGGINGS));
        ItemStack leatherBoots = team.getColoredLeatherArmor(new ItemStack(Material.LEATHER_BOOTS));

        armorStand.setItem(EquipmentSlot.HEAD, leatherHelmet);
        armorStand.setItem(EquipmentSlot.CHEST, leatherChestplate);
        armorStand.setItem(EquipmentSlot.LEGS, leatherLeggings);
        armorStand.setItem(EquipmentSlot.FEET, leatherBoots);
    }

    public static Horcrux getHorcrux(List<Horcrux> horcruxList, ArmorStand a) {
        for (Horcrux h : horcruxList) {
            if (h.armorStand != null && h.armorStand.equals(a)) {
                return h;
            }
        }

        return null;
    }
}