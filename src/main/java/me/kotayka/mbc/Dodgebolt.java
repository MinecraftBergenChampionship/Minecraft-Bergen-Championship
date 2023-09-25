package me.kotayka.mbc;

import me.kotayka.mbc.comparators.TeamScoreSorter;
import me.kotayka.mbc.gamePlayers.DodgeboltPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
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
    private final Location TEAM_ONE_ARROW_SPAWN = new Location(world, 6.5, 20, 0.5, -90, -90);
    private final Location TEAM_TWO_ARROW_SPAWN = new Location(world, -5.5, 20, 0.5, -90, -90);
    private List<Location> TEAM_ONE_SPAWNS;
    private List<Location> TEAM_TWO_SPAWNS;
    private final Location SPAWN = new Location(world, 0, 22, 17, 180, 0);
    private final Vector SPAWN_ARROW_VELOCITY = new Vector(0, 0.3, 0);
    public static final ItemStack BOW = new ItemStack(Material.BOW);
    public static final ItemStack BOOTS = new ItemStack(Material.LEATHER_BOOTS);
    private final List<DodgeboltPlayer> teamOnePlayers = new ArrayList<>();
    private final List<DodgeboltPlayer> teamTwoPlayers = new ArrayList<>();
    private int[] playersAlive;
    private boolean setTimerLine = false;
    private boolean disconnect = false;
    private int noDespawnTask = -1;
    private Set<Arrow> middleArrows = new HashSet<>(2);
    private List<Entity> spawnedArrowItems = new ArrayList<>();
    private HashSet<Arrow> firedArrows = new HashSet<>();
    private Map<Integer, Map<Block, Material>> borderBlocks = new HashMap<>();
    int arrowsFired = 0;
    int borderShrink = 0;
    int trailTask = -1;

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

        for (Participant p : firstPlace.teamPlayers) {
            teamOnePlayers.add(new DodgeboltPlayer(p, true));
        }
        for (Participant p : secondPlace.teamPlayers) {
            teamTwoPlayers.add(new DodgeboltPlayer(p, false));
        }

        playersAlive = new int[]{firstPlace.teamPlayers.size(), secondPlace.teamPlayers.size()};
    }

    @Override
    public void start() {
        if (firstPlace == null) { return; }
        setGameState(GameState.STARTING);
        MBC.getInstance().setCurrentGame(this);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(this, MBC.getInstance().plugin);
        setupArena();
        loadPlayers();
        stopTimer();
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
                new Location(world, 9.5, 17, 9.5, 90, 0), new Location(world, 12.5, 17, 3.5,90, 0),
                new Location(world, 9.5, 17, -8.5, 90, 0), new Location(world, 12.5, 17, -2.5,90,0)
        ));
        TEAM_TWO_SPAWNS = new ArrayList<>(Arrays.asList(
                new Location(world, -8.5, 17, 9.5,-90,0), new Location(world, -11.5, 17, 3.5,-90,0),
                new Location(world, -8.5, 17, -8.5,-90,0), new Location(world, -11.5, 17, -2.5,-90,0)
        ));

        // for when the game starts
        if (roundNum == 1) {
            for (Participant p : firstPlace.teamPlayers) {
                teamOnePlayers.add(new DodgeboltPlayer(p, true));
            }
            for (Participant p : secondPlace.teamPlayers) {
                teamTwoPlayers.add(new DodgeboltPlayer(p, false));
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
        }

        for (DodgeboltPlayer p : teamOnePlayers) {
            p.outOfBounds = false;
            p.shotBy = null;
            p.dead = false;
            p.fell = false;
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setVelocity(new Vector(0, 0, 0));
            int rand = (int) (Math.random() * TEAM_ONE_SPAWNS.size());
            Location spawn = TEAM_ONE_SPAWNS.get(rand);
            p.getPlayer().teleport(spawn);
            p.getPlayer().getInventory().clear();
            p.getPlayer().getInventory().setBoots(p.getParticipant().getTeam().getColoredLeatherArmor(BOOTS));
            TEAM_ONE_SPAWNS.remove(spawn);
            p.getPlayer().getInventory().addItem(BOW);
        }
        for (DodgeboltPlayer p : teamTwoPlayers) {
            p.outOfBounds = false;
            p.shotBy = null;
            p.dead = false;
            p.fell = false;
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setVelocity(new Vector(0, 0, 0));
            int rand = (int) (Math.random() * TEAM_TWO_SPAWNS.size());
            Location spawn = TEAM_TWO_SPAWNS.get(rand);
            p.getPlayer().teleport(spawn);
            p.getPlayer().getInventory().clear();
            p.getPlayer().getInventory().setBoots(p.getParticipant().getTeam().getColoredLeatherArmor(BOOTS));
            TEAM_TWO_SPAWNS.remove(spawn);
            p.getPlayer().getInventory().addItem(BOW);
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
                if (timeRemaining == 16) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
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
            if (noDespawnTask != -1) {
                Bukkit.getScheduler().cancelTask(noDespawnTask);
            }
            if (trailTask != -1) {
                Bukkit.getScheduler().cancelTask(trailTask);
            }
        }
    }

    @Override
    public void createScoreboard(Participant p) {
        newObjective(p);
        String winsOne = "";
        String winsTwo = "";
        switch (score[0]) {
            case 0 -> winsOne = "① ① ③ " + firstPlace.teamNameFormat();
            case 1 -> winsOne = "② ① ③ " + firstPlace.teamNameFormat();
            case 2 -> winsOne = "② ② ③ " + firstPlace.teamNameFormat();
            case 3 -> winsOne = "② ② ④ " + firstPlace.teamNameFormat();
        }
        switch (score[1]) {
            case 0 -> winsTwo = "① ① ③ " + secondPlace.teamNameFormat();
            case 1 -> winsTwo = "② ① ③ " + secondPlace.teamNameFormat();
            case 2 -> winsTwo = "② ② ③ " + secondPlace.teamNameFormat();
            case 3 -> winsTwo = "② ② ④ " + secondPlace.teamNameFormat();
        }
        createLine(22, ChatColor.AQUA.toString()+ChatColor.BOLD+"Final Game: " + ChatColor.RESET+"Dodgebolt", p);
        createLine(1, winsOne, p);
        createLine(0, winsTwo, p);
    }

    private void startRound() {
        barriers(false);
        spawnArrow(true);
        spawnArrow(false);
        createLineAll(21, ChatColor.RESET.toString());
    }

    private void setupArena() {
        arrowsFired = borderShrink = 0;
        barriers(true);
        for (Arrow a : middleArrows) {
            a.remove();
        }
        for (Item item : world.getEntitiesByClass(Item.class)) {
            item.remove();
        }
        middleArrows.clear();
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

                if (roundNum > 1) {continue;}
                int borderID = 0;
                switch (world.getBlockAt(x, 2, z).getType()) {
                    case RED_CONCRETE -> borderID = 1;
                    case ORANGE_CONCRETE -> borderID = 2;
                    case YELLOW_CONCRETE -> borderID = 3;
                    case LIME_CONCRETE -> borderID = 4;
                    case GREEN_CONCRETE -> borderID = 5;
                    case GRAY_CONCRETE -> borderID = 6;
                }
                if (borderID == 0) continue;
                Block borderBlock = world.getBlockAt(x, 17, z);
                if (borderBlocks.get(borderID) != null) {
                    borderBlocks.get(borderID).put(borderBlock, borderBlock.getType());
                } else {
                    Map<Block, Material> newMap = new HashMap<>();
                    newMap.put(borderBlock, borderBlock.getType());
                    borderBlocks.put(borderID, newMap);
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
        if (!(getState().equals(GameState.ACTIVE))) return;
        borderShrink++;
        Map<Block, Material> blocks = borderBlocks.get(borderShrink);
        if (blocks == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, new Runnable() {
           @Override
           public void run() {
               for (Block b : blocks.keySet()) {
                   Block delete = world.getBlockAt(b.getX(), b.getY()-1, b.getZ());
                   b.setType(Material.AIR);
                   delete.setType(Material.AIR);
               }
               for (Player p : Bukkit.getOnlinePlayers()) {
                   p.playSound(p, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1, 1);
               }
           }
        }, 100L);
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean carpet = false;
                for (Block b : blocks.keySet()) {
                    switch (b.getType()) {
                        case AIR -> this.cancel();
                        case ORANGE_CARPET -> b.setType(blocks.get(b));
                        default -> {
                            b.setType(Material.ORANGE_CARPET);
                            carpet = true;
                        }
                    }
                }
                if (carpet) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                    }
                }
            }
        }.runTaskTimer(MBC.getInstance().plugin, 10,10);
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
        if (noDespawnTask == -1) {
            noDespawnTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Arrow a : middleArrows) {
                    a.setTicksLived(1);
                }
            }, 100, 100);
        }
        if (first) {
            Arrow a1 = world.spawnArrow(TEAM_ONE_ARROW_SPAWN, SPAWN_ARROW_VELOCITY, (float) 0.3, 0);
            a1.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
            a1.setGlowing(true);
            middleArrows.add(a1);
        } else {
            Arrow a2 = world.spawnArrow(TEAM_TWO_ARROW_SPAWN, SPAWN_ARROW_VELOCITY, (float) 0.3, 0);
            a2.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
            a2.setGlowing(true);
            middleArrows.add(a2);
        }
    }

    private void endGame(MBCTeam t) {
        Bukkit.broadcastMessage(t.teamNameFormat() + " win MBC!");
        for (Player p : Bukkit.getOnlinePlayers()) {
            createScoreboard();
            p.sendTitle(t.teamNameFormat() + " win MBC!", " ", 0, 100, 20);
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.playSound(p, Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 1);
            p.setInvulnerable(true);
        }
        for (Participant p : t.teamPlayers) {
            p.winner = true;
        }
        setGameState(GameState.END_GAME);
        createLineAll(21, ChatColor.RED.toString()+ChatColor.BOLD+"Back to lobby:");
        setTimer(13);
    }

    private void endRound() {
        createScoreboard();
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.stopSound(Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS);
            p.setInvulnerable(true);
            p.playSound(p, Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
        }
        setTimerLine = false;
        createLineAll(21, ChatColor.RED.toString()+ChatColor.BOLD+"Next Round:");
        roundNum++;
        if (disconnect) {
            Bukkit.broadcastMessage("Event Paused!");
            setGameState(GameState.PAUSED);
        } else {
            setGameState(GameState.END_ROUND);
            setTimer(6);
        }
    }

    @Override
    public void Unpause() {
        setGameState(GameState.END_ROUND);
        setTimer(6);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow)) return;
        if (!(e.getEntity().getShooter() instanceof Player)) { e.setCancelled(true); return; }

        Arrow arrow = (Arrow) e.getEntity();
        firedArrows.remove(arrow);

        if (e.getHitBlock() != null && e.getHitBlockFace() != null) {
            if (middleArrows.contains(arrow)) return;
            Block b = e.getHitBlock();
            Location l = b.getLocation();
            world.playSound(l, Sound.ENTITY_ARROW_HIT, 1, 1);
            l.add(0.5, 0, 0.5);
            // west -x east +x south +z north -z
            switch (e.getHitBlockFace()) {
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
            spawnedArrowItems.add(world.dropItem(l, new ItemStack(Material.ARROW)));

            // don't increase arrows fired if on same side
            boolean first = l.getX() >= 0.5;
            DodgeboltPlayer p = getDodgeboltPlayer((Player) e.getEntity().getShooter());
            if (p == null) return;
            if ((!first && p.getParticipant().getTeam().equals(firstPlace)) || (first && p.getParticipant().getTeam().equals(secondPlace))
                && !(l.getX() > -2 && l.getX() < 3)) {
                arrowsFired++;
                if (arrowsFired % 4 == 0) {
                    arenaShrink();
                }
            }
            arrow.remove();
        }

        if (e.getHitEntity() instanceof Player && e.getEntity().getShooter() instanceof Player) {
            Participant shooter = Participant.getParticipant((Player) e.getEntity().getShooter());
            DodgeboltPlayer p = getDodgeboltPlayer((Player) e.getHitEntity());
            if (p == null) {
                // hits a spectator ??
                Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " somehow hit a spectator.");
                e.setCancelled(true);
                arrow.remove();
                spawnArrow(!shooter.getTeam().equals(firstPlace));
                return;
            }

            p.shotBy = shooter;
            MBC.spawnFirework(p.getParticipant());
            spawnedArrowItems.add(world.dropItem(p.getPlayer().getLocation(), new ItemStack(Material.ARROW)));
            properKill(p);
            arrow.remove();
            e.setCancelled(true);
        }
    }

    private Material coloredCarpet(ChatColor c) {
        return switch (c) {
            case RED -> Material.RED_CARPET;
            case YELLOW -> Material.YELLOW_CARPET;
            case GREEN -> Material.GREEN_CARPET;
            case BLUE -> Material.BLUE_CARPET;
            case DARK_PURPLE -> Material.PURPLE_CARPET;
            case LIGHT_PURPLE -> Material.PINK_CARPET;
            default -> Material.WHITE_CARPET;
        };
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().getInventory().clear();
        e.setRespawnLocation(SPAWN);
        e.getPlayer().playSound(e.getPlayer(), Sound.MUSIC_DISC_CHIRP, SoundCategory.RECORDS, 1, 1);
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        if (!(e.getPlayer().getWorld().equals(world))) return;
        if (e.getItem().getItemStack().getType().equals(Material.ARROW)) {
            spawnedArrowItems.remove(e.getItem());
        }
    }

    @EventHandler
    public void itemDamage(EntityDamageEvent e) {
        if (!(e.getEntity().getLocation().getWorld().equals(world))) return;
        if (e.getEntity() instanceof Player) return;
        if (e.getCause().equals(EntityDamageEvent.DamageCause.LAVA) && e.getEntity() instanceof Item && ((Item) e.getEntity()).getItemStack().getType().equals(Material.ARROW)) {
            Entity arrow = e.getEntity();
            Location l = arrow.getLocation();
            // if arrow was game spawned, it wasn't player's fault; spawn on same side
            if (spawnedArrowItems.contains(arrow)) {
                spawnArrow(l.getX() > 0.5);
                spawnedArrowItems.remove(arrow);
            } else {
                spawnArrow(l.getX() <= 0.5);
            }
            arrow.remove();
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        middleArrows.remove((Arrow) e.getArrow());
    }

    @EventHandler
    public void onArrowFire(ProjectileLaunchEvent e) {
        if (!e.getEntity().getWorld().equals(world)) return;
        if (!(e.getEntity() instanceof Arrow && e.getEntity().getShooter() instanceof Player)) return;

        DodgeboltPlayer p = getDodgeboltPlayer((Player) e.getEntity().getShooter());
        if (p == null) return;

        Arrow a = (Arrow) e.getEntity();
        Vector velocity = a.getVelocity();
        a.setVelocity(new Vector(velocity.getX() * 0.95, velocity.getY(), velocity.getZ()*0.95));
        firedArrows.add(a);
        if (trailTask == -1) {
            trailTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
                for (Arrow arrow : firedArrows) {
                    DodgeboltPlayer shooter = getDodgeboltPlayer((Player) arrow.getShooter());
                    world.spawnParticle(Particle.REDSTONE, arrow.getLocation(), 3, new Particle.DustOptions(shooter.getParticipant().getTeam().getColor(), 3));
                }
            }, 0, 2);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null) return;
        for (ItemStack i : p.getPlayer().getInventory()) {
            if (i == null) continue;
            if (i.getType() == Material.ARROW) {
                world.dropItem(p.getPlayer().getLocation(), i);
            }
        }
        e.setDeathSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);

        p.dead = true;

        if (p.shotBy != null) {
            e.setDeathMessage(p.getParticipant().getFormattedName() + " was tagged by " + p.shotBy.getFormattedName());
        } else if (p.fell) {
            e.setDeathMessage(p.getParticipant().getFormattedName() + " fell out of the arena!");
        } else {
            e.setDeathMessage(p.getParticipant().getFormattedName() + " crossed into enemy territory!");
        }
    }

    @EventHandler
    public void onPunch(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player)
            e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getEntity().getWorld() != world) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
            DodgeboltPlayer p = getDodgeboltPlayer((Player) e.getEntity());
            if (p == null) return;
            p.fell = true;
            properKill(p);
            p.getPlayer().setFireTicks(0);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!getState().equals(GameState.ACTIVE)) return;

        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null || p.dead) { return; }
        Location l = p.getPlayer().getLocation();
        // todo: move most of this into dodgeboltplayer class
        if (p.FIRST) {
            // slow players crossing sides
            if (l.getX() < 0 && l.getX() > -2) {
                if (e.getPlayer().getLocation().getDirection().getX() < 0) {
                    e.getPlayer().damage(0.5);
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 5, true));
                    return;
                }
            }

            // kill players completely crossing sides
            if (l.getX() <= -2) {
                properKill(p);
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
            // slow players crossing sides
            if (l.getX() > 1 && l.getX() < 3) {
                if (e.getPlayer().getLocation().getDirection().getX() > 0) {
                    e.getPlayer().damage(0.5);
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 5, true));
                    return;
                }
            }

            // kill players completely crossing
            if (l.getX() >= 3) {
                properKill(p);
            }

            if (!p.outOfBounds) {
                if (l.getX() > -3 && (l.getZ() > -4 && l.getZ() < 5)) {
                    p.removeBow();
                } else if (l.getX() > -2 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.removeBow();
                }
            } else {
                if (l.getX() <= -3 && (l.getZ() > -4 && l.getZ() < 5)) {
                    p.giveBow();
                } else if (l.getX() <= -2 && (l.getZ() <= -4 || l.getZ() >= 5)) {
                    p.giveBow();
                }
            }
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

    private void properKill(DodgeboltPlayer p) {
        p.dead = true;
        if (p.FIRST) {
            if (playersAlive[0] != 1) {
                p.getPlayer().damage(50);
                p.getPlayer().setVelocity(new Vector(0, 0, 0));
                playersAlive[0]--;
                if (p.shotBy != null) { arenaShrink(); }
            } else {
                p.getPlayer().teleport(SPAWN);
                if (p.shotBy != null) {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " was tagged by " + p.shotBy.getFormattedName());
                } else if (p.fell) {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " fell out of the arena!");
                } else {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " crossed into enemy territory!");
                }
                score[1]++;
                playersAlive[0] = firstPlace.teamPlayers.size();
                playersAlive[1] = secondPlace.teamPlayers.size();
                if (score[1] == 3) endGame(secondPlace);
                else endRound();
            }
        } else {
            if (playersAlive[1] != 1) {
                p.getPlayer().damage(50);
                p.getPlayer().setVelocity(new Vector(0, 0, 0));
                playersAlive[1]--;
                if (p.shotBy != null) { arenaShrink(); }
            } else {
                p.getPlayer().teleport(SPAWN);
                if (p.shotBy != null) {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " was tagged by " + p.shotBy.getFormattedName());
                } else if (p.fell) {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " fell out of the arena!");
                } else {
                    Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " crossed into enemy territory!");
                }
                score[0]++;
                playersAlive[0] = firstPlace.teamPlayers.size();
                playersAlive[1] = secondPlace.teamPlayers.size();
                if (score[0] == 3) endGame(firstPlace);
                else endRound();
            }
        }
        p.getPlayer().setFireTicks(0);
    }

    private void returnToLobby() {
        HandlerList.unregisterAll(this);    // game specific listeners are only active when game is
        setGameState(GameState.INACTIVE);
        MBC.getInstance().plugin.getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, MBC.getInstance().plugin);
        for (Participant p : MBC.getInstance().getPlayersAndSpectators()) {
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

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null) return;

        disconnect = true;
        Bukkit.broadcastMessage(p.getParticipant().getFormattedName() + " disconnected!");
        if (p.FIRST) {
            if (playersAlive[0] != 1) {
                playersAlive[0]--;
            } else {
                p.getPlayer().teleport(SPAWN);
                p.dead = true;
                score[1]++;
                playersAlive[0] = firstPlace.teamPlayers.size();
                playersAlive[1] = secondPlace.teamPlayers.size();
                if (score[1] == 3) endGame(secondPlace);
                else endRound();
            }
        } else {
            if (playersAlive[1] != 1) {
                playersAlive[1]--;
            } else {
                p.getPlayer().teleport(SPAWN);
                score[0]++;
                playersAlive[0] = firstPlace.teamPlayers.size();
                playersAlive[1] = secondPlace.teamPlayers.size();
                if (score[0] == 3) endGame(firstPlace);
                else endRound();
            }
        }
    }

    @EventHandler
    public void onReconnect(PlayerJoinEvent e) {
        DodgeboltPlayer p = getDodgeboltPlayer(e.getPlayer());
        if (p == null) return; // new login; doesn't matter
        p.setPlayer(e.getPlayer());
        // switch
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
