package me.kotayka.mbc.partygames;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.List;

public final class BeepTestLevelLoader {
    private static final int X_INITIAL = -1;
    private static final int OFFSET_AMT = 28;
    private static final int MAX_Y = -50;
    private static final int MIN_Y = -60;
    private static final World world = Bukkit.getWorld("Party");
    private BeepTestLevelLoader() {}

    /**
     * Loads all easy levels for BeepTest.
     * Returns a copy of a list of all BeepTestLevels.
     *
     * @return copy of ArrayList of all easy BeepTestLevels.
     */
    public static List<BeepTestLevel> loadEasyLevels() {
        List<BeepTestLevel> easyLevels = new ArrayList<>();
        Block sign = new Location(world, X_INITIAL, -56, 25).getBlock();
        int offset = 0;

        // get name
        String name = "";
        Location levelInitial = null;
        while (sign.getType() == Material.OAK_SIGN) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                name = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length() - 1, str.length(), "");
                name = str.toString();
            }
            // load level
            levelInitial = new Location(world, offset, MIN_Y, 24);
            CuboidRegion level = new CuboidRegion(
                BukkitAdapter.asBlockVector(levelInitial),
                BukkitAdapter.asBlockVector(new Location(world, 24+offset, MAX_Y, 0)
            ));
            offset+=OFFSET_AMT;
            easyLevels.add(new BeepTestLevel(level, name.trim(), levelInitial));
            sign = new Location(world, X_INITIAL + offset, -56, 25).getBlock();
        }
        //return List.copyOf(easyLevels);
        return easyLevels;
    }


    /**
     * Loads all regular levels for BeepTest.
     * Returns a list of all BeepTestLevels.
     *
     * @return ArrayList of all regular BeepTestLevels.
     */
    public static List<BeepTestLevel> loadRegularLevels() {
        List<BeepTestLevel> regularLevels = new ArrayList<>();
        Block sign = new Location(world, X_INITIAL, -56, 53).getBlock();
        int offset = 0;

        // get name
        String name = "";
        Location levelInitial = null;
        while (sign.getType() == Material.OAK_SIGN) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                name = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length() - 1, str.length(), "");
                name = str.toString();
            }
            // load level
            levelInitial = new Location(world, offset, MIN_Y, 52);
            CuboidRegion level = new CuboidRegion(
                BukkitAdapter.asBlockVector(levelInitial),
                BukkitAdapter.asBlockVector(new Location(world, 24 + offset, MAX_Y,28)
            ));
            offset+=OFFSET_AMT;
            regularLevels.add(new BeepTestLevel(level, name, levelInitial));
            sign = new Location(world, X_INITIAL + offset, -56, 53).getBlock();
        }
        //return List.copyOf(regularLevels);
        return regularLevels;
    }

    /**
     * Loads all medium levels for BeepTest.
     * Returns a copy of a list of all medium BeepTestLevels.
     *
     * @return copy of a list of all medium BeepTestLevels.
     */
    public static List<BeepTestLevel> loadMediumLevels() {
        ArrayList<BeepTestLevel> mediumLevels = new ArrayList<>();
        Block sign = new Location(world, X_INITIAL, -56, 81).getBlock();
        int offset = 0;

        // get name
        String name = "";
        Location levelInitial = null;
        while (sign.getType() == Material.OAK_SIGN) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                name = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length() - 1, str.length(), "");
                name = str.toString();
            }
            // load level
            levelInitial = new Location(world, offset, MIN_Y, 80);
            CuboidRegion level = new CuboidRegion(
                BukkitAdapter.asBlockVector(levelInitial),
                BukkitAdapter.asBlockVector(new Location(world, 24 + offset, MAX_Y, 56)
            ));
            offset+=OFFSET_AMT;
            mediumLevels.add(new BeepTestLevel(level, name, levelInitial));
            sign = new Location(world, X_INITIAL + offset, -56,81).getBlock();
        }
        //return List.copyOf(mediumLevels);
        return mediumLevels;
    }

    /**
     * Loads all hard levels for BeepTest.
     * Returns a copy of a list of all hard BeepTestLevels.
     *
     * @return copy of a list of all hard BeepTestLevels.
     */
    public static List<BeepTestLevel> loadHardLevels() {
        List<BeepTestLevel> hardLevels = new ArrayList<>();
        Block sign = new Location(world, X_INITIAL, -56, 109).getBlock();
        int offset = 0;

        // get name
        String name = "";
        Location levelInitial = null;
        while (sign.getType() == Material.OAK_SIGN) {
            String[] lines = ((Sign) sign.getState()).getLines();
            if (lines[1].isBlank()) {
                String s = lines[0];
                name = s.trim();
            } else {
                StringBuilder str = new StringBuilder();
                for (String s : lines) {
                    str.append(s.trim()).append(" ");
                }
                str.replace(str.length() - 1, str.length(), "");
                name = str.toString();
            }
            // load level
            levelInitial = new Location(world, offset, MIN_Y, 108);
            CuboidRegion level = new CuboidRegion(
                BukkitAdapter.asBlockVector(levelInitial),
                BukkitAdapter.asBlockVector(new Location(world, 24 + offset, MAX_Y, 84)
            ));
            offset+=OFFSET_AMT;
            hardLevels.add(new BeepTestLevel(level, name, levelInitial));
            sign = new Location(world, X_INITIAL + offset, -56,109).getBlock();
        }
        //return List.copyOf(hardLevels);
        return hardLevels;
    }
}
