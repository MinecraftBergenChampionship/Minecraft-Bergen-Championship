package me.kotayka.mbc;

import me.kotayka.mbc.gamePlayers.DodgeboltPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class Dodgebolt extends Minigame {
    private World world = Bukkit.getWorld("Dodgebolt");
    private MBCTeam firstPlace = null;
    private MBCTeam secondPlace = null;
    // {1st place, 2nd place}
    public int[] score = {0, 0};
    int roundNum = 1;
    //private ArmorStand cameraman = null;
    private final Location TEAM_ONE_ARROW_SPAWN = new Location(world, -5.5, 20, 0.5, -90, -90);
    private final Location TEAM_TWO_ARROW_SPAWN = new Location(world, 6.5, 20, 0.5, -90, -90);
    private List<Location> TEAM_ONE_SPAWNS = new ArrayList<>(Arrays.asList(
            new Location(world, 9.5, 17, 9.5), new Location(world, 12.5, 17, 3.5),
            new Location(world, 9.5, 17, -8.5), new Location(world, 12.5, 17, -2.5)
    ));
    private List<Location> TEAM_TWO_SPAWNS = new ArrayList<>(Arrays.asList(
            new Location(world, -8.5, 17, 9.5), new Location(world, -11.5, 17, 3.5),
            new Location(world, -8.5, 17, -8.5), new Location(world, -11.5, 17, -2.5)
    ));
    private final Location SPAWN = new Location(world, 0, 22, 17, 180, 0);
    private final Vector SPAWN_ARROW_VELOCITY = new Vector(0, 0.3, 0);
    private final ItemStack ARROW_ITEM = new ItemStack(Material.ARROW);
    public static final ItemStack BOW = new ItemStack(Material.BOW);
    private final List<DodgeboltPlayer> teamOnePlayers = new ArrayList<>(MBC.MAX_PLAYERS_PER_TEAM);
    private final List<DodgeboltPlayer> teamTwoPlayers = new ArrayList<>(MBC.MAX_PLAYERS_PER_TEAM);
    private int[] playersAlive;
    private boolean setTimerLine = false;
    private List<Arrow> arrows = new ArrayList<>(2);

    public Dodgebolt() {
        super("Dodgebolt");

        /*
        if (MBC.getInstance().getValidTeams().size() < 2) {
            Bukkit.broadcastMessage("[Dodgebolt] Not enough teams!");
            return;
        }*/

        if (MBC.getInstance().getValidTeams().size() == 1) {
            // only for debug
            firstPlace = getValidTeams().get(0);
            secondPlace = MBC.getInstance().spectator;
        } else {
            List<MBCTeam> teams = getValidTeams();
            teams.sort(new TeamScoreSorter());
            Collections.reverse(teams);
            firstPlace = teams.get(0);
            secondPlace = teams.get(1);
        }

        for (Participant p : firstPlace.teamPlayers) {
            teamOnePlayers.add(new DodgeboltPlayer(p, true));
        }
        for (Participant p : secondPlace.teamPlayers) {
            teamTwoPlayers.add(new DodgeboltPlayer(p, false));
        }

        ItemMeta bowMeta = BOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        BOW.setItemMeta(bowMeta);

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    public Dodgebolt(MBCTeam firstPlace, MBCTeam secondPlace) {
        super("Dodgebolt");
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;

        ItemMeta bowMeta = BOW.getItemMeta();
        bowMeta.setUnbreakable(true);
        BOW.setItemMeta(bowMeta);
    }

    @Override
    public void start() {
        if (firstPlace == null) { return; }
        setGameState(GameState.STARTING);
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        setupArena();
        loadPlayers();
        setTimer(30);
    }

    @Override
    public void loadPlayers() {
        /*
        if (getState().equals(GameState.TUTORIAL)) {
            cameraman = (ArmorStand) world.spawnEntity(new Location(world, 0.5, 28, -11.5), EntityType.ARMOR_STAND);
            cameraman.setInvisible(true);
            cameraman.setGravity(false);
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.setSpectatorTarget(cameraman);
            });
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location l = cameraman.getLocation();
                    Location newL = new Location(world, l.getX(), l.getY()-0.1, l.getZ()-0.3);
                    cameraman.teleport(newL);
                }
            }.runTaskTimer(MBC.getInstance().plugin, 10,10);
        }
         */

        TEAM_ONE_SPAWNS = new ArrayList<>(Arrays.asList(
                new Location(world, 9.5, 18, 9.5, 90, 0), new Location(world, 12.5, 18, 3.5,90, 0),
                new Location(world, 9.5, 18, -8.5, 90, 0), new Location(world, 12.5, 18, -2.5,90,0)
        ));
        TEAM_TWO_SPAWNS = new ArrayList<>(Arrays.asList(
                new Location(world, -8.5, 18, 8.5,-90,0), new Location(world, -11.5, 18, 3.5,-90,0),
                new Location(world, -8.5, 18, -8.5,-90,0), new Location(world, -11.5, 18, -2.5,-90,0)
        ));

        // for when the game starts
        if (roundNum == 1) {
            for (Participant p : firstPlace.teamPlayers) {
                teamOnePlayers.add(new DodgeboltPlayer(p, true));
                int rand = (int) (Math.random() * TEAM_ONE_SPAWNS.size());
                Location spawn = TEAM_ONE_SPAWNS.get(rand);
                p.getPlayer().teleport(spawn);
                p.getPlayer().getInventory().clear();
                TEAM_ONE_SPAWNS.remove(spawn);
                p.getPlayer().getInventory().addItem(BOW);
            }
            for (Participant p : secondPlace.teamPlayers) {
                teamTwoPlayers.add(new DodgeboltPlayer(p, false));
                int rand = (int) (Math.random() * TEAM_TWO_SPAWNS.size());
                Location spawn = TEAM_TWO_SPAWNS.get(rand);
                p.getPlayer().getInventory().clear();
                p.getPlayer().teleport(spawn);
                TEAM_TWO_SPAWNS.remove(spawn);
                p.getPlayer().getInventory().addItem(BOW);
            }
            for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
                p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
                p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
                if (!p.getTeam().equals(firstPlace) && !p.getTeam().equals(secondPlace)) {
                    p.getPlayer().teleport(SPAWN);
                }
            }
        } else {
            for (DodgeboltPlayer p : teamOnePlayers) {
                p.outOfBounds = false;
                p.shotBy = null;
                p.fell = false;
            }
            for (DodgeboltPlayer p : teamTwoPlayers) {
                p.outOfBounds = false;
                p.shotBy = null;
                p.dead = false;
                p.fell = false;
            }
        }
    }

    @Override
    public void events() {
        /*
        if (getState().equals(GameState.TUTORIAL)) {
            if (timeRemaining == 0) {
                setGameState(GameState.STARTING);
                loadPlayers();
                cameraman.remove();
                timeRemaining = 20;
            }
        } else*/
        if (getState().equals(GameState.STARTING)) {
            if (timeRemaining == 0) {
                startRound();
                setGameState(GameState.ACTIVE);
                createLineAll(20, ChatColor.RED.toString()+ChatColor.BOLD+"Finale Active");
            } else {
                if (timeRemaining == 15) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_CHIRP, 1, 1);
                    }
                }
                startingCountdown();
            }
        } else if (getState().equals(GameState.ACTIVE)) {
            if (timeRemaining <= 0 && !setTimerLine) {
                setTimerLine = true;
                createLineAll(20, ChatColor.RED.toString()+ChatColor.BOLD+"Finale Active");
            }
        } else if (getState().equals(GameState.END_ROUND)) {
            if (timeRemaining == 0) {
                setupArena();
                loadPlayers();
                setGameState(GameState.STARTING);
                timeRemaining = 10;
            }
        } else if (getState().equals(GameState.END_GAME)) {
            if (timeRemaining == 0) {
                returnToLobby();
            }
        }
    }

    @Override
    public void createScoreboard(Participant p) {
        newObjective(p);
        String winsOne = "";
        String winsTwo = "";
        switch (score[0]) {
            case 0 -> winsOne = firstPlace.teamNameFormat() + "     ① ① ③";
            case 1 -> winsOne = firstPlace.teamNameFormat() + "     ② ① ③";
            case 2 -> winsOne = firstPlace.teamNameFormat() + "     ② ② ③";
            case 3 -> winsOne = firstPlace.teamNameFormat() + "     ② ② ④";
        }
        switch (score[1]) {
            case 0 -> winsTwo = secondPlace.teamNameFormat() + "     ① ① ③";
            case 1 -> winsTwo = secondPlace.teamNameFormat() + "     ② ① ③";
            case 2 -> winsTwo = secondPlace.teamNameFormat() + "     ② ② ③";
            case 3 -> winsTwo = secondPlace.teamNameFormat() + "     ② ② ④";
        }
        createLine(22, ChatColor.AQUA.toString()+ChatColor.BOLD+"Final Game: " + ChatColor.RESET+"Dodgebolt", p);
        createLine(1, winsOne, p);
        createLine(0, winsTwo, p);
    }

    private void startRound() {
        barriers(false);
        spawnArrow(true);
        spawnArrow(false);
    }

    private void setupArena() {
        barriers(true);
        Material concreteOne = firstPlace.getConcrete().getType();
        Material concreteTwo = secondPlace.getConcrete().getType();
        // ice & carpet
        for (int x = -16; x <= 16; x++) {
            for (int z = -14; z <= 14; z++) {
                world.getBlockAt(x, 16, z).setType(Material.ICE);
                Block copyBlock = world.getBlockAt(x, 1, z);
                if (copyBlock.getType().equals(Material.WHITE_CARPET) || copyBlock.getType().equals(Material.BLACK_CARPET)) {
                    world.getBlockAt(x, 17, z).setType(copyBlock.getType());
                } else {
                    if (x < 0) {
                        world.getBlockAt(x, 17, z).setType(coloredCarpet(secondPlace.getChatColor()));
                    } else {
                        world.getBlockAt(x, 17, z).setType(coloredCarpet(firstPlace.getChatColor()));
                    }
                }
            }
        }

        for (int x = -16; x <= -2; x++) {
            for (int y = 17; y <= 20; y++) {
                world.getBlockAt(x, y, -15).setType(concreteTwo);
                world.getBlockAt(x, y, 15).setType(concreteTwo);
                world.getBlockAt(x*-1, y, -15).setType(concreteOne);
                world.getBlockAt(x*-1, y, 15).setType(concreteOne);
            }
        }

        // back wall
        for (int z = -14; z <= 14; z++) {
            for (int y = 17; y <= 20; y++) {
                world.getBlockAt(17, y, z).setType(concreteOne);
                world.getBlockAt(-17, y, z).setType(concreteTwo);
            }
        }
    }

    private void arenaShrink() {

    }

    // true = barriers, false = air
    private void barriers(boolean b) {
        Material m = b ? Material.BARRIER : Material.AIR;
        int[] x = {8, 9, 9, 10, 13, 12, 11, 12};
        int[] z = {9, 10, 8, 9, 3, 4, 3, 2};

        for (int i = 0; i < 8; i++) {
            world.getBlockAt(x[i], 18, z[i]).setType(m);
            world.getBlockAt(x[i]*-1, 18, z[i]).setType(m);
            world.getBlockAt(x[i], 18, z[i]*-1).setType(m);
            world.getBlockAt(x[i]*-1, 18, z[i]*-1).setType(m);
        }
    }

    private void spawnArrow(boolean first) {
        if (first) {
            Arrow a1 = world.spawnArrow(TEAM_ONE_ARROW_SPAWN, SPAWN_ARROW_VELOCITY, (float) 0.3, 0);
            a1.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
            a1.setGlowing(true);
            arrows.add(a1);
        } else {
            Arrow a2 = world.spawnArrow(TEAM_TWO_ARROW_SPAWN, SPAWN_ARROW_VELOCITY, (float) 0.3, 0);
            a2.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
            a2.setGlowing(true);
            arrows.add(a2);
        }
    }

    private void endGame(MBCTeam t) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(t.teamNameFormat() + " wins MCC!", " ", 0, 100, 20);
            p.playSound(p, Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 2);
        }
        for (Participant p : t.teamPlayers) {
            p.winner = true;
        }
        setGameState(GameState.END_GAME);
        setTimer(10);
    }

    private void endRound() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_CHIRP);
            p.playSound(p, Sound.MUSIC_DISC_CHIRP, 1, 1);
        }
        setGameState(GameState.END_ROUND);
        setTimer(5);
        roundNum++;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getHitBlock() != null) {
            Block b = e.getHitBlock();
            if (b.getType().toString().endsWith("CONCRETE") || b.getType().equals(Material.BARRIER)) {
                boolean side = e.getEntity().getLocation().getX() > 0.5;
                e.getEntity().remove();
                spawnArrow(side);
            }
        }

        if (e.getHitEntity() instanceof Player && e.getEntity().getShooter() instanceof Player) {
            DodgeboltPlayer p = getDodgeboltPlayer((Player) e.getHitEntity());
            Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());
            if (p == null) {
                // hits a spectator ??
                e.setCancelled(true);
                spawnArrow(!shooter.getTeam().equals(firstPlace));
            }

            p.shotBy = shooter;
            MBC.spawnFirework(p.getParticipant());
            world.dropItem(p.getPlayer().getLocation(), ARROW_ITEM);
            if (p.FIRST) {
                if (playersAlive[0] != 1) {
                    p.getPlayer().damage(50);
                    p.getPlayer().setVelocity(new Vector(0, 0, 0));
                    playersAlive[0]--;
                } else {
                    p.getPlayer().teleport(SPAWN);
                    score[1]++;
                    playersAlive[0] = firstPlace.teamPlayers.size();
                    if (score[1] == 3) endGame(secondPlace);
                    else endRound();
                }
            } else {
                if (playersAlive[1] != 1) {
                    p.getPlayer().damage(50);
                    p.getPlayer().setVelocity(new Vector(0, 0, 0));
                    playersAlive[1]--;
                } else {
                    p.getPlayer().teleport(SPAWN);
                    score[0]++;
                    playersAlive[1] = secondPlace.teamPlayers.size();
                    if (score[0] == 3) endGame(firstPlace);
                    else endRound();
                }
            }
        }
    }

    private Material coloredCarpet(ChatColor c) {
        switch (c) {
            case RED:
                return Material.RED_CARPET;
            case YELLOW:
                return Material.YELLOW_CARPET;
            case GREEN:
                return Material.GREEN_CARPET;
            case BLUE:
                return Material.BLUE_CARPET;
            case DARK_PURPLE:
                return Material.PURPLE_CARPET;
            case LIGHT_PURPLE:
                return Material.PINK_CARPET;
            default:
                return Material.WHITE_CARPET;
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().getInventory().clear();
        e.setRespawnLocation(SPAWN);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null) return;
        e.setDeathSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);

        p.dead = true;

        if (p.shotBy != null) {
            e.setDeathMessage(p.getParticipant().getFormattedName() + " was tagged by " + p.shotBy.getFormattedName());
        } else if (p.fell) {
            e.setDeathMessage(p.getParticipant().getFormattedName() + " fell out of the arena!");
        } else {
            e.setDeathMessage(p.getParticipant().getFormattedName() + "... really?");
        }
    }

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player)
            e.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;

        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null || p.dead) { return; }
        Location l = p.getPlayer().getLocation();
        // todo: move most of this into dodgeboltplayer class
        if (p.FIRST) {
            if (l.getX() < 0 && l.getX() > -2) {
                if (e.getPlayer().getLocation().getDirection().getX() < 0) {
                    e.getPlayer().damage(0.5);
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 5, true));
                    return;
                }
            }

            if (l.getX() <= -2) {
                e.getPlayer().damage(50);
            }

            if (!p.outOfBounds) {
                if (l.getX() < 4 && (l.getZ() > -4 && l.getZ() < 5)) {
                    p.removeBow();
                } else if (l.getX() < 3 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.removeBow();
                }
            } else {
                if (l.getX() >= 4 && (l.getZ() > -4 && l.getZ() < 5)) {
                    p.giveBow();
                } else if (l.getX() >= 3 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.giveBow();
                }
            }
        } else {
            if (l.getX() > 1 && l.getX() < 3) {
                if (e.getPlayer().getLocation().getDirection().getX() > 0) {
                    e.getPlayer().damage(0.5);
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1, 5, true));
                    return;
                }
            }
            if (l.getX() >= 3) {
                e.getPlayer().damage(50);
            }

            if (!p.outOfBounds) {
                if (l.getX() > -3 && (l.getZ() > -4 || l.getZ() < 5)) {
                    p.removeBow();
                } else if (l.getX() > -2 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.removeBow();
                }
            } else {
                if (l.getX() <= -3 && (l.getZ() > -4 || l.getZ() < 5)) {
                    p.giveBow();
                } else if (l.getX() <= -2 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.giveBow();
                }
            }
        }

        // TEMPORARY UNTIL I CAN FIND A BETTER SOLUTION: ALSO KIND OF RANDOM!!!
        for (Arrow a : arrows) {
            a.setTicksLived(1);
        }
    }

    public DodgeboltPlayer getDodgeboltPlayer(Player p) {
        for (DodgeboltPlayer d : teamOnePlayers) {
            if (d.getPlayer().getUniqueId().equals(p.getUniqueId()))
                return d;
        }
        for (DodgeboltPlayer d : teamTwoPlayers) {
            if (d.getPlayer().getUniqueId().equals(p.getUniqueId()))
                return d;
        }
        return null;
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent e) {
        if (e.getEntity().getType().equals(EntityType.ARROW)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemThrow(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType().equals(Material.BOW)) {
            e.setCancelled(true);
        }
    }

    private void returnToLobby() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            p.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            p.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().getInventory().clear();
            p.getPlayer().setExp(0);
            p.getPlayer().setLevel(0);
        }

        MBC.getInstance().lobby.end();
    }
    /*
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e) {
        if (!getState().equals(GameState.TUTORIAL)) return;

        if (e.getPlayer().getSpectatorTarget() != null && e.getPlayer().getSpectatorTarget().equals(cameraman)) {
            e.setCancelled(true);
        }
    }
     */
}
