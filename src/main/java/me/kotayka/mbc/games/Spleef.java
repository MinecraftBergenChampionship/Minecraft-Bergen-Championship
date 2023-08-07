package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.spleefMap.Classic;
import me.kotayka.mbc.gameMaps.spleefMap.SpleefMap;
import me.kotayka.mbc.gamePlayers.SpleefPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Spleef extends Game {
    private SpleefMap map = new Classic();
    //private List<SpleefMap> maps = new ArrayList<>(Arrays.asList(new Classic()));
    public List<SpleefPlayer> spleefPlayers = new ArrayList<SpleefPlayer>();
    private int roundNum = 0;
    private final Location lobby;
    private final Location spawnpoint;
    private final int RESET_DAMAGE_TIME = 8;

    // scoring
    private final int SURVIVAL_POINTS = 2;
    private final int KILL_POINTS = 2;
    private final int[] BONUS_POINTS = {25, 15, 15, 10, 10, 8, 8, 8, 5, 5, 3, 3}; // 24 player; these numbers are arbitrary
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
    public void loadPlayers() {
        setPVP(false);
        openFloor(false);
        if (roundNum == 0)
            teamsAlive.addAll(getValidTeams());
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
                spleefPlayers.add(new SpleefPlayer(p));
                playersAlive.add(p);
            } else {
                resetAliveLists();
            }
            // reset scoreboard & variables after each round
            updatePlayersAliveScoreboard(p);
            createLine(1, ChatColor.YELLOW+""+ChatColor.BOLD+"Spleefs: "+ChatColor.RESET+getSpleefPlayer(p.getPlayer()).getKills(), p);
        }
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

        //map = maps.get((int) (Math.random()*maps.size()));
        //maps.remove(map);
        map.pasteMap();

        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
            createLine(22, ChatColor.AQUA+""+ChatColor.BOLD+"Map: "+ChatColor.RESET+map.Name(), p);
            createLine(21, ChatColor.GREEN +  "Round: "+ ChatColor.RESET+roundNum+"/3", p);
        }

        setGameState(GameState.STARTING);
        setTimer(30);
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
                timeRemaining = 210;
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            checkResetDamagers();
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
            map.Border();
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                openFloor(false);
                loadPlayers();
                startRound();
                setGameState(GameState.STARTING);
                timeRemaining = 20;
            }
        } else if (getState().equals(GameState.END_GAME)) {
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
        for (SpleefPlayer p : spleefPlayers) {
            if (p.getPlacement() > 0 && p.getPlacement() <= BONUS_POINTS.length) {
                Bukkit.broadcastMessage("[Debug] doing placement for " + p.getPlacement());
                p.getParticipant().addCurrentScore(BONUS_POINTS[p.getPlacement()-1]);
            }
        }
    }

    public void handleDeath(SpleefPlayer victim) {
        if (victim.getLastDamager() == null) {
            Bukkit.broadcastMessage(victim.getParticipant().getFormattedName()+" fell into the void.");
        } else {
            SpleefPlayer killer = getSpleefPlayer(victim.getLastDamager().getPlayer());
            killer.incrementKills();
            killer.getParticipant().addCurrentScore(KILL_POINTS);
            killer.getPlayer().sendMessage(ChatColor.GREEN+"You spleefed " + victim.getPlayer().getName() + "!");
            killer.getPlayer().sendTitle(" ", "[" + ChatColor.BLUE + "x" + ChatColor.RESET + "] " + victim.getParticipant().getFormattedName(), 0, 60, 20);
            createLine(1, ChatColor.YELLOW+""+ChatColor.BOLD+"Spleefs: "+ChatColor.RESET+killer.getKills(), killer.getParticipant());
            Bukkit.broadcastMessage(victim.getParticipant().getFormattedName()+" was spleefed by " + killer.getParticipant().getFormattedName());
        }
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
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;

        Player p = e.getPlayer();
        if (!p.getGameMode().equals(GameMode.SURVIVAL)) return;

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
        b.breakNaturally();
        map.getWorld().playSound(b.getLocation(), b.getBlockSoundGroup().getBreakSound(), 1, 1);
        e.getPlayer().getInventory().addItem(new ItemStack(Material.SNOWBALL));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(getState().equals(GameState.ACTIVE))) return;
        if (!(e.getEntity() instanceof Snowball)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) return;

        // deal slight knockback to other players
        if (e.getHitEntity() != null && e.getHitEntity() instanceof Player) {
            Player p = (Player) e.getHitEntity();
            Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());
            Vector snowballVelocity = e.getEntity().getVelocity();

            // temp variable until debug is gone
            SpleefPlayer s = getSpleefPlayer(p);
            s.setLastDamager(shooter);
            p.damage(0.5);
            p.setVelocity(new Vector(snowballVelocity.getX() * 0.1, 0.5, snowballVelocity.getZ() * 0.1));
            s.setResetTime(timeRemaining-RESET_DAMAGE_TIME);
        }

        // destroy map blocks (not gold blocks) in contact with snowballs
        if (e.getHitBlock() != null) {
            Block b = e.getHitBlock();
            if (b.getType().equals(Material.GOLD_BLOCK) || b.getType().equals(Material.PACKED_ICE) || b.getType().equals(Material.BARRIER)) return;

            b.breakNaturally();
            map.getWorld().playSound(b.getLocation(), b.getBlockSoundGroup().getBreakSound(), 1, 1);
        }
    }

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) return;

        SpleefPlayer hit = getSpleefPlayer((Player) e.getEntity());
        Vector velocity = e.getEntity().getVelocity();

        hit.getPlayer().setVelocity(new Vector(velocity.getX()*0.1, 0.1, velocity.getZ()*0.1));
        hit.setLastDamager(Participant.getParticipant((Player) e.getDamager()));
        hit.setResetTime(timeRemaining - RESET_DAMAGE_TIME);
        e.setCancelled(true);
    }

    public void resetSpleefers() {
        for (SpleefPlayer p : spleefPlayers) {
            p.resetKiller();
            p.setPlacement(-1);
            p.setResetTime(-1);
        }
    }

    public void checkResetDamagers() {
        for (SpleefPlayer p : spleefPlayers) {
            if (!(p.getPlayer().getGameMode().equals(GameMode.SURVIVAL))) continue;

            if (p.getResetTime() >= timeRemaining) {
                p.resetKiller();
                p.setResetTime(-1);
            }
        }
    }


    public SpleefPlayer getSpleefPlayer(Player p) {
        for (SpleefPlayer x : spleefPlayers) {
            if (x.getPlayer().getName().equals(p.getName())) {
                return x;
            }
        }
        return null;
    }
}
