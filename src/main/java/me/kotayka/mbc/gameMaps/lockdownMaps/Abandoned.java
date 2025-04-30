package me.kotayka.mbc.gameMaps.lockdownMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.games.Lockdown;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Abandoned extends LockdownMap {
    private final Location CENTER = new Location(getWorld(), 1064, 100, 1064);
    private final Location[] SPAWNS = {
        new Location(getWorld(), 44, 3, -34),
        new Location(getWorld(), 161, 3, -34),
        new Location(getWorld(), 161, 3, 83),
        new Location(getWorld(), 83, 3, 161),
        new Location(getWorld(), -34, 3, 161),
        new Location(getWorld(), -34, 3, 44),
    };

    public Abandoned(Lockdown lockdown) {
        super(lockdown);
        super.mapName = "Abandoned";
        super.creatorName = "bigkirbypuff_";
        Location[][] w = new Location[6][6];

        int x = -34;
        int z = -34;
        for (int i = 0; i <6; i++) {
            for (int j = 0; j < 6; j++) {
                if ((i == 5 && j == 2) || (i == 3 && j == 0) || (i == 0 && j == 3) || (i == 2 && j == 5)) w[i][j] = new Location(Bukkit.getWorld("Lockdown"), x, 6, z);
                else if ((i == 4 && j == 1) || (i == 1 && j == 4)) w[i][j] = new Location(Bukkit.getWorld("Lockdown"), x, 5, z);
                else w[i][j] = new Location(Bukkit.getWorld("Lockdown"), x, 2, z);
                x+=39;
            }
            z+=39;
            x= -34;
        }
        

        loadWorld(CENTER, w) ;
    }

    /**
     * Resets map to default state using a copy of the map stored elsewhere in the world.
     * O(3million) ~ O(1) trust (not really actually this takes like a second)
     * there is also probably a better way to do this but nah
     */
    public void resetMap() {
        LOCKDOWN.resetMaps();

        // reset world (center @ 1064, 100, 1064)
        int x = 947;
        int y = 97;
        int z = 947;
        World world = getWorld(); // convenience
        for (int mapX = -53; mapX <= 189; mapX++) {
            for (int mapY = 0; mapY <= 45; mapY++) {
                for (int mapZ = -53; mapZ <= 189; mapZ++) {
                    Block originalBlock = world.getBlockAt(x, y, z);
                    Block possiblyChangedBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (!(originalBlock.getType().name().equals(possiblyChangedBlock.getType().name()))) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                        possiblyChangedBlock.setBlockData(originalBlock.getBlockData());
                    }
                    
                    z++;
                }
                z = 947;
                y++;
            }
            y = 97;
            x++;
        }
        removeEntities();
        //backup at 500 0 500
    }

    public void spawnPlayers() {
        ArrayList<Location> tempSpawns = new ArrayList<>(SPAWNS.length);
        tempSpawns.addAll(Arrays.asList(SPAWNS));

        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (Participant p : t.teamPlayers) {
                Location spawn = tempSpawns.get(randomNum);
                p.getPlayer().teleport(spawn);
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
            }
            tempSpawns.remove(randomNum);
        }
    }

    @Override
    public void removeBarriers() {
        for (Location l : SPAWNS) {
            for (int x_offset = -2; x_offset <= 2; x_offset++) {
                for (int y_offset = 0; y_offset <= 3; y_offset++) {
                    for (int z_offset = -2; z_offset <= 2; z_offset++) {
                        if (getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).getType().equals(Material.BARRIER)) {
                            getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addBarriers() {
        for (Location l : SPAWNS) {
            for (int x_offset = -2; x_offset <= 2; x_offset++) {
                for (int y_offset = 0; y_offset <= 3; y_offset++) {
                    for (int z_offset = -2; z_offset <= 2; z_offset++) {
                        if (x_offset == -2 || x_offset== 2 || z_offset==-2|| z_offset == 2) {
                            if (getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).getType().equals(Material.AIR)) {
                                getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).setType(Material.BARRIER);
                            }
                        }
                        
                    }
                }
            }
        }
    }

}
