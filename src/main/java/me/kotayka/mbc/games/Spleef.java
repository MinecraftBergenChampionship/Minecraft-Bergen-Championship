package me.kotayka.mbc.games;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.spleefMap.Classic;
import me.kotayka.mbc.gameMaps.spleefMap.SkySpleef;
import me.kotayka.mbc.gameMaps.spleefMap.Space;
import me.kotayka.mbc.gameMaps.spleefMap.SpleefMap;
import me.kotayka.mbc.gamePlayers.SpleefPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class Spleef extends Game {
    private SpleefMap map = null;
    private List<SpleefMap> maps = new ArrayList<>(
            Arrays.asList(new Classic(), new Space(), new SkySpleef())
    );
    //private List<SpleefMap> maps = new ArrayList<>(Arrays.asList(new Classic()));
    //public List<SpleefPlayer> spleefPlayers = new ArrayList<SpleefPlayer>();
    public Map<UUID, SpleefPlayer> spleefPlayers = new HashMap<>();
    private int roundNum = 0;
    private final Location lobby;
    private final Location spawnpoint;
    private final int RESET_DAMAGE_TIME = 5;
    private List<MBCTeam> fullyAliveTeams = getValidTeams();

    // scoring
    private final int SURVIVAL_POINTS = 2;
    private final int KILL_POINTS = 2;
    private final int LAST_TEAM_BONUS = 5;
    private final int[] BONUS_POINTS = {20, 15, 15, 10, 10, 8, 8, 8, 5, 5, 3, 3}; // 24 player; these numbers are arbitrary
    // NOTE: 16 player bonus is probably different

    public Spleef() {
        super("Spleef");
        lobby = new Location(Bukkit.getWorld("spleef"), 0, 126, 0);
        spawnpoint = new Location(Bukkit.getWorld("spleef"), 0, 115, 0);
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);
        if (roundNum != 1) {
            if (getSpleefPlayer(p.getPlayer()) != null) {
                createLine(1, ChatColor.YELLOW+""+ChatColor.BOLD+"Spleefs: "+ChatColor.RESET+getSpleefPlayer(p.getPlayer()).getKills(), p);
            }
        } else {
            createLine(1, ChatColor.YELLOW+""+ChatColor.BOLD+"Spleefs: "+ChatColor.RESET+"0", p);
        }
        updatePlayersAliveScoreboard();
        updateInGameTeamScoreboard();
    }

    @Override
    public void start() {
        super.start();

        //setGameState(GameState.TUTORIAL);
        setGameState(GameState.STARTING);

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            p.getPlayer().setGameMode(GameMode.ADVENTURE);
        }

        startRound();
    }

    @Override
    public void onRestart() {
        roundNum = 0;
        resetPlayers();
        maps = new ArrayList<>(
                Arrays.asList(new Classic(), new Space(), new SkySpleef())
        );
        map.resetMap();
    }

    @Override
    public void loadPlayers() {
        setPVP(false);
        if (map != null) {
            map.deleteMap();
        }
        loadMap();
        if (roundNum == 0) {
            teamsAlive.addAll(getValidTeams());
        }
        openFloor(false);

        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().teleport(lobby);
            p.getPlayer().getInventory().clear();
            p.getPlayer().setFlying(false);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setHealth(20);
            p.getPlayer().setGameMode(GameMode.ADVENTURE);

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 10, false, false));
            if (roundNum == 0) {
                spleefPlayers.put(p.getPlayer().getUniqueId(), new SpleefPlayer(p));
                playersAlive.add(p);
            } else {
                resetAliveLists();
            }
            // reset scoreboard & variables after each round
            updatePlayersAliveScoreboard(p);
        }

        fullyAliveTeams.clear();
        fullyAliveTeams = getValidTeams();
    }

    public void startRound() {
        if (roundNum > 3) {
            setGameState(GameState.END_GAME);
            timeRemaining = 37;
            return;
        } else {
            roundNum++;
        }

        resetSpleefers();

        if (map == null) {
            loadMap();
        }

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            createLine(22, ChatColor.AQUA+""+ChatColor.BOLD+"Map: "+ChatColor.RESET+map.Name(), p);
            createLine(21, ChatColor.GREEN +  "Round: "+ ChatColor.RESET+roundNum+"/3", p);
        }

        setGameState(GameState.STARTING);
        setTimer(30);
    }

    private void loadMap() {
        map = maps.get((int) (Math.random()*maps.size()));
        maps.remove(map);
        map.resetMap();
    }

    @Override
    public void events() {
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining > 0) {
                startingCountdown();
            } else {
                setPVP(true);
                openFloor(true);
                for (Participant p : MBC.getInstance().getPlayers()) {
                    p.getPlayer().setGameMode(GameMode.SURVIVAL);
                }
                setGameState(GameState.ACTIVE);
                timeRemaining = 240;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            checkResetDamagers();
            map.Border(timeRemaining);
            if (timeRemaining == 0) {
                setPVP(false);
                for (Participant p : playersAlive) {
                    (getSpleefPlayer(p.getPlayer())).setPlacement(1);
                }

                if (roundNum < 3) {
                    roundOverGraphics();
                    roundWinners(0);
                    placementPoints();
                    setGameState(GameState.END_ROUND);
                    timeRemaining = 9;
                } else {
                    gameOverGraphics();
                    roundWinners(0);
                    placementPoints();
                    setGameState(GameState.END_GAME);
                    timeRemaining = 37;
                }
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                openFloor(false);
                loadPlayers();
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                map.deleteMap();
            }
            gameEndEvents();
        }
    }


    /**
     * Sets lobby floor to barriers or air.
     * @param b true = set air, false = set barriers
     */
    public void openFloor(boolean b) {
        Material m = b ? Material.AIR : Material.BARRIER;

        for (int x = -10; x < 11; x++) {
            for (int z = -10; z < 11; z++) {
                map.getWorld().getBlockAt(x, 125, z).setType(m);
            }
        }
    }

    /**
     * Award placement points to Top 12 spleef players; this should probably be adjusted for < 24 players
     */
    public void placementPoints() {
        for (SpleefPlayer p : spleefPlayers.values()) {
            if (p.getPlacement() > 0 && p.getPlacement() <= BONUS_POINTS.length) {
                int placement = p.getPlacement();
                int points = BONUS_POINTS[placement-1];
                p.getParticipant().addCurrentScore(points);
                p.getPlayer().sendMessage(ChatColor.GREEN+"You placed " + getPlace(placement) + " and earned a bonus of " + (points*MBC.getInstance().multiplier) + " points!");
            }
        }

        // in case multiple teams do not die (??)
        for (MBCTeam t : fullyAliveTeams) {
            for (Participant p : t.teamPlayers) {
                p.getPlayer().sendMessage(ChatColor.GREEN+"Your team earned a bonus for being the last fully alive team!");
                p.addCurrentScore(LAST_TEAM_BONUS);
            }
        }
    }

    public void handleDeath(SpleefPlayer victim) {
        String deathMessage;
        if (victim.getLastDamager() == null) {
            deathMessage = victim.getParticipant().getFormattedName()+" fell into the void.";
        } else {
            SpleefPlayer killer = getSpleefPlayer(victim.getLastDamager().getPlayer());
            killer.incrementKills();
            killer.getParticipant().addCurrentScore(KILL_POINTS);
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You spleefed " + victim.getPlayer().getName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            createLine(1, ChatColor.YELLOW+""+ChatColor.BOLD+"Spleefs: "+ChatColor.RESET+killer.getKills(), killer.getParticipant());

            deathMessage = victim.getParticipant().getFormattedName()+" was spleefed by " + killer.getParticipant().getFormattedName();
        }

        getLogger().log(deathMessage);
        Bukkit.broadcastMessage(deathMessage);

        updatePlayersAlive(victim.getParticipant());
        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 20);
        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        MBC.spawnFirework(victim.getParticipant());
        victim.getPlayer().teleport(spawnpoint);
        victim.setPlacement(playersAlive.size()+1);

        for (Participant p : playersAlive) {
            p.addCurrentScore(SURVIVAL_POINTS);
        }

        if (fullyAliveTeams.size() > 1) {
            fullyAliveTeams.remove(victim.getParticipant().getTeam());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;

        Player p = e.getPlayer();
        if (p.getLocation().getY() < map.getDeathY()) {
            SpleefPlayer s = getSpleefPlayer(p);
            handleDeath(s);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockDamageEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;
        if (!e.getBlock().getLocation().getWorld().equals(map.getWorld())) return;

        Block b = e.getBlock();
        if (b.getType().equals(Material.PACKED_ICE)) return;
        b.breakNaturally();
        map.getWorld().playSound(b.getLocation(), b.getBlockSoundGroup().getBreakSound(), 1, 1);
        e.getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;
        if (!(e.getEntity() instanceof Snowball) && !(e.getEntity() instanceof Fireball)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        // deal slight knockback to other players
        if (e.getEntity() instanceof Snowball) {
            if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
                Player p = (Player) e.getHitEntity();
                Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());

                snowballHit((Snowball) e.getEntity(), p);
                SpleefPlayer s = getSpleefPlayer(p);
                s.setLastDamager(shooter);
                s.setResetTime(timeRemaining-RESET_DAMAGE_TIME);
            }

            // destroy map blocks (not gold blocks) in contact with snowballs
            if (e.getHitBlock() != null) {
                Block b = e.getHitBlock();
                if (b.getType().equals(Material.GOLD_BLOCK) || b.getType().equals(Material.PACKED_ICE) || b.getType().equals(Material.BARRIER)) return;

                b.breakNaturally();
                map.getWorld().playSound(b.getLocation(), b.getBlockSoundGroup().getBreakSound(), 1, 1);
            }
        } else {
            Fireball f = (Fireball) e.getEntity();
            if (e.getHitEntity() != null && e.getHitEntity().equals(f.getShooter())) return;
            f.getWorld().createExplosion(f.getLocation(), 1, false, true);
            f.remove();
            for (Entity ent : f.getNearbyEntities(3, 3, 3)) {
                if (ent instanceof LargeFireball) { ent.remove(); continue; }
                if (!(ent instanceof Player)) continue;
                Player p = (Player) ent;
                double distance =  f.getLocation().distanceSquared(p.getLocation());
                if (distance <= 0.5) {
                    p.setVelocity(new Location(p.getWorld(), 0, 1.25, 0).toVector());
                } else {
                    p.setVelocity(p.getLocation().subtract(f.getLocation()).toVector().multiply(1/distance));
                }
            }
        }

    }

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;
        if (!getState().equals(GameState.ACTIVE)) return;

        Player p = (Player) e.getEntity();
        SpleefPlayer hit = getSpleefPlayer(p);

        Vector velocity = e.getDamager().getLocation().getDirection();
        p.setVelocity(new Vector(velocity.getX()*0.1, 0.1, velocity.getZ()*0.1));
        p.damage(0.5);
        hit.setLastDamager(Participant.getParticipant((Player) e.getDamager()));
        hit.setResetTime(timeRemaining - RESET_DAMAGE_TIME);
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;
        if (!(e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR))) return;
        if (!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SHOVEL)) return;

        Fireball f = map.getWorld().spawn(e.getPlayer().getEyeLocation(), Fireball.class);
        f.setShooter(e.getPlayer());
        f.setIsIncendiary(false);
        f.setYield(0);
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (!e.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;

        SpleefPlayer p = getSpleefPlayer(e.getPlayer());
        if (p == null) return;
        updatePlayersAlive(p.getParticipant());
        Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " disconnected!");

        Participant killer = p.getLastDamager();
        if (killer != null) {
            killer.addCurrentScore(KILL_POINTS);
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You spleefed " + p.getParticipant().getPlayerName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + p.getParticipant().getFormattedName(), 0, 60, 20);
            getSpleefPlayer(killer.getPlayer()).incrementKills();
        }

        for (Participant n : playersAlive) {
            n.addCurrentScore(SURVIVAL_POINTS);
        }

        p.setPlacement(playersAlive.size()+1);
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        SpleefPlayer p = spleefPlayers.get(e.getPlayer().getUniqueId());
        if (p == null) return; // new login; doesn't matter

        // if log back in during paused/starting, manually teleport them
        if (!(getState().equals(GameState.PAUSED)) && !(getState().equals(GameState.STARTING))) {
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
            e.getPlayer().teleport(spawnpoint);
        } else {
            p.setPlayer(e.getPlayer());
            p.resetKiller();
            p.setResetTime(-1);
            e.getPlayer().setGameMode(GameMode.ADVENTURE);
            e.getPlayer().teleport(lobby);
        }
    }

    public void resetSpleefers() {
        for (SpleefPlayer p : spleefPlayers.values()) {
            p.resetKiller();
            p.setPlacement(-1);
            p.setResetTime(-1);
        }
    }

    private void resetPlayers() {
        for (SpleefPlayer p : spleefPlayers.values()) {
            p.resetKiller();
            p.setPlacement(-1);
            p.setResetTime(-1);
            p.resetKills();
        }
    }

    public void checkResetDamagers() {
        for (SpleefPlayer p : spleefPlayers.values()) {
            if (!(p.getPlayer().getGameMode().equals(GameMode.SURVIVAL))) continue;

            if (p.getResetTime() >= timeRemaining) {
                p.resetKiller();
                p.setResetTime(-1);
            }
        }
    }


    public SpleefPlayer getSpleefPlayer(Player p) {
        return spleefPlayers.get(p.getUniqueId());
    }
}
