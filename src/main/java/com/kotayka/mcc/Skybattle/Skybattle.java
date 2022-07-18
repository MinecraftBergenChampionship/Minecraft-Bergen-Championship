package com.kotayka.mcc.Skybattle;

import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Skybattle {
    public final Players players;
    public final Plugin plugin;
    public boolean stage = false;
    public int roundNum = 0;
    public int timeLeft;

    public List<Location> spawnPoints = new ArrayList<>(6);
    public List<ItemStack> spawnItems = new ArrayList<>(5);
    public World world;
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

        // Spawn items
        ItemStack pick = new ItemStack(Material.IRON_PICKAXE);
        pick.addEnchantment(Enchantment.DIG_SPEED, 3);

        spawnItems = Arrays.asList(
                new ItemStack(Material.STONE_SWORD), pick,
                new ItemStack(Material.WHITE_CONCRETE, 64),
                new ItemStack(Material.COOKED_BEEF, 4),
                new ItemStack(Material.IRON_CHESTPLATE)
        );

        Location spawnOne = new Location(world, -218, -9, -265);
        Location spawnTwo = new Location(world, -188, -9, -211);
        Location spawnThree = new Location(world, -122, -9, -211);
        Location spawnFour = new Location(world, -92, -9, -265);
        Location spawnFive = new Location(world, -122, -9, -319);
        Location spawnSix = new Location(world, -188, -9, -319);

        spawnPoints = Arrays.asList(spawnOne, spawnTwo, spawnThree, spawnFour, spawnFive, spawnSix);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().clear();
        }

        for (ItemStack i : spawnItems) {
            for (Participant p : players.participants) {
                if (i.getType() == Material.WHITE_CONCRETE) {
                    i.setAmount(64);
                    switch (p.team) {
                        case "RedRabbits" -> i.setType(Material.RED_CONCRETE);
                        case "YellowYaks" -> i.setType(Material.YELLOW_CONCRETE);
                        case "GreenGuardians" -> i.setType(Material.GREEN_CONCRETE);
                        case "BlueBats" -> i.setType(Material.BLUE_CONCRETE);
                        case "PurplePandas" -> i.setType(Material.PURPLE_CONCRETE);
                        case "PinkPiglets" -> i.setType(Material.PINK_CONCRETE);
                        default -> p.player.sendMessage("You're not on a team");
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

        if (Bukkit.getWorld("Skybattle") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("Skybattle");
        }

        int x = 225;
        int y = -16;
        int z = 322;
        for (int mapX = -225; mapX <= -87; mapX++) {
            for (int mapY = -17; mapY <= 16; mapY++) {
                for (int mapZ = -325; mapZ <= -207; mapZ++) {
                    assert world != null;
                    Block originalBlock = world.getBlockAt(x, y, z);
                    Block possiblyChangedBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (!(originalBlock.getType().name().equals(possiblyChangedBlock.getType().name()))) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                    }
                    if (possiblyChangedBlock.getState() instanceof Chest && originalBlock.getState() instanceof Chest) {
                        Container container = (Chest) possiblyChangedBlock.getState();
                        ItemStack[] itemsForChest = container.getInventory().getContents();
                        ((Chest) possiblyChangedBlock.getState()).getInventory().setContents(itemsForChest);
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
        loadMap();
        players.spectators.clear();
        if (roundNum < 3) {
            roundNum++;
            startRound();
        }
    }
    public void startRound() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                // countdown TODO
                timeLeft = 240;
                String[] teamListFull = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
                // Randomly place each team at a different spawn
                // TODO
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
            }
        }, 0);
    }

    public boolean enabled() {
        return stage;
    }
}