package com.kotayka.mcc.Skybattle;

import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class Skybattle {
    public final Players players;
    public final Plugin plugin;
    public boolean on = false;
    public int roundNum;
    public int playerAmount = 0;
    public int playerPoints;
    public int timeLeft;

    public List<Location> spawnPoints = new ArrayList<>();
    public List<ItemStack> spawnItems = new ArrayList<>();
    public Map<Location, Block> skyMap;
    public Map<Location, ItemStack[]> chestContents;
    public World world = Bukkit.getWorld("Skybattle");


    public Skybattle(Players players, Plugin plugin) {
        this.players = players;
        this.plugin = plugin;
    }

    public void copyMap() {
        // MAP COPY IS FROM 224, -16, 322 TO 363, 18, 440
        for (int x = 225; x < 364; x++) {
            for (int y = -15; y < 19; y++) {
                for (int z = 322; z < 441; z++) {
                    assert world != null;
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getState() instanceof Chest) {
                        Container container = (Chest) b.getState();
                        ItemStack[] itemsForChest = container.getInventory().getContents();
                        chestContents.put(new Location(world, x, y, z), itemsForChest);
                    }
                    skyMap.put(new Location(world, x, y, z), b);
                }
            }
        }
    }

    public void loadMap() {
        // Spawn items
        ItemStack pick = new ItemStack(Material.IRON_PICKAXE);
        pick.addEnchantment(Enchantment.DIG_SPEED, 2);

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
        // CENTER: -155, -7, -265

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
        on = true;
        roundNum = 0;

        loadMap();
        copyMap();

        startRound(roundNum);
    }

    public void resetMap() {
        int x = 225;
        int y = -15;
        int z = 322;
        for (int mapX = -225; mapX < -85; mapX++) {
            for (int mapY = -15; mapY < 17; mapY++) {
                for (int mapZ = -325; mapZ < -206; mapZ++) {
                    Block originalBlock = skyMap.get(new Location(world, x, y, z));
                    Block possiblyChangedBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (originalBlock != possiblyChangedBlock) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                    }
                    if (world.getBlockAt(mapX, mapY, mapZ).getState() instanceof Chest) {
                        ((Chest) possiblyChangedBlock.getState()).getInventory().setContents(chestContents.get(new Location(world, x, y, z)));
                    }
                    z++;
                }
                y++;
            }
            x++;
        }
    }

    public void startRound(int gameRound) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                // countdown TODO
                timeLeft = 240;
                playerPoints=players.players.size();
                playerAmount = 0;
                Location playerLoc = (Location) spawnPoints.get(gameRound);

                // List<Location> tempSpawns = new ArrayList<>(spawnPoints);

               /*
                 for each team
                    for each player in team
                        random spawn
                        reduce tempspawnsize
                 */

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                    p.teleport(playerLoc);
                    p.setGameMode(GameMode.SURVIVAL);
                }

            }
        }, 0);
    }

    public boolean enabled() {
        return on;
    }

}