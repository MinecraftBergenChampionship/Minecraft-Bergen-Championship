package me.kotayka.mbc.gameMaps.dragonsMap;

import me.kotayka.mbc.partygames.Dragons;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;

public class Arrgh extends DragonsMap {
    public Arrgh(Dragons dragons) {
        super(dragons);
        super.mapName = "Arrgh";
        super.creatorName = "iDrg";
        minX = -1038;
        maxX = -968;
        minY = -3;
        maxY = 60;
        minZ = -1020;
        maxZ = -980;
        CENTER_OF_BUILD = new Location(getWorld(), -1005, 16, -1000);
        SPAWN = new Location(getWorld(), 0, 16, 0);
        DRAGON_SPAWN = new Location(getWorld(), 0, 46, 40);
        DEATH_Y = -12;
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
