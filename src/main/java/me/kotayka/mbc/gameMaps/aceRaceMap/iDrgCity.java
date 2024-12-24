package me.kotayka.mbc.gameMaps.aceRaceMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class iDrgCity extends AceRaceMap {
    Location[] respawns = {
            new Location(getWorld(), -926, 42, -1080, -45, 0),  // default
            new Location(getWorld(), -910, 37, -993, 0, 0),
            new Location(getWorld(), -915, 37, -927, 45, 0),
            new Location(getWorld(), -966, 45, -897, 90, 0),
            new Location(getWorld(), -983, 65, -887, 0, 0),
            new Location(getWorld(), -1021, 35, -892, 90, 0),
            new Location(getWorld(), -1107, 31, -966, -135, 0),
            new Location(getWorld(), -1094, 35, -1019, 180, 0),
            new Location(getWorld(), -1053, 35, -1085, -135, 0),
            new Location(getWorld(), -947, 42, -1101, -45, 0)
    };

    Location[] checkpoints = {
            new Location(getWorld(), -926, 42, -1080),    // default
            new Location(getWorld(), -910, 37, -980),
            new Location(getWorld(), -915, 37, -927),
            new Location(getWorld(), -936, 46, -906),
            new Location(getWorld(), -984, 65, -885),
            new Location(getWorld(), -1031, 35, -897),
            new Location(getWorld(), -1107, 31, -966),
            new Location(getWorld(), -1097, 35, -1027),
            new Location(getWorld(), -1085, 36, -1053),
            new Location(getWorld(), -975, 37, -1093)
    };

    public iDrgCity() {
        super(5, new Location(Bukkit.getWorld("AceRace"), -926, 42, -1080, -45, 0), "Lava");
        mapName = "iDrg City";
        loadCheckpoints(respawns, checkpoints);
    }

    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 42; y <= 45; y++) {
            for (int z = -1087; z <= -1083; z++) {
                getWorld().getBlockAt(z+166, y, z).setType(block); //left
            }
            for (int z = -1075; z <= -1071; z++) {
                getWorld().getBlockAt(z+142, y, z).setType(block); //right
            }
            for (int z = -1083; z <= -1071; z++) {
                int x = -2000 - z;
                getWorld().getBlockAt(x, y, z).setType(block); //front
            }
            for (int z = -1088; z <= -1076; z++) {
                int x = -2009 - z;
                getWorld().getBlockAt(x, y, z).setType(block); //back
            }
        }
    }
}