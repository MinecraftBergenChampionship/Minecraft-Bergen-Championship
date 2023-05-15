package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.Participant;
import me.kotayka.mbc.Team;
import me.kotayka.mbc.games.Skybattle;
import org.bukkit.*;
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
    private int voidHeight = 30;
    private int topBorder = 100;
    private int borderRadius = 80;
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
        loadWorld(center, voidHeight, topBorder, borderRadius);
    }

    /**
     * Resets map to default state using a copy of the map stored elsewhere in the world.
     * All chests/brewing stands are refilled and all mobs, items, tnt, etc are cleared.
     * O(562,394) ~ O(1) trust
     * there is also probably a better way to do this but nah
     */
    public void resetMap() {
        SKYBATTLE.resetKillMaps();

        // reset world
        int x = 225;
        int y = -16;
        int z = 322;
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
            y = -16;
            x++;
        }
        removeEntities();
        //backup at 500 0 500
    }

    public void borderParticles() {
        for (int y = 50; y < 120; y += 20) {
            for (double t = 0; t < 50; t+=0.5) {
                float x = borderRadius * (float) Math.sin(t);
                float z = borderRadius * (float) Math.cos(t);
                getWorld().spawnParticle(Particle.SMOKE_LARGE, x, y, z, 1);
            }
        }
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
        ArrayList<Location> tempSpawns = new ArrayList<>(spawns.length);
        tempSpawns.addAll(Arrays.asList(spawns));

        initSpawnItems();

        for (Team t : SKYBATTLE.getValidTeams()) {
            int randomNum = (int) (Math.random() * tempSpawns.size());
            for (Participant p : t.teamPlayers) {
                p.getPlayer().teleport(tempSpawns.get(randomNum));
                p.getPlayer().setGameMode(GameMode.ADVENTURE);
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
