package me.kotayka.mbc.games.acerace;

import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * This class handles powerup related effects for AceRace.
 * Note: The `public` access modifier exists so outside packages can access this class, which is planned to be changed.
 */
public final class PowerupHandler {
    // Ace Race Powerups represented as Minecraft Items.
    private static final Material BANANA_PEEL = Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    private static final Material MEATBALL = Material.SNOWBALL;
    private static final Material LEAP = Material.FEATHER;
    private static final Material STAR = Material.NETHER_STAR;

    // PotionEffects
    private static final PotionEffect STUN_SLOW = new PotionEffect(PotionEffectType.SLOWNESS, 10, 10, false);
    private static final PotionEffect STUN_JUMP = new PotionEffect(PotionEffectType.JUMP_BOOST, 10, 128, false);

    // Collections of Powerup meta for convenient access.
    private static final Map<Material, AceRacePowerup> POWERUP_MATERIALS = Map.ofEntries(
        Map.entry(BANANA_PEEL, AceRacePowerup.BANANA_PEEL),
        Map.entry(MEATBALL, AceRacePowerup.MEATBALL),
        Map.entry(LEAP, AceRacePowerup.LEAP),
        Map.entry(STAR, AceRacePowerup.STAR)
    );

    private static final Map<AceRacePowerup, ItemStack> POWERUP_ITEMS = Map.ofEntries(
        Map.entry(AceRacePowerup.BANANA_PEEL, new ItemStack(BANANA_PEEL, 2)),
        Map.entry(AceRacePowerup.MEATBALL, new ItemStack(MEATBALL, 3)),
        Map.entry(AceRacePowerup.LEAP, new ItemStack(LEAP, 1)),
        Map.entry(AceRacePowerup.STAR, new ItemStack(STAR, 1))
    );

    /**
     * TODO: access modifier is questionable, all AceRace items should be in the same package.
     *
     * Handles providing a player with a powerup, depending on their position at function call.
     * @param player player to receive a powerup
     */
    public static void givePowerup(AceRacePlayer player) {
        player.getPlayer().getInventory().addItem(POWERUP_ITEMS.get(AceRacePowerup.LEAP));
    }

    /**
     * Handles calling the appropriate effects depending on powerup used.
     * For specific powerup implementation, please see the corresponding method.
     *
     * @param player Player who is using a powerup
     * @param heldItem Item currently being used
     * @implNote Currently only fires powerups if used in their main hand, not in their offhand.
     */
    static void usePowerup(AceRacePlayer player, Material heldItem) {
        if (!POWERUP_MATERIALS.containsKey(heldItem)) return;

        AceRacePowerup powerup = POWERUP_MATERIALS.get(heldItem);
        PlayerInventory playerInventory = player.getPlayer().getInventory();
        switch (powerup) {
            case BANANA_PEEL:
                int count = playerInventory.getItemInMainHand().getAmount();
                if (count > 1) {
                    playerInventory.getItemInMainHand().setAmount(1);
                } else {
                    playerInventory.remove(BANANA_PEEL);
                }
                useBanana(player.getPlayer());
                break;
            case LEAP:
                player.getPlayer().getInventory().remove(LEAP);
                useLeap(player.getPlayer());
                break;
        }
    }

    // Handles calling the appropriate effects depending on how a powerup interacted with a player.
    // For specific implementation, please see the corresponding interaction method.
    static void powerupInteract() {

    }

    private static void useBanana(Player player) {
        Location location = player.getLocation();

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
     * Effects for stunning a player.
     * Prevents player from jumping and applies a brief slowness effect.
     *
     * @param stunnedPlayer Player being stunned
     */
    public void stunPlayer(Player stunnedPlayer) {
        stunnedPlayer.addPotionEffect(STUN_SLOW);
        stunnedPlayer.addPotionEffect(STUN_JUMP);
    }
}
