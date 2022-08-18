package com.kotayka.mcc.SG;

import com.kotayka.mcc.Scoreboards.ScoreboardManager;
import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.TGTTOS.managers.Firework;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import com.kotayka.mcc.mainGame.manager.Players;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.kotayka.mcc.mainGame.manager.teamManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SG {

    private final Players players;
    private final MCC mcc;
    private final Plugin plugin;
    public List<UUID> playerSpawnTeleported = new ArrayList<UUID>();
    public List<UUID> playersDeadList = new ArrayList<UUID>();
    public List<String> names=  new ArrayList<>();
    List<String> teams = new ArrayList<>();
    public List<Integer> spawns  = new ArrayList<Integer>();;
    public String stage = "None";
    public int timer = 10;
    public int eventTimer = 720;
    public int relaventEventTimer = 60;
    public String eventName = "Starting";
    public int playersDead = 0;
    int eventID = 0;
    public List<String> teamsAlive = new ArrayList<>();
    public int teamsDead = 0;

    World world;
    public int[][] spawnLocs = {{3,-10},{0,-10},{-3,-10},{-6,-8},{-9,-6},{-10,-3},{-10,0},{-10,3},{-8,6},{-6,8,},{-3,10},{1,10},{4,9},{7,7},{9,4},{10,1},{10,-2},{9,-5},{6,-8}};
    public Object[][] items = {{Material.STONE_SWORD,1,3},{Material.WOODEN_SWORD,1,3},{Material.STONE_AXE,1,1},{Material.GOLDEN_SWORD,1,2},{Material.MUSHROOM_STEW,1,1},{Material.LEATHER_HELMET,1,3},{Material.LEATHER_CHESTPLATE,1,2},{Material.LEATHER_LEGGINGS,1,2},{Material.LEATHER_BOOTS,1,2},{Material.GOLDEN_LEGGINGS,1,2},{Material.GOLDEN_BOOTS,1,2},{Material.BOW,1,2},{Material.CROSSBOW,1,2},{Material.ARROW,3,1},{Material.ARROW,2,2},{Material.GOLDEN_HELMET,1,3},{Material.GOLDEN_CHESTPLATE,1,2},{Material.LAPIS_LAZULI,1,3},{Material.GOLD_INGOT,1,3},{Material.IRON_INGOT,1,3},{Material.GOLDEN_LEGGINGS,1,2},{Material.GOLDEN_BOOTS,1,3},{Material.CHAINMAIL_CHESTPLATE,1,1},{Material.CHAINMAIL_LEGGINGS,1,1},{Material.CHAINMAIL_HELMET,1,2},{Material.CHAINMAIL_BOOTS,1,2},{Material.COOKIE,2,3},{Material.MELON_SLICE,4,3},{Material.STICK,1,2},{Material.STICK,2,2},{Material.STRING,1,2},{Material.STRING,2,2},{Material.BEEF,3,3},{Material.COOKED_BEEF,2,2},{Material.COOKED_PORKCHOP,1,2},{Material.PORKCHOP,3,3},{Material.APPLE,2,2},{Material.EXPERIENCE_BOTTLE,1,4}};
    public Material[] supplyDropItems = {Material.IRON_HELMET,Material.IRON_CHESTPLATE,Material.IRON_LEGGINGS,Material.IRON_BOOTS,Material.COBWEB,Material.FISHING_ROD,Material.DIAMOND,Material.GOLDEN_APPLE};
    public List<ShulkerBox> boxes = new ArrayList<>();
    public SG(Players players, MCC mcc, Plugin plugin) {
        this.players = players;
        this.mcc = mcc;
        this.plugin = plugin;
    }

    public void regenChest() {
        List<ItemStack> itemsForChest=  new ArrayList<>();
        for (Object[] it : items) {
            ItemStack x = new ItemStack((Material) it[0]);
            x.setAmount((int)it[1]);
            for (int i = 0; i < (int)it[2]; i++) {
                itemsForChest.add(x);
            }
        }

        Chunk[] c = world.getLoadedChunks();
        Random rand = new Random();
        for(int i=0;i<c.length;i++){//loop through loaded chunks
            for(int x=0;x<c[i].getTileEntities().length;x++){//loop through tile entities within loaded chunks
                if(c[i].getTileEntities()[x] instanceof Chest){
                    Chest c1 = (Chest) c[i].getTileEntities()[x];
                    c1.getInventory().clear();
                    int numofItems = rand.nextInt(2)+5;
                    for (int b=0; b<numofItems;b++) {
                        int lootNum = rand.nextInt(itemsForChest.size());
                        c1.getInventory().setItem(rand.nextInt(27), itemsForChest.get(lootNum));
                    }
                }
            }
        }
    }

    public void spawnPlayers() {
        Random rand = new Random();
        for (Participant p : players.participants) {
            int index = rand.nextInt(spawnLocs.length);
            while (spawns.contains(index)) {
                index = rand.nextInt(spawnLocs.length);
            }
            int[] coords = spawnLocs[index];
            p.player.teleport(new Location(world, coords[0], 3, coords[1]));
            playerSpawnTeleported.add(p.player.getUniqueId());
            spawns.add(index);
            teamsAlive.add(p.team);
            playersDead++;
        }
        String[] teamNames = {"RedRabbits", "YellowYaks", "GreenGuardians", "BlueBats", "PurplePandas", "PinkPiglets"};
        for (String team : teamNames) {
            if (teamsAlive.contains(team)) {
                teamsDead++;
            }
        }
    }

    public void loadWorld() {
        if (Bukkit.getWorld("Survival_Games") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("Survival_Games");
        }

        Chunk[] c = world.getLoadedChunks();
        for(int i=0;i<c.length;i++) {//loop through loaded chunks
            for (int x = 0; x < c[i].getTileEntities().length; x++) {//loop through tile entities within loaded chunks
                if (c[i].getTileEntities()[x] instanceof ShulkerBox) {
                    c[i].getTileEntities()[x].setType(Material.CHEST);
                }
            }
        }
    }

    public void events() {
        if (mcc.game.stage.equals("SG")) {
            String value = ChatColor.BOLD+""+ChatColor.GREEN + "Next Event: "+ChatColor.LIGHT_PURPLE;
            int eventTime = 0;
            switch (eventID) {
                case 0:
                    eventName="Starting";
                    eventTime=10;
                    break;
                case 1:
                    eventName="Grace Period Ends";
                    for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
                        p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60*20, 255, false, false));
                    }
                    eventTime=60;
                    break;
                case 2:
                    Bukkit.broadcastMessage(ChatColor.RED+"Grace Period is Over");
                    eventName="Supply Drop";
                    eventTime=240;
                    break;
                case 3:
                    setSupplyDropItems();
                    eventName="Chest Refill";
                    eventTime=240;
                    break;
                case 4:
                    regenChest();
                    Bukkit.broadcastMessage(ChatColor.RED+"Chest are refilled");
                    eventName="Supply Drop";
                    eventTime=240;
                    break;
                case 5:
                    setSupplyDropItems();
                    eventName="Deathmatch";
                    eventTime=240;
                    break;
                case 6:
                    endGame();
                    break;
            }
            eventID++;
            mcc.scoreboardManager.changeLine(21, value+eventName);
            mcc.scoreboardManager.startTimerForGame(eventTime, "SG");
        }
    }


    public void startWorldBorder() {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0.0, 0.0);
        border.setSize(400.0);
    }

    public void setSupplyDropItems() {
        List<Chest> chests = new ArrayList<>();
        Chunk[] c = world.getLoadedChunks();
        Random rand = new Random();
        for(int i=0;i<c.length;i++){//loop through loaded chunks
            for(int x=0;x<c[i].getTileEntities().length;x++){//loop through tile entities within loaded chunks
                if(c[i].getTileEntities()[x] instanceof Chest){
                    Chest c1 = (Chest) c[i].getTileEntities()[x];
                    if (c1.getX() > -200 && c1.getX() < 200 && c1.getZ() > -200 && c1.getZ() < 200)
                    chests.add(c1);
                }
            }
        }
        Chest chest = chests.get(rand.nextInt(chests.size()));
        Block supplyDrop = world.getBlockAt(chest.getLocation());
        supplyDrop.setType(Material.BLACK_SHULKER_BOX);
        ShulkerBox box = (ShulkerBox) supplyDrop.getState();
        for (int i = 0; i < 3; i++) {
            int lootNum = rand.nextInt(supplyDropItems.length);
            box.getInventory().setItem(rand.nextInt(27), new ItemStack(supplyDropItems[lootNum]));
        }
        boxes.add(box);
        Bukkit.broadcastMessage(ChatColor.RED+"Supply Drop Spawned at "+supplyDrop.getX()+", "+supplyDrop.getY()+", "+supplyDrop.getZ());
}

    public void start() {
        loadWorld();
        regenChest();
        stage="Starting";
        for (Entity e : world.getEntities()) {
            if (e.getType() == EntityType.DROPPED_ITEM) {
                e.remove();
            }
        }
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            for (PotionEffect effect : p.player.player.getActivePotionEffects()) {
                p.player.player.removePotionEffect(effect.getType());
            }
            names.add(p.player.ign);
            p.player.player.getInventory().clear();
            mcc.scoreboardManager.createSGBoard(p);
            teams.add(mcc.scoreboardManager.playerTeams.get(p).teamName);
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12, 255, false, false));
        }
        spawnPlayers();
        startWorldBorder();
        events();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                WorldBorder border = world.getWorldBorder();
                border.setSize(30, 120);
            }
        }, 12000);

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
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
                            for (Participant p : players.participants) {
                                p.player.sendTitle(ChatColor.WHITE+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 3:
                            for (Participant p : players.participants) {
                                p.player.sendTitle(ChatColor.RED+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 2:
                            for (Participant p : players.participants) {
                                p.player.sendTitle(ChatColor.GRAY+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 1:
                            for (Participant p : players.participants) {
                                p.player.sendTitle(ChatColor.GOLD+String.valueOf(timer), "", 1, 18, 1);
                            }
                            break;
                        case 0:
                            stage="Game";
                            break;
                    }
                    timer--;
                }
            }
        }, 20L, 20L);
    }

    public void outLivePlayer() {
        for (Participant p : Participant.participantsOnATeam) {
            if (!playersDeadList.contains(p.player.getUniqueId())) {
                mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.player.getUniqueId()), 3);
            }
        }
    }

    public void kill(Participant killer) {
        mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(killer.player.getUniqueId()), 45);
    }

    public Boolean checkIfGameEnds() {
        Bukkit.broadcastMessage(teams.toString());
        if (teams.size()==0) {
            Bukkit.broadcastMessage("Team Size is 0");
            return true;
        }
        String firstTeam = teams.get(0);
        Bukkit.broadcastMessage(ChatColor.GOLD+""+teams.size());
        for (String t : teams) {
            if (!firstTeam.equals(t)) {
                Bukkit.broadcastMessage("Teams Are Different");
                return false;
            }
            else {
                Bukkit.broadcastMessage(t+"="+firstTeam);
            }
        }
        return true;
    }

    public void PlayerDied(Player p) {
        teams.remove(mcc.scoreboardManager.playerTeams.get(mcc.scoreboardManager.players.get(p.getUniqueId())).teamName);
        if (checkIfGameEnds()) {
            for (String name : names) {
                mcc.scoreboardManager.addScore(mcc.scoreboardManager.playersWithNames.get(name), 45);
            }
            Bukkit.broadcastMessage("Game Ended");
            endGame();
        }
    }

    public void checkIfGameEnds(Participant p) {
        mcc.scoreboardManager.lastOneStanding(mcc.scoreboardManager.players.get(p.player.getUniqueId()), "SG");
    }

    public void completeEndGame() {
        stage="x";
        mcc.game.endGame();
    }

    public void endGame() {
        mcc.scoreboardManager.startTimerForGame(10, "SGEnd");
        mcc.scoreboardManager.changeLine(21, ChatColor.LIGHT_PURPLE+"NEXT GAME STARTING");
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.sendTitle(ChatColor.GOLD+"Round Over","",20,160,20);
        }
    }
}
