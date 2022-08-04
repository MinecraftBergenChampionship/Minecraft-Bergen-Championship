package com.kotayka.mcc.BSABM;

import com.kotayka.mcc.TGTTOS.managers.Firework;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.*;

public class BSABM {

    public World world;
    public List<List<Material>> maps = new ArrayList<>();
    public List<String> names = new ArrayList<>();
    public List<Integer> mapFinishes = new ArrayList<>();
    public int[] teamsProgress = {0,0,0,0,0,0};
    public int[][] teamFields = {{0,1,2},{0,1,2},{0,1,2},{0,1,2},{0,1,2},{0,1,2}};
    public int[] teamsCoords = {-91,-52,-13,26,65,104};
    public int[] teamsCoordsBuilding = {-102,-63,-24,15,54,93};



    public void loadWorld() {
        if (Bukkit.getWorld("BSABM") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("BSABM");
        }
    }
    public void loadMaps() {
        World mapWorld;
        if (Bukkit.getWorld("bsabmMaps") == null) {
            mapWorld = Bukkit.getWorld("world");
        }
        else {
            mapWorld = Bukkit.getWorld("bsabmMaps");
        }
        List<List<Material>> tempMaps = new ArrayList<>();
        List<String> tempNames = new ArrayList<>();
        Location map = new Location(mapWorld, -3, 186, 5);
        int numOfMaps = 0;
        Bukkit.broadcastMessage(ChatColor.RED+"Loading Maps");
        while (mapWorld.getBlockAt((int) (map.getX()-3), (int) (map.getY()-1), (int) (map.getZ()-3)).getType() == Material.DIAMOND_BLOCK) {
            Bukkit.broadcastMessage(ChatColor.GREEN+"Map#" +numOfMaps+" Loaded");
            Location genMap = map.clone();
            List<Material> blocks = new ArrayList<>();
            for (int y = (int) genMap.getY(); y <= genMap.getY()+5; y++) {
                for (int x = (int) genMap.getX(); x >= genMap.getX()-6; x--) {
                    for (int z = (int) genMap.getZ(); z >= genMap.getZ()-6; z--) {
                        blocks.add(mapWorld.getBlockAt(x,y,z).getType());
                    }
                }
            }
            numOfMaps++;
            Block b = mapWorld.getBlockAt((int) (map.getX()-3), (int) (map.getY()+1), (int) (map.getZ()-10));
            Sign sign = (Sign) b.getState();
            tempNames.add(sign.getLine(0));
            map.setX(map.getX()-9);
            tempMaps.add(blocks);
        }
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < tempMaps.size(); i++) {
            nums.add(i);
        }
        mapFinishes = new ArrayList<>(nums);
        Random rand = new Random();
        for (int i = 0; i < tempMaps.size(); i++) {
            int num = rand.nextInt(nums.size());
            maps.add(tempMaps.get(num));
            names.add(tempNames.get(num));
            nums.remove(num);
        }
    }

    public Location getCoords(int teamNum, int fieldNum) {
        int spaceBetweenBoards = 4;
        int yCoord = 1;
        int xCoord = teamsCoords[teamNum];
        int z = 136;
        int zCoord = (z+7*fieldNum)+(spaceBetweenBoards*fieldNum);
        return new Location(world, xCoord, yCoord, zCoord);
    }

    public Location getCoordsForMap(int teamNum, int fieldNum) {
        int spaceBetweenBoards = 4;
        int yCoord = 1;
        int xCoord = teamsCoordsBuilding[teamNum];
        int z = 136;
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
        int startX = -102;
        int betweenTeams = 31;
        int teamNum =  ((int) (location.getX()-startX)/(7+betweenTeams));

        int startZ = 136;
        int betweenFields = 4;
        int fieldNum =  ((int) (location.getZ()-startZ)/(7+betweenFields));

        if (checkIfCompleted(teamNum, fieldNum)) {
            completeBuild(teamNum,fieldNum);
        }
    }

    public void completeBuild(int teamNum, int fieldNum) {
        Location fireworkLoc = getCoordsForMap(teamNum,fieldNum);
        fireworkLoc.setX(fireworkLoc.getX()+3);
        fireworkLoc.setY(fireworkLoc.getY()+3);
        fireworkLoc.setZ(fireworkLoc.getZ()+3);
        Firework firework = new Firework();
        firework.spawnFirework(fireworkLoc);
        mapFinishes.set(teamFields[teamNum][fieldNum], mapFinishes.get(teamFields[teamNum][fieldNum])+1);
        switch (teamNum) {
            case 0:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.RED+"Red Rabbits"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+" in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
            case 1:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.YELLOW+"Yellow Yaks"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+" in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
            case 2:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.GREEN+"Green Guardians"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+"in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
            case 3:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.BLUE+"Blue Bats"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+" in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
            case 4:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.DARK_PURPLE+"Purple Pandas"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+" in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
            case 5:
                Bukkit.broadcastMessage("["+ChatColor.GOLD+"BuildMart"+ChatColor.WHITE+"] "+ChatColor.LIGHT_PURPLE+"Pink Piglets"+ChatColor.WHITE+" Finished "+ChatColor.GOLD+names.get(teamFields[teamNum][fieldNum])+ChatColor.WHITE+" in place#"+ChatColor.GOLD+(6-mapFinishes.get(teamFields[teamNum][fieldNum])));
                break;
        }
        teamFields[teamNum][fieldNum] = teamsProgress[teamNum];
        placeMap(teamNum, fieldNum);
    }

    public void start() {
        loadMaps();
        for (int i = 0; i < 6; i++) {
            for (int x = 0; x < 3; x++) {
                placeMap(i, x);
            }
        }
    }
}
