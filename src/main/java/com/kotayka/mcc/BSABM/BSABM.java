package com.kotayka.mcc.BSABM;

import org.bukkit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BSABM {

    public World world;
    public List<List<Material>> maps = new ArrayList<>();
    public int[] teamsProgress = {0,0,0,0,0,0};
    public int[][] teamFields = {{0,1,2,3},{0,1,2,3},{0,1,2,3},{0,1,2,3},{0,1,2,3},{0,1,2,3}};
    public int[] teamsCoords = {77,68,59,50,41,32};
    public int[] teamsCoordsBuilding = {95,104,113,122,131,140};

    public void loadWorld() {
        if (Bukkit.getWorld("BSABM") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("BSABM");
        }
    }
    public void loadMaps() {
        maps = new ArrayList<>();
        Location map = new Location(world, 57, 8, 35);
        int numOfMaps = 0;
        Bukkit.broadcastMessage(ChatColor.RED+"Loading Maps");
        while (world.getBlockAt((int) (map.getX()+3), (int) (map.getY()-1), (int) (map.getZ()+3)).getType() == Material.DIAMOND_BLOCK) {
            Bukkit.broadcastMessage(ChatColor.GREEN+"Map#" +numOfMaps+" Loaded");
            Location genMap = map.clone();
            List<Material> blocks = new ArrayList<>();
            for (int y = (int) genMap.getY(); y <= genMap.getY()+5; y++) {
                for (int x = (int) genMap.getX(); x <= genMap.getX()+6; x++) {
                    for (int z = (int) genMap.getZ(); z <= genMap.getZ()+6; z++) {
                        blocks.add(world.getBlockAt(x,y,z).getType());
                    }
                }
            }
            numOfMaps++;
            map.setX(map.getX()+8);
            maps.add(blocks);
        }
        Collections.shuffle(maps);
    }

    public Location getCoords(int teamNum, int fieldNum) {
        int spaceBetweenBoards = 1;
        int yCoord = 8;
        int xCoord = teamsCoords[teamNum];
        int z = 109;
        int zCoord = (z+7*fieldNum)+(spaceBetweenBoards*fieldNum);
        return new Location(world, xCoord, yCoord, zCoord);
    }

    public Location getCoordsForMap(int teamNum, int fieldNum) {
        int spaceBetweenBoards = 1;
        int yCoord = 8;
        int xCoord = teamsCoordsBuilding[teamNum];
        int z = 109;
        int zCoord = (z+7*fieldNum)+(spaceBetweenBoards*fieldNum);
        return new Location(world, xCoord, yCoord, zCoord);
    }

    public void placeMap(int teamNum, int fieldNum) {
        Location map = getCoords(teamNum, fieldNum);
        Location buildMap = getCoordsForMap(teamNum, fieldNum);
        int i=0;
        for (int y = (int) map.getY(); y <= map.getY()+5; y++) {
            for (int x = (int) map.getX(); x <= map.getX()+6; x++) {
                for (int z = (int) map.getZ(); z <= map.getZ()+6; z++) {
                    world.getBlockAt(x,y,z).setType(maps.get(teamsProgress[teamNum]).get(i));
                    i++;
                }
            }
        }
        i=0;
        teamsProgress[teamNum]++;
        for (int y = (int) buildMap.getY(); y <= buildMap.getY()+5; y++) {
            for (int x = (int) buildMap.getX(); x <= buildMap.getX()+6; x++) {
                for (int z = (int) buildMap.getZ(); z <= buildMap.getZ()+6; z++) {
                    if (y == buildMap.getY()) {
                        world.getBlockAt(x,y,z).setType(maps.get(teamsProgress[teamNum]).get(i));
                        i++;
                    }
                    else {
                        world.getBlockAt(x,y,z).setType(Material.AIR);
                    }
                }
            }
        }
    }

    public Boolean checkIfCompleted(int teamNum, int fieldNum) {
        Location map = getCoordsForMap(teamNum, fieldNum);
        int i=0;
        for (int y = (int) map.getY(); y <= map.getY()+5; y++) {
            for (int x = (int) map.getX(); x <= map.getX()+6; x++) {
                for (int z = (int) map.getZ(); z <= map.getZ()+6; z++) {
                    if (world.getBlockAt(x,y,z).getType() != maps.get(teamFields[teamNum][fieldNum]).get(i)) {
//                        Bukkit.broadcastMessage("X: "+x+"Y: "+y+"Z: "+z+": Block Type: "+world.getBlockAt(x,y,z).getType()+"!= Block Type: "+maps.get(teamFields[teamNum][fieldNum]).get(i)+", Map: "+teamFields[teamNum][fieldNum]);
                        return false;
                    }
                    i++;
                }
            }
        }
        return true;
    }

    public void mapUpdate(Location location) {

        int startX = 95;
        int betweenTeams = 2;
        int teamNum =  ((int) (location.getX()-startX)/(7+betweenTeams));

        int startZ = 109;
        int betweenFields = 1;
        int fieldNum =  ((int) (location.getZ()-startZ)/(7+betweenFields));

        if (checkIfCompleted(teamNum, fieldNum)) {
            Bukkit.broadcastMessage("Done");
            teamFields[teamNum][fieldNum] = teamsProgress[teamNum];
            placeMap(teamNum, fieldNum);
        }
    }

    public void start() {
        for (int i = 0; i < 6; i++) {
            for (int x = 0; x < 4; x++) {
                placeMap(i, x);
            }
        }
    }
}
