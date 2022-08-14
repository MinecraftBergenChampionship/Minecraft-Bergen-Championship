package com.kotayka.mcc.Paintdown;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import net.bytebuddy.ClassFileVersion;
import net.minecraft.core.particles.ParticleParam;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Paintdown {
//  Game State
    private final Players players;
    private final MCC mcc;
    private String state = "INACTIVE";
    public int roundNum = 0;

// World
    public World world;
    public Location CENTER;
    public List<Location> spawnPoints;

// Items
    List<ItemStack> spawnItems;


    public Paintdown(Players players, MCC mcc) {
        this.players = players;
        this.mcc = mcc;

        // world = Bukkit.getWorld("Meltdown") unless null
        world = Bukkit.getWorld("Meltdown") == null ? Bukkit.getWorld("world") : Bukkit.getWorld("Meltdown");

        CENTER = new Location(world, 63, -13, 64);
    }

    /*
     * Prepare map for use
     */
    public void loadMap() {

        // Spawn Items (for now, just iron horse armor and potions)
        spawnItems = Arrays.asList(
                new ItemStack(Material.IRON_HORSE_ARMOR),
                new ItemStack(Material.SPLASH_POTION, 3)
        );

        Location spawnOne = new Location(world, -39, 0, -17);
        Location spawnTwo = new Location(world, -17, 0, 64);
        Location spawnThree = new Location(world, -39, 0, 144);
        Location spawnFour = new Location(world, 165, 0, 144);
        Location spawnFive = new Location(world, 143, 0, 64);
        Location spawnSix = new Location(world, 166, 0, -16);

        spawnPoints = Arrays.asList(spawnOne, spawnTwo, spawnThree, spawnFour, spawnFive, spawnSix);
    }

    public void start() {
        for (ScoreboardPlayer player : mcc.scoreboardManager.playerList) {
            mcc.scoreboardManager.createPaintdownScoreboard(player);
        }

        loadMap();

        startRound();
    }

    public void startRound() {
        loadMap();

        String roundValue = ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ ChatColor.WHITE+ roundNum + "/3";
        mcc.scoreboardManager.changeLine(22, roundValue);

        setState("PLAYING");
        mcc.scoreboardManager.startTimerForGame(240, "Paintdown");

        // Randomly place each team at a different spawn
        List<Location> tempSpawns = new ArrayList<>(spawnPoints);

        for (int i = 0; i < mcc.teamList.size(); i++) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (int j = 0; j < mcc.teamList.get(i).size(); j++) {
                mcc.teamList.get(i).get(j).player.teleport(tempSpawns.get(randomNum));
            }
            tempSpawns.remove(randomNum);
        }

        for (Participant p : Participant.participantsOnATeam) {
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 2, false, false));
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 2, false, false));

            for (ItemStack i : spawnItems) {
                p.player.getInventory().addItem(i);
            }
        }
    }

    public void nextRound() {
        if (roundNum == 3) return;

        loadMap();
        players.spectators.clear();
        if (roundNum < 3) {
            roundNum++;
            startRound();
        }
    }

    /*
    public void timedEventsHandler(int time, ScoreboardPlayer p) {
    }

     */

    public void setState(String state) {
        this.state = state;
    }

    public Location getCenter() {
        return CENTER;
    }

    public String getState() {
        return state;
    }
}
