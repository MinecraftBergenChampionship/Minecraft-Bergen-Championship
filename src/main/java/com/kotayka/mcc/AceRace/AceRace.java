package com.kotayka.mcc.AceRace;

import com.kotayka.mcc.Scoreboards.ScoreboardPlayer;
import com.kotayka.mcc.mainGame.MCC;
import com.kotayka.mcc.mainGame.manager.Participant;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class AceRace {
    public List<String> mapOrder = new ArrayList<>(Arrays.asList("Forest", "Desert", "Snow", "Jungle", "Underwater", "Nether", "End", "Small"));
    public Map<String, Location> respawnPoints = new HashMap<>();
    Map<Integer, Integer> playerLapPlacement = new HashMap<>();
    public Map<String, Location> bouncerPoints = new HashMap<>();
    public Map<UUID, String> playerProgress = new HashMap<>();
    public Map<UUID, Integer> playerLaps= new HashMap<>();
    public Map<UUID, Boolean> playerFinish = new HashMap<>();

    public List<UUID> playsFin = new ArrayList<>();

    public World world;

    int playersFinished = 0;

    public final MCC mcc;

    public AceRace(MCC mcc) {
        this.mcc = mcc;
    }

    public void loadWorld() {
        if (Bukkit.getWorld("AceRace") == null) {
            world = Bukkit.getWorld("world");
        }
        else {
            world = Bukkit.getWorld("AceRace");
        }
    }

    public void loadCheckpoints() {
        respawnPoints.put("Forest", new Location(world, 95, 25, 119, 65, 0));
        respawnPoints.put("Desert", new Location(world, -59, 26, 140, 125, 0));
        respawnPoints.put("Snow", new Location(world, -119, 27, 77, 135, 0));
        respawnPoints.put("Jungle", new Location(world, -138, 26, -58, -160, 0));
        respawnPoints.put("Underwater", new Location(world, -49, 33, -144, -90, 0));
        respawnPoints.put("Nether", new Location(world, 40, 26, -142, -90, 0));
        respawnPoints.put("End", new Location(world, 138, 26, -62, -10, 0));
        respawnPoints.put("Small", new Location(world, 136, 26, 67, 35, 0));

        bouncerPoints.put("Forest", new Location(world, -22, 28, 150));
        bouncerPoints.put("Desert", new Location(world, -109, 32, 103));
        bouncerPoints.put("Snow", new Location(world, -150, 28, -25));
        bouncerPoints.put("Jungle", new Location(world, -77, 54, -129));
        bouncerPoints.put("Underwater", new Location(world, 7, 24, -144));
        bouncerPoints.put("Nether", new Location(world, 119, 34, -93));
        bouncerPoints.put("End", new Location(world, 150, 26, 25));
        bouncerPoints.put("Small", new Location(world, 125, 26, 84));
    }

    public Boolean checkCoords(Player p) {
        if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getX()-15 <= p.getLocation().getX() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getX()+15 >= p.getLocation().getX()) {
            if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getZ()-15 <= p.getLocation().getZ() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getZ()+15 >= p.getLocation().getZ()) {
                if (bouncerPoints.get(playerProgress.get(p.getUniqueId())).getY()-15 <= p.getLocation().getY() && bouncerPoints.get(playerProgress.get(p.getUniqueId())).getY()+15 >= p.getLocation().getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void nextCheckpoint(Player p) {
        if (checkCoords(p)) {
            if (mapOrder.indexOf(playerProgress.get(p.getUniqueId())) < 7) {
                playerProgress.put(p.getUniqueId(), mapOrder.get(mapOrder.indexOf(playerProgress.get(p.getUniqueId()))+1));
            }
            else if (!(playsFin.contains(p.getUniqueId()))) {

                playerFinish.put(p.getUniqueId(), true);
                playerProgress.put(p.getUniqueId(), "Forest");
            }
        }
    }

    public void playerFinish(Player p) {
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
    }

    public void playerFinishLap(Player p) {
        if (playerFinish.get(p.getUniqueId())) {
            Participant lapper= Participant.findParticipantFromPlayer(p);
            Bukkit.broadcastMessage(lapper.teamPrefix + lapper.chatColor + p.getDisplayName()+ChatColor.WHITE+ " finished Lap #"+(playerLaps.get(p.getUniqueId())+1)+".");
            mcc.scoreboardManager.placementPoints(mcc.scoreboardManager.players.get(p.getUniqueId()), 2, playerLapPlacement.get(playerLaps.get(p.getUniqueId())));
            playerLapPlacement.put(playerLaps.get(p.getUniqueId()), playerLapPlacement.get(playerLaps.get(p.getUniqueId()))+1);
            playerLaps.put(p.getUniqueId(), playerLaps.get(p.getUniqueId())+1);
            if (playerLaps.get(p.getUniqueId()) < 3) {
                playerFinish.put(p.getUniqueId(), false);
                mcc.scoreboardManager.changePlayerLine(3, ChatColor.LIGHT_PURPLE+"Completed Laps: "+ChatColor.WHITE+playerLaps.get(p.getUniqueId())+"/3", mcc.scoreboardManager.players.get(p.getUniqueId()));
            }
            else if (playerLaps.get(p.getUniqueId()) == 3 && !(playsFin.contains(p.getUniqueId()))){
                playersFinished++;
                Participant finisher= Participant.findParticipantFromPlayer(p);
                Bukkit.broadcastMessage(finisher.teamPrefix + finisher.chatColor + "" + ChatColor.BOLD +p.getDisplayName()+ChatColor.WHITE+ "" + ChatColor.BOLD+" finished the course in place #"+playersFinished+".");
                playerFinish.put(p.getUniqueId(), false);
                if (playerLapPlacement.get(playerLaps.get(p.getUniqueId())) == 1) {
                    mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.getUniqueId()), 25);
                }
                else if (playerLapPlacement.get(playerLaps.get(p.getUniqueId())) == 2) {
                    mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.getUniqueId()), 15);
                }
                else if (playerLapPlacement.get(playerLaps.get(p.getUniqueId())) == 3) {
                    mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.getUniqueId()), 10);
                }
                mcc.scoreboardManager.addScore(mcc.scoreboardManager.players.get(p.getUniqueId()), 10);
                mcc.scoreboardManager.changePlayerLine(3, ChatColor.LIGHT_PURPLE+"Finished Course", mcc.scoreboardManager.players.get(p.getUniqueId()));
                playerFinish(p);
                playsFin.add(p.getUniqueId());
                mcc.scoreboardManager.teamFinish(mcc.scoreboardManager.players.get(p.getUniqueId()), 20);
                Bukkit.broadcastMessage(""+playerLapPlacement.get(2)+" "+mcc.scoreboardManager.playerList.size());
                if (playerLapPlacement.get(2) >= mcc.scoreboardManager.playerList.size()) {
                    mcc.game.endGame();
                }
            }
        }
    }

    public void endGame() {
        Bukkit.broadcastMessage(ChatColor.RED+"Game has ended");
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            p.player.player.setGameMode(GameMode.SPECTATOR);
        }
    }

    public void start() {
        loadCheckpoints();
        ItemStack trident = new ItemStack(Material.TRIDENT);
        trident.addEnchantment(Enchantment.RIPTIDE, 1);
        trident.addEnchantment(Enchantment.DURABILITY, 3);
        mcc.scoreboardManager.startTimerForGame(600, "AceRace");
        for (int i = 0; i < 5; i++) {
            playerLapPlacement.put(i, 0);
        }
        for (ScoreboardPlayer p : mcc.scoreboardManager.playerList) {
            playerProgress.put(p.player.player.getUniqueId(), "Forest");
            playerLaps.put(p.player.player.getUniqueId(), 0);
            playerFinish.put(p.player.player.getUniqueId(), false);
            p.player.player.getInventory().clear();
            p.player.player.getInventory().addItem(trident);
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 10, false, false));
            p.player.player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.player.player.teleport(new Location(world, 1, 26, 150, 90, 0));
             mcc.scoreboardManager.createAceRaceBoard(p);
        }
    }
}
