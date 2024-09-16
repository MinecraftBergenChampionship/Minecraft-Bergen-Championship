package me.kotayka.mbc.gameMaps.sgMaps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;

public class JesuscraftTwo extends SurvivalGamesMap {
    int[][] spawns = {{9996,10005},{9996,9998},{10003,9998},{10003,10005},{10001,10003},{9998,10003},{9998,10000},{10001,10000}};

    Location[] middleChests = {};

    public JesuscraftTwo() {
        super.spawns = this.spawns;
        super.middleChests = this.middleChests;
        super.spawnY = 151;
        super.mapName = "Jesuscraft II";
        super.type = "Elytra";
        super.CENTER = new Location(getWorld(), 10000, 63, 10001);
        //super.airdrops = false;

        resetBorder();
    }

    @Override
    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;

        for (int x = 9994; x <= 10005; x++) {
            for (int z = 9996; z <= 10007; z++) {
                // directly north / south
                getWorld().getBlockAt(x, 157, z).setType(block);
                if (x == 9994 || x == 10005 || z == 9996 || z == 10007) {
                    for (int y = 151; y <= 156; y++) {
                        getWorld().getBlockAt(x, y, z).setType(block);
                    }
                }
                getWorld().getBlockAt(x, 150, z).setType(block);
            }
        }
    }

    @Override
    public boolean checkChest(Chest chest) {
        Location l = chest.getLocation();

        return l.getX() > 9798 && l.getX() < 10202
            && l.getZ() > 9798 && l.getZ() < 10202;
    }

    @Override
    public void resetBorder() {
        border.setCenter(10000, 10001);
        border.setSize(400);
    }

    @Override
    public void startBorder() {
        border.setSize(30, 420);
    }

    @Override
    public void Overtime() {
        border.setSize(12, 15);
    }
}
