package me.kotayka.mbc.games.acerace;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This class handles powerup related effects for AceRace.
 * Note: The `public` access modifier exists so outside packages can access this class, which is planned to be changed.
 */
public final class PowerupHandler {
    // Ace Race Powerups represented as Minecraft Items.
    private static final Material BANANA_PEEL = Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    private static final Material MEATBALL = Material.SNOWBALL;
    private static final Material RED_SHELL = Material.RED_CONCRETE_POWDER;
    private static final Material SUGAR_HIGH = Material.SUGAR;
    private static final Material LEAP = Material.FEATHER;
    private static final Material STAR = Material.NETHER_STAR;
    private static final Material ROCKET_LAUNCHER = Material.WOODEN_SHOVEL;
    private static final Material TNT = Material.TNT;
    private static final Material TELEPORTER = Material.ENDER_PEARL;
    private static final Material FIREBALL = Material.FIRE_CHARGE;
    //private static final Material LUNGE = Material.WOODEN_SPEAR;
    //private static final Material GOLDEN_LUNGE = Material.GOLDEN_SPEAR;

    // Appropriate Set of Powerups
    private static final List<AceRacePowerup> TOP_SIXTH = Arrays.asList(AceRacePowerup.BANANA_PEEL, AceRacePowerup.TNT, AceRacePowerup.MEATBALL);
    private static final List<AceRacePowerup> TOP_THIRD = Arrays.asList(AceRacePowerup.BANANA_PEEL, AceRacePowerup.LEAP, AceRacePowerup.TNT, AceRacePowerup.MEATBALL);
    private static final List<AceRacePowerup> TOP_HALF = Arrays.asList(AceRacePowerup.LEAP, AceRacePowerup.TNT, AceRacePowerup.MEATBALL, AceRacePowerup.SUGAR_HIGH);
    private static final List<AceRacePowerup> BOTTOM_HALF = Arrays.asList(AceRacePowerup.LEAP, AceRacePowerup.STAR, AceRacePowerup.ROCKET_LAUNCHER, AceRacePowerup.SUGAR_HIGH);
    private static final List<AceRacePowerup> BOTTOM_SIXTH = Arrays.asList(AceRacePowerup.TELEPORTER, AceRacePowerup.STAR, AceRacePowerup.SUGAR_HIGH, AceRacePowerup.ROCKET_LAUNCHER);

    private static final List<AceRacePowerup> SPINNING_POWERUPS = Arrays.asList(
            AceRacePowerup.BANANA_PEEL,
            AceRacePowerup.SUGAR_HIGH,
            AceRacePowerup.MEATBALL,
            AceRacePowerup.LEAP,
            AceRacePowerup.ROCKET_LAUNCHER,
            AceRacePowerup.TNT,
            AceRacePowerup.TELEPORTER,
            AceRacePowerup.STAR
    );


    // This list is reserved for the very first checkpoint on the very first lap.
    private static final List<AceRacePowerup> FIRST_CHECKPOINT_POWERUPS = Arrays.asList(AceRacePowerup.BANANA_PEEL, AceRacePowerup.MEATBALL, AceRacePowerup.TNT);
    //private static final List<AceRacePowerup> FIRST_CHECKPOINT_POWERUPS = Arrays.asList(AceRacePowerup.GOLDEN_LUNGE);
    private static final List<AceRacePowerup> PRACTICE_POWERUPS = Arrays.asList(
            AceRacePowerup.BANANA_PEEL, AceRacePowerup.LEAP, AceRacePowerup.TNT, AceRacePowerup.STAR,
            AceRacePowerup.MEATBALL, AceRacePowerup.SUGAR_HIGH, AceRacePowerup.TELEPORTER, AceRacePowerup.ROCKET_LAUNCHER
    );

    // Cutoffs for each powerup level
    private static final double CUTOFF_TOP_SIXTH = (1.0)/6;
    private static final double CUTOFF_TOP_THIRD = (1.0)/3;
    private static final double CUTOFF_TOP_HALF =  (1.0)/2;
    private static final double CUTOFF_BOTTOM_HALF =  (2.0)/3;
    private static final double CUTOFF_BOTTOM_SIXTH =  (5.0)/6;

    // Powerup Customization
    private static final int POWERUP_SPIN_DURATION_TICKS = 60; // 3 seconds
    private static final int STAR_DURATION_SECONDS = 10;
    private static final int LUNGE_DURATION_SECONDS = 8;
    private static final int STUN_DURATION_TICKS = 60; // 3 seconds
    private static final int TELEPORTER_DELAY_TICKS = 50; // 2.5 seconds
    private static final int SUGAR_HIGH_DURATION_TICKS = 140;

    // PotionEffects
    private static final PotionEffect STUN_SLOW = new PotionEffect(PotionEffectType.SLOWNESS, STUN_DURATION_TICKS, 10, false, true, true);
    private static final PotionEffect STUN_BLIND = new PotionEffect(PotionEffectType.BLINDNESS, STUN_DURATION_TICKS + 15, 1, false, true, true);

    // Collections of Powerup meta for convenient access.
    private static final Map<Material, AceRacePowerup> POWERUP_MATERIALS = Map.ofEntries(
            Map.entry(BANANA_PEEL, AceRacePowerup.BANANA_PEEL),
            Map.entry(SUGAR_HIGH, AceRacePowerup.SUGAR_HIGH),
            Map.entry(MEATBALL, AceRacePowerup.MEATBALL),
            Map.entry(LEAP, AceRacePowerup.LEAP),
            Map.entry(ROCKET_LAUNCHER, AceRacePowerup.ROCKET_LAUNCHER),
            Map.entry(TNT, AceRacePowerup.TNT),
            Map.entry(TELEPORTER, AceRacePowerup.TELEPORTER),
            Map.entry(FIREBALL, AceRacePowerup.FIREBALL),
            Map.entry(STAR, AceRacePowerup.STAR)
            //Map.entry(LUNGE, AceRacePowerup.LUNGE),
            //Map.entry(GOLDEN_LUNGE, AceRacePowerup.GOLDEN_LUNGE)
    );

    private static final Map<AceRacePowerup, ItemStack> POWERUP_ITEMS = initializePowerupMeta();
    private static final Map<AreaEffectCloud, AceRacePlayer> playerCloudMap = new HashMap<>();

    private static final Set<Player> starPlayers = new HashSet<>();
    private static final Set<Player> goldenLungers = new HashSet<>();
    private static final Set<Player> playersWithPowerup = new HashSet<>();
    private static final Set<Player> stunnedPlayers = new HashSet<>();

    private static final Map<Entity, Participant> tntMap = new HashMap<>();

    /**
     * Separate simplified function for providing a powerup during tutorial phase.
     * Rather than
     *
     * @see PowerupHandler givePowerup()
     * @see PowerupHandler giveRandomPowerupTutorial()
     * @param aceRacePlayer
     */
    public static void givePowerupTutorial(AceRacePlayer aceRacePlayer, int targetSlot) {
        Player player = aceRacePlayer.getPlayer();

        for (Material powerupMaterial : POWERUP_MATERIALS.keySet()) {
            player.setCooldown(powerupMaterial, POWERUP_SPIN_DURATION_TICKS + 2);
        }

        if (targetSlot < 0 || targetSlot > 8) return;

        giveRandomPowerupTutorial(player, targetSlot);
    }

    /**
     *
     * Handles providing a player with a powerup, depending on their position at function call.
     * @param aceRacePlayer player to receive a powerup
     */
    public static void givePowerup(AceRacePlayer aceRacePlayer) {
        Player player = aceRacePlayer.getPlayer();

        // Do not give powerup if player has not fully used theirs
        if (playersWithPowerup.contains(player)) return;

        for (Material powerupMaterial : POWERUP_MATERIALS.keySet()) {
            player.setCooldown(powerupMaterial, POWERUP_SPIN_DURATION_TICKS + 2);
        }

        // Find first available slot in slots 0-8 (slot 9 is reserved for the cooldown spinner)
        int targetSlot = -1;
        for (int s = 0; s < 1; s++) {
            if (player.getInventory().getItem(s) == null) {
                targetSlot = s;
                break;
            }
        }

        // If slots 0-8 are all filled, do not give a powerup
        if (targetSlot == -1) return;

        List<AceRacePowerup> powerups;
        if (aceRacePlayer.onFirstCheckpoint()) {
            powerups = FIRST_CHECKPOINT_POWERUPS;
        } else {
            int place = aceRacePlayer.getCurrentPlacement();
            int players = MBC.getInstance().getPlayers().size();
            double percentile = (1.0 * place) / players;
            if (percentile <= CUTOFF_TOP_SIXTH) {
                powerups = TOP_SIXTH;
                Bukkit.broadcastMessage("DEBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in top sixth");
            } else if (percentile <= CUTOFF_TOP_THIRD) {
                powerups = TOP_THIRD;
                Bukkit.broadcastMessage("DEGBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in top third");
            } else if (percentile <= CUTOFF_TOP_HALF) {
                powerups = TOP_HALF;
                Bukkit.broadcastMessage("DEBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in top half");
            } else if (percentile <= CUTOFF_BOTTOM_HALF) {
                powerups = BOTTOM_HALF;
                Bukkit.broadcastMessage("DEBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in bottom half");
            } else if (percentile <= CUTOFF_BOTTOM_SIXTH) {
                powerups = BOTTOM_SIXTH;
                Bukkit.broadcastMessage("DEBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in bottom third");
            } else {
                powerups = BOTTOM_SIXTH;
                Bukkit.broadcastMessage("DEBUG: " + aceRacePlayer.getParticipant().getFormattedName() + ": " + aceRacePlayer.currentPlace + " place, thus in bottom sixth");
            }
        }

        playersWithPowerup.add(player);
        giveRandomPowerup(powerups, player, targetSlot);
    }

    /**
     * Handles calling the appropriate effects depending on powerup used.
     * For specific powerup implementation, please see the corresponding method.
     *
     * @param player Player who is using a powerup
     * @param heldItem Item currently being used
     * @implNote Currently only fires powerups if used in their main hand, not in their offhand.
     */
    static void usePowerup(AceRacePlayer player, Material heldItem, boolean lookingAtBlock, boolean tutorial) {
        if (!POWERUP_MATERIALS.containsKey(heldItem)) return;
        if (player.getPlayer().getInventory().getHeldItemSlot() == 8)  {
            return;
        }

        AceRacePowerup powerup = POWERUP_MATERIALS.get(heldItem);
        PlayerInventory playerInventory = player.getPlayer().getInventory();
        switch (powerup) {
            case BANANA_PEEL:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useBanana(player);
                break;
            case LEAP:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useLeap(player.getPlayer());
                break;
            case SUGAR_HIGH:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useSugarHigh(player.getPlayer());
                break;
            case MEATBALL:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                break;
            case TNT:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                throwTNT(player.getParticipant());
                break;
            case ROCKET_LAUNCHER:
                if (!lookingAtBlock) {
                    player.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Look down and behind to propel forward!");
                    return;
                }

                ItemStack item = playerInventory.getItemInMainHand();
                ItemMeta meta = item.getItemMeta();

                if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                    int max = item.getType().getMaxDurability();
                    int step = max / 4;

                    int newDamage = damageable.getDamage() + step;

                    if (newDamage >= max) {
                        if (tutorial) {
                            ItemStack practicePowerup = new ItemStack(Material.PURPLE_DYE);
                            ItemMeta purpleMeta = practicePowerup.getItemMeta();
                            purpleMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "Practice Powerup");
                            practicePowerup.setItemMeta(purpleMeta);

                            playerInventory.setItemInMainHand(null);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                                playerInventory.addItem(practicePowerup);
                            }, 20L);
                        } else {
                            playerInventory.setItemInMainHand(null);
                            playersWithPowerup.remove(player.getPlayer());
                        }
                    } else {
                        damageable.setDamage(newDamage);
                        item.setItemMeta((ItemMeta) damageable);
                        playerInventory.setItemInMainHand(item);
                    }
                }

                useRocketLauncher(player.getPlayer());
                break;
            case TELEPORTER:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useTeleporter(player);
                break;
            case STAR:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useStar(player);
                break;
            case FIREBALL:
                consumeOneFromMainHand(player.getPlayer(), playerInventory, tutorial);
                useFireball(player.getPlayer());
                break;
            case LUNGE:
                break;
            case GOLDEN_LUNGE:
                break;
        }
    }

    /**
     * Removes exactly one item from the player's main hand.
     * If the stack has more than one, decrements the count.
     * If it was the last one, clears the slot and removes the player from playersWithPowerup.
     */
    private static void consumeOneFromMainHand(Player player, PlayerInventory inventory, boolean tutorial) {
        ItemStack held = inventory.getItemInMainHand();
        if (held == null || held.getType() == Material.AIR) return;
        Material mat = held.getType();
        int newAmount = held.getAmount() - 1;
        if (newAmount <= 0) {
            // do not allow player to receive powerups until duration of effects is over
            long powerupDuration = 0L;
            if (mat == STAR) {
                powerupDuration = STAR_DURATION_SECONDS * 20;
            } else if (mat == SUGAR_HIGH) {
                powerupDuration = SUGAR_HIGH_DURATION_TICKS;
            }

            inventory.setItemInMainHand(null);
            Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                if (!tutorial) {
                    playersWithPowerup.remove(player);
                } else {
                    ItemStack practicePowerup = new ItemStack(Material.PURPLE_DYE);
                    ItemMeta purpleMeta = practicePowerup.getItemMeta();
                    purpleMeta.setDisplayName(ChatColor.BOLD + "" + ChatColor.DARK_PURPLE + "Practice Powerup");
                    practicePowerup.setItemMeta(purpleMeta);
                    if (inventory.getItem(8) == null || inventory.getItem(8).getType() != AceRace.SPECTATOR_ITEM.getType()) {
                        inventory.setItem(8, AceRace.SPECTATOR_ITEM);
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                        inventory.addItem(practicePowerup);
                    }, 20L);
                }
            }, powerupDuration);

        } else {
            held.setAmount(newAmount);
        }
    }

    private static void useRocketLauncher(Player player) {
        Vector direction = player.getLocation().getDirection();
        Vector reverse = direction.multiply(-1).normalize();
        double power = 1.5;

        Vector velocity = reverse.multiply(power);
        velocity.setY(velocity.getY());

        player.setVelocity(velocity);
    }

    

    private static void throwTNT(Participant eventPlayer) {
        Player player = eventPlayer.getPlayer();
        Location loc = player.getEyeLocation().add(
                player.getLocation().getDirection().multiply(1.2)
        );

        TNTPrimed tnt = player.getWorld().spawn(loc, TNTPrimed.class);
        tntMap.put(tnt, eventPlayer);

        tnt.setFuseTicks(50);
        tnt.setYield(0f);

        Vector velocity = player.getLocation().getDirection().multiply(1.2);
        tnt.setVelocity(velocity);
    }

    private static void useBanana(AceRacePlayer player) {
        Location location = player.getPlayer().getLocation().clone();

        Bukkit.getScheduler().runTaskLater(MBC.getInstance().getPlugin(), () -> {
            AreaEffectCloud cloud = (AreaEffectCloud) location.getWorld()
                    .spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);
            playerCloudMap.put(cloud, player);

            cloud.setDuration(140);
            cloud.setRadius(2.5f);
            cloud.setColor(Color.YELLOW);

            cloud.setParticle(Particle.DUST, new Particle.DustOptions(Color.YELLOW, 1.0f));

            cloud.setReapplicationDelay(10);
            cloud.setWaitTime(0);

            cloud.addCustomEffect(
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 3),
                    true
            );
        }, 60L);

        AreaEffectCloud warningCloud = (AreaEffectCloud) location.getWorld()
                .spawnEntity(location, EntityType.AREA_EFFECT_CLOUD);

        warningCloud.setDuration(60);
        warningCloud.setRadius(2.5f);
        warningCloud.setColor(Color.GREEN);

        warningCloud.setParticle(Particle.DUST, new Particle.DustOptions(Color.GREEN, 1.0f));
    }


    private static void useSugarHigh(Player player) {
        player.playSound(player.getLocation(), "sfx.speed_pad", SoundCategory.BLOCKS, 1, 2);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, SUGAR_HIGH_DURATION_TICKS, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SUGAR_HIGH_DURATION_TICKS, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, SUGAR_HIGH_DURATION_TICKS, 4));
    }

    /**
     * Effects when a player uses the LEAP powerup.
     * @param player player using the leap powerup.
     */
    private static void useLeap(Player player) {
        player.setVelocity(player.getLocation().getDirection().multiply(1.4));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 2);
        player.setFallDistance(0);
    }

    /**
     * Handles effects for teleporter powerup.
     * @implNote Checkpoint behavior is currently handled in AceRacePlayer warpCheckpointSetter
     * @see AceRacePlayer warpCheckpointSetter
     * @param player Player using the teleporter powerup.
     */
    private static void useTeleporter(AceRacePlayer player) {
        Player p = player.getPlayer();
        int checkpoint = player.checkpoint;
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
            player.warpCheckpointSetter(checkpoint+1);
            p.teleport(AceRaceMap.respawns.get((checkpoint%AceRaceMap.respawns.size())));
            p.removePotionEffect(PotionEffectType.SPEED);
            p.setFireTicks(0);
            p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        }, TELEPORTER_DELAY_TICKS);

        p.addPotionEffect(STUN_SLOW);
        p.addPotionEffect(STUN_BLIND);

        for (int i = 0; i < STUN_DURATION_TICKS; i+=4) {
            MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                p.spawnParticle(
                        Particle.PORTAL,
                        p.getLocation().add(
                                -1 + Math.random()*3, 0.75, -1 + Math.random()*3
                        ), 8);
            }, i);
        }
    }

    /**
     * Handles using star effects for player.
     *
     * @see this.starEffects
     * @param player The player who is activating the effects.
     */
    private static void useStar(AceRacePlayer player) {
        Bukkit.broadcastMessage(player.getParticipant().getFormattedName() + ChatColor.GOLD + ChatColor.BOLD + " activated a Star Powerup!");
        Player p = player.getPlayer();
        p.sendMessage(ChatColor.GREEN + "Punch a player to stun them!");
        starPlayers.add(p);
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.SLOWNESS);
        p.removePotionEffect(PotionEffectType.WEAKNESS);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, STAR_DURATION_SECONDS * 20, 5));
        starEffects(p, STAR_DURATION_SECONDS);
    }

    /**
     * Handles using golden lunge effects for player.
     *
     * @param player The player who is activating the effects.
     */
    //public static void useGoldenLunge(Player player) {
        //if (!goldenLungers.contains(player)) {
            //player.sendMessage(ChatColor.GREEN + "You have " + LUNGE_DURATION_SECONDS + " seconds of unlimited lunges!");
            //goldenLungers.add(player);
            //MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() { @Override
            //public void run() {
               // player.getInventory().remove(GOLDEN_LUNGE);
               // goldenLungers.remove(player);
           // }
           // }, 160L); // change this if the lunge duration changes
       // }   
   // }

    /**
     * Handles initial effects of using the fireball powerup
     * @param player The player using the fireball powerup
     */
    private static void useFireball(Player player) {
        Fireball f = player.getWorld().spawn(player.getEyeLocation(), Fireball.class);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
        f.setShooter(player.getPlayer());
        f.setIsIncendiary(false);
        f.setYield(0);
    }

    /**
     * Effects for stunning a player.
     * Prevents player from jumping and applies a brief slowness effect.
     *
     * Stunned players are tracked such that they cannot be stunned multiple times
     * while still in a stunned state.
     *
     * @param stunnedPlayer Player being stunned
     * @return Whether the result of this call was successful
     */
    public static boolean stunPlayer(Player stunnedPlayer) {
        // Prevent multi-stun
        if (stunnedPlayers.contains(stunnedPlayer)) return false;

        stunnedPlayers.add(stunnedPlayer);

        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
            stunnedPlayers.remove(stunnedPlayer);
        }, STUN_DURATION_TICKS);

        stunnedPlayer.addPotionEffect(STUN_SLOW);
        stunnedPlayer.addPotionEffect(STUN_BLIND);
        return true;
    }

    /**
     * Handles visual effects and duration for players who use the star powerup.
     *
     * @param player player using the star powerup.
     * @param timeLeft remaining time for star powerup.
     */
    private static void starEffects(Player player, int timeLeft) {
        if (timeLeft == 0) {
            starPlayers.remove(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 10, false, false));
            return;
        }

        for (int i = 0; i < STAR_DURATION_SECONDS; i++) {
            MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() { @Override
            public void run() {
                player.spawnParticle(
                        Particle.FIREWORK,
                        player.getLocation().add(
                                -1 + Math.random()*3, 0.75, -1 + Math.random()*3
                        ), 8);
                player.spawnParticle(
                        Particle.NOTE,
                        player.getLocation().add(
                                -1 + Math.random()*3, Math.random(), -1 + Math.random()*3
                        ), 8);
            }
            }, i);
        }


        MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), new Runnable() { @Override
        public void run() { starEffects(player, timeLeft - 1); }
        }, 20L);
    }

    /**
     * Handles filtering area cloud effects from the BANANA_PEEL powerup.
     *
     * @see AceRace onAreaCloud
     * @param cloud The AreaEffectCloud entity
     * @param iterator Iterator over a mutable collection of affected entities.
     */
    static void handleAreaCloudEffect(AreaEffectCloud cloud, Iterator<LivingEntity> iterator) {
        while (iterator.hasNext()) {
            LivingEntity entity = iterator.next();
            if (!(entity instanceof Player player)) continue;

            Participant eventPlayer = Participant.getParticipant(player);
            AceRacePlayer powerupUser = playerCloudMap.get(cloud);
            if (powerupUser == null) continue;

            if (eventPlayer == null || player.getGameMode() == GameMode.SPECTATOR ||
                    eventPlayer.getTeam().getColor().equals(powerupUser.getParticipant().getTeam().getColor()) ||
                    starPlayers.contains(player) || stunnedPlayers.contains(player)) {
                iterator.remove();
            } else {
                stunnedPlayers.add(player);
                Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                    stunnedPlayers.remove(player);
                }, STUN_DURATION_TICKS);
                player.sendMessage(powerupUser.getParticipant().getFormattedName() + ChatColor.RED + " slowed you with their " + ChatColor.YELLOW + ChatColor.BOLD + "Banana Peel "
                        + ChatColor.RESET + ChatColor.RED + "powerup!");
                powerupUser.getParticipant().getPlayer().sendMessage(ChatColor.GREEN + "You slowed " + eventPlayer.getFormattedName() + " with your "
                        + ChatColor.YELLOW + ChatColor.BOLD + "Banana Peel "  + ChatColor.RESET + ChatColor.RED + "powerup!");
            }
        }
    }

    static void handleFireballExplosion(List<Player> affected, Entity fireball) {

    }

    /**
     * Handles visual effects and providing a powerup to a given player.
     * The spin animation plays in slot 9 (index 8). Once the spin finishes,
     * the chosen powerup is placed in {@code targetSlot} and a cooldown is
     * applied only to that powerup's material.
     *
     * @param powerups   List of powerups the player may receive.
     * @param player     Player who is receiving the powerup.
     * @param targetSlot The inventory slot (0-8) where the final powerup will land.
     */
    private static void giveRandomPowerup(List<AceRacePowerup> powerups, Player player, int targetSlot) {
        final int SPIN_SLOT = 8; // slot 9 in the hotbar (0-indexed)
        AceRacePowerup chosenPowerup = powerups.get((int) (Math.random() * powerups.size()));
        List<AceRacePowerup> spin_powerups = new ArrayList<>();

        for (AceRacePowerup p : SPINNING_POWERUPS) {
            if (powerups.contains(p)) {
                spin_powerups.add(p);
            }
        }
        if (spin_powerups.isEmpty()) {
            for (AceRacePowerup p : SPINNING_POWERUPS) {
                spin_powerups.add(p);
            }
        }

        // Spin animation in slot 9
        for (int i = 0; i < POWERUP_SPIN_DURATION_TICKS; i += 4) {
            int frame = i / 4;
            Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                player.getInventory().setItem(SPIN_SLOT, POWERUP_ITEMS.get(spin_powerups.get(frame % spin_powerups.size())));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }, i);
        }

        // After spin: clear slot 9, place powerup in target slot, apply cooldown only to chosen material
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
            player.getInventory().setItem(SPIN_SLOT, null);
            player.getInventory().setItem(targetSlot, POWERUP_ITEMS.get(chosenPowerup));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        }, POWERUP_SPIN_DURATION_TICKS + 2L);
    }

    private static void giveRandomPowerupTutorial(Player player, int targetSlot) {
        AceRacePowerup chosenPowerup = PRACTICE_POWERUPS.get((int) (Math.random() * PRACTICE_POWERUPS.size()));
        List<AceRacePowerup> spin_powerups = new ArrayList<>();

        for (AceRacePowerup p : SPINNING_POWERUPS) {
            if (PRACTICE_POWERUPS.contains(p)) {
                spin_powerups.add(p);
            }
        }
        if (spin_powerups.isEmpty()) {
            for (AceRacePowerup p : SPINNING_POWERUPS) {
                spin_powerups.add(p);
            }
        }

        // Spin animation in slot 9
        for (int i = 0; i < POWERUP_SPIN_DURATION_TICKS; i += 4) {
            int frame = i / 4;
            Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
                player.getInventory().setItem(targetSlot, POWERUP_ITEMS.get(spin_powerups.get(frame % spin_powerups.size())));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }, i);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().getPlugin(), () -> {
            player.getInventory().setItem(targetSlot, null);
            player.getInventory().setItem(targetSlot, POWERUP_ITEMS.get(chosenPowerup));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        }, POWERUP_SPIN_DURATION_TICKS + 2L);
    }

    // Initialize ItemStack items for Powerups.
    private static Map<AceRacePowerup, ItemStack> initializePowerupMeta() {
        // banana
        ItemStack banana = new ItemStack(BANANA_PEEL, 1);
        ItemMeta meta = banana.getItemMeta();
        Component displayName = Component.text("Banana Peel").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        banana.setItemMeta(meta);

        // sugar
        ItemStack sugar = new ItemStack(SUGAR_HIGH, 1);
        meta = sugar.getItemMeta();
        displayName = Component.text("Sugar Rush").decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        sugar.setItemMeta(meta);

        // meatball
        ItemStack meatball = new ItemStack(MEATBALL, 5);
        meta = meatball.getItemMeta();
        displayName = Component.text("Meatball").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        meatball.setItemMeta(meta);

        // leap
        ItemStack leap = new ItemStack(LEAP, 1);
        meta = leap.getItemMeta();
        displayName = Component.text("Leap").decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        leap.setItemMeta(meta);

        // tnt
        ItemStack tnt= new ItemStack(TNT, 2);
        meta = tnt.getItemMeta();
        displayName = Component.text("TNT").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        tnt.setItemMeta(meta);

        // rocket
        ItemStack rocket = new ItemStack(ROCKET_LAUNCHER, 1);
        meta = rocket.getItemMeta();
        displayName = Component.text("Rocket Launcher").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        rocket.setItemMeta(meta);

        // star
        ItemStack star = new ItemStack(STAR, 1);
        meta = star.getItemMeta();
        displayName = Component.text("Super Star").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        star.setItemMeta(meta);

        // teleporter
        ItemStack teleporter = new ItemStack(TELEPORTER, 1);
        meta = teleporter.getItemMeta();
        displayName = Component.text("Warp").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        teleporter.setItemMeta(meta);

        // fireball
        ItemStack fireball = new ItemStack(FIREBALL, 1);
        meta = fireball.getItemMeta();
        displayName = Component.text("Fireball").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        meta.displayName(displayName);
        fireball.setItemMeta(meta);

        // lunge
        //ItemStack lunge = new ItemStack(LUNGE, 1);
        //meta = lunge.getItemMeta();
        //displayName = Component.text("Lunge").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD);
        //meta.displayName(displayName);
        //Damageable damageable = (Damageable) meta;
        //damageable.setDamage(lunge.getType().getMaxDurability() - 1);
        //lunge.setItemMeta((ItemMeta) damageable);
        //lunge.addEnchantment(Enchantment.LUNGE, 2);

        // lunge
        //ItemStack goldenLunge = new ItemStack(GOLDEN_LUNGE, 1);
        //meta = goldenLunge.getItemMeta();
        //displayName = Component.text("Golden Lunge").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
        //meta.displayName(displayName);
        //goldenLunge.setItemMeta(meta);
        //goldenLunge.addEnchantment(Enchantment.LUNGE, 2);

        return Map.ofEntries(
                Map.entry(AceRacePowerup.BANANA_PEEL, banana),
                Map.entry(AceRacePowerup.SUGAR_HIGH, sugar),
                Map.entry(AceRacePowerup.MEATBALL, meatball),
                Map.entry(AceRacePowerup.LEAP, leap),
                Map.entry(AceRacePowerup.TNT, tnt),
                Map.entry(AceRacePowerup.ROCKET_LAUNCHER, rocket),
                Map.entry(AceRacePowerup.TELEPORTER, teleporter),
                Map.entry(AceRacePowerup.STAR, star)
                //Map.entry(AceRacePowerup.LUNGE, lunge),
                //Map.entry(AceRacePowerup.GOLDEN_LUNGE, goldenLunge)
        );
    }

    /**
     *
     * @return Unmodifiable view of players that are currently using the Star Powerup.
     */
    public static Set<Player> getStarPlayers() {
        return Collections.unmodifiableSet(starPlayers);
    }

    /**
     *
     * @return Unmodifiable view of map over TNT entities and the player that threw them.
     */
    public static Map<Entity, Participant> getTNTMap() {
        return Collections.unmodifiableMap(tntMap);
    }

    /**
     * @return Unmodifiable view over all powerup materials
     */
    public static Set<Material> getPowerupMaterials() {
        return Collections.unmodifiableSet(POWERUP_MATERIALS.keySet());
    }
}