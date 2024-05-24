package me.kotayka.mbc.games;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.sgMaps.BCA;
import me.kotayka.mbc.gameMaps.sgMaps.SurvivalGamesMap;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

public class SurvivalGames extends Game {
    private final SurvivalGamesMap map = new BCA();
    private List<SurvivalGamesItem> items;
    //private List<SurvivalGamesItem> supply_items;

    // This file must be in /home/MBCAdmin/MBC/ and re-added each time an update is made
    private final File CHEST_FILE = new File("survival_games_items.json");
    private final File SUPPLY_FILE = new File("supply_crate_items.json");
    private SurvivalGamesEvent event = SurvivalGamesEvent.GRACE_OVER;
    //private final List<Location> chestLocations = new ArrayList<Location>(50);
    //private final List<SupplyCrate> crates = new ArrayList<SupplyCrate>(3);
    private Map<Player, Integer> playerKills = new HashMap<>();
    private Map<MBCTeam, Integer> teamPlacements = new HashMap<>();
    //private boolean dropLocation = false;
    //private int crateNum = 0;
    private int deadTeams = 0; // just to avoid sync issues w/teamsAlive.size()
    private boolean firstRound = true;

    // Enchantment
    private final GUIItem[] guiItems = setupGUIItems();

    // SCORING
    public final int KILL_POINTS = 10;
    public final int SURVIVAL_POINTS = 2;
    // Shared amongst each team: 10, 8, 7, 6, 5, 4 points for each player
    public final int[] TEAM_BONUSES_4 = {40, 32, 28, 24, 20, 16};
    public final int[] TEAM_BONUSES_3 = {30, 24, 21, 18, 15, 12};
    // public final int WIN_POINTS = 36; // shared amongst all remaining players

    public SurvivalGames() {
        super("SurvivalGames", new String[] {
                "⑬ " + ChatColor.BOLD + "In a world..." + ChatColor.RESET + "spawn in with nothing, collect items through chests, emerge the last team standing!\n\n" + 
                "⑬ There will be a short grace period.",
                "⑬ Purchase items at the enchant table, drag and click books to enchant!\n\n" + 
                "⑬ Additionally, watch out for the border, " + ChatColor.BOLD + " you will not be able to break glass or open doors behind the border.",
                "⑬ Your team will get a placement bonus, so live as long as you can!\n\n" +
                "But there are two rounds, so remember who killed you...",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                        "⑬ +10 points for eliminations\n" +
                        "⑬ +2 points for every player outlived\n" +
                        "⑬ Team Bonuses (split amongst team):\n" +
                        "     ⑬ 1st: +10 points, 2nd: +8 points, 3rd: +7 points\n" +
                        "     ⑬ 4th: +6 points, 5th: +5 points, 6th: +4 points"
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
        //reader = new FileReader(SUPPLY_FILE);
        //supply_items = gson.fromJson(reader, listType);
        reader.close();
    }

    @Override
    public void loadPlayers() {
        setPVP(false);
        deadTeams = 0;
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
            playersAlive.add(p);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setInvulnerable(true);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 10, false, false));
            p.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
        updatePlayersAliveScoreboard();
        regenChest();
    }

    @Override
    public void start() {
        super.start();

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
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                    p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                    p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                }
                setGameState(GameState.ACTIVE);
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Grace ends in 30 seconds!");
                timeRemaining = 450;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            /*
             * Event timeline:
             *  7:30: Game Starts
             *  7:00: Grace Ends, border starts to move
             *  6:00: 1 minute to chest refill, 1st supply crate announced
             *  5:00: Chest Refill, 1st supply crate drops, 2nd supply crate announced
             *  4:00: 2nd supply crate drops, 3rd supply crate announced
             *  3:00: Last supply crate lands
             */
            //if (crates.size() > 0) { crateParticles(); }

            if (timeRemaining == 0) {
                if (teamsAlive.size() > 1) {
                    Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Border shrinking!");
                    map.Overtime();
                    setGameState(GameState.OVERTIME);
                    timeRemaining = 45;
                } else {
                    for (Participant p : playersAlive) {
                        createLine(23, ChatColor.RED+"Round Over!", p);
                        MBCTeam t = p.getTeam();
                        teamPlacements.put(t, 1);
                    }
                    placementPoints();
                    //createLineAll(23, "\n");
                    if (!firstRound) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS);
                        }
                        gameOverGraphics();
                        roundWinners(0);
                        setGameState(GameState.END_GAME);
                        timeRemaining = 37;
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_FAR, SoundCategory.RECORDS);
                        }
                        roundOverGraphics();
                        roundWinners(0);
                        setGameState(GameState.END_ROUND);
                        firstRound = false;
                        timeRemaining = 10;
                    }
                }
            }

            if (timeRemaining == 420) {
                //event = SurvivalGamesEvent.SUPPLY_CRATE;
                event = SurvivalGamesEvent.CHEST_REFILL;
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().playSound(p.getPlayer(), Sound.ENTITY_WITHER_SPAWN, 1, 1);
                    p.getPlayer().setInvulnerable(false);
                }
                setPVP(true);
                getLogger().log(ChatColor.DARK_RED+"Grace period is now over.");
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED+"Grace period is now over.");
                map.startBorder();
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+"Border will continue to shrink!");
            } else if (timeRemaining == 360) {
                event = SurvivalGamesEvent.CHEST_REFILL;
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+""+ChatColor.BOLD+"Chests will refill in one minute!");
                //crateLocation();
            } else if (timeRemaining == 300) {
                //spawnSupplyCrate();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendTitle("", MBC.MBC_STRING_PREFIX + ChatColor.RED+"Chests refilled!", 20, 60, 20);
                    player.playSound(player, Sound.BLOCK_CHEST_OPEN, 1, 1);
                }
                regenChest();
                getLogger().log(ChatColor.RED+""+ChatColor.BOLD+"Chests have been refilled!");
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.RED+""+ChatColor.BOLD+"Chests have been refilled!");
                //event = SurvivalGamesEvent.SUPPLY_CRATE;
                event = SurvivalGamesEvent.DEATHMATCH;
                //crateLocation();
                /*
            } else if (timeRemaining == 240) {
                //spawnSupplyCrate();
            } else if (timeRemaining == 239) {
                //crateLocation();
            } else if (timeRemaining == 180) {
                //spawnSupplyCrate();

                //event = SurvivalGamesEvent.DEATHMATCH;
                 */
            }
            UpdateEvent();
        } else if (getState().equals(GameState.OVERTIME)) {
            if (timeRemaining == 0) {
                for (Participant p : playersAlive) {
                    MBCTeam t = p.getTeam();
                    teamPlacements.put(t, 1);
                }
                placementPoints();
                createLineAll(23, ChatColor.RED+"Round Over!");
                if (!firstRound) {
                    gameOverGraphics();
                    roundWinners(0);
                    setGameState(GameState.END_GAME);
                    timeRemaining = 37;
                } else {
                    roundOverGraphics();
                    roundWinners(0);
                    setGameState(GameState.END_ROUND);
                    firstRound = false;
                    timeRemaining = 10;
                }
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 1) {
                map.resetMap();
                loadPlayers();
                setGameState(GameState.STARTING);
                timeRemaining = 30;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 36) {
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
            // display coordinates of each drop separately
            /*
            if (dropLocation && crates.size() > 0) {
                Location l = crates.get(crateNum).getLocation();
                createLine(22, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Supply drop: " + ChatColor.RESET + "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")", p);
                dropLocation = false;
            }
             */

            switch (event) {
                case GRACE_OVER ->
                        createLineAll(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Grace ends in: " + ChatColor.RESET + getFormattedTime(timeRemaining - 420));
                case CHEST_REFILL ->
                        createLineAll(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chest refill: " + ChatColor.RESET + getFormattedTime(timeRemaining - 300));
                case DEATHMATCH ->
                        createLineAll(23, ChatColor.RED + "" + ChatColor.BOLD + "Deathmatch: " + ChatColor.WHITE + "Active");
                case SUPPLY_CRATE -> {
                    // hard coded times; the 2nd supply drop coincides with chest refill
                    int nextTime = (timeRemaining > 240) ? timeRemaining - 240 : timeRemaining - 180;
                    createLineAll(23, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Next supply crate: " + ChatColor.RESET + getFormattedTime(nextTime));
                }
            }
        }
    }

    /**
     * Generate location of supply crate
     */
    /*
    public void crateLocation() {
        int index = (int) (Math.random()*chestLocations.size());

        Location l = (chestLocations.get(index));
        chestLocations.remove(l);
        dropLocation = true;
        crates.add(new SupplyCrate(l, false));
        String s = ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Supply crate spawning at " + ChatColor.RESET+"("+l.getX()+", "+l.getY()+", "+l.getZ()+")";
        getLogger().log(s);
        Bukkit.broadcastMessage(s);
    }
     */

    /**
     * Given the location of the crate is predetermined, replace the block
     * with the crate block and generate loot
     * @see SurvivalGames crateLocation()
     * TODO: Improve implementation of crates
     */
    /*
    public void spawnSupplyCrate() {
        double totalWeight = 42;
        // delete this once it is final
        /*for (SurvivalGamesItem item : supply_items) {
            totalWeight += item.getWeight();
        }
        Bukkit.broadcastMessage("[Debug] Total Supply Weight == " + totalWeight);
         */

            /*
        Location l = crates.get(crateNum).getLocation();
        l.getBlock().setType(Material.BLACK_SHULKER_BOX);
        ShulkerBox crate = (ShulkerBox) map.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()).getState();

        int chestItems = (int) (Math.random()*6+5);
        for (int b = 0; b < chestItems; b++) {
            // Now choose a random item.
            int idx = 0;
            for (double r = Math.random() * totalWeight; idx < supply_items.size() - 1; ++idx) {
                r -= supply_items.get(idx).getWeight();
                if (r <= 0.0) break;
            }

            crate.getInventory().setItem((int) (Math.random()*27), supply_items.get(idx).getItem());
        }
    }
    */

    /**
     * Spawn particles at super chest spawning location
     */
    /*
    private void crateParticles() {
        for (SupplyCrate crate : crates) {
            if (!crate.beenOpened()) {
                // TODO ? the particles are kind of bad looking LOL
                Location l = crate.getLocation();
                map.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, l.getX() + Math.random()*0.5-0.5, l.getBlockY() + 1, l.getZ() + Math.random()*0.5-0.5, 5);
                map.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, l.getX() + Math.random()*0.5-0.5, l.getBlockY() + Math.random(), l.getZ() + Math.random()*0.5-0.5, 5);
            }
        }
    }
     */

    /**
     * Regenerates the loot within every chest in the map.
     * If empty, updates list of eligible Super Chests.
     */
    public void regenChest() {
        double totalWeight = 115.5;
        /*
        //double totalWeight = 0;
        // delete when final.
        for (SurvivalGamesItem item : items) {
            totalWeight += item.getWeight();
        }
        Bukkit.broadcastMessage("[Debug] Total Weight == " + totalWeight);
         */


        Random rand = new Random();
        Chunk[] c = map.getWorld().getLoadedChunks();
        for (Chunk chunk : c) {//loop through loaded chunks
            for (int x = 0; x < chunk.getTileEntities().length; x++) {//loop through tile entities within loaded chunks
                if (chunk.getTileEntities()[x] instanceof Chest) {
                    Chest chest = (Chest) chunk.getTileEntities()[x];

                    /*
                    if (crates.size() < 1 && map.checkChest(chest)) {
                        chestLocations.add(chest.getLocation());
                    }
                     */

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

    public void placementPoints() {
        for (MBCTeam t : getValidTeams()) {
            for (Participant p : t.getPlayers()) {
                int placement = teamPlacements.get(t);
                if (t.getPlayers().size() == 4) {
                    p.addCurrentScore(TEAM_BONUSES_4[placement-1] / t.getPlayers().size());
                    p.getPlayer().sendMessage(ChatColor.GREEN + "Your team came in " + getPlace(placement) + " and earned a bonus of " + (TEAM_BONUSES_4[placement-1] * MBC.getInstance().multiplier) + " points!");
                } else {
                    p.addCurrentScore(TEAM_BONUSES_3[placement-1] / t.getPlayers().size());
                    p.getPlayer().sendMessage(ChatColor.GREEN + "Your team came in " + getPlace(placement) + " and earned a bonus of " + (TEAM_BONUSES_3[placement-1] * MBC.getInstance().multiplier) + " points!");
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
        map.getWorld().spawnParticle(Particle.BLOCK_CRACK, p.getLocation(), 3, Material.DIRT.createBlockData());

        if (mainHand) {
            p.getInventory().setItemInMainHand(null);
        } else {
            p.getInventory().setItemInOffHand(null);
        }
    }

    /**
     * Handles Custom Inventory for Enchanting, Tracking opened loot boxes, and mushroom stew.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(e.getAction().isRightClick())) return;
        Player p = e.getPlayer();

        if (p.getInventory().getItemInMainHand().getType() == Material.MUSHROOM_STEW || p.getInventory().getItemInOffHand().getType() == Material.MUSHROOM_STEW) {
            eatMushroomStew(p);
            return;
        }

        // Custom GUI for Enchanting
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.ENCHANTING_TABLE)) {
            e.setCancelled(true);
            setupGUI(e.getPlayer());
        }

        /*
        if (e.getClickedBlock() != null && e.getClickedBlock() instanceof Container &&
                (!e.getClickedBlock().getType().equals(Material.CHEST) && !e.getClickedBlock().getType().equals(Material.CRAFTING_TABLE))) {
           e.setCancelled(true);
           return;
        }

        if (p.getInventory().getItemInMainHand().getType().equals(Material.CROSSBOW)) {
            CrossbowMeta cb1 = (CrossbowMeta) p.getInventory().getItemInMainHand().getItemMeta();
            if (!cb1.getChargedProjectiles().isEmpty()) {
                return;
            }
            // check if there exists another crossbow loaded in inventory
            for (ItemStack i : p.getInventory()) {
                if (i != null && i.getType().equals(Material.CROSSBOW)) {
                    CrossbowMeta cb2 = (CrossbowMeta) i.getItemMeta();
                    if (!cb2.getChargedProjectiles().isEmpty()) {
                        p.sendMessage(ChatColor.RED+"You cannot load more than one crossbow!");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (p.getInventory().getItemInOffHand().getType().equals(Material.CROSSBOW)) {
            CrossbowMeta cb1 = (CrossbowMeta) p.getInventory().getItemInOffHand().getItemMeta();
            if (!cb1.getChargedProjectiles().isEmpty()) {
                return;
            }

            // check if there exists another crossbow loaded in inventory
            for (ItemStack i : p.getInventory()) {
                if (i != null && i.getType().equals(Material.CROSSBOW)) {
                    CrossbowMeta cb2 = (CrossbowMeta) i.getItemMeta();
                    if (!cb2.getChargedProjectiles().isEmpty()) {
                        p.sendMessage(ChatColor.RED+"You cannot load more than one crossbow!");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Track opened crates
        /*
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().equals(Material.BLACK_SHULKER_BOX)) {
            if (crates.size() < 1) {
                return;
            }
            crates.get(crateNum).setOpened(true);
            dropLocation = false;
            createLineAll(22, "");
            crateNum++;
            return;
        }
         */

        // prevent stripping trees :p
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType().toString().endsWith("LOG") && p.getInventory().getItemInMainHand().getType().toString().endsWith("AXE")) {
            e.setCancelled(true);
        }
    }

    /**
     * Death events
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Participant killer = Participant.getParticipant(victim.getKiller());
        if (killer != null) {
            killer.addCurrentScore(KILL_POINTS);
            if (playerKills.get(victim.getKiller()) == null) {
                playerKills.put(victim.getKiller(), 1);
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your Kills: "+ChatColor.RESET+"1", killer);
            } else {
                int kills = playerKills.get(victim.getKiller());
                kills++;
                playerKills.put(e.getPlayer().getKiller(), kills);
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+kills, killer);
            }
            deathEffectsWithHealth(e);
        } else {
            Participant p = Participant.getParticipant(victim);
            if (p == null) return;
            MBC.spawnFirework(p);
            e.setDeathMessage(e.getDeathMessage().replace(e.getPlayer().getName(), p.getFormattedName()));
            updatePlayersAlive(p);
        }

        victim.setGameMode(GameMode.SPECTATOR);
        getLogger().log(e.getDeathMessage());

        Bukkit.broadcastMessage(e.getDeathMessage());
        for (ItemStack i : victim.getPlayer().getInventory()) {
            if (i == null) continue;
            map.getWorld().dropItemNaturally(victim.getLocation(), i);
        }
        e.setCancelled(true);

        int count = 0;
        Participant victimParticipant = Participant.getParticipant(victim);
        for (Participant p : victimParticipant.getTeam().teamPlayers) {
            if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                count++;
            }
        }

        if (count == victimParticipant.getTeam().teamPlayers.size()) {
            teamPlacements.put(victimParticipant.getTeam(), getValidTeams().size() - deadTeams);
            deadTeams++;
        }

        // may require testing due to concurrency
        for (Participant p : playersAlive) {
            p.addCurrentScore(SURVIVAL_POINTS);
        }
    }

    /*
    // Prevent players from picking up crossbows that are loaded
    @EventHandler
    public void PickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getItem().getItemStack().getType().equals(Material.CROSSBOW))) return;

        CrossbowMeta cbm = (CrossbowMeta) e.getItem().getItemStack().getItemMeta();
        // if loaded crossbow
        if (!cbm.getChargedProjectiles().isEmpty()) {
            Player p = (Player) e.getEntity();
            for (ItemStack i : p.getInventory()) {
                if (i != null && i.getType().equals(Material.CROSSBOW)) {
                    CrossbowMeta cbm2 = (CrossbowMeta) i.getItemMeta();
                    if (!cbm2.getChargedProjectiles().isEmpty()) {
                        p.sendMessage(ChatColor.RED + "You cannot have more than one charged crossbow!");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
     */

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
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getY() <= -25) {
            e.getPlayer().teleport(map.Center());
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            Participant p = Participant.getParticipant(e.getPlayer());
            if (p == null) return;
            Bukkit.broadcastMessage(p.getFormattedName() + " disconnected!");
            updatePlayersAlive(p);
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
    public void onInventoryClick(InventoryClickEvent e) {
        /*
        if (e.getView().getType().equals(InventoryType.CHEST)) {
            Player p = (Player) e.getWhoClicked();
            ItemStack crossbow = e.getCursor();
            if (crossbow == null || !crossbow.getType().equals(Material.CROSSBOW)) return;
            CrossbowMeta cbm1 = (CrossbowMeta) crossbow.getItemMeta();
            // check if player has another charged crossbow
            if (!cbm1.getChargedProjectiles().isEmpty()) {
                for (ItemStack i : p.getInventory()) {
                    if (i != null && i.getType().equals(Material.CROSSBOW)) {
                        CrossbowMeta cbm2 = (CrossbowMeta) i.getItemMeta();
                        if (!cbm2.getChargedProjectiles().isEmpty()) {
                            p.sendMessage(ChatColor.RED + "You cannot have more than one charged crossbow!");
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
            return;
        }
         */

        if (e.getView().getTitle().equals("Enchanting")) {
            // handle custom enchants;
            handleEnchantGUI(e);
            return;
        }
        Player p = (Player) e.getWhoClicked();
        ItemStack book = e.getCursor();
        if (book == null || book.getType() != Material.ENCHANTED_BOOK) return;
        ItemStack tool = e.getCurrentItem();
        if (tool == null) return;
        if (tool.getEnchantments().size() > 0) {
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
        if (toolName.contains("SWORD") && (ench.equals(Enchantment.KNOCKBACK) || ench.equals(Enchantment.DAMAGE_ALL))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (tool.getType().equals(Material.CROSSBOW) && (ench.equals(Enchantment.QUICK_CHARGE) || ench.equals(Enchantment.MULTISHOT))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (toolName.contains("BOOTS") && (ench.equals(Enchantment.PROTECTION_FALL) || ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL))) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (armorNotBoots && ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else if (tool.getType().equals(Material.BOW) && ench.equals(Enchantment.ARROW_DAMAGE)){
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
            tool.addEnchantment(ench, 1);
            e.setCursor(null);
        } else {
            p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
            p.sendMessage(ChatColor.RED + "Cannot apply this enchantment to this item!");
            e.setCancelled(true);
        }
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
        sharpness.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        ItemMeta sharpnessMeta = sharpness.getItemMeta();
        sharpnessMeta.setDisplayName(ChatColor.RED+"Sharpness I");
        sharpness.setItemMeta(sharpnessMeta);
        sharpness.setLore(List.of("Cost: 3 XP"));
        items[0] = new GUIItem(sharpness, Enchantment.DAMAGE_ALL, 3);

        ItemStack knockback = new ItemStack(Material.IRON_SWORD);
        knockback.addEnchantment(Enchantment.KNOCKBACK, 1);
        ItemMeta knockMeta = knockback.getItemMeta();
        knockMeta.setDisplayName(ChatColor.RED+"Knockback I");
        knockback.setItemMeta(knockMeta);
        knockback.setLore(List.of("Cost: 1 XP"));
        items[1] = new GUIItem(knockback, Enchantment.KNOCKBACK, 1);

        ItemStack protection = new ItemStack(Material.DIAMOND_CHESTPLATE);
        protection.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
        ItemMeta protMeta = protection.getItemMeta();
        protMeta.setDisplayName(ChatColor.GREEN+"Protection I");
        protMeta.setLore(List.of("Cost: 2 XP"));
        protection.setItemMeta(protMeta);
        items[2] = new GUIItem(protection, Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        ItemStack featherFalling = new ItemStack(Material.IRON_BOOTS);
        featherFalling.addEnchantment(Enchantment.PROTECTION_FALL, 1);
        ItemMeta ffMeta = featherFalling.getItemMeta();
        ffMeta.setDisplayName(ChatColor.GREEN+"Feather Falling I");
        featherFalling.setItemMeta(ffMeta);
        featherFalling.setLore(List.of("Cost: 1 XP"));
        items[3] = new GUIItem(featherFalling, Enchantment.PROTECTION_FALL, 1);

        ItemStack power = new ItemStack(Material.BOW);
        power.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        ItemMeta powerMeta = power.getItemMeta();
        powerMeta.setDisplayName(ChatColor.RED+"Power I");
        power.setItemMeta(powerMeta);
        power.setLore(List.of("Cost: 3 XP"));
        items[4] = new GUIItem(power, Enchantment.ARROW_DAMAGE, 3);

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

    /**
     * Reset all transformed crates
     */
    /*
    public void resetCrates() {
        for (SupplyCrate crate : crates) {
            crate.getLocation().getBlock().setType(Material.CHEST);
        }
        crates.clear();
    }
     */
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