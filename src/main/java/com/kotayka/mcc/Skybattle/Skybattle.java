package com.kotayka.mcc.Skybattle;

import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static org.bukkit.GameMode.SURVIVAL;

public class Skybattle {
    // (PLANNED) STATES: INACTIVE, PLAYING, PAUSED, STARTING, ENDING (?)
    private String state = "INACTIVE";

    public final Players players;
    public final Plugin plugin;
    public boolean stage = false;
    public int roundNum = 0;
    public double borderHeight = 17.0;
    public int timeLeft;
    public List<Location> spawnPoints = new ArrayList<>(6);
    public List<ItemStack> spawnItems = new ArrayList<>(5);
    public World world;
    public WorldBorder border;
    public MCC mcc;


    public Skybattle(Players players, Plugin plugin, MCC mcc) {
        this.players = players;
        this.plugin = plugin;
        this.mcc = mcc;
    }

    public void loadMap() {
        if (Bukkit.getWorld("Skybattle") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("Skybattle");
        }

        resetMap();

        // Spawn items
        ItemStack pick = new ItemStack(Material.IRON_PICKAXE);
        pick.addEnchantment(Enchantment.DIG_SPEED, 3);

        spawnItems = Arrays.asList(
                new ItemStack(Material.STONE_SWORD), pick,
                new ItemStack(Material.WHITE_CONCRETE, 64),
                new ItemStack(Material.COOKED_BEEF, 7),
                new ItemStack(Material.IRON_CHESTPLATE)
        );

        Location spawnOne = new Location(world, -220, -9, -266);
        Location spawnTwo = new Location(world, -190, -9, -212);
        Location spawnThree = new Location(world, -124, -9, -212);
        Location spawnFour = new Location(world, -94, -9, -266);
        Location spawnFive = new Location(world, -124, -9, -320);
        Location spawnSix = new Location(world, -190, -9, -320);

        spawnPoints = Arrays.asList(spawnOne, spawnTwo, spawnThree, spawnFour, spawnFive, spawnSix);

        for (Participant p : players.participants) {
            p.player.getInventory().clear();
            p.player.setHealth(20);
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 10, 4, false, false));
            p.player.setGameMode(SURVIVAL);

            for (ItemStack i : spawnItems) {
                if (i.getType() == Material.WHITE_CONCRETE) {
                    ItemStack concrete = new ItemStack(Material.WHITE_CONCRETE);
                    concrete.setAmount(64);
                    switch (p.team) {
                        case "RedRabbits":
                            concrete.setType(Material.RED_CONCRETE);
                            break;
                        case "YellowYaks":
                            concrete.setType(Material.YELLOW_CONCRETE);
                            break;
                        case "GreenGuardians":
                            concrete.setType(Material.GREEN_CONCRETE);
                            break;
                        case "BlueBats":
                            concrete.setType(Material.BLUE_CONCRETE);
                            break;
                        case "PurplePandas":
                            concrete.setType(Material.PURPLE_CONCRETE);
                            break;
                        case "PinkPiglets":
                            concrete.setType(Material.PINK_CONCRETE);
                            break;
                        default:
                            p.player.sendMessage("You're not on a team");
                    }
                    p.player.getInventory().addItem(i);
                }
                else if (i.getType() == Material.IRON_CHESTPLATE) {
                    p.player.getInventory().setChestplate(i);
                }
                else {
                    p.player.getInventory().addItem(i);
                }
            }
        }

        for (Item item : world.getEntitiesByClass(Item.class)) {
            item.remove();
        }
        for (Entity tnt : world.getEntitiesByClass(TNTPrimed.class)) {
            tnt.remove();
        }

        // Border
        assert world != null;
        border = world.getWorldBorder();
        border.setCenter(-157, -266);
        border.setSize(150);
        border.setDamageAmount(0.5);
        border.setDamageBuffer(0);
        border.setWarningDistance(5);

        borderHeight = 17;
    }

    public void start() {
        stage = true;

        loadMap();

        startRound();
    }

    /*
     * Reset Map
     */
    public void resetMap() {
        int x = 225;
        int y = -16;
        int z = 322;
        for (int mapX = -225; mapX <= -87; mapX++) {
            for (int mapY = -17; mapY <= 17; mapY++) {
                for (int mapZ = -325; mapZ <= -207; mapZ++) {
                    assert world != null;
                    Block originalBlock = world.getBlockAt(x, y, z);
                    Block possiblyChangedBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (!(originalBlock.getType().name().equals(possiblyChangedBlock.getType().name()))) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                        possiblyChangedBlock.setBlockData(originalBlock.getBlockData());
                    }
                    if (possiblyChangedBlock.getState() instanceof Chest && originalBlock.getState() instanceof Chest) {
                        Container container = (Chest) originalBlock.getState();
                        ItemStack[] itemsForChest = container.getInventory().getContents();
                        ((Chest) possiblyChangedBlock.getState()).getInventory().setContents(itemsForChest);
                    }
                    if (possiblyChangedBlock.getState() instanceof BrewingStand && originalBlock.getState() instanceof BrewingStand) {
                        Container container = (BrewingStand) originalBlock.getState();
                        ItemStack[] potions = container.getInventory().getContents();
                        ((BrewingStand) possiblyChangedBlock.getState()).getInventory().setContents(potions);
                    }
                    z++;
                }
                z = 322;
                y++;
            }
            z = 322;
            y = -16;
            x++;
        }
    }


    public void nextRound() {
        resetMap();
        resetBorder();
        loadMap();
        players.spectators.clear();
        if (roundNum < 3) {
            roundNum++;
            startRound();
        }
    }

    public void startRound() {
        // Scoreboard shenanigans will be handled in MCC.java although it would be nice to have a scoreboard manager for each game
        timeLeft = 10;
        setState("STARTING");
        // countdown in MCC.java

        // Randomly place each team at a different spawn
        List<Location> tempSpawns = new ArrayList<>(spawnPoints);
        List<Team[]> teamListPogU = new ArrayList<>(mcc.teams.values());

        int i = 0;
        // this implementation looks super inefficient and wack but brain is too fried
        for (Team t : teamListPogU.get(i)) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (String s : t.getEntries()) {
                for (Participant p : players.participants) {
                    if (p.ign.equals(s)) {
                        p.player.teleport(tempSpawns.get(randomNum));
                    }
                }
            }
            tempSpawns.remove(randomNum);
            i++;
        }

        // SetState("PLAYING")
        // timeLeft = 240
        // both in MCC.java
    }

    public void resetBorder() {
        world.getWorldBorder().reset();
    }

    /*
    public void startingCountdown() {

        // not sure if smart to use another run here instead of doing it in MCC but it'd be neater if this works
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                for (Participant p : players.participants) {
                    p.player.sendTitle("Starting in: ", "> " + timeLeft + " <", 0, 20, 0);
                }
            }
        }, 20);
    }
    */

    public void setState(String state) {
        this.state = state;
    }
    public String getState() {
        return state;
    }
    public boolean enabled() {
        return stage;
    }
}