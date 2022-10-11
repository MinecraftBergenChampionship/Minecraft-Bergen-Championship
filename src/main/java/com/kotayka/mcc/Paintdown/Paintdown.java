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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bukkit.GameMode.SURVIVAL;

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
    public WorldBorder border;
    public List<PaintdownRoom> rooms;

    // Store painted blocks;
    // Location also stored for easy access
    public Map<Location, Block> paintedBlocks;

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
        //putGlass();

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
        border.setSize(150);
        border.setDamageAmount(0.5);
        border.setDamageBuffer(0);
        border.setWarningDistance(5);
    }

    public void start() {
        for (ScoreboardPlayer player : mcc.scoreboardManager.playerList) {
            mcc.scoreboardManager.createPaintdownScoreboard(player);
            player.player.player.getInventory().clear();
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000, 10, false, false));
            player.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
        }

        // Create all the paintdown rooms
        // Spawn Rooms
        PaintdownRoom spawnRoomOne = new PaintdownRoom(this, 161, 169, 147, 141, RoomType.SPAWN);
        PaintdownRoom spawnRoomTwo = new PaintdownRoom(this, 146, 140, 68, 60, RoomType.SPAWN);
        PaintdownRoom spawnRoomThree = new PaintdownRoom(this, 169, 161, -19, -13, RoomType.SPAWN);
        PaintdownRoom spawnRoomFour = new PaintdownRoom(this, -35, -43, 19, -13, RoomType.SPAWN);
        PaintdownRoom spawnRoomFive = new PaintdownRoom(this, 20, -14, 60, 68, RoomType.SPAWN);
        PaintdownRoom spawnRoomSix = new PaintdownRoom(this, -35, -43, 141, 147, RoomType.SPAWN);

        // Coin Rooms
        PaintdownRoom coinRoomOne = new PaintdownRoom(this, 138, 90, -11, 37, RoomType.COIN);
        PaintdownRoom coinRoomTwo = new PaintdownRoom(this, 138, 90, 91, 139, RoomType.COIN);
        PaintdownRoom coinRoomThree = new PaintdownRoom(this, -12, 36, 139, 91, RoomType.COIN);
        PaintdownRoom coinRoomFour = new PaintdownRoom(this, 36, -12, -11, 37, RoomType.COIN);

        // Regular
        PaintdownRoom regularRoomOne = new PaintdownRoom(this, 141, 178, 139, 93, RoomType.REGULAR);
        PaintdownRoom regularRoomTwo = new PaintdownRoom(this, 178, 141, -11, 35, RoomType.REGULAR);
        PaintdownRoom regularRoomThree = new PaintdownRoom(this, 90, 138, 88, 40, RoomType.REGULAR);
        PaintdownRoom regularRoomFour = new PaintdownRoom(this,39,87,37,-11, RoomType.REGULAR);
        PaintdownRoom regularRoomFive = new PaintdownRoom(this, 39, 87, 139, 91, RoomType.REGULAR);
        PaintdownRoom regularRoomSix = new PaintdownRoom(this, 36, -12, 40, 88, RoomType.REGULAR);
        PaintdownRoom regularRoomSeven = new PaintdownRoom(this, -52, -15, 35, -11, RoomType.REGULAR);
        PaintdownRoom regularRoomEight = new PaintdownRoom(this, -52, -15, 139, 93, RoomType.REGULAR);

        rooms = Arrays.asList(spawnRoomOne, spawnRoomTwo, spawnRoomThree, spawnRoomFour, spawnRoomFive, spawnRoomSix,
                coinRoomOne, coinRoomTwo, coinRoomThree, coinRoomFour, regularRoomOne, regularRoomTwo, regularRoomThree,
                regularRoomFour, regularRoomFive, regularRoomSix, regularRoomSeven, regularRoomEight);

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

    // Perform all necessary operations to prepare map for next game
    public void resetMap() {
        cleanPaintOfWalls();
        replaceCoinCage(Material.LIGHT_BLUE_STAINED_GLASS);
        replaceMiddleEntrance(Material.WHITE_STAINED_GLASS);
        replaceSpawnDoors(Material.WHITE_STAINED_GLASS);
        replaceMiddleCoinCage(Material.LIGHT_BLUE_STAINED_GLASS);

        paintedBlocks.clear();
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
    public void cleanPaintOfWalls() {
        for (Map.Entry<Location, Block> entry : paintedBlocks.entrySet()) {
            Block b = world.getBlockAt(entry.getKey());
            b.setType(entry.getValue().getType());
            world.setBlockData(entry.getKey(), entry.getValue().getBlockData());
        }
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
                mcc.scoreboardManager.startTimerForGame(300, "Paintdown");
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
                }
                break;
        }
    }

    // Middle Coin Crate
    public void replaceMiddleCoinCage(Material m) {
        for (int y = -12; y < -10; y++) {
            for (int z = 62; z < 66; z++) {
                Location l = new Location(world, 65, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 61; x < 64; x++) {
                Location l = new Location(world, x, y, 62);
                l.getBlock().setType(m);
            }

            for (int z = 63; z < 66; z++) {
                Location l = new Location(world, 61, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 62; x < 64; x++) {
                Location l = new Location(world, x, y, 66);
                l.getBlock().setType(m);
            }
        }

        for (int x = 62; x < 64; x++) {
            for (int z = 63; z < 65; z++) {
                Location l = new Location(world, x, -10, z);
                l.getBlock().setType(m);
            }
        }
    }

    // Open Middle
    public void replaceMiddleEntrance(Material m) {
        for (int y = 0; y < 2; y++) {
            for (int x = 61; x < 65; x++) {
                for (int z = 38; z < 39; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }

                for (int z = 89; z < 90; z++) {
                    Location l = new Location(world, x, y, z);
                    l.getBlock().setType(m);
                }
            }
        }
    }


    // Coin Crates
    public void replaceCoinCage(Material m) {
        for (int y = -6; y < -4; y++) {
            for (int z = 11; z < 15; z++) {
                Location l = new Location(world, 112, y, z);
                l.getBlock().setType(m);
            }

            for (int z = 113; z < 117; z++) {
                Location l = new Location(world, 112, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 112; x < 116; x++){
                Location l = new Location(world, x, y, 117);
                l.getBlock().setType(m);
            }

            for (int x = 10; x < 14; x++) {
                Location l = new Location(world, x, y, 117);
                l.getBlock().setType(m);
            }

            for (int z = 113; z < 117; z++) {
                Location l = new Location(world, 14, y, z);
                l.getBlock().setType(m);
            }

            for (int z = 11; z < 15; z++) {
                Location l = new Location(world, 14, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 10; x < 14; x++) {
                Location l = new Location(world, x, y, 11);
                l.getBlock().setType(m);
            }

            for (int x = 112; x < 116; x++) {
                Location l = new Location(world, x, y, 11);
                l.getBlock().setType(m);
            }
        }

        // Roof
        for (int z = 12; z < 14; z++) {
            for (int x = 11; x < 13; x++) {
                Location l = new Location(world, x, -4, z);
                l.getBlock().setType(m);
            }

            for (int x = 113; x < 115; x++) {
                Location l = new Location(world, x, -4, z);
                l.getBlock().setType(m);
            }
        }

        for (int z = 114; z < 116; z++) {
            for (int x = 113; x < 116; x++) {
                Location l = new Location(world, x, -4, z);
                l.getBlock().setType(m);
            }

            for (int x = 11; x < 13; x++) {
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
        for (int y = 0; y < 2; y++) {
            for (int z = 62; z < 66; z++) {
                Location l = new Location(world, 139, y, z);
                l.getBlock().setType(m);
            }

            for (int x = 163; x < 167; x++) {
                Location l = new Location(world, x, y, -12);
                l.getBlock().setType(m);
            }

            for (int x = 163; x < 167; x++) {
                Location l = new Location(world, x, y, 140);
                l.getBlock().setType(m);
            }

            for (int x = -41; x < -37; x++) {
                Location l = new Location(world, x, y, 140);
                l.getBlock().setType(Material.AIR);
            }

            for (int z = 62; z < 66; z++) {
                Location l = new Location(world, -13, y, z);
                l.getBlock().setType(m);
            }

            for (int x = -41; x < -37; x++) {
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
