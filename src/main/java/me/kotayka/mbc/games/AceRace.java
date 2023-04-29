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

    public static AceRaceMap map = new Biomes();
    public static World world = Bukkit.getWorld("AceRace");;
    public static List<AceRacePlayer> aceRacePlayerList = new ArrayList<>();

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

    public void events() {

    }

    public void start() {
        setTimer(600);
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
            p.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100000, 10, false, false));
            p.getPlayer().teleport(new Location(world, 1, 26, 150, 90, 0));

            aceRacePlayerList.add(new AceRacePlayer(p));
        }
        createScoreboard();
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        if (!isGameActive()) return;

        map.checkDeath(e);
        map.checkFinished(e);

        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_WEATHERED_CUT_COPPER) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(4));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.65, e.getPlayer().getVelocity().getZ()));
            ((AceRacePlayer) GamePlayer.getGamePlayer(e.getPlayer())).nextCheckpoint();
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_EXPOSED_CUT_COPPER) {
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(2));
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.WAXED_WEATHERED_COPPER) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1.25, e.getPlayer().getVelocity().getZ()));
        }
        if (e.getTo().getBlock().getRelative(BlockFace.DOWN).getType() == Material.OBSERVER) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        }
    }


}
