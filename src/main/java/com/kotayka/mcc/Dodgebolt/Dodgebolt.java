package com.kotayka.mcc.Dodgebolt;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Dodgebolt {

    public World world;

    private final MCC mcc;

    public Arrow a1;
    public Arrow a2;

    int timer = 10;

    public Dodgebolt(MCC mcc) {
        this.mcc = mcc;
    }

    public String team11;
    public String team22;

    public List<ScoreboardPlayer> playerListForDD = new ArrayList<>();
    public List<ScoreboardPlayer> team1List = new ArrayList<>();
    public List<ScoreboardPlayer> team2List = new ArrayList<>();
    public List<String> team1Names = new ArrayList<>();
    public List<String> team2Names = new ArrayList<>();

    public List<String> playerNames = new ArrayList<>();

    public List<String> deadPlayersNames = new ArrayList<>();

    public List<Location> team1Locs = new ArrayList<>();
    public List<Location> team2Locs = new ArrayList<>();

    public int team1Size = 0;
    public int team2Size = 0;

    public int team1Wins = 0;
    public int team2Wins = 0;

    public int team1Remaining = 0;
    public int team2Remaining = 0;

    final int[] taskID = {-1};

    public String stage = "Starting";

    public void loadWorld() {
        if (Bukkit.getWorld("Dodgebolt") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("Dodgebolt");
        }
    }

    public void start(String team1, String team2) {
        team11=team1;
        team22=team2;

        for (Entity e : world.getEntities()) {
            if (e.getType().equals(EntityType.ARROW)) {
                e.remove();
            }
        }

        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            if (mcc.scoreboardManager.playerTeams.get(p).teamName.equals(team1)) {
                team1List.add(p);
                team1Names.add(p.player.player.getName());
                playerListForDD.add(p);
                playerNames.add(p.player.player.getName());
            }
            else if (mcc.scoreboardManager.playerTeams.get(p).teamName.equals(team2)) {
                team2List.add(p);
                team2Names.add(p.player.player.getName());
                playerListForDD.add(p);
                playerNames.add(p.player.player.getName());
            }
        }

        team1Size = team1List.size();
        team2Size = team2List.size();

        team1Locs.add(new Location(world, 12.5, 17, 3.5));
        team1Locs.add(new Location(world, 12.5, 17, -2.5));
        team1Locs.add(new Location(world, 9.5, 17, 9.5));
        team1Locs.add(new Location(world, 9.5, 17, -8.5));

        team2Locs.add(new Location(world, -12, 17, 3.5));
        team2Locs.add(new Location(world, -12, 17, -2.5));
        team2Locs.add(new Location(world, -9, 17, 9.5));
        team2Locs.add(new Location(world, -9, 17, -8.5));

        resetWorld();
    }

    public void changeScoreboard() {
        String t2 = "";
        String t1 = "";
        switch (team2Wins) {
            case 0:
                t2=mcc.scoreboardManager.teamColors.get(team22)+"     ① ① ③";
                break;
            case 1:
                t2=mcc.scoreboardManager.teamColors.get(team22)+"     ② ① ③";
                break;
            case 2:
                t2=mcc.scoreboardManager.teamColors.get(team22)+"     ② ② ③";
                break;
            case 3:
                t2=mcc.scoreboardManager.teamColors.get(team22)+"     ② ② ④";
                break;
        }
        switch (team1Wins) {
            case 0:
                t1=mcc.scoreboardManager.teamColors.get(team11)+"     ① ① ③";
                break;
            case 1:
                t1=mcc.scoreboardManager.teamColors.get(team11)+"     ② ① ③";
                break;
            case 2:
                t1=mcc.scoreboardManager.teamColors.get(team11)+"     ② ② ③";
                break;
            case 3:
                t1=mcc.scoreboardManager.teamColors.get(team11)+"     ② ② ④";
                break;
        }
        mcc.scoreboardManager.changeLine(2, t2);
        mcc.scoreboardManager.changeLine(1, t1);
    }

    public void roundOver(String team) {
        if (team1Wins==3 || team2Wins==3) {
            Bukkit.broadcastMessage("Game Over");
        }
        else {
            mcc.scoreboardManager.dodgeboltTimer();
            changeScoreboard();
        }

    }

    public void resetWorld() {
        deadPlayersNames = new ArrayList<>();
        timer = 10;
        stage="Starting";
        team1Remaining = team1Size;
        team2Remaining = team2Size;

        int team1SpawnLoc = 0;
        int team2SpawnLoc = 0;

        for (ScoreboardPlayer p : team1List) {
            p.player.player.getInventory().clear();
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10000000, 255, false, false));
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 10000000, 255, false, false));
            p.player.player.teleport(team1Locs.get(team1SpawnLoc));
            p.player.player.getInventory().addItem(new ItemStack(Material.BOW));
            team1SpawnLoc++;
        }

        for (ScoreboardPlayer p : team2List) {
            p.player.player.getInventory().clear();
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 10000000, 255, false, false));
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 10000000, 255, false, false));
            p.player.player.teleport(team2Locs.get(team2SpawnLoc));
            p.player.player.getInventory().addItem(new ItemStack(Material.BOW));
            team2SpawnLoc++;
        }

        for (Entity cur : world.getEntities()) {
            if (cur instanceof Item || cur instanceof Arrow){//make sure we aren't deleting mobs/players
                cur.remove();//remove it
            }
        }

        if (taskID[0] != -1) {
            Bukkit.getServer().getScheduler().cancelTask(taskID[0]);
        }

        taskID[0] = mcc.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(mcc.plugin, new Runnable() {
            public void run() {
                if (timer >= 0) {
                    switch (timer) {
                        case 10:
                        case 9:
                        case 8:
                        case 7:
                        case 6:
                        case 5:
                        case 4:
                            for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                                p.player.player.sendTitle(ChatColor.WHITE+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 3:
                            for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                                p.player.player.sendTitle(ChatColor.RED+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 2:
                            for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                                p.player.player.sendTitle(ChatColor.GRAY+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 1:
                            for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                                p.player.player.sendTitle(ChatColor.GOLD+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 0:
                            stage="Game";
                            matchStart();
                            break;
                    }
                    timer--;
                }
            }
        }, 20L, 20L);
    }

    public void matchStart() {
        stage="Playing";
        a1 = (Arrow) world.spawnEntity(new Location(world, -6.5, 20, -0.5), EntityType.ARROW);
        a1.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
        a2 = (Arrow) world.spawnEntity(new Location(world, 6.5, 20, 0.5), EntityType.ARROW);
        a2.setPickupStatus(AbstractArrow.PickupStatus.ALLOWED);
    }
}
