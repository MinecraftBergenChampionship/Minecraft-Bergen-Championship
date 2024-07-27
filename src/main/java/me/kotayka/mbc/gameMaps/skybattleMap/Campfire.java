package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.games.Skybattle;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Campfire extends SkybattleMap {
    private final Location CENTER = new Location(getWorld(), 0, 75, 0);
    private List<ItemStack> spawnItems = new ArrayList<>();
    private final Location[] SPAWNS = {
            new Location(getWorld(), 33, 71, 54),
            new Location(getWorld(), -33, 71, 54),
            new Location(getWorld(), -63, 71, 0),
            new Location(getWorld(), 63, 71, 0),
            new Location(getWorld(), -33, 71, -54),
            new Location(getWorld(), 33, 71, -54),
    };
    public Campfire(Skybattle skb) {
        super(skb);
        int topBorder = 120;

    }

    @Override
    public void resetMap() {

    }

    @Override
    public void Border() {

    }

    @Override
    public void spawnPlayers() {
        ArrayList<Location> tempSpawns = new ArrayList<>(SPAWNS.length);
        tempSpawns.addAll(Arrays.asList(SPAWNS));

        initSpawnItems();

        for (MBCTeam t : MBC.getInstance().getValidTeams()) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (Participant p : t.teamPlayers) {
                Location spawn = tempSpawns.get(randomNum);
                p.getPlayer().teleport(spawn);
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
                colorBalloon(spawn, t.getChatColor());

                // give spawn items
                for (ItemStack i : spawnItems) {
                    if (i.getType() == Material.WHITE_CONCRETE) {
                        ItemStack concrete = p.getTeam().getConcrete();
                        concrete.setAmount(64);
                        p.getPlayer().getInventory().addItem(concrete);
                    } else if (i.getType() == Material.IRON_CHESTPLATE) {
                        p.getPlayer().getInventory().setChestplate(i);
                    } else {
                        p.getPlayer().getInventory().addItem(i);
                    }
                }
            }
            tempSpawns.remove(randomNum);
        }

    }

    @Override
    public void Overtime() {

    }

    @Override
    public void removeBarriers() {

    }

    public void initSpawnItems() {
        ItemStack pick = new ItemStack(Material.IRON_PICKAXE);
        pick.addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemMeta meta = pick.getItemMeta();
        meta.setUnbreakable(true);
        pick.setItemMeta(meta);
        // not gonna bother making everything unbreakable
        // since crafted items won't be
        spawnItems = Arrays.asList(
                new ItemStack(Material.STONE_SWORD), pick,
                new ItemStack(Material.WHITE_CONCRETE, 64),
                new ItemStack(Material.COOKED_BEEF, 7),
                new ItemStack(Material.IRON_CHESTPLATE)
        );
    }

    private void colorBalloon(Location l, ChatColor color) {
        for (int y = 0; y <= 5; y++) {
            for (int x = 0; x <= 3; x++) {
                for (int z = 0; z <= 3; z++) {
                    Block b = getWorld().getBlockAt(l.getBlockX()+1-x, l.getBlockY()+25-y, l.getBlockZ()+1-z);
                    if (!b.getType().toString().endsWith("CONCRETE")) continue;
                    Material m = switch (color) {
                        case RED -> Material.RED_CONCRETE;
                        case YELLOW -> Material.YELLOW_CONCRETE;
                        case GREEN -> Material.GREEN_CONCRETE;
                        case BLUE -> Material.BLUE_CONCRETE;
                        case DARK_PURPLE -> Material.PURPLE_CONCRETE;
                        case LIGHT_PURPLE -> Material.PINK_CONCRETE;
                        default -> Material.WHITE_CONCRETE;
                    };
                    b.setType(m);
                }
            }
        }

    }
}
