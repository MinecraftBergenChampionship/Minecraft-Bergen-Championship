package com.kotayka.mcc.Paintdown;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static org.bukkit.GameMode.SURVIVAL;

public class Paintdown {
//  Game State
    private final Players players;
    public final MCC mcc;
    private String state = "INACTIVE";
    public int roundNum = 1;

// World
    public World world;
    public Location CENTER;
    public List<Location> spawnPoints;
    public WorldBorder border;
    //public List<PaintdownRoom> rooms;

    // Store painted blocks;
    // Location also stored for easy access
    public Map<Location, Block> paintedBlocks = new HashMap<>(10);

// Items
    List<ItemStack> spawnItems;
    
// Coins
    public List<Location> coinLocations;


    public Paintdown(Players players, MCC mcc) {
        this.players = players;
        this.mcc = mcc;

        // world = Bukkit.getWorld("Meltdown") unless null
        world = Bukkit.getWorld("Meltdown") == null ? Bukkit.getWorld("world") : Bukkit.getWorld("Meltdown");
        assert world != null;

        CENTER = new Location(world, 63, 0, 64);
        createCoinLocations();
        resetMap();
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
                new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.WOODEN_PICKAXE)
        );

        Location spawnOne = new Location(world, -39, 0, -17);
        Location spawnTwo = new Location(world, -17, 0, 64);
        Location spawnThree = new Location(world, -39, 0, 144);
        Location spawnFour = new Location(world, 165, 0, 144);
        Location spawnFive = new Location(world, 143, 0, 64);
        Location spawnSix = new Location(world, 166, 0, -16);

        spawnPoints = Arrays.asList(spawnOne, spawnTwo, spawnThree, spawnFour, spawnFive, spawnSix);

        assert world != null;
        border = world.getWorldBorder();
        border.setCenter(CENTER);
        border.setSize(500);
        border.setDamageAmount(0.5);
        border.setDamageBuffer(0);
        border.setWarningDistance(5);

        setCoinCrates();
    }

    public void createCoinLocations() {
        coinLocations = Arrays.asList(
                // Room One
                new Location(world, -4, -2, 55), new Location(world, 7, -3, 56),
                new Location(world, 33, -2, 63), new Location(world, 19, 0, 75),
                new Location(world, 10, -1, 86),
                // Room Two
                new Location(world, 105, 0, 53), new Location(world, 115, -1, 42),
                new Location(world, 130, -2,71), new Location(world, 120, -3, 72),
                new Location(world, 95, -2, 66),
                // Room three
                new Location(world, 153, -1, 118), new Location(world, 161, -1, 119),
                new Location(world, 158, 1,136), new Location(world, 159, -4, 101),
                new Location(world, 150, -4, 102),
                // Room four
                new Location(world, 154, -4, 29), new Location(world, 165, 1, 34),
                new Location(world, 161, -1, 8), new Location(world, 166, 0, -1),
                new Location(world, 142, -3, 6),
                // Room five
                new Location(world, 69, -2, 31), new Location(world, 51, -3, 17),
                new Location(world, 68, -6, 18), new Location(world, 71, -2, -4),
                new Location(world, 55, -5, 7),
                // Room Six
                new Location(world, -24, -4, 30), new Location(world, -34, -4, 20),
                new Location(world, -35, -1, 7), new Location(world, -34, -2, -5),
                new Location(world, -26, 0, 14),
                // Room Seven
                new Location(world, -31, 0, 134), new Location(world, -45, 2, 96),
                new Location(world, -25, 0, 123), new Location(world, -18, -3, 110),
                new Location(world, -32, -1, 120)
        );
    }

    public void start() {
        loadMap();
        for (ScoreboardPlayer player : mcc.scoreboardManager.playerList) {
            mcc.scoreboardManager.createPaintdownScoreboard(player);
            player.player.player.getInventory().clear();
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 255, false, false));
        }
        // Create all the paintdown rooms
        // Paintdown rooms are currently not needed
        /*
        // Coin Rooms
        PaintdownRoom coinRoomOne = new PaintdownRoom(this, 138, 90, -11, 37, RoomType.COIN);
        PaintdownRoom coinRoomTwo = new PaintdownRoom(this, 138, 90, 91, 139, RoomType.COIN);
        PaintdownRoom coinRoomThree = new PaintdownRoom(this, -12, 36, 139, 91, RoomType.COIN);
        PaintdownRoom coinRoomFour = new PaintdownRoom(this, 36, -12, -11, 37, RoomType.COIN);
        Bukkit.broadcastMessage("coinRoomCheckpoint");
        // Regular
        PaintdownRoom regularRoomOne = new PaintdownRoom(this, 141, 178, 139, 93, RoomType.REGULAR);
        PaintdownRoom regularRoomTwo = new PaintdownRoom(this, 178, 141, -11, 35, RoomType.REGULAR);
        PaintdownRoom regularRoomThree = new PaintdownRoom(this, 90, 138, 88, 40, RoomType.REGULAR);
        PaintdownRoom regularRoomFour = new PaintdownRoom(this,39,87,37,-11, RoomType.REGULAR);
        PaintdownRoom regularRoomFive = new PaintdownRoom(this, 39, 87, 139, 91, RoomType.REGULAR);
        PaintdownRoom regularRoomSix = new PaintdownRoom(this, 36, -12, 40, 88, RoomType.REGULAR);
        PaintdownRoom regularRoomSeven = new PaintdownRoom(this, -52, -15, 35, -11, RoomType.REGULAR);
        PaintdownRoom regularRoomEight = new PaintdownRoom(this, -52, -15, 139, 93, RoomType.REGULAR);
        Bukkit.broadcastMessage("regularRoomCheckpoint");

        rooms = Arrays.asList(coinRoomOne, coinRoomTwo, coinRoomThree, coinRoomFour, regularRoomOne, regularRoomTwo, regularRoomThree,
                regularRoomFour, regularRoomFive, regularRoomSix, regularRoomSeven, regularRoomEight); */

        setCoinCrates();
        startRound();
    }

    public void startRound() {
        String roundValue = ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ ChatColor.WHITE+ roundNum + "/3";
        mcc.scoreboardManager.changeLine(22, roundValue);

        this.setState("STARTING");
        mcc.scoreboardManager.startTimerForGame(10, "Paintdown");

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
            p.player.getInventory().clear();
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

        resetMap();
        loadMap();
        players.spectators.clear();
        if (roundNum < 3) {
            roundNum++;
            startRound();
        }
    }

    // Perform all necessary operations to prepare map for next game
    public void resetMap() {
        if (paintedBlocks.size() >= 1)
            cleanPaintOffWalls();
        replaceCoinCage(Material.LIGHT_BLUE_STAINED_GLASS);
        replaceMiddleEntrance(Material.WHITE_STAINED_GLASS);
        replaceSpawnDoors(Material.WHITE_STAINED_GLASS);
        replaceMiddleCoinCage(Material.LIGHT_BLUE_STAINED_GLASS);

        /*
        // Reset all unmined coin crates
        for (PaintdownRoom r : rooms) {
            r.resetCoinCrates();
        }
         */

        resetCoinCrates();

        //rooms.clear();
        /*
        moved to cleanPaintOffWalls()
        if (paintedBlocks.size() >= 1)
            paintedBlocks.clear();
         */
        resetBorder();
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

    /* Turn painted walls back to original state */
    public void cleanPaintOffWalls() {
        Bukkit.broadcastMessage("Cleaning paint...");
        for (Map.Entry<Location, Block> entry : paintedBlocks.entrySet()) {
            Block b = world.getBlockAt(entry.getKey());
            b.setType(entry.getValue().getType());
            world.setBlockData(entry.getKey(), entry.getValue().getBlockData());
        }
        paintedBlocks.clear();
    }

    // Events that happen at the end of each game state
    public void timeEndEvents() {
        switch(this.getState()) {
            case "PLAYING":
                mcc.paintdown.setState("END_ROUND");
                mcc.scoreboardManager.startTimerForGame(10, "Paintdown");
                break;
            case "STARTING":
                replaceSpawnDoors(Material.AIR);
                for (Participant p : Participant.participantsOnATeam) {
                    p.player.setGameMode(SURVIVAL);
                }
                mcc.scoreboardManager.startTimerForGame(240, "Paintdown");
                setState("PLAYING");
                break;
            case "END_ROUND":
                if (roundNum < 3) {
                    nextRound();
                } else {
                    mcc.game.endGame();
                    setState("INACTIVE");
                    mcc.setGameOver(true);
                    resetMap();
                }
                break;
        }
    }

    // Events that happen during the game and should be applied PER PERSON
    public void timedEventsHandler(int time, ScoreboardPlayer p) {
        switch (this.getState()) {
            case "STARTING":
                p.player.player.sendTitle("Starting in:", "> " + time + " <", 0, 20, 0);
                break;
            case "END_ROUND":
                if (time == 9) {
                    p.player.player.sendTitle(ChatColor.BOLD + "" + ChatColor.RED + "Round Over!", null, 0, 20, 0);
                    p.player.player.sendMessage(ChatColor.BOLD+""+ChatColor.RED+"Round Over!");
                }
                break;
        }
    }

    // For events that should happen ONCE per game
    public void specialEvents(int time) {
        switch(this.getState()) {
            case "PLAYING":
                if (time == 265) {
                    Bukkit.broadcastMessage(ChatColor.BOLD + "> Coin Crates will open in 15 seconds!");
                } else if (time == 240) {
                    Bukkit.broadcastMessage(ChatColor.AQUA + "> Coin Crates have been opened!");
                    replaceCoinCage(Material.AIR);
                } else if (time == 180) {
                    Bukkit.broadcastMessage(ChatColor.BOLD + "> Doors to the middle room are now open!");
                    replaceMiddleEntrance(Material.AIR);
                } else if (time == 135) {
                  Bukkit.broadcastMessage(ChatColor.BOLD + "> The middle coin crate will open in 15 seconds!");
                } else if (time == 120) {
                    Bukkit.broadcastMessage(ChatColor.AQUA + "> The middle coin crate is now open!");
                    replaceMiddleCoinCage(Material.AIR);
                }
                break;
            case "END_ROUND":
                if (time == 9) {
                    // rewardLastPlayers();
                    Bukkit.broadcastMessage("We should reward the last players now");
                } else if (time == 3) {
                    resetBorder();
                }
                break;
        }
    }

    // Middle Coin Crate
    public void replaceMiddleCoinCage(Material m) {
        for (int y = -12; y <= -10; y++) {
            for (int z = 62; z <= 66; z++) {
                Location l = new Location(world, 65, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 61; x <= 64; x++) {
                Location l = new Location(world, x, y, 62);
                l.getBlock().setType(m);
            }

            for (int z = 63; z <= 66; z++) {
                Location l = new Location(world, 61, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 62; x <= 64; x++) {
                Location l = new Location(world, x, y, 66);
                l.getBlock().setType(m);
            }
        }

        for (int x = 62; x <= 64; x++) {
            for (int z = 63; z <= 65; z++) {
                Location l = new Location(world, x, -10, z);
                l.getBlock().setType(m);
            }
        }
    }

    // Open Middle
    public void replaceMiddleEntrance(Material m) {
        for (int y = 0; y <= 2; y++) {
            for (int x = 61; x <= 65; x++) {
                for (int z = 38; z <= 39; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }

                for (int z = 89; z <= 90; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
            }
        }
    }


    // Coin Crates
    public void replaceCoinCage(Material m) {
        // Sides
        for (int y = -6; y <= -4; y++) {
            for (int z = 11; z <= 15; z++) { Location l = new Location(world, 112, y, z); l.getBlock().setType(m); }
            for (int z = 113; z <= 117; z++) { Location l = new Location(world, 112, y, z); l.getBlock().setType(m); }
            for (int z = 11; z <= 15; z++) { Location l = new Location(world, 116, y, z); l.getBlock().setType(m); }
            for (int z = 113; z <= 116; z++) { Location l = new Location(world, 116, y, z); l.getBlock().setType(m); }
            for (int x = 112; x <= 116; x++){ Location l = new Location(world, x, y, 117); l.getBlock().setType(m); }
            for (int x = 10; x <= 14; x++) { Location l = new Location(world, x, y, 117); l.getBlock().setType(m); }
            for (int z = 113; z <= 117; z++) { Location l = new Location(world, 14, y, z); l.getBlock().setType(m); }
            for (int z = 11; z <= 15; z++) { Location l = new Location(world, 14, y, z); l.getBlock().setType(m); }
            for (int x = 10; x <= 14; x++) { Location l = new Location(world, x, y, 11); l.getBlock().setType(m); }
            for (int x = 112; x <= 116; x++) { Location l = new Location(world, x, y, 11); l.getBlock().setType(m); }
            for (int x = 113; x <= 116; x++) { Location l = new Location(world, x, y, 15); l.getBlock().setType(m); }
            for (int x = 10; x <= 13; x++) { Location l = new Location(world, x, y, 15); l.getBlock().setType(m); }
        }

        // Roof
        for (int z = 12; z <= 14; z++) {
            for (int x = 11; x <= 13; x++) {
                Location l = new Location(world, x, -4, z); l.getBlock().setType(m);
            }
            for (int x = 113; x <= 115; x++) {
                Location l = new Location(world, x, -4, z); l.getBlock().setType(m);
            }
        }

        for (int z = 114; z <= 116; z++) {
            for (int x = 113; x <= 116; x++) {
                Location l = new Location(world, x, -4, z);
                l.getBlock().setType(m);
            }

            for (int x = 11; x <= 13; x++) {
                Location l = new Location(world, x, -4, z);
                l.getBlock().setType(m);
            }
        }
    }

    public void resetBorder() {
        world.getWorldBorder().reset();
    }

    // Give coins to last players
    /*
    public void rewardLastPlayers() {
        for (Participant p : Participant.participantsOnATeam) {
            if (!playersDeadList.contains(p.player.getUniqueId())) {
                mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.player.getUniqueId()), 15);
                p.player.setAllowFlight(true);
                p.player.sendMessage(ChatColor.GREEN+"You survived the round!");
                p.player.setFlying(true);
                p.player.setInvulnerable(true);
            }
        }
    } */


    /*
    // Glass for room entrances
    public void replaceCoinRoomEntrances(Material m) {
        for (int y = 0; y < 2; y++) {
            for (int x = 139; x < 140; x++) {
                for (int z = 11; z < 15; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
                for (int z = 113; z < 117; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
            }

            for (int z = 89; z < 90; z++) {
                for (int x = 112; x < 116; x++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
                for (int x = 10; x < 14; x++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
            }

            for (int x = 37; x < 38; x++) {
                for (int z = 113; z < 117; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
                for (int z = 11; z < 15; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
            }

            for (int z = 38; z < 39; z++) {
                for (int x = 10; x < 14; x++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
                for (int x = 112; x < 114; x++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(Material.AIR);
                }
            }
        }
    }*/

    // Open/Close Spawn Doors
    public void replaceSpawnDoors(Material m) {
        for (int y = 0; y <= 2; y++) {
            for (int z = 62; z <= 66; z++) {
                Location l = new Location(world, 139, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 163; x <= 167; x++) {
                Location l = new Location(world, x, y, -12);
                l.getBlock().setType(m);
            }

            for (int x = 163; x <= 167; x++) {
                Location l = new Location(world, x, y, 140);
                l.getBlock().setType(m);
            }

            for (int x = -41; x <= -37; x++) {
                Location l = new Location(world, x, y, 140);
                l.getBlock().setType(Material.AIR);
            }

            for (int z = 62; z <= 66; z++) {
                Location l = new Location(world, -13, y, z);
                l.getBlock().setType(m);
            }

            for (int x = -41; x <= -37; x++) {
                Location l = new Location(world, x, y, -12);
                l.getBlock().setType(m);
            }
        }
    }


    // Eliminate team
    public void eliminateTeam(int index) {
        for (Participant p : mcc.teamList.get(index)) {
            p.player.sendTitle(ChatColor.RED + "TEAM PAINTED", null, 0, 60, 40);
            Bukkit.getScheduler().scheduleSyncDelayedTask(mcc.plugin, new Runnable() {
                @Override
                public void run() {
                    p.player.setGameMode(GameMode.SPECTATOR);
                    p.player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 2, false, false));
                    p.player.teleport(getCenter());
                }
            }, 60);
        }
    }

    public void resetCoinCrates() {
        for (Location l : coinLocations) {
            l.getBlock().setType(Material.AIR);
        }
    }

    public void setCoinCrates() {
        for (Location l : coinLocations) {
            l.getBlock().setType(Material.LODESTONE);
        }
    }


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
