package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.tgttosMap.TGTTOSMap;
import me.kotayka.mbc.gameMaps.tgttosMap.Test;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TGTTOS extends Game {

    private static int roundNum = 0;
    private TGTTOSMap map = null;

    private List<TGTTOSMap> maps = new ArrayList<>(Arrays.asList(new Test(), new Test()));

    private List<Participant> finishedParticipants;

    public TGTTOS() {
        super(2, "TGTTOS");
    }

    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+ MBC.gameNum+"/8:" + ChatColor.WHITE + " TGTTOS", p);

        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void events() {

    }

    public void start() {
        super.start();
        startRound();
    }

    /**
     * Moved from startRound()
     * repurpose loadPlayers() however is best needed for tgttos
     */
    public void loadPlayers() {
        for (Participant p : MBC.getIngamePlayer()) {
            p.getInventory().clear();
            p.getPlayer().setVelocity(new Vector(0,0,0));
            p.getPlayer().teleport(map.getSpawnLocation());
            for (ItemStack i : map.getItems()) {
                if (i.getType().equals(Material.WHITE_WOOL)) {
                    ItemStack wool = p.getTeam().getColoredWool();
                    wool.setAmount(64);
                    p.getInventory().addItem(wool);
                    p.getInventory().addItem(new ItemStack(Material.SHEARS));
                }
            }
            map.getWorld().spawnEntity(map.getEndLocation(), EntityType.CHICKEN);
        }
    }

    public void startRound() {
        finishedParticipants = new ArrayList<>();

        roundNum++;
        setTimer(240);
        TGTTOSMap newMap = maps.get((int) (Math.random()*maps.size()));
        maps.remove(newMap);

        map = newMap;
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Round: "+ roundNum+"/6:" + ChatColor.WHITE + map.getName());

        loadPlayers();
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;
        if (map == null) return;

        if (e.getPlayer().getLocation().getY() < map.getDeathY()) {
            e.getPlayer().setVelocity(new Vector(0,0,0));
            e.getPlayer().teleport(map.getSpawnLocation());
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        if (!isGameActive()) return;

        if (!(event.getBlock().getType().toString().endsWith("WOOL"))) {
            event.setCancelled(true);
        }

        event.setDropItems(false);
    }

    public void chickenClick(Participant p, Entity chicken) {
        p.addRoundScore(MBC.getIngamePlayer().size()-finishedParticipants.size());
        finishedParticipants.add(p);
        String place = getPlace(finishedParticipants.size());
        chicken.remove();
        Bukkit.broadcastMessage(p.getTeam().getChatColor()+p.getPlayerName()+ChatColor.WHITE+" finished in "+ChatColor.AQUA+place);
        p.getPlayer().sendMessage(ChatColor.GREEN+"You finished in "+ ChatColor.AQUA+place+ChatColor.GREEN+" place!");

        if (finishedParticipants.size() == MBC.getIngamePlayer().size()) {
            startRound();
        }
    }

    @EventHandler
    public void chickenLeftClick(EntityDamageByEntityEvent event) {
        if (!isGameActive()) return;

        if (event.getEntity() instanceof Chicken && event.getDamager() instanceof Player) {
            chickenClick(Participant.getParticipant((Player) event.getDamager()), event.getEntity());
        }
    }

    @EventHandler
    public void chickenRightClick(PlayerInteractEntityEvent event) {
        if (!isGameActive()) return;

        if (event.getRightClicked() instanceof Chicken) {
            chickenClick(Participant.getParticipant(event.getPlayer()), event.getRightClicked());
        }
    }

    @EventHandler
    public void boatExit(VehicleExitEvent event) {
        if (!isGameActive()) return;

        if (event.getVehicle() instanceof Boat && event.getExited() instanceof Player) {
            Boat boat = (Boat) event.getVehicle();
            boat.remove();

            ItemStack boatItem = new ItemStack(Material.OAK_BOAT);
            Player p = (Player) event.getExited();
            p.getInventory().addItem(boatItem);
        }
    }

    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent event) {
        if (!isGameActive()) return;

        if (event.getBlock().getType().toString().endsWith("WOOL")) {
            ItemStack i = new ItemStack(event.getItemInHand());
            i.setAmount(1);
            MBC.plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.plugin, () -> event.getPlayer().getInventory().addItem(i), 20);
        }
    }
}
