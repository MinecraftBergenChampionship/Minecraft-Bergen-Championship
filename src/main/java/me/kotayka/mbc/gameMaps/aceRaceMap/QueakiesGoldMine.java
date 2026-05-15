package me.kotayka.mbc.gameMaps.aceRaceMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class QueakiesGoldMine extends AceRaceMap {
    boolean powerups = true;
    Location[] respawns = {
            new Location(getWorld(), -1005, 101, 880, -90, 0),  // default
            new Location(getWorld(), -975, 102, 882, -60, -5),
            new Location(getWorld(), -929, 103, 902, -45, 0),
            new Location(getWorld(), -890, 101, 1053, 30, -5),
            new Location(getWorld(), -926, 105, 1095, 60, 0),
            new Location(getWorld(), -1010, 101, 1119, 90, 0),
            new Location(getWorld(), -1094, 101, 1074, 150, -5),
            new Location(getWorld(), -1116, 101, 961, -150, 0),
            new Location(getWorld(), -1083, 101, 914, -135, 0),
    };

    Location[] checkpoints = {
            new Location(getWorld(), -1000, 101, 880),  // default
            new Location(getWorld(), -971, 102, 883),
            new Location(getWorld(), -925, 103, 905),
            new Location(getWorld(), -893, 101, 1056),
            new Location(getWorld(), -929, 105, 1097),
            new Location(getWorld(), -1015, 101, 1120),
            new Location(getWorld(), -1098, 101, 1070),
            new Location(getWorld(), -1114, 101, 958),
            new Location(getWorld(), -1081, 101, 910),
    };

    public QueakiesGoldMine() {
        super(97, new Location(Bukkit.getWorld("AceRace"), -1005, 101, 880, -90, 0), "Lava");
        mapName = "Queakie's Gold Mine";
        creatorName = "bigkirbypuff_";
        loadCheckpoints(respawns, checkpoints);
    }

    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;
        for (int y = 101; y <= 105; y++) {
            for (int z = 869; z <= 890; z++) {
                getWorld().getBlockAt(-997, y, z).setType(block); //left
            }
        }
    }
}