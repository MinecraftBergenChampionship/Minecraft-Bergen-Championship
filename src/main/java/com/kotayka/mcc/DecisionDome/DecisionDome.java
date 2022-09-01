package com.kotayka.mcc.DecisionDome;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DecisionDome {
    public List<Entity> chickens = new ArrayList<org.bukkit.entity.Entity>();


    List<Material> quadrantMats = new ArrayList<>(Arrays.asList(Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE, Material.GREEN_CONCRETE));

    List<String> games = new ArrayList<>(Arrays.asList("SG", "TGTTOS", "BSABM","AceRace", "Skybattle", "Paintdown"));
    public List<List<Block>> quadrants = new ArrayList<>();
    public Map<Material, List<Block>> quadMap = new HashMap<>();

    int[] currMax = new int[]{0, 0};

    public int[][] coordsForBorder = {
            {0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7},{0,-1},{0,-2},{0,-3},{0,-4},{0,-5},{0,-6},{0,-7},
            {1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0},{-1,0},{-2,0},{-3,0},{-4,0},{-5,0},{-6,0},{-7,0},
            {1,1},{2,2},{3,3},{4,4},{5,5},{1,-1},{2,-2},{3,-3},{4,-4},{5,-5},{-1,1},{-2,2},{-3,3},{-4,4},
            {-5,5},{-1,-1},{-2,-2},{-3,-3},{-4,-4},{-5,-5}
    };

    public List<Integer> removedQuads = new ArrayList<>();
    public final MCC mcc;

    public World world;

    public void loadWorld() {
        if (Bukkit.getWorld("mbcIngameLobby") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("mbcIngameLobby");
        }
    }

    public DecisionDome(MCC mcc) {
        this.mcc = mcc;
    }

    public void loadBlocks(int i, Block b) {
        for (int x : new int[]{-1, 1}) {
            Location location1 = b.getLocation();
            location1.setX(location1.getX()+x);
            Location location2 = b.getLocation();
            location2.setZ(location2.getZ()+x);

            if (quadrantMats.contains(world.getBlockAt(location1).getType())) {
                if (!quadrants.get(i).contains(world.getBlockAt(location1))) {
                    quadrants.get(i).add(world.getBlockAt(location1));
                    loadBlocks(i, world.getBlockAt(location1));
                }
            }

            if (quadrantMats.contains(world.getBlockAt(location2).getType())) {
                if (!quadrants.get(i).contains(world.getBlockAt(location2))) {
                    quadrants.get(i).add(world.getBlockAt(location2));
                    loadBlocks(i, world.getBlockAt(location2));
                }
            }
        }
    }

    public void loadQuadrants() {
        for (int i = 0; i < 8; i++) {
            quadrants.add(new ArrayList<>());
        }
        int yCoord = -37;
        int i = 0;
        for (int z : new int[]{-2,-1,1,2}) {
            for (int x : new int[]{-1,1}) {
                int zCoord = z;
                int xCoord = (3-Math.abs(z))*x;
                quadrants.get(i).add(world.getBlockAt(xCoord,yCoord,zCoord));
                loadBlocks(i, world.getBlockAt(xCoord,yCoord,zCoord));
                quadMap.put(world.getBlockAt(xCoord,yCoord,zCoord).getType(), quadrants.get(i));
                i++;
            }
        }
        for (int b = 0; b < 8; b++) {
            for (Block x : quadrants.get(b)) {
                world.getBlockAt(x.getX(), x.getY()+1, x.getZ()).setType(Material.WHITE_CONCRETE);
            }
        }
        removedQuads.add(4);
        removedQuads.add(5);
        removedQuads.add(6);
        removedQuads.add(7);

        for (Integer removedQuad : removedQuads) {
            for (Block x : quadMap.get(quadrantMats.get(removedQuad))) {
                world.getBlockAt(x.getX(), x.getY()+1, x.getZ()).setType(Material.RED_CONCRETE);
            }
        }
    }

    public void removeQuadrant(Integer quadNum) {
        removedQuads.add(quadNum);
        for (Block x : quadMap.get(quadrantMats.get(quadNum))) {
            world.getBlockAt(x.getX(), x.getY()+1, x.getZ()).setType(Material.RED_CONCRETE);
        }
    }

    public int[] getValidFirst() {
        for (int i = 0; i < 8; i++) {
            if (!removedQuads.contains(i)) {
                return new int[]{0,i};
            }
        }
        return new int[]{0,0};
    }

    public void nextGame() {
        mcc.game.changeGame(games.get(currMax[1]));
    }

    public void addLevitation() {
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 90, 1, false, false));
        }

        mcc.scoreboardManager.changeLine(21, ChatColor.GREEN+"Event: "+ChatColor.LIGHT_PURPLE+"Launching Game");
        mcc.scoreboardManager.startTimerForGame(4, "DDThree");
    }

    public void voteCounterEnds() {
        int[] scores = {0,0,0,0,0,0,0,0};

        for (Entity chick : chickens) {
            if (quadrantMats.contains(world.getBlockAt((int) chick.getLocation().getBlockX(), (int) (chick.getLocation().getBlockY()-2), (int) chick.getLocation().getBlockZ()).getType())) {
                scores[quadrantMats.indexOf(world.getBlockAt((int) chick.getLocation().getBlockX(), (int) (chick.getLocation().getBlockY()-2), (int) chick.getLocation().getBlockZ()).getType())]++;
            }
            Bukkit.broadcastMessage(world.getBlockAt((int) chick.getLocation().getBlockX(), (int) (chick.getLocation().getBlockY()-2), (int) chick.getLocation().getBlockZ()).getType().toString());
        }

        currMax = getValidFirst();

        // Get game
        for (int i = 0; i < 8; i++) {
            if (scores[i] > currMax[0] && !removedQuads.contains(i)) {
                currMax[0] = scores[i];
                currMax[1] = i;
            }
        }
        removedQuads.add(currMax[1]);

        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.sendTitle(ChatColor.WHITE+games.get(currMax[1]),ChatColor.GOLD+"Teleporting soon...",20,80,20);
        }

        mcc.scoreboardManager.changeLine(21, ChatColor.GREEN+"Event: "+ChatColor.LIGHT_PURPLE+"Starting Game Loading");
        mcc.scoreboardManager.startTimerForGame(6, "DDTwo");
    }

    public void timerEnds() {
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.getInventory().clear();
        }
        int slabY = -34;
        int ironBarY = -35;
        int baseY = -36;

        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], slabY, coord[1]).setType(Material.BLACKSTONE_SLAB);
        }
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], ironBarY, coord[1]).setType(Material.BLACK_STAINED_GLASS);
        }
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], baseY, coord[1]).setType(Material.BLACK_CONCRETE);
        }

        mcc.scoreboardManager.startTimerForGame(10, "DDOne");
        mcc.scoreboardManager.changeLine(21, ChatColor.GREEN+"Event: "+ChatColor.LIGHT_PURPLE+"Game Choosing");
    }

    public void start() {
        chickens = new ArrayList<>();

        int slabY = -35;
        int ironBarY = -36;
        int baseY = -37;
        int airY = -34;
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], airY, coord[1]).setType(Material.AIR);
        }
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], slabY, coord[1]).setType(Material.BLACKSTONE_SLAB);
        }
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], ironBarY, coord[1]).setType(Material.BLACK_STAINED_GLASS);
        }
        for (int[] coord : coordsForBorder) {
            world.getBlockAt(coord[0], baseY, coord[1]).setType(Material.BLACK_CONCRETE);
        }
        loadQuadrants();
        List<Entity> entList = world.getEntities();
        for(Entity current : entList){
            if (current.getType() == EntityType.CHICKEN){
                current.remove();
            }
        }
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            mcc.scoreboardManager.createDecisionDome(p);
            p.player.player.sendMessage(mcc.scoreboardManager.playerTeams.get(p).teamName);
            switch (mcc.scoreboardManager.playerTeams.get(p).teamName) {
                case "RedRabbits":
                    p.player.player.teleport(new Location(world, 9, -28, 15));
                    break;
                case "YellowYaks":
                    p.player.player.teleport(new Location(world, -9, -28, 15));
                    break;
                case "GreenGuardians":
                    p.player.player.teleport(new Location(world, -9, -28, -15));
                    break;
                case "BlueBats":
                    p.player.player.teleport(new Location(world, -18, -28, 0));
                    break;
                case "PurplePandas":
                    p.player.player.teleport(new Location(world, 9, -28, -15));
                    break;
                case "PinkPiglets":
                    p.player.player.teleport(new Location(world, 18, -28, 0));
                    break;
            }
            p.player.player.getInventory().addItem(new ItemStack(Material.EGG));
        }
        mcc.scoreboardManager.startTimerForGame(30, "DD");
    }
}
