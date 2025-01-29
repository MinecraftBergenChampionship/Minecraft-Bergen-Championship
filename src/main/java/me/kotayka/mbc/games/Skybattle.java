package me.kotayka.mbc.games;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.skybattleMap.Campfire;
import me.kotayka.mbc.gameMaps.skybattleMap.SkybattleMap;
import me.kotayka.mbc.gamePlayers.SkybattlePlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Skybattle extends Game {
    public final Particle.DustOptions BORDER_PARTICLE = new Particle.DustOptions(Color.RED, 3);
    public final Particle.DustOptions TOP_BORDER_PARTICLE = new Particle.DustOptions(Color.ORANGE, 3);
    public SkybattleMap map = new Campfire(this);
    public Map<UUID, SkybattlePlayer> skybattlePlayerMap = new HashMap<>();
    // Primed TNT Entity, Player (that placed that block); used for determining kills since primed tnt is spawned by world
    public Map<Entity, Player> TNTPlacers = new HashMap<Entity, Player>(5);
    // Creeper Entity, Player (that spawned them); used for determining kills by creeper explosion
    public Map<Entity, Player> creeperSpawners = new HashMap<Entity, Player>(5);
    public Map<Entity,Player> witchSpawners = new HashMap<>();
    private final int KILL_POINTS = 9;
    private int deadTeams = 0; // just to avoid sync issues w/teamsAlive.size()
    private Map<MBCTeam, Integer> teamPlacements = new HashMap<>();
    private final int SURVIVAL_POINTS = 1;
    private final int WIN_POINTS = 0;
    // Team bonuses split among team
    private final int[] TEAM_BONUSES_3 = {30, 21, 18, 15, 12, 9};

    public Skybattle() {
        super("Skybattle", new String[] {
                "⑫ Make your way to the center of the map, collecting resources and eliminating other players as you go!\n\n" + 
                "⑫ Utility items are spread across the map, but the best items are always in the middle.",
                "⑫ You'll have " + ChatColor.BOLD + "infinite blt ocks" + ChatColor.RESET + " so build as much as you need to!\n\n" + 
                "⑫ Just be careful of the void below...",
                "⑫ Make sure to " + ChatColor.BOLD + "TURN ON PARTICLES" + ChatColor.RESET + " to see the border.\n\n" +
                "⑫ There's also a " + ChatColor.BOLD + "height border" + ChatColor.RESET + " that lowers over time!",
                ChatColor.BOLD + "Scoring: \n" + ChatColor.RESET +
                                "⑫ +9 points for eliminations\n" +
                                "⑫ +1 points for every player outlived\n" +
                                "⑫ Team Bonuses (split amongst team):\n" +
                                "     ⑫ 1st: +10 points, 2nd: +7 points, 3rd: +6 points\n" +
                                "     ⑫ 4th: +5 points, 5th: +4 points, 6th: +3 points"
        });
    }
    private int roundNum = 1;

    @Override
    public void createScoreboard(Participant p) {
        createLine(22, ChatColor.GREEN+""+ChatColor.BOLD+"Round: " + ChatColor.RESET+roundNum+"/3", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        updatePlayersAliveScoreboard();
        if (skybattlePlayerMap.size() < 1) {
            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+"0", p);
        } else {
            for (SkybattlePlayer x : skybattlePlayerMap.values()) {
                createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+x.kills, p);
            }
        }

        updateInGameTeamScoreboard();
    }

    @Override
    public void start() {
        super.start();

        setGameState(GameState.TUTORIAL);
        //setGameState(GameState.STARTING);

        setTimer(30);
    }

    public void loadPlayers() {
        setPVP(false);
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
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 255, false, false));
            if (roundNum == 1) {
                skybattlePlayerMap.put(p.getPlayer().getUniqueId(), new SkybattlePlayer(p));
                playersAlive.add(p);
            } else {
                resetAliveLists();
            }
            // reset scoreboard & variables after each round
            updatePlayersAliveScoreboard(p);
            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+(Objects.requireNonNull(skybattlePlayerMap.get(p.getPlayer().getUniqueId()))).kills, p);
        }
        map.spawnPlayers();
    }

    /**
     * Used for determining kills by mapping each entity to the player that placed them.
     * If a killed player's last damager is in either map, the kill credit goes to the placer.
     *
     * creeperSpawners maps Creeper Entities to the player that summoned them.
     * TNTPlacers maps Primed TNT Entities to the player that placed the TNT.
     * skybattlePlayerMap is the map of all skybattlePlayers.
     *
     */
    public void resetMaps() {
        if (creeperSpawners != null) {
            creeperSpawners.clear();
        }
        if (witchSpawners != null) {
            witchSpawners.clear();
        }

        if (TNTPlacers != null) {
            TNTPlacers.clear();
        }

        if (teamPlacements != null) {
            teamPlacements.clear();
        }
        deadTeams = 0;

        // not sure if necessary
        if (skybattlePlayerMap != null) {
            for (SkybattlePlayer p : skybattlePlayerMap.values()) {
                p.lastDamager = null;
            }
        }
    }

    @Override
    public void onRestart() {
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
                timeRemaining = 15;
            } else if (timeRemaining % 7 == 0) {
                Introduction();
            }
        } else if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown(Sound.ITEM_GOAT_HORN_SOUND_1);
                if (timeRemaining == 9) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_7, SoundCategory.RECORDS, 1, 1);
                    }
                }
            } else {
                setGameState(GameState.ACTIVE);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_2, SoundCategory.RECORDS, 0.75f, 1);
                    p.playSound(p, Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS, 1, 1);
                }
                map.removeBarriers();
                setPVP(true);
                for (SkybattlePlayer p : skybattlePlayerMap.values()) {
                    p.getPlayer().setInvulnerable(false);
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                    p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                    p.getPlayer().removePotionEffect(PotionEffectType.SATURATION);
                }
                timeRemaining = 240;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            map.Border();

            if (timeRemaining == 0) {
                if (teamsAlive.size() > 1) {
                    timeRemaining = 30;
                    setGameState(GameState.OVERTIME);
                } else {
                    if (roundNum < 3) {
                        roundOverGraphics();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS);
                        }
                        for (Participant p : playersAlive) {
                            MBCTeam t = p.getTeam();
                            teamPlacements.put(t, 1);
                        }
                        placementPoints();
                        roundWinners(WIN_POINTS, SURVIVAL_POINTS);
                        timeRemaining = 10;
                        setGameState(GameState.END_ROUND);
                    } else {
                        gameOverGraphics();
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.stopSound(Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS);
                        }
                        placementPoints();
                        roundWinners(WIN_POINTS, SURVIVAL_POINTS);
                        timeRemaining = 38;
                        setGameState(GameState.END_GAME);
                    }
                }
            }

            if (timeRemaining == 210) {
                for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
                    p.getPlayer().sendTitle(" ", ChatColor.DARK_RED+"Border shrinking", 0, 60, 20);
                }
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED+"Horizontal border is shrinking!");
            } else if (timeRemaining == 180) {
                Bukkit.broadcastMessage(MBC.MBC_STRING_PREFIX + ChatColor.DARK_RED+"Vertical border is falling!");
            }

            if (timeRemaining <= 210) {
                if (map.getBorderRadius() >= 0) {
                    map.reduceBorderRadius(map.getBorderShrinkRate());
                }
            }
            if (timeRemaining <= 180) {
                map.reduceBorderHeight(map.getVerticalBorderShrinkRate());
            }
        } else if (getState().equals(GameState.OVERTIME)) {
            map.Overtime();
            if (timeRemaining == 0 && roundNum < 3) {
                roundOverGraphics();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS);
                }
                roundWinners(WIN_POINTS, SURVIVAL_POINTS);
                for (Participant p : playersAlive) {
                    MBCTeam t = p.getTeam();
                    teamPlacements.put(t, 1);
                }
                placementPoints();
                timeRemaining = 10;
                setGameState(GameState.END_ROUND);
            } else if (timeRemaining == 0) {
                gameOverGraphics();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.stopSound(Sound.MUSIC_DISC_STAL, SoundCategory.RECORDS);
                }
                roundWinners(WIN_POINTS, SURVIVAL_POINTS);
                placementPoints();
                timeRemaining = 38;
                setGameState(GameState.END_GAME);
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 1) {
                roundNum++;
                map.resetMap();
                loadPlayers();
                createLineAll(22, ChatColor.GREEN+""+ChatColor.BOLD+"Round: " + ChatColor.RESET+roundNum+"/3");
                timeRemaining = 20;
                setGameState(GameState.STARTING);
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining <= 35) {
                gameEndEvents();
            }
        }
    }


    /**
     * Handles auto-priming of TNT and giving players infinite concrete
     * @param e BlockPlaceEvent e
     */
    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Player p = e.getPlayer();

        // auto-ignite TNT
        if (e.getBlock().getType().equals(Material.TNT)) {
            b.setType(Material.AIR);
            BlockFace blockFace = e.getBlockAgainst().getFace(b);
            Location spawn = getLocationToSpawnEntity(p.getTargetBlock(null, 5), blockFace);
            TNTPlacers.put(map.getWorld().spawnEntity(spawn, EntityType.TNT), p);
        } else if (e.getBlock().getType().equals(Material.TNT_MINECART)) {
            Block against = e.getBlockAgainst();
            if (against.getType().toString().contains("RAIL")) {
                Location spawn = p.getTargetBlock(null, 5).getLocation();
                spawn.add(0.5, 1.5, 0.5);
                TNTPlacers.put(map.getWorld().spawnEntity(spawn, EntityType.TNT_MINECART), p);
            } else {
                e.setCancelled(true);
            }
        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            // if block was concrete, give appropriate amount back
            String concrete = e.getBlock().getType().toString();
            // check item slot
            if (e.getHand() == EquipmentSlot.HAND) {
                int index = p.getInventory().getHeldItemSlot();
                int amt = Objects.requireNonNull(p.getInventory().getItem(index)).getAmount();
                p.getInventory().setItem(index, new ItemStack(Objects.requireNonNull(Material.getMaterial(concrete)), amt));
                return;
            }
            if (e.getHand() == EquipmentSlot.OFF_HAND) {
                int amt = Objects.requireNonNull(p.getInventory().getItem(40)).getAmount();
                p.getInventory().setItem(40, new ItemStack(Objects.requireNonNull(Material.getMaterial(concrete)), amt));
            }
        }
    }

    /**
     * Handle players spawning creepers (solely for kill credit)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!isGameActive()) return;

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.CREEPER_SPAWN_EGG) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            for(int i = 0; i < p.getInventory().getSize(); i++){
                ItemStack itm = p.getInventory().getItem(i);
                if(itm != null && itm.getType().equals(Material.CREEPER_SPAWN_EGG)) {
                    int amt = itm.getAmount() - 1;
                    itm.setAmount(amt);
                    p.getInventory().setItem(i, amt > 0 ? itm : null);
                    p.updateInventory();
                    break;
                }
            }

            // Add each creeper spawned to a map, use to check kill credit
            Location spawn = getLocationToSpawnEntity(p.getTargetBlock(null, 5), e.getBlockFace());
            creeperSpawners.put(p.getWorld().spawn(spawn, Creeper.class), p);
        }

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getMaterial() == Material.WITCH_SPAWN_EGG) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            for(int i = 0; i < p.getInventory().getSize(); i++){
                ItemStack itm = p.getInventory().getItem(i);
                if(itm != null && itm.getType().equals(Material.WITCH_SPAWN_EGG)) {
                    int amt = itm.getAmount() - 1;
                    itm.setAmount(amt);
                    p.getInventory().setItem(i, amt > 0 ? itm : null);
                    p.updateInventory();
                    break;
                }
            }

            // Add each creeper spawned to a map, use to check kill credit
            Location spawn = getLocationToSpawnEntity(p.getTargetBlock(null, 5), e.getBlockFace());
            witchSpawners.put(p.getWorld().spawn(spawn, Witch.class), p);
        }
    }

    /**
     *
     */
    public void placementPoints() {
        for (MBCTeam t : getValidTeams()) {
            for (Participant p : t.getPlayers()) {
                if (p.getTeam() == MBC.getInstance().spectator) continue;
                int placement = teamPlacements.get(t);
                p.addCurrentScore(TEAM_BONUSES_3[placement-1] / t.getPlayers().size());
                p.getPlayer().sendMessage(ChatColor.GREEN + "Your team came in " + getPlace(placement) + "!" + MBC.scoreFormatter((int)(TEAM_BONUSES_3[placement-1] / t.getPlayers().size())));
            }
        }
    }

    /**
     * Arithmetic to
     * @param target Block to be placed on
     * @param blockFace Corresponding blockFace the new entity will spawn on or next to
     * @return The Location to spawn the entity
     */
    private Location getLocationToSpawnEntity(Block target, BlockFace blockFace) {
        Location l = target.getLocation();
        l.add(0.5, 0, 0.5);
        // west -x east +x south +z north -z
        switch (blockFace) {
            case EAST:
                l.add(1, 0, 0);
                break;
            case WEST:
                l.add(-1, 0, 0);
                break;
            case SOUTH:
                l.add(0, 0, 1);
                break;
            case NORTH:
                l.add(0, 0, -1);
                break;
            default: // UP
                l.add(0, 1, 0);
        }
        return l;
    }

    /**
     * All concrete broken during game shouldn't drop itself
     */
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        if (!isGameActive()) return;
        if (!e.getBlock().getLocation().getWorld().toString().equals(map.getWorld().toString())) return;
        if (e.getBlock().getType().toString().endsWith("CONCRETE")) return;

        if (e.getBlock().getType() == Material.IRON_ORE) {
            map.getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(Material.IRON_INGOT, 1));
            return;
        }

        for (ItemStack i : e.getBlock().getDrops()) {
            map.getWorld().dropItemNaturally(e.getBlock().getLocation(), i);
        }
    }

    /**
     * Track damage for appropriate kill credit
     */
    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (!isGameActive()) return;
        if (!((e.getEntity()) instanceof Player)) return;

        SkybattlePlayer player = skybattlePlayerMap.get(e.getEntity().getUniqueId());
        if (player == null) return;

        if (e.getDamager() instanceof Player) {
            Participant damager = Participant.getParticipant((Player) e.getDamager());
            if (damager.getTeam().equals(player.getParticipant().getTeam())) return;
        }

        // if creeper hurt player, last damager was who spawned that creeper
        if (creeperSpawners.containsKey(e.getDamager())) {
            Participant damager = Participant.getParticipant(creeperSpawners.get(e.getDamager()));
            if (damager.getTeam().equals(player.getParticipant().getTeam())) return;
            player.lastDamager = creeperSpawners.get(e.getDamager());
            return;
        }

        if (witchSpawners.containsKey(e.getDamager())) {
            Participant damager = Participant.getParticipant(witchSpawners.get(e.getDamager()));
            if (damager.getTeam().equals(player.getParticipant().getTeam())) return;
            player.lastDamager = witchSpawners.get(e.getDamager());
            return;
        }

        if (TNTPlacers.containsKey(e.getDamager())) {
            Participant damager = Participant.getParticipant(TNTPlacers.get(e.getDamager()));
            if (damager.getTeam().equals(player.getParticipant().getTeam())) return;
            player.lastDamager = TNTPlacers.get(e.getDamager());
            return;
        }

        // for any general attack
        if (e.getDamager() instanceof Player) {
            player.lastDamager = (Player) e.getDamager();
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        // TODO this isn't registering for some reason
        if (!isGameActive()) return;
        if (e.getHitEntity() == null) return;
        if (!(e.getEntity().getShooter() instanceof Player) || !(e.getHitEntity() instanceof Player)) return;

        /* temp
        if(e.getEntityType() != EntityType.SNOWBALL) return;
        if(!(e.getEntity().getShooter() instanceof Player) || !(e.getHitEntity() instanceof Player)) return;
        */

        SkybattlePlayer player = skybattlePlayerMap.get(e.getHitEntity().getUniqueId());
        Participant damager = Participant.getParticipant((Player) e.getEntity().getShooter());
        if (player == null || damager == null) return;

        if (player.getParticipant().getTeam().equals(damager.getTeam())) return;

        if (e.getEntity() instanceof Snowball) {
            snowballHit((Snowball) e.getEntity(), player.getPlayer());
            player.lastDamager = damager.getPlayer();
            return;
        }
        player.lastDamager = (Player) e.getEntity().getShooter();
    }

    @EventHandler
    public void onSplashEvent(PotionSplashEvent e) {
        if (!isGameActive()) return;

        if (!(e.getHitEntity() instanceof Player)) return;

        if (e.getPotion().getShooter() instanceof Witch && witchSpawners.containsKey((Witch) e.getPotion().getShooter())) {
            Participant damager = Participant.getParticipant(witchSpawners.get((Witch) e.getPotion().getShooter()));
            SkybattlePlayer hurt = skybattlePlayerMap.get(((Player) e.getHitEntity()).getUniqueId());
            if (damager.getTeam().equals(hurt.getParticipant().getTeam())) return;
            hurt.lastDamager = damager.getPlayer();
        }

        if (!(e.getPotion().getShooter() instanceof Player)) return;

        ThrownPotion potion = e.getPotion();

        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect effect : effects) {
            PotionEffectType potionType = effect.getType();
            if (!potionType.equals(PotionEffectType.INSTANT_DAMAGE)) return;
        }

        SkybattlePlayer player = skybattlePlayerMap.get(e.getHitEntity().getUniqueId());
        if (player == null) return;

        Participant damager = Participant.getParticipant((Player) e.getEntity().getShooter());
        if (damager.getTeam().equals(player.getParticipant().getTeam())) return;

        player.lastDamager = (Player) potion.getShooter();
    }

    /**
     * Give kill credit to last damager ONLY if nobody else had hit them between
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!isGameActive()) return;
        SkybattlePlayer player = skybattlePlayerMap.get(e.getEntity().getUniqueId());
        if (player == null) return;

        // remove any concrete
        for (ItemStack i : player.getPlayer().getInventory()) {
            if (i != null && i.getType().toString().endsWith("CONCRETE")) {
                player.getPlayer().getInventory().remove(i);
            }
        }

        SkybattlePlayer killer = skybattlePlayerMap.get(e.getEntity().getUniqueId());
        if (player.lastDamager != null) {
            killer = skybattlePlayerMap.get(player.lastDamager.getUniqueId());
        }

        EntityDamageEvent damageEvent = e.getPlayer().getLastDamageCause();
        if (damageEvent == null) {
            if (killer == null) {
                e.setDeathMessage(player.getParticipant().getFormattedName() + " died mysteriously!");
            } else {
                e.setDeathMessage(player.getParticipant().getFormattedName() + " mysteriously died to " + killer.getParticipant().getFormattedName());
                killer.getParticipant().addCurrentScore(KILL_POINTS);
                killer.kills++;
                createLine(2, ChatColor.YELLOW + "" + ChatColor.BOLD + "Your kills: " + ChatColor.RESET + killer.kills, killer.getParticipant());
            }
            updatePlayersAlive(player.getParticipant());
        } else {
            skybattleDeathGraphics(e, damageEvent.getCause());
        }

        if (killer!=null) {
            killer.getPlayer().playSound(killer.getPlayer(), Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.BLOCKS, 0.5f, 1);
        }

        int count = 0;
        for (Participant p : player.getParticipant().getTeam().teamPlayers) {
            if (p.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                count++;
            }
        }

        if (count == player.getParticipant().getTeam().teamPlayers.size() && player.getParticipant().getTeam() != MBC.getInstance().spectator) {
            teamPlacements.put(player.getParticipant().getTeam(), getValidTeams().size() - deadTeams);
            deadTeams++;
        }

        e.setCancelled(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (playersAlive.contains(Participant.getParticipant(p))) {
                p.sendMessage(e.getDeathMessage() + MBC.scoreFormatter(SURVIVAL_POINTS));
            }
            else {
                p.sendMessage(e.getDeathMessage());
            }
        }

        getLogger().log(e.getDeathMessage());

        // Drop Player's Items
        for (ItemStack i : player.getPlayer().getInventory()) {
            if (i == null) continue;
            if (i.getType().toString().endsWith("CONCRETE")) continue;

            map.getWorld().dropItemNaturally(player.getPlayer().getLocation(), i);
        }

        // Dealing with now dead player
        player.getPlayer().setGameMode(GameMode.SPECTATOR);
        if (player.getPlayer().getLocation().getY() < map.getVoidHeight()) {
            player.getPlayer().teleport(map.getCenter());
        }

        // Scoring
        for (Participant p : playersAlive) {
            p.addCurrentScore(SURVIVAL_POINTS);
        }
    }

    /**
     * Explicitly handles deaths where the player dies indirectly to combat or to
     * custom damage (border or void).
     * Checks for last team remaining.
     * @param e Event thrown when a player dies
     */
    public void skybattleDeathGraphics(PlayerDeathEvent e, EntityDamageEvent.DamageCause damageCause) {
        SkybattlePlayer victim = skybattlePlayerMap.get(e.getPlayer().getUniqueId());
        String deathMessage = e.getDeathMessage();

        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 30);
        MBC.spawnFirework(victim.getParticipant());
        deathMessage = deathMessage.replace(victim.getPlayer().getName(), victim.getParticipant().getFormattedName());

        if (victim.lastDamager != null) {
            SkybattlePlayer killer = skybattlePlayerMap.get(victim.lastDamager.getUniqueId());
            killer.kills++;

            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+killer.kills, killer.getParticipant());
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayer().getName() + "!" + MBC.scoreFormatter(KILL_POINTS));
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            killer.getParticipant().addCurrentScore(KILL_POINTS);

            switch (damageCause) {
                case ENTITY_ATTACK:
                case ENTITY_SWEEP_ATTACK:
                    deathMessage = victim.getParticipant().getFormattedName() + " was slain by " + killer.getParticipant().getFormattedName();
                    break;
                case PROJECTILE:
                    deathMessage = victim.getParticipant().getFormattedName() + " was shot by " + killer.getParticipant().getFormattedName();
                    break;
                case CUSTOM:
                    if (victim.voidDeath) {
                        deathMessage = victim.getParticipant().getFormattedName() + " didn't want to live in the same world as " + killer.getParticipant().getFormattedName();
                        victim.getPlayer().getInventory().clear();
                        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
                        victim.getPlayer().teleport(map.getCenter());
                        victim.voidDeath = false;
                    } else {
                        deathMessage = victim.getParticipant().getFormattedName() + " was killed in the border whilst fighting " + killer.getParticipant().getFormattedName();
                    }
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
                default:
                    deathMessage = victim.getParticipant().getFormattedName() + " has died to " + killer.getParticipant().getFormattedName();
                    break;
            }
        } else if (victim.getPlayer().getKiller() != null) {
            SkybattlePlayer killer = skybattlePlayerMap.get(victim.getPlayer().getKiller().getUniqueId());
            killer.kills++;

            createLine(2, ChatColor.YELLOW+""+ChatColor.BOLD+"Your kills: "+ChatColor.RESET+killer.kills, killer.getParticipant());
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + victim.getPlayer().getName() + "!" + MBC.scoreFormatter(KILL_POINTS));
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            killer.getParticipant().addCurrentScore(KILL_POINTS);

            if (!(deathMessage.contains(" " + killer.getPlayer().getName()))) {
                deathMessage += " whilst fighting " + killer.getParticipant().getFormattedName();
            }
        } else {
            // if no killer, the player killed themselves
            if (damageCause.equals(EntityDamageEvent.DamageCause.CUSTOM)) {
                if (victim.voidDeath) {
                    deathMessage = victim.getParticipant().getFormattedName() + " fell out of the world";
                    victim.getPlayer().getInventory().clear();
                    victim.getPlayer().setGameMode(GameMode.SPECTATOR);
                    victim.getPlayer().teleport(map.getCenter());
                    victim.voidDeath = false;
                } else {
                    deathMessage = victim.getParticipant().getFormattedName() + " was killed by border damage";
                }
            }
        }

        e.setDeathMessage(deathMessage);

        updatePlayersAlive(victim.getParticipant());
    }

    /**
     * Use SkybattlePlayer's lastDamager to track kills if players fall into void
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getState().equals(GameState.ACTIVE) && !getState().equals(GameState.END_ROUND) && !getState().equals(GameState.END_GAME) && !getState().equals(GameState.OVERTIME)) return;
        if (!(e.getPlayer().getWorld().equals(map.getWorld()))) return;

        SkybattlePlayer player = skybattlePlayerMap.get(e.getPlayer().getUniqueId());
        if (player == null) return;

        // kill players immediately in void
        if (player.getPlayer().getLocation().getY() <= map.getVoidHeight()) {
            if (player.getPlayer().getGameMode() == GameMode.SPECTATOR || !(getState().equals(GameState.OVERTIME) || getState().equals(GameState.ACTIVE))) {
                player.getPlayer().teleport(map.getCenter());
                return;
            }
            player.voidDeath = true;
            player.getPlayer().damage(50);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        if (!isGameActive()) return;
        if (!(e.getCaught() instanceof Player)) return;

        SkybattlePlayer hooked = skybattlePlayerMap.get(e.getCaught().getUniqueId());
        if (hooked == null) return;
        hooked.lastDamager = e.getPlayer();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) {
            SkybattlePlayer p = skybattlePlayerMap.get(e.getPlayer().getUniqueId());

            updatePlayersAlive(p.getParticipant());

            for (Player play : Bukkit.getOnlinePlayers()) {
                if (playersAlive.contains(Participant.getParticipant(play))) {
                    play.sendMessage(p.getParticipant().getFormattedName() + " disconnected!" + MBC.scoreFormatter(SURVIVAL_POINTS));
                }
                else {
                    play.sendMessage(p.getParticipant().getFormattedName() + " disconnected!");
                }
            }

            if (p.lastDamager != null) {
                SkybattlePlayer killer = skybattlePlayerMap.get(p.getPlayer().getUniqueId());
                if (killer == null) return;
                killer.getParticipant().addCurrentScore(KILL_POINTS);
                killer.getPlayer().sendMessage(ChatColor.GREEN+"You killed " + p.getParticipant().getPlayerName() + "!" + MBC.scoreFormatter(KILL_POINTS));
                killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + p.getParticipant().getFormattedName(), 0, 60, 20);
                killer.kills++;
            }

            for (Participant x : playersAlive) {
                x.addCurrentScore(SURVIVAL_POINTS);
            }
        }
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        SkybattlePlayer p = skybattlePlayerMap.get(e.getPlayer().getUniqueId());
        if (p == null) return;
        p.voidDeath = false;
        p.lastDamager = null;
        p.setPlayer(e.getPlayer());

        // if log back in during paused/starting, manually teleport them
        if (!(getState().equals(GameState.PAUSED)) && !(getState().equals(GameState.STARTING))) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().teleport(map.getCenter());
        }
    }

    public void resetPlayers() {
        for (SkybattlePlayer p : skybattlePlayerMap.values()) {
            p.kills = 0;
            p.voidDeath = false;
            p.lastDamager = null;
        }
    }

}
