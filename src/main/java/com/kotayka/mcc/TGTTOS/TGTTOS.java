package com.kotayka.mcc.TGTTOS;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.TGTTOS.managers.NPCManager;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class TGTTOS {
    public boolean stage;
    public final Players players;
    public final NPCManager npcManager;
    public final Plugin plugin;
    public Integer[] gameOrder = { 0,1, 2, 3, 4, 5, 6};

    public int roundNum;

    public int playerAmount = 0;
    public int playerPoints;

    public int timeLeft = 120;
    public String[] mapOrder = {"Cliff", "Meatball", "Skydive", "Glide", "Boats", "Pit", "Walls"};
    public List<Location> spawnPoints = new ArrayList<>();
    public List<int[]> npcSpawnpoints = new ArrayList<>();
    public List<ItemStack[]> items = new ArrayList<>();

    public World world;

    private final MCC mcc;

    public TGTTOS(Players players, NPCManager npcManager, Plugin plugin, MCC mcc) {
        this.players = players;
        this.npcManager = npcManager;
        this.plugin = plugin;
        this.mcc = mcc;

        List<Integer> randomizer = Arrays.asList(gameOrder);
        Collections.shuffle(randomizer);
        randomizer.toArray(gameOrder);
    }

    public void loadMaps() {

        if (Bukkit.getWorld("TGTTOS") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("TGTTOSAWAP");
        }


        Location cliffSpawn = new Location(world, -148, 1, -284);
        Location meatballSpawn = new Location(world, 615, -28, 133);
        Location skydiveSpawn = new Location(world, 757, 53, 442);
        Location glideSpawn = new Location(world, -176, 1, 174);
        Location boatsSpawn = new Location(world, 175, 1, -96);
        Location pitSpawn = new Location(world, 34, 1, 585);
        Location wallsSpawn = new Location(world, 215, -24, 341);

        int[] cliffNPC = {5, -262, -274, -256, -269};
        int[] meatballNPC = {48, 607,134, 611, 138};
        int[] skydiveNPC = {-17, 656, 438, 665, 447};
        int[] glideNPC = {18, -255, 169, -249, 179};
        int[] boatsNPC = {-4, 335, -69, 347, -54};
        int[] pitNPC = {-10, -17, 582, -10, 593};
        int[] wallsNPC = {-12, 205, 235, 227, 259};

        ItemStack[] cliffItems = {};
        ItemStack[] meatballItems = {};
        ItemStack[] skydiveItems = {new ItemStack(Material.WHITE_WOOL)};
        ItemStack[] glideItems = {new ItemStack(Material.WHITE_WOOL), new ItemStack(Material.ELYTRA)};
        ItemStack[] boatItems = {new ItemStack(Material.OAK_BOAT)};
        ItemStack[] pitItems = {new ItemStack(Material.WHITE_WOOL)};
        ItemStack[] wallsItems = {new ItemStack(Material.WHITE_WOOL)};


        ItemStack[][] itemss = {cliffItems,meatballItems,skydiveItems,glideItems,boatItems,pitItems,wallsItems};
        int[][] npcSpawns = {cliffNPC,meatballNPC,skydiveNPC,glideNPC,boatsNPC,pitNPC,wallsNPC};
        Location[] spawn = {cliffSpawn,meatballSpawn,skydiveSpawn,glideSpawn,boatsSpawn,pitSpawn,wallsSpawn};
        for (Location loc : spawn) {
            spawnPoints.add(loc);
        }

        for (int i = 0; i < mapOrder.length; i++) {
            npcSpawnpoints.add(npcSpawns[i]);
        }

        for (ItemStack[] item : itemss) {
            items.add(item);
        }
    }

    public void start() {
        stage = true;
        roundNum = 0;

        for (Player p : players.players) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000000, 4, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100000000, 4, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000000, 4, false, false));
        }


        startRound(gameOrder[roundNum]);

    }

    public void startRound(int gameRound) {
        mcc.scoreboardManager.startTimerForGame(120, "TGTTOS");
        String rvalue = ChatColor.BOLD+""+ChatColor.GREEN + "Round: "+ChatColor.WHITE+(roundNum+1)+"/7";
        String mvalue = ChatColor.BOLD+""+ChatColor.AQUA + "Map: "+ChatColor.WHITE+mapOrder[gameOrder[roundNum]];
        mcc.scoreboardManager.changeLine(22, mvalue);
        mcc.scoreboardManager.changeLine(21, rvalue);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                npcManager.removeAllNPC();
                Random random = new Random();
                playerPoints=players.players.size();
                playerAmount = 0;
                Location playerLoc = (Location) spawnPoints.get(gameRound);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.getInventory().clear();
                    p.teleport(playerLoc);
                    p.setGameMode(GameMode.SURVIVAL);
                }

                ItemStack[] item = items.get(gameRound);

                for (Participant p : players.participants) {
                    for (ItemStack i : item) {
                        if (i.getType() == Material.WHITE_WOOL) {
                            ItemStack wool = new ItemStack(Material.WHITE_WOOL);
                            wool.setAmount(64);
                            switch (p.team) {
                                case "RedRabbits":
                                    wool.setType(Material.RED_WOOL);
                                    break;
                                case "YellowYaks":
                                    wool.setType(Material.YELLOW_WOOL);
                                    break;
                                case "GreenGuardians":
                                    wool.setType(Material.GREEN_WOOL);
                                    break;
                                case "BlueBats":
                                    wool.setType(Material.BLUE_WOOL);
                                    break;
                                case "PurplePandas":
                                    wool.setType(Material.PURPLE_WOOL);
                                    break;
                                case "PinkPiglets":
                                    wool.setType(Material.PINK_WOOL);
                                    break;
                                default:
                                    p.player.sendMessage("Your not on a team");
                            }
                            ItemStack shears = new ItemStack(Material.SHEARS);
                            p.player.getInventory().addItem(wool);
                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                public void run() {
                                    p.player.getInventory().addItem(shears);
                                }
                            }, 20);
                        }
                        else if (i.getType() == Material.ELYTRA) {
                            p.player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
                        }
                        else {
                            p.player.getInventory().addItem(new ItemStack(Material.OAK_BOAT));
                        }

                    }
                }

                int[] coords =  npcSpawnpoints.get(gameRound);
                int xupperBound = coords[3] - coords[1];
                int yCoord = coords[0];
                int zupperBound = coords[4] - coords[2];


                for (Player p : players.players) {
                    int xCoord = random.nextInt(xupperBound) + coords[1];
                    int zCoord = random.nextInt(zupperBound) + coords[2];
                    Location npcLoc = new Location(world, xCoord, yCoord, zCoord);
                    npcManager.spawnNPC(p,npcLoc);
                }
            }
        }, 0);
    }

    public void nextRound() {
        players.spectators.clear();
        mcc.scoreboardManager.resetVars();
        if (roundNum < mapOrder.length) {
            roundNum++;
            startRound(gameOrder[roundNum]);
        }
    }

    public boolean enabled() {
        if (stage) {
            return true;
        }
        return false;
    }
}
