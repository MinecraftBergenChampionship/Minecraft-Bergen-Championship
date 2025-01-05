package me.kotayka.mbc.gameMaps.spleefMap;

import me.kotayka.mbc.MBC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

public class Space extends SpleefMap {
    private List<Block> blocks = new ArrayList<>();
    private List<Block> moons = new ArrayList<>();
    private List<Block> rings = new ArrayList<>();
    private List<Block> planet = new ArrayList<>();
    private Set<Block> decaying = new HashSet<>();
    private Set<Block> toRemove = new HashSet<>();
    int erosion = -1;
    public Space() {
        super("Space", 80, "Gravity");
    }

    @Override
    public void resetMap() {
        if (erosion != -1) {
            MBC.getInstance().cancelEvent(erosion);
            erosion = -1;
        }
        // paste map
        int copy_from_x = 169;
        int copy_from_z = -227;
        for (int paste_to_x = -31; paste_to_x <= 24; paste_to_x++) {
            for (int paste_to_z = -27; paste_to_z <= 30; paste_to_z++) {
                for (int y = 90; y<= 120; y++) {
                    Block copy = getWorld().getBlockAt(copy_from_x, y, copy_from_z);
                    if (copy.getType().equals(Material.AIR) || copy.getType().equals(Material.GOLD_BLOCK)) continue;

                    Block paste_to = getWorld().getBlockAt(paste_to_x, y, paste_to_z);
                    Material type = copy.getType();
                    paste_to.setType(type);
                    if (type == Material.END_STONE) {
                        moons.add(paste_to);
                    } else if (type.toString().endsWith("TERRACOTTA")) {
                        rings.add(paste_to);
                    } else {
                        planet.add(paste_to);
                    }
                }
                copy_from_z++;
            }
            copy_from_z = -227;
            copy_from_x++;
        }
    }

    @Override
    public void deleteMap() {
        for (int paste_to_x = -31; paste_to_x <= 24; paste_to_x++) {
            for (int paste_to_z = -27; paste_to_z <= 30; paste_to_z++) {
                for (int y = 90; y<= 120; y++) {
                    getWorld().getBlockAt(paste_to_x, y, paste_to_z).setType(Material.AIR);
                }
            }
        }
    }


    @Override
    public void Border(int timeRemaining) {
        if (timeRemaining == 150) {
            Bukkit.broadcastMessage(ChatColor.RED+"The outside moons are decaying!");
        }
        if (timeRemaining < 150) {
            for (int i = 0; i < 10 && moons.size() > 1; i++) {
                int rand = (int)(Math.random()*moons.size());
                decaying.add(moons.get(rand));
                moons.remove(rand);
            }
            erodeMap();
        }

        if (timeRemaining == 105) {
            Bukkit.broadcastMessage(ChatColor.RED+"The outer rings are decaying!");
        }
        if (timeRemaining < 105) {
            for (int i = 0; i < 8 && rings.size() > 1; i++) {
                int rand = (int)(Math.random() * rings.size());
                decaying.add(rings.get(rand));
                rings.remove(rand);
            }
        }
        if (timeRemaining == 75) {
            Bukkit.broadcastMessage(ChatColor.RED+"The planet is decaying!");
        }
        if (timeRemaining < 75) {
            for (int i = 0; i < 10 && planet.size() > 1; i++) {
                int rand = (int) (Math.random() * planet.size());
                decaying.add(planet.get(rand));
                planet.remove(rand);
            }
        }
    }

    private void erodeMap() {
        if (erosion == -1) {
            erosion = Bukkit.getScheduler().scheduleSyncRepeatingTask(MBC.getInstance().getPlugin(), () -> {
                for (Block b : decaying) {
                    switch (b.getType()) {
                        case YELLOW_CONCRETE -> b.setType(Material.ORANGE_CONCRETE);
                        case ORANGE_CONCRETE -> b.setType(Material.RED_CONCRETE);
                        case RED_CONCRETE -> {
                            b.setType(Material.AIR);
                            toRemove.add(b);
                        }
                        case AIR -> toRemove.add(b);
                        default -> b.setType(Material.YELLOW_CONCRETE);
                    }
                }
                for (Block b : toRemove) {
                    decaying.remove(b);
                }
            }, 40, 40);
        }
        for (int i = 0; i < 5 && blocks.size() > 1; i++) {
            int rand = (int) (Math.random() * blocks.size());
            decaying.add(blocks.get(rand));
        }
    }
}
