package com.kotayka.mcc.Paintdown;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Paintdown {
//  Game State
    private final Players players;
    public final MCC mcc;
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
        ItemStack potion = new ItemStack(Material.SPLASH_POTION, 3);
        PotionMeta meta = (PotionMeta)potion.getItemMeta();
        assert meta != null;
        meta.setColor(Color.BLUE);

        // Spawn Items (for now, just iron horse armor and potions)
        spawnItems = Arrays.asList(
                new ItemStack(Material.IRON_HORSE_ARMOR), potion,
                new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_LEGGINGS)
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
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
        }

        startRound();
    }

    public void startRound() {
        loadMap();

        String roundValue = ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ ChatColor.WHITE+ roundNum + "/3";
        mcc.scoreboardManager.changeLine(22, roundValue);

        this.setState("PLAYING");
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
                if (i.getType().toString().startsWith("LEATHER")) {
                    ItemStack newArmor = p.getColoredLeatherArmor(i);
                    switch (newArmor.getType()) {
                        case LEATHER_HELMET -> p.player.getInventory().setHelmet(newArmor);
                        case LEATHER_CHESTPLATE -> p.player.getInventory().setChestplate(newArmor);
                        case LEATHER_LEGGINGS -> p.player.getInventory().setLeggings(newArmor);
                        default -> p.player.getInventory().setBoots(newArmor);
                    }
                } else {
                    p.player.getInventory().addItem(i);
                }
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

    // Paint a random number of armor from 1-3 (4 painted slots signifies eliminated)
    public void paintHitPlayer(Participant p) {
        ItemStack[] armor = p.player.getInventory().getArmorContents();
        int random = (int) (Math.random() * 2 + 1);

        // TODO refine algorithm to prevent double changes
        for (int i = 0; i < random; i++) {
            int slot = (int) (Math.random() * 2 + 1);
            ItemStack leatherPiece = getPaintedLeatherArmor(armor[Math.abs(slot-random)]);
            switch (leatherPiece.getType()) {
                case LEATHER_HELMET -> p.player.getInventory().setHelmet(leatherPiece);
                case LEATHER_CHESTPLATE -> p.player.getInventory().setChestplate(leatherPiece);
                case LEATHER_LEGGINGS -> p.player.getInventory().setLeggings(leatherPiece);
                case LEATHER_BOOTS -> p.player.getInventory().setBoots(leatherPiece);
            }
        }
    }

    public ItemStack getPaintedLeatherArmor(ItemStack i) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
            assert meta != null;
            if (meta.getColor().equals(Color.BLACK)) return i;
            meta.setColor(Color.BLACK);
            i.setItemMeta(meta);
            return i;
        } catch (ClassCastException e) {
            Bukkit.broadcastMessage("Passed Item Stack was not leather armor!");
            return i;
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
