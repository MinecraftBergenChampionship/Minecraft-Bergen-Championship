package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.GameState;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.skybattleMap.Classic;
import me.kotayka.mbc.gameMaps.skybattleMap.SkybattleMap;
import me.kotayka.mbc.gamePlayers.SkybattlePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Skybattle extends Game {
    public SkybattleMap map = new Classic(this);
    public List<SkybattlePlayer> skybattlePlayerList = new ArrayList<>();
    // Primed TNT Entity, Player (that placed that block); used for determining kills since primed tnt is spawned by world
    public Map<Entity, Player> TNTPlacers = new HashMap<Entity, Player>(5);
    // Creeper Entity, Player (that spawned them); used for determining kills by creeper explosion
    public Map<Entity, Player> creeperSpawners = new HashMap<Entity, Player>(5);

    public Skybattle() {
        super(3, "Skybattle");
    }

    @Override
    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+ MBC.gameNum+"/8:" + ChatColor.WHITE + " Sky Battle", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    public void loadPlayers() {
        for (Participant p : MBC.getIngamePlayer()) {
            p.getInventory().clear();

            p.getPlayer().setFlying(false);
            p.getPlayer().setAllowFlight(false);
            p.getPlayer().setInvulnerable(false);
            p.getPlayer().setHealth(20);

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 30, 10, false, false));
            skybattlePlayerList.add(new SkybattlePlayer(p));
        }
        map.spawnPlayers();
    }

    /**
     * Reset maps used for determining kills
     */
    public void resetKillMaps() {
        creeperSpawners.clear();
        TNTPlacers.clear();

        for (SkybattlePlayer p : skybattlePlayerList) {
            p.lastDamager = null;
        }
    }

    @Override
    public void events() {
        setTimer(340);

    }

    @Override
    public void start() {

    }

    /**
     * Handles auto-priming of TNT and giving players infinite concrete
     * @param e BlockPlaceEvent e
     */
    @EventHandler
    public void blockPlaceEvent(BlockPlaceEvent e) {
        if (!this.getState().equals(GameState.ACTIVE)) { e.setCancelled(true); }

        Block b = e.getBlock();
        Player p = e.getPlayer();

        // auto-ignite TNT
        if (e.getBlock().getType().equals(Material.TNT)) {
            b.setType(Material.AIR);


        } else if (e.getBlock().getType().toString().matches(".*CONCRETE$")) {
            // if block was concrete, give appropriate amount back
            String concrete = e.getBlock().getType().toString();
            // check item slot
            assert concrete != null;
            int index = p.getInventory().getHeldItemSlot();
            if (Objects.requireNonNull(p.getInventory().getItem(index)).getType().toString().equals(concrete)) {
                int amt = Objects.requireNonNull(p.getInventory().getItem(index)).getAmount();
                p.getInventory().setItem(index, new ItemStack(Objects.requireNonNull(Material.getMaterial(concrete)), amt));
                return;
            }
            if (p.getInventory().getItem(40) != null) {
                if (Objects.requireNonNull(p.getInventory().getItem(40)).getType().toString().equals(concrete)) {
                    int amt;
                    // some wacky bullshit prevention
                    if (Objects.requireNonNull(p.getInventory().getItem(40)).getAmount() + 63 > 100) {
                        amt = 64;
                    } else {
                        amt = Objects.requireNonNull(p.getInventory().getItem(40)).getAmount() + 63;
                    }
                    p.getInventory().setItem(40, new ItemStack(Objects.requireNonNull(Material.getMaterial(concrete)), amt));
                }
            }
        }
    }


    /**
     * All concrete broken during game shouldn't drop itself
     * @param e BlockBreakEvent e
     */
    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        if (!(this.getState().equals(GameState.ACTIVE))) return;

        if (e.getBlock().getType().toString().endsWith("CONCRETE")) {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

    }
}
