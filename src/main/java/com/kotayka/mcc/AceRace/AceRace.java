package com.kotayka.mcc.AceRace;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class AceRace {
    public List<String> mapOrder = new ArrayList<>(Arrays.asList("Forest", "Desert", "Snow", "Jungle", "Underwater", "Nether", "End", "Small"));
    public Map<String, Location> respawnPoints = new HashMap<>();
    public Map<String, Location> bouncerPoints = new HashMap<>();
    public Map<UUID, String> playerProgress = new HashMap<>();
    public Map<UUID, Integer> playerLaps= new HashMap<>();
    public Map<UUID, Boolean> playerFinish = new HashMap<>();

    public World world;

    public final MCC mcc;

    public AceRace(MCC mcc) {
        this.mcc = mcc;
    }

    public void loadWorld() {
        if (Bukkit.getWorld("AceRace") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("AceRace");
        }
    }

    public void loadCheckpoints() {
        respawnPoints.put("Forest", new Location(world, 45, 26, 145));
        respawnPoints.put("Desert", new Location(world, -59, 26, 140));
        respawnPoints.put("Snow", new Location(world, -119, 27, 77));
        respawnPoints.put("Jungle", new Location(world, -138, 26, -58));
        respawnPoints.put("Underwater", new Location(world, -49, 33, -144));
        respawnPoints.put("Nether", new Location(world, 40, 26, -142));
        respawnPoints.put("End", new Location(world, 138, 26, -62));
        respawnPoints.put("Small", new Location(world, 136, 26, 67));

        bouncerPoints.put("Forest", new Location(world, -22, 28, 150));
        bouncerPoints.put("Desert", new Location(world, -109, 32, 103));
        bouncerPoints.put("Snow", new Location(world, -150, 28, -25));
        bouncerPoints.put("Jungle", new Location(world, -77, 54, -129));
        bouncerPoints.put("Underwater", new Location(world, 7, 24, -144));
        bouncerPoints.put("Nether", new Location(world, 119, 34, -93));
        bouncerPoints.put("End", new Location(world, 150, 26, 25));
        bouncerPoints.put("Small", new Location(world, 125, 26, 84));
    }

    public Boolean checkCoords(Player p) {
        if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getX()-15 <= p.getLocation().getX() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getX()+15 >= p.getLocation().getX()) {
            if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getZ()-15 <= p.getLocation().getZ() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getZ()+15 >= p.getLocation().getZ()) {
                if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getY()-15 <= p.getLocation().getY() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getY()+15 >= p.getLocation().getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void nextCheckpoint(Player p) {
        if (checkCoords(p)) {
            if (mapOrder.indexOf(playerProgress.get(p.getUniqueId())) < 7) {
                playerProgress.put(p.getUniqueId(), mapOrder.get(mapOrder.indexOf(playerProgress.get(p.getUniqueId()))+1));
            }
            else {
                playerFinish.put(p.getUniqueId(), true);
                playerProgress.put(p.getUniqueId(), "Forest");
            }
        }
    }

    public void playerFinish(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
    }

    public void playerFinishLap(Player p) {
        if (playerFinish.get(p.getUniqueId())) {
            if (playerLaps.get(p.getUniqueId()) < 2) {
                playerLaps.put(p.getUniqueId(), playerLaps.get(p.getUniqueId())+1);
            }
            else {
                playerFinish(p);
            }
        }
    }

    public void start() {
        loadCheckpoints();
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.addEnchantment(Enchantment.RIPTIDE, 1);
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            playerProgress.put(p.player.player.getUniqueId(), "Forest");
            p.player.player.getInventory().clear();
            p.player.player.getInventory().addItem(trident);
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 10, false, false));
            p.player.player.teleport(new Location(world, 1, 26, 150));
            mcc.scoreboardManager.createAceRaceBoard(p);
        }
    }
}
