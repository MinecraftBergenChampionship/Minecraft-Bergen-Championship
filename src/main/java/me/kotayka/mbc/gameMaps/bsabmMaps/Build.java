package me.kotayka.mbc.gameMaps.bsabmMaps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Chain;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;

import me.kotayka.mbc.gamePlayers.PowerTagPlayer;

public class Build {
    private Block[][][] blocks = new Block[6][7][7];
    private final String NAME;
    private final String AUTHOR;
    private final World WORLD;
    public Map<Material, Integer> blockList = new HashMap<>();

    public Build(Location diamondBlock) {
        WORLD = diamondBlock.getWorld();

        Block sign = WORLD.getBlockAt(diamondBlock.getBlockX(), diamondBlock.getBlockY()+2, diamondBlock.getBlockZ()-5);

        if (sign.getType().equals(Material.OAK_WALL_SIGN)) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                NAME = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length()-1, str.length(), "");
                NAME = str.toString().trim();
            }
        } else {
            Bukkit.broadcastMessage("[Debug] No build name! Sign (XYZ):" + ChatColor.LIGHT_PURPLE+"X: "+sign.getLocation().getX()+", Y: "+sign.getLocation().getY()+", Z: "+sign.getLocation().getZ());
            NAME ="Undefined";
        }

        Block authorSign = WORLD.getBlockAt(diamondBlock.getBlockX(), diamondBlock.getBlockY()+5, diamondBlock.getBlockZ()-5);
        if (authorSign.getType().equals(Material.OAK_WALL_SIGN)) {
            AUTHOR = ((Sign) authorSign.getState()).getLine(0);
        } else {
            Bukkit.broadcastMessage("[Debug] No author! Sign (XYZ):" + ChatColor.LIGHT_PURPLE+"X: "+sign.getLocation().getX()+", Y: "+sign.getLocation().getY()+", Z: "+sign.getLocation().getZ());
            AUTHOR = "Undefined";
        }

        Location startBlock = new Location(WORLD, (int) diamondBlock.getX()+3, (int) diamondBlock.getY()+1, (int) diamondBlock.getZ()+3);

        for (int y = startBlock.getBlockY(); y <= startBlock.getBlockY()+5; y++) {
            for (int x = startBlock.getBlockX(); x >= startBlock.getBlockX()-6; x--) {
                for (int z = startBlock.getBlockZ(); z >= startBlock.getBlockZ()-6; z--) {
                    blocks[y- startBlock.getBlockY()][startBlock.getBlockX()-x][startBlock.getBlockZ()-z] = WORLD.getBlockAt(x,y,z);

                    if (!blockList.containsKey(WORLD.getBlockAt(x,y,z).getType())) blockList.put(WORLD.getBlockAt(x,y,z).getType(), 1);
                    else blockList.replace(WORLD.getBlockAt(x,y,z).getType(), blockList.get(WORLD.getBlockAt(x,y,z).getType()));
                }
            }
        }

        //Location endBlock = new Location(world, startBlock.getBlockX()+6, startBlock.getBlockY()+5, startBlock.getBlockZ()+6);
    }

    public void placeFirstLayer(Location midBlock) {
        placeLayer(midBlock, 0);
    }

    public void placeLayer(Location midBlock, int y) {
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 7; z++) {
                Block copyFrom = blocks[y][x][z];
                Block pasteTo = midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z);
                pasteTo.setType(copyFrom.getType());
                pasteTo.setBlockData(copyFrom.getBlockData());
            }
        }
    }

    public void placeCompleteBuild(Location midBlock) {
        for (int y = 0; y < 6; y++) {
            placeLayer(midBlock, y);
        }
    }

    public void setAir(Location midBlock) {
        for (int y = 1; y < 6; y++) {
            setLayerAir(midBlock, y);
        }
    }

    private void setLayerAir(Location midBlock, int y) {
       for (int x = 0; x < 7; x++) {
           for (int z = 0; z < 7; z++) {
               midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z).setType(Material.AIR);
           }
       }
    }

    /**
     * Returns a number between 0 and 1 representing how much a build is completed.
     * @param midBlock Location of middle block.
     * @return Number between 0 and 1 representing amount of build finished.
     */

    public double getPercentCompletion(Location midBlock) {
        int count = 0;
        int total = 0;
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    if (b.getType().equals(Material.AIR)) continue;
                    total++;
                    if (midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z).getType().equals(b.getType())) {
                        count++;
                    }
                }
            }
        }
        return (((1.0 * count) / total));
    }

    private boolean matches(Block b, Block toMatch) {
        if (!b.getType().equals(toMatch.getType())) {
            return false;
        }

        /*
        // Check if stairs, slabs, and  are in the right orientation
        if (b.getType().toString().endsWith("STAIRS")) {
            if (((Stairs) b.getBlockData()).getFacing() != ((Stairs) toMatch.getBlockData()).getFacing()) {
                return false;
            }
        }

        if (b.getType() == Material.CHAIN) {
            if (((Chain) b.getBlockData()).getAxis() != ((Chain) toMatch.getBlockData()).getAxis()) {
                return false;
            }
        }*/

        return true;
    }


    public boolean checkBuild(Location midBlock) {
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    Block toMatch = midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z);
                    if (!matches(b, toMatch)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean checkBuildBreak(Location midBlock, Location brokeBlock) {
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    Block toMatch = midBlock.getWorld().getBlockAt(midBlock.getBlockX()+3-x, midBlock.getBlockY()+y, midBlock.getBlockZ()+3-z);
                    if (toMatch.getLocation().equals(brokeBlock)) {
                        // if air where broken block supposed to be, keep checking
                        if (b.getType() == Material.AIR) {
                            continue;
                        } else {
                            // otherwise, it cannot possibly be correct
                            return false;
                        }
                    }
                    if (!matches(b, toMatch)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Tells the sender the first incorrect block found.
     * @param sender Player that executed the command.
     * @param midBlock Location of the middle of the block.
     */
    public void checkBuildCommand(Player sender, Location midBlock) {
        //sender.sendMessage(ChatColor.GREEN+"[checkbuild] The first 5 mistakes will be outputted.");
        sender.sendMessage(ChatColor.GREEN+"[checkbuild] Searching plot...");
        int i = 0;
        for (int y = 1; y < 6; y++) {
            for (int x = 0; x < 7; x++) {
                for (int z = 0; z < 7; z++) {
                    Block b = blocks[y][x][z];
                    int checkX = midBlock.getBlockX()+3-x;
                    int checkY = midBlock.getBlockY()+y;
                    int checkZ = midBlock.getBlockZ()+3-z;
                    Block check = midBlock.getWorld().getBlockAt(checkX, checkY, checkZ);
                    if (!check.getType().equals(b.getType())) {
                        sender.sendMessage(
                                String.format("%s[checkbuild] %sFound block %s which should be %s at (%d, %d, %d)",
                                        ChatColor.GREEN, ChatColor.RESET, check.getType(), b.getType(), checkX, checkY, checkZ)
                        );
                        i++;
                    }
                    /*
                    else if (b.getType().toString().endsWith("STAIRS")) {
                        if (((Stairs) check.getBlockData()).getFacing() != ((Stairs) b.getBlockData()).getFacing()) {
                            sender.sendMessage(
                                    String.format("%s[checkbuild] %sFound stair block facing %s which should be %s at (%d, %d, %d)",
                                            ChatColor.GREEN, ChatColor.RESET, ((Stairs) b).getFacing(), ((Stairs) check).getFacing(), checkX, checkY, checkZ)
                            );
                            i++;
                        }
                    } else if (b.getType() == Material.CHAIN) {
                        if (((Chain) b.getBlockData()).getAxis() != ((Chain) check.getBlockData()).getAxis()) {
                            sender.sendMessage(
                                    String.format("%s[checkbuild] %sFound chain block with axis %s which should be %s at (%d, %d, %d)",
                                            ChatColor.GREEN, ChatColor.RESET, ((Chain) b).getAxis(), ((Chain) check).getAxis(), checkX, checkY, checkZ)
                            );
                            i++;
                        }
                    }

                     */
                    if (i >= 5) {
                        return;
                    }

                }
            }
        }
    }

    public String getName() {
        return NAME;
    }

    public String getAuthor() { return AUTHOR;}
}