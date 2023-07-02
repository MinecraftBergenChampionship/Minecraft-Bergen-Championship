package me.kotayka.mbc.gameMaps.sgMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.gameMaps.Map;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class SurvivalGamesMap extends Map {
    public int[][] spawns;
    private Location center;
    public java.util.Map<Location, Block> blocks = new HashMap<Location, Block>();
    public Location[] middleChests;
    public java.util.Map<Location, Material> brokenBlocks = new HashMap<Location, Material>();

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
            tempSpawns.add(new Location(getWorld(), spawn[0], 2, spawn[1]));
        }

        for (MBCTeam t : MBC.getValidTeams()) {
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

        MBC.getInstance().sg.resetCrates();
    }

    /**
     * Check a given chest whether it is within the given range
     * of a map to be considered a super chest
     */
    public abstract boolean checkChest(Chest chest);

    public abstract void resetBorder();

    public abstract void startBorder();

    public abstract void Overtime();
}
