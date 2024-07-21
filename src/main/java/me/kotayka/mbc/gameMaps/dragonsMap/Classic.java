package me.kotayka.mbc.gameMaps.dragonsMap;

import me.kotayka.mbc.partygames.Dragons;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;

public class Classic extends DragonsMap {
    public Classic(Dragons dragons) {
        super(dragons);
        minX = 972;
        maxX = 1013;
        minY = 50;
        maxY = 61;
        minZ = 990;
        maxZ = 1020;
        CENTER_OF_BUILD = new Location(getWorld(), 1000, 60, 1000);
        SPAWN = new Location(getWorld(), 0, 60, 0);
        DRAGON_SPAWN = new Location(getWorld(), 20, 90, 20);
        DEATH_Y = 40;
    }

    @Override
    public void resetMap() {
        World world = getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block originalBlock = world.getBlockAt(x, y, z);
                    Block possiblyChangedBlock = world.getBlockAt((int) (x-CENTER_OF_BUILD.getX()), y, (int) (z-CENTER_OF_BUILD.getZ()));
                    if (!(originalBlock.getType().name().equals(possiblyChangedBlock.getType().name()))) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                        possiblyChangedBlock.setBlockData(originalBlock.getBlockData());
                    }
                }
            }
        }

        for (Item item : getWorld().getEntitiesByClass(Item.class)) {
            item.remove();
        }
    }
}
