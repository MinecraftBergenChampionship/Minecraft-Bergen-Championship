package me.kotayka.mbc.games;

import me.kotayka.mbc.Game;
import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameMaps.aceRaceMap.AceRaceMap;
import me.kotayka.mbc.gameMaps.aceRaceMap.Biomes;
import me.kotayka.mbc.gamePlayers.AceRacePlayer;
import me.kotayka.mbc.gamePlayers.GamePlayer;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class AceRace extends Game {
    // Change this to determine played map
    public AceRaceMap map = new Biomes();
    public static World world = Bukkit.getWorld("AceRace");;
    public List<AceRacePlayer> aceRacePlayerList = new ArrayList<>();
    public short[] finishedPlayersByLap = {0, 0, 0};
    public long startingTime;

    // SCORING VARIABLES
    public static final short FINISH_RACE_POINTS = 8;           // points for finishing the race
    public static final short PLACEMENT_LAP_POINTS = 1;         // points for placement for first laps
    public static final short PLACEMENT_FINAL_LAP_POINTS = 4;   // points for placement for last lap
    public static final short[] PLACEMENT_BONUSES = {25, 15, 15, 10, 10, 5, 5, 5}; // points for Top 8 finishers

    public AceRace() {
        super(1, "Ace Race");
    }

    public void createScoreboard(Participant p) {
        createLine(23, ChatColor.BOLD + "" + ChatColor.AQUA + "Game: "+MBC.gameNum+"/8:" + ChatColor.WHITE + " Ace Race", p);
        createLine(19, ChatColor.RESET.toString(), p);
        createLine(15, ChatColor.AQUA + "Game Coins:", p);
        createLine(3, ChatColor.RESET.toString() + ChatColor.RESET.toString(), p);

        teamRounds();
        updateTeamRoundScore(p.getTeam());
        updatePlayerRoundScore(p);
    }

    // TODO: implement 3-5 min practice lap
    // this may be excessive, have to see if players are willing to try out lap before event
    public void events() {

    }

    /**
     * load players into world
     * TODO: implement practice lap ?
     */
    public void loadPlayers() {
        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta itemMeta = trident.getItemMeta();
        itemMeta.setUnbreakable(true);
        trident.setItemMeta(itemMeta);
        trident.addEnchantment(Enchantment.RIPTIDE, 1);
        ItemStack leatherBoots = new ItemStack(Material.LEATHER_BOOTS);

        for (Participant p : MBC.getIngamePlayer()) {
            p.getInventory().clear();
            p.getInventory().addItem(trident);
            p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(leatherBoots));

            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000, 10, false, false));
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100000, 10, false, false)); // can probably use scoreboard teams but until then
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().teleport(new Location(map.getWorld(), 1, 26, 150, 90, 0));

            aceRacePlayerList.add(new AceRacePlayer(p, this));
        }
    }

    public void start() {
        setTimer(600);
        loadPlayers();
        createScoreboard();
        startingTime = System.currentTimeMillis();
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;

        if (map.checkDeath(e.getPlayer().getLocation())) {
            e.getPlayer().sendMessage("TEST");
            AceRacePlayer player = ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer()));
            int checkpoint = player.checkpoint;
            e.getPlayer().teleport(map.getRespawns().get((checkpoint == 0) ? map.mapLength-1 : checkpoint-1));
            e.getPlayer().setFireTicks(0);
        }
        // now auto checks for finished lap in setCheckpoint()
        //map.checkFinished(e);

        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.MEGA_BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).setCheckpoint();
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.BOOST_PAD) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.JUMP_PAD) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
            return;
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == MBC.SPEED_PAD) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
            return;
        }
        if (e.getTo().getBlock().getType().toString().toLowerCase().contains("carpet")) {
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).setCheckpoint();
        }
    }


}
