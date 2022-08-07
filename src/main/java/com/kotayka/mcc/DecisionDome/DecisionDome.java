package com.kotayka.mcc.DecisionDome;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DecisionDome {
    public List<Entity> chickens = new ArrayList<org.bukkit.entity.Entity>();


    List<Material> quadrantMats = new ArrayList<>(Arrays.asList(Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.PINK_CONCRETE, Material.GREEN_CONCRETE));

    List<String> games = new ArrayList<>(Arrays.asList("SG", "TGTTOS", "BSABM"));
    public List<List<Block>> quadrants = new ArrayList<>();
    public Map<Material, List<Block>> quadMap = new HashMap<>();
    public List<Integer> removedQuads = new ArrayList<>();
    public final MCC mcc;

    World world;

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
        removedQuads.add(3);
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

    public void timerEnds() {
        int[] scores = {0,0,0,0,0,0,0,0};

        for (Entity chick : chickens) {
            if (quadrantMats.contains(world.getBlockAt((int) chick.getLocation().getX(), (int) (chick.getLocation().getY()-2), (int) chick.getLocation().getZ()).getType())) {
                scores[quadrantMats.indexOf(world.getBlockAt((int) chick.getLocation().getX(), (int) (chick.getLocation().getY()-2), (int) chick.getLocation().getZ()).getType())]++;
            }
            Bukkit.broadcastMessage(world.getBlockAt((int) chick.getLocation().getX(), (int) (chick.getLocation().getY()-2), (int) chick.getLocation().getZ()).getType().toString());
        }

        int[] currMax = getValidFirst();

        for (int i = 0; i < 8; i++) {
            if (scores[i] > currMax[0] && !removedQuads.contains(i)) {
                currMax[0] = scores[i];
                currMax[1] = i;
            }
        }
        mcc.game.changeGame(games.get(currMax[1]));
    }

    public void start() {
        loadQuadrants();
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
