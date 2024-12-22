package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.games.Skybattle;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Classic extends SkybattleMap {
    private final Location CENTER = new Location(getWorld(), 0, 100, 0);
    public final double RADIUS_SHRINK_AMOUNT = 0.35; // previously 0.37
    public final double HEIGHT_SHRINK_AMOUNT = 0.2; // previously 0.22
    private float borderRadius = 80;
    private final Location[] SPAWNS = {
        new Location(getWorld(), 33, 71, 54),
        new Location(getWorld(), -33, 71, 54),
        new Location(getWorld(), -63, 71, 0),
        new Location(getWorld(), 63, 71, 0),
        new Location(getWorld(), -33, 71, -54),
        new Location(getWorld(), 33, 71, -54),
    };

    private List<ItemStack> spawnItems = new ArrayList<>(5);

    public Classic(Skybattle skybattle) {
        super(skybattle);
        int topBorder = 120;
        int voidHeight = 30;
        loadWorld(CENTER, voidHeight, topBorder, borderRadius, RADIUS_SHRINK_AMOUNT, HEIGHT_SHRINK_AMOUNT);
    }

    /**
     * Resets map to default state using a copy of the map stored elsewhere in the world.
     * All chests/brewing stands are refilled and all mobs, items, tnt, etc are cleared.
     * O(562,394) ~ O(1) trust
     * there is also probably a better way to do this but nah
     */
    public void resetMap() {
        SKYBATTLE.resetMaps();
        setBorderHeight(120);
        setBorderRadius(80);

        // reset world (center @ -300 75 300)
        int x = -368;
        int y = 63;
        int z = 241;
        World world = getWorld(); // convenience
        for (int mapX = -68; mapX <= 70; mapX++) {
            for (int mapY = 63; mapY <= 96; mapY++) {
                for (int mapZ = -59; mapZ <= 59; mapZ++) {
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
                z = 241;
                y++;
            }
            y = 63;
            x++;
        }
        removeEntities();
        //backup at 500 0 500
    }

    /**
     * Handles spawning particles for circular and top border.
     * Damages players that are not in the border scaling with their distance.
     * Does not decrease border size.
     */
    public void Border() {
        for (int y = 50; y <= 110; y += 10) {
            for (double t = 0; t < 50; t+=0.5) {
                double x = (getBorderRadius() * (float) Math.cos(t)) + CENTER.getX();
                double z = (getBorderRadius() * (float) Math.sin(t)) + CENTER.getZ();
                getWorld().spawnParticle(Particle.DUST, x, y, z, 1, SKYBATTLE.BORDER_PARTICLE);
            }
        }

        for (int x = -14; x < 14; x+=2) {
            for (int z = -13; z < 13; z+=2) {
                getWorld().spawnParticle(Particle.DUST, x, getBorderHeight(), z, 1, SKYBATTLE.TOP_BORDER_PARTICLE);
            }
        }

        for (Participant p : SKYBATTLE.playersAlive) {
            Player player = p.getPlayer();
            if (!(player.getGameMode().equals(GameMode.SURVIVAL))) { continue; }

            double distance = getBorderRadius()*getBorderRadius() - player.getLocation().distanceSquared(new Location(getWorld(), CENTER.getX(), player.getLocation().getY(), CENTER.getZ()));
            boolean aboveBorder = player.getLocation().getY() >= getBorderHeight();
            boolean outsideBorder = distance < 0;

            if (aboveBorder || outsideBorder) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + "You're in the border!"));
                if (aboveBorder && outsideBorder) {
                    player.damage(0.5*Math.abs(player.getLocation().getY()-getBorderHeight()+0.5 + 0.009*Math.abs(distance)+0.5));
                } else if (aboveBorder) {
                    player.damage(0.5*Math.abs(player.getLocation().getY()-getBorderHeight()+0.5));
                } else {
                    player.damage(0.009*Math.abs(distance)+0.5);
                }
            }
        }
    }

    public void initSpawnItems() {
        ItemStack pick = new ItemStack(Material.IRON_PICKAXE);
        pick.addEnchantment(Enchantment.EFFICIENCY, 3);
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

    public void Overtime() {
        Border();
        reduceBorderHeight(HEIGHT_SHRINK_AMOUNT);
        reduceBorderRadius(RADIUS_SHRINK_AMOUNT);
    }

    public void removeBarriers() {
        // TODO ?
        // since this goes through the whole map, there's a lot of redundant checking going on.
        // if anyone is stumbling across this in the future, feel free to write more code to reduce this.
        for (int x = -65; x <= 65; x++) {
            for (int y = 71; y <= 73; y++) {
                for (int z = -56; z <= 56; z++) {
                    if (getWorld().getBlockAt(x, y, z).getType().equals(Material.BARRIER)) {
                        getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }
}
