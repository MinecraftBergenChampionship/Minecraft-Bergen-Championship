package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.gamePlayers.SkybattlePlayer;
import me.kotayka.mbc.games.Skybattle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Classic extends SkybattleMap {
    private Location center = new Location(getWorld(), -157, 0, -266);
    private short voidHeight = 0;
    private short topBorder = 100;
    private Location[] spawns = {
        new Location(getWorld(), -220, 71, -266),
        new Location(getWorld(), -190, 71, -212),
        new Location(getWorld(), -124, 71, -212),
        new Location(getWorld(), -94, 71, -266),
        new Location(getWorld(), -124, 71, -320),
        new Location(getWorld(), -190, 71, -320),
    };

    private List<ItemStack> spawnItems = new ArrayList<>(5);

    public Classic(Skybattle skybattle) {
        super(skybattle);
        loadWorld(center, voidHeight, topBorder);
    }

    /**
     * Resets map to default state using a copy of the map stored elsewhere in the world.
     * All chests/brewing stands are refilled and all mobs, items, tnt, etc are cleared.
     * O(562,394) ~ O(1) trust
     * there is also probably a better way to do this but nah
     */
    public void resetMap() {
        short x = 225;
        short y = -16;
        short z = 322;
        World world = getWorld(); // convenience
        for (int mapX = -225; mapX <= -87; mapX++) {
            for (int mapY = 63; mapY <= 96; mapY++) {
                for (int mapZ = -325; mapZ <= -207; mapZ++) {
                    assert world != null;
                    Block originalBlock = world.getBlockAt(x, y, z);
                    Block possiblyChangedBlock = world.getBlockAt(mapX, mapY, mapZ);
                    if (!(originalBlock.getType().name().equals(possiblyChangedBlock.getType().name()))) {
                        possiblyChangedBlock.setType(originalBlock.getType());
                        possiblyChangedBlock.setBlockData(originalBlock.getBlockData());
                    }
                    if (possiblyChangedBlock.getState() instanceof Chest && originalBlock.getState() instanceof Chest) {
                        Container container = (Chest) originalBlock.getState();
                        ItemStack[] itemsForChest = container.getInventory().getContents();
                        ((Chest) possiblyChangedBlock.getState()).getInventory().setContents(itemsForChest);
                    }
                    if (possiblyChangedBlock.getState() instanceof BrewingStand && originalBlock.getState() instanceof BrewingStand) {
                        Container container = (BrewingStand) originalBlock.getState();
                        ItemStack[] potions = container.getInventory().getContents();
                        ((BrewingStand) possiblyChangedBlock.getState()).getInventory().setContents(potions);
                    }
                    z++;
                }
                z = 322;
                y++;
            }
            z = 322;
            y = 62;
            x++;
        }
        removeEntities();
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

    public void spawnPlayers() {
        List<Location> tempSpawns = Arrays.asList(spawns);
        initSpawnItems();
        for (SkybattlePlayer p : SKYBATTLE.skybattlePlayerList) {
            // give spawn items
            for (ItemStack i : spawnItems) {
                if (i.getType() == Material.WHITE_CONCRETE) {
                    ItemStack concrete = new ItemStack(Material.WHITE_CONCRETE);
                    concrete.setAmount(64);
                    switch (p.getParticipant().getTeam().getTeamName()) {
                        case "RedRabbits" -> concrete.setType(Material.RED_CONCRETE);
                        case "YellowYaks" -> concrete.setType(Material.YELLOW_CONCRETE);
                        case "GreenGuardians" -> concrete.setType(Material.GREEN_CONCRETE);
                        case "BlueBats" -> concrete.setType(Material.BLUE_CONCRETE);
                        case "PurplePandas" -> concrete.setType(Material.PURPLE_CONCRETE);
                        case "PinkPiglets" -> concrete.setType(Material.PINK_CONCRETE);
                    }
                    p.getPlayer().getInventory().addItem(concrete);
                } else if (i.getType() == Material.IRON_CHESTPLATE) {
                    p.getPlayer().getInventory().setChestplate(i);
                } else {
                    p.getPlayer().getInventory().addItem(i);
                }
            }
        }

    }

    public void removeBarriers() {
        // TODO ?
        // since this goes through the whole map, there's a lot of redundant checking going on.
        // if anyone is stumbling across this in the future, feel free to write more code to reduce this.
        for (int x = -222; x <= -92; x++) {
            for (int y = 71; y <= 73; y++) {
                for (int z = -322; z <= -210; z++) {
                    if (getWorld().getBlockAt(x, y, z).getType().equals(Material.BARRIER)) {
                        getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }
}
