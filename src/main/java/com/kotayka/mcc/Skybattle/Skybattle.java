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
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static org.bukkit.GameMode.SURVIVAL;

public class Skybattle {
    // (PLANNED) STATES: INACTIVE, PLAYING, PAUSED, STARTING, ENDING (?)
    private String state = "INACTIVE";

    public final Players players;
    public final Plugin plugin;
    public boolean stage = false;
    public int playersAlive;
    public List<String> teamsAlive = new ArrayList<>();
    public Map<Entity, Player> creepersAndSpawned = new HashMap<>(5);
    public Map<Entity, Player> playersShot = new HashMap<>(5);
    public int roundNum = 0;
    public double borderHeight = 17.0;
    public int timeLeft;
    public List<Location> spawnPoints = new ArrayList<>(6);
    public List<ItemStack> spawnItems = new ArrayList<>(5);
    public World world;
    public WorldBorder border;
    private final Location CENTER;
    public MCC mcc;


    public Skybattle(Players players, Plugin plugin, MCC mcc) {
        this.players = players;
        this.plugin = plugin;
        this.mcc = mcc;

        if (Bukkit.getWorld("Skybattle") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("Skybattle");
        }

        CENTER = new Location(world, -157, 0, -266);
    }

    public void loadMap() {
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

        for (Participant p : Participant.participantsOnATeam) {
            p.player.getInventory().clear();
            p.player.setHealth(20);
            p.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 10, 4, false, false));
            p.player.setGameMode(SURVIVAL);
            p.player.playSound(p.player.getLocation(), Sound.MUSIC_DISC_STAL, 1, 1);

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
                    p.player.getInventory().addItem(concrete);
                }
                else if (i.getType() == Material.IRON_CHESTPLATE) {
                    p.player.getInventory().setChestplate(i);
                }
                else {
                    p.player.getInventory().addItem(i);
                }
            }
        }

        // Border
        assert world != null;
        border = world.getWorldBorder();
        border.setCenter(CENTER);
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
        creepersAndSpawned.clear();
        playersShot.clear();

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

        // Clear all floor items, primed tnt, and creepers
        for (Item item : world.getEntitiesByClass(Item.class)) {
            item.remove();
        }
        for (Entity tnt : world.getEntitiesByClass(TNTPrimed.class)) {
            tnt.remove();
        }
        for (Entity creeper : world.getEntitiesByClass(Creeper.class)) {
            creeper.remove();
        }
    }

    public void nextRound() {
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
                for (Participant p : Participant.participantsOnATeam) {
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

    public Location getCenter() { return CENTER; }

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