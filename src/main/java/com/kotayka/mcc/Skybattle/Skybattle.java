package com.kotayka.mcc.Skybattle;

import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Skybattle {
    public final Players players;
    public final Plugin plugin;
    public boolean stage = false;
    public int roundNum;
    public int playerAmount = 0;
    public int playerPoints;
    public int timeLeft;

    public List<Location> spawnPoints = new ArrayList<>();
    public List<ItemStack> spawnItems = new ArrayList<>();

    public Skybattle(Players players, Plugin plugin) {
        this.players = players;
        this.plugin = plugin;
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

        /* Need map coords but server is down
        Location spawnOne = new Location();
        Location spawnTwo = new Location();
        Location spawnThree = new Location();
        Location spawnFour = new Location();
        Location spawnFive = new Location();
        Location spawnSix = new Location();
         */
    }

    public void start() {
        stage = true;
        roundNum = 0;

        startRound(roundNum);
    }

    public void startRound(int gameRound) {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                timeLeft = 240;
                // Use for randomizing spawn point
                Random random = new Random();
                playerPoints=players.players.size();
                playerAmount = 0;
                Location playerLoc = (Location) spawnPoints.get(gameRound);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                    p.teleport(playerLoc);
                    p.setGameMode(GameMode.SURVIVAL);
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
        }, 0);
    }

    public boolean enabled() {
        return stage;
    }

}
