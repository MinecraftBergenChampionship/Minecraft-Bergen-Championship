package me.kotayka.mbc.gameMaps.sgMaps;

import com.ibm.icu.impl.Pair;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.gameMaps.MBCMap;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class SurvivalGamesMap extends MBCMap {
    public int[][] spawns;
    public Location CENTER;
    public String type;
    public java.util.Map<Location, Block> blocks = new HashMap<Location, Block>();
    public Location[] middleChests;
    public int spawnY;
    public java.util.Map<Location, Material> brokenBlocks = new HashMap<Location, Material>();
    public boolean hasElevationBorder;
    public int borderHeight = -128; // if used (indicated by hasElevationBorder), should be reset in map.resetBorder()

    protected WorldBorder border = getWorld().getWorldBorder();
    //public boolean airdrops; // incase future maps may require airdrops: not implemented yet

    public SurvivalGamesMap() {
        super(Bukkit.getWorld("Survival_Games"));
    }

    public abstract void setBarriers(boolean barriers);

    /**
     * Spawn players randomly with teammates
     */
    public void spawnPlayers() {
        if (MBC.MAX_TEAMS > spawns.length) {
            Bukkit.broadcastMessage(ChatColor.RED+"ERROR: Not enough spawns! Please use a different map!");
            Bukkit.broadcastMessage("[Debug] spawns.length == " + spawns.length);
            return;
        }

        ArrayList<Location> tempSpawns = new ArrayList<>(spawns.length);
        for (int[] spawn : spawns) {
            tempSpawns.add(new Location(getWorld(), spawn[0], spawnY, spawn[1]));
        }

        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (Participant p : t.teamPlayers) {
                p.getPlayer().teleport(tempSpawns.get(randomNum));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
            }
            tempSpawns.remove(randomNum);
        }
    }

    public void resetMap() {
        resetBorder();

        for (java.util.Map.Entry<Location,Material> entry : brokenBlocks.entrySet()) {
            getWorld().getBlockAt(entry.getKey()).setType(entry.getValue());
        }

        for (Entity e : getWorld().getEntitiesByClass(Item.class)) {
            e.remove();
        }

        //MBC.getInstance().sg.resetCrates();
    }

    public Location Center() { return CENTER; }

    /**
     * Check a given chest whether it is within the given range
     * of a map to be considered a super chest
     */
    public abstract boolean checkChest(Chest chest);

    public abstract void resetBorder();

    public abstract void startBorder();

    public abstract void Overtime();

    public abstract void Border();
}
