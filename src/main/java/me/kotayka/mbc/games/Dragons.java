package me.kotayka.mbc.games;

import me.kotayka.mbc.*;
import me.kotayka.mbc.gameMaps.dragonsMap.Classic;
import me.kotayka.mbc.gameMaps.dragonsMap.DragonsMap;
import me.kotayka.mbc.gamePlayers.SpleefPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Dragons extends Game {

    public DragonsMap map;

    private static Dragons instance = null;

    private HashMap<UUID, Boolean> canJump = new HashMap<>();
    private HashMap<Player, Long> cooldowns = new HashMap<>();

    private DecimalFormat df = new DecimalFormat("#.#");

    private static final double DRAGON_MAX_SPEED = 0.6;
    private static final double REACH_DISTANCE = 1.0;
    private List<MBCTeam> fullyAliveTeams = getValidTeams();

    public List<EnderDragon> enderDragons = new ArrayList<>();
    public HashMap<EnderDragon, Location> targetLocations = new HashMap<>();
    public HashMap<EnderDragon, Boolean> resetting = new HashMap<>();
    public HashMap<EnderDragon, Double> yLocations = new HashMap<>();
    private Random random = new Random();

    private final int SURVIVAL_POINTS = 2;

    public Dragons() {
        super("Dragons", new String[]{});

        map = new Classic(this);
        map.resetMap();
    }

    @Override
    public void start() {
        super.start();

        setGameState(GameState.STARTING);

        setTimer(3);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().plugin, () -> {
            if (playersAlive.size() == 0) {
                return;
            }

//            if (enderDragon != null && playerWhoBrokeBlock != null) {
//                enderDragon.setTarget(playerWhoBrokeBlock);
//                enderDragon.setPhase(EnderDragon.Phase.CHARGE_PLAYER);
//                enderDragon.setTarget(playerWhoBrokeBlock);
//            }

            for (EnderDragon dragon : enderDragons) {
                dragon.setPhase(EnderDragon.Phase.CIRCLING);

                Location currentLocation = dragon.getLocation();

                Location targetLocation = targetLocations.get(dragon);

                Vector direction = targetLocation.toVector().subtract(currentLocation.toVector());

                Vector normalizedDirection = direction.normalize();
                Vector velocity = normalizedDirection.multiply(DRAGON_MAX_SPEED);
                dragon.setVelocity(velocity);

                double yaw = Math.toDegrees(Math.atan2(direction.getZ(), direction.getX())) + 90;
                double distanceXZ = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
                double pitch = -Math.toDegrees(Math.atan2(direction.getY(), distanceXZ));
                dragon.setRotation((float) yaw, (float) pitch);

                if (currentLocation.distance(targetLocation) < REACH_DISTANCE) {
                    if (resetting.get(dragon)) {
                        int index = random.nextInt(playersAlive.size());

                        targetLocations.put(dragon, playersAlive.get(index).getPlayer().getLocation().clone().add(new Vector(0, -2, 0)));
                        resetting.put(dragon, false);
                    }
                    else {
                        yLocations.put(dragon, targetLocation.getY());
                        Vector upwardOffset = direction.normalize().multiply(150);
                        Location newTargetLocation = targetLocation.clone().add(upwardOffset);
                        targetLocations.put(dragon, newTargetLocation);

                        resetting.put(dragon, true);
                        targetLocations.get(dragon).setY(yLocations.get(dragon)+40);
                    }
                }
            }



            Iterator<Map.Entry<Player, Long>> iterator = cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Player, Long> entry = iterator.next();
                long storedTime = entry.getValue();

                if (System.currentTimeMillis() - storedTime >= 10000) {
                    entry.getKey().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));
                    canJump.put(entry.getKey().getUniqueId(), true);
                    iterator.remove();
                    continue;
                }

                long timeLeft = 10000 - (System.currentTimeMillis() - storedTime);
                double secondsLeft = timeLeft / 1000.0;
                int iCooldown = (int) secondsLeft;

                String seconds = df.format(secondsLeft);

                if (seconds.length() == 1) {
                    seconds+=".0";
                }

                String actionBarMessage = seconds + " seconds left §c" + "▐".repeat(iCooldown) + "§a" + "▐".repeat(10 - iCooldown);
                entry.getKey().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBarMessage));
            }
        }, 20, 1);

    }

    @Override
    public void onRestart() {

    }

    @Override
    public void loadPlayers() {
        for (Participant p : MBC.getInstance().getPlayers()) {
            p.getInventory().clear();
            p.getPlayer().teleport(map.SPAWN);
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, -1, 255, false, false));
            playersAlive.add(p);
            p.board.getTeam(p.getTeam().getTeamFullName()).setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            canJump.put(p.getPlayer().getUniqueId(), true);

            ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
            ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
            ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
            ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);

            p.getInventory().setHelmet(helmet);
            p.getInventory().setChestplate(chestplate);
            p.getInventory().setLeggings(leggings);
            p.getInventory().setBoots(boots);
        }
    }

    public void handleDeath(Player victim, boolean fallDamage) {
        String deathMessage;

        Participant p = Participant.getParticipant(victim);

        if (!fallDamage) {
            deathMessage = p.getFormattedName()+" fell into the void";
        } else {
            deathMessage = p.getFormattedName()+" fell from too high";
        }

        getLogger().log(deathMessage);
        Bukkit.broadcastMessage(deathMessage);

        updatePlayersAlive(p);
        victim.getPlayer().sendMessage(ChatColor.RED+"You died!");
        victim.getPlayer().sendTitle(" ", ChatColor.RED+"You died!", 0, 60, 20);
        victim.getPlayer().setGameMode(GameMode.SPECTATOR);
        MBC.spawnFirework(p);
        victim.getPlayer().teleport(map.SPAWN);

        for (Participant p1 : playersAlive) {
            p1.addCurrentScore(SURVIVAL_POINTS);
        }

        if (fullyAliveTeams.size() > 1) {
            fullyAliveTeams.remove(p.getTeam());
        }
    }


    @Override
    public void events() {
        switch (getState()) {
            case TUTORIAL:
                if (timeRemaining == 0) {
                    setGameState(GameState.STARTING);
                    setTimer(15);
                } else if (timeRemaining % 7 == 0) {
                    Introduction();
                }
                break;
            case STARTING:
                startingCountdown();
                if (timeRemaining == 0) {
                    MBC.getInstance().hideAllPlayers();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p, Sound.MUSIC_DISC_13, SoundCategory.RECORDS,1,1); // temp?
                    }
                    for (Participant p : MBC.getInstance().getPlayers()) {
                        p.getInventory().addItem(new ItemStack(Material.IRON_AXE));
                    }
                    setGameState(GameState.ACTIVE);
                    setTimer(300);
                }
                break;
            case END_GAME:
                gameEndEvents();
                break;
            case ACTIVE:
                if ((timeRemaining < 297 && enderDragons.size() == 0) || (timeRemaining < 240 && enderDragons.size() == 1) || (timeRemaining < 180 && enderDragons.size() == 2)
                    || (timeRemaining < 120 && enderDragons.size() == 3) || (timeRemaining < 60 && enderDragons.size() == 4)) {

                    EnderDragon enderDragon = (EnderDragon) map.getWorld().spawnEntity(map.DRAGON_SPAWN, EntityType.ENDER_DRAGON);

                    Bukkit.broadcastMessage(ChatColor.GOLD+"End Dragon Spawning!!!");

                    enderDragon.setPhase(EnderDragon.Phase.CIRCLING);

                    resetting.put(enderDragon, false);

                    int index = random.nextInt(playersAlive.size());

                    targetLocations.put(enderDragon, playersAlive.get(index).getPlayer().getLocation());

                    enderDragons.add(enderDragon);

                }
                break;
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!isGameActive()) return;

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getPlayer().getItemInHand().getType() == Material.IRON_AXE) {
            Player player = e.getPlayer();
            if (canJump.containsKey(player.getUniqueId()) && canJump.get(player.getUniqueId())) {
                player.setVelocity(player.getLocation().getDirection().multiply(2));
                player.setFallDistance(0);
                canJump.put(player.getUniqueId(), false);
                cooldowns.put(player, System.currentTimeMillis());
            }
            else {
                long storedTime = cooldowns.get(player);

                long timeLeft = 10000 - (System.currentTimeMillis() - storedTime);
                double secondsLeft = timeLeft / 1000.0;

                String seconds = df.format(secondsLeft);

                if (seconds.length() == 1) {
                    seconds+=".0";
                }

                player.sendMessage(ChatColor.RED+seconds+" seconds left until you can use this");
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event)
    {
        if (!isGameActive()) return;

        event.setCancelled(true);
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(4, ChatColor.RESET.toString() + ChatColor.RESET, p);

        updatePlayersAliveScoreboard();
        updateInGameTeamScoreboard();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the damager is an Ender Dragon
        if (event.getDamager() instanceof EnderDragon) {
            // Check if the entity being damaged is a player or another entity (you can specify this if needed)
            if (event.getEntity() instanceof org.bukkit.entity.Player) {
                // Reduce the damage
                double reducedDamage = event.getDamage() * 0.5; // Reduce damage by 50%, adjust as needed
                event.setDamage(reducedDamage);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();

        if (p.getLocation().getY() < map.DEATH_Y) {
            if (p.getGameMode() != GameMode.ADVENTURE) {
                p.teleport(map.SPAWN);
            } else {
                handleDeath(p, false);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getPlayer();

        if (p.getGameMode() != GameMode.ADVENTURE) {
            p.teleport(map.SPAWN);
        } else {
            handleDeath(p, false);
        }
    }
}
