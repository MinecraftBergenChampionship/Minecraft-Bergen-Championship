package me.kotayka.mbc.gameMaps.skybattleMap;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.MBCTeam;
import me.kotayka.mbc.Participant;
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

public class Campfire extends SkybattleMap {
    private final Location CENTER = new Location(getWorld(), 0, 75, 0);
    private List<ItemStack> spawnItems = new ArrayList<>();
    public final double RADIUS_SHRINK_AMOUNT = 0.365;
    public final double HEIGHT_SHRINK_AMOUNT = 0.2;
    private float borderRadius = 90;
    private final Location[] SPAWNS = {
            new Location(getWorld(), 0, 78, 74),
            new Location(getWorld(), -60, 78, 37),
            new Location(getWorld(), -60, 78, -37),
            new Location(getWorld(), 0, 78, -74),
            new Location(getWorld(), 60, 78, -37),
            new Location(getWorld(), 60, 78, 37),
    };
    public Campfire(Skybattle skb) {
        super(skb);
        int topBorder = 120;
        int voidHeight = 25;
        loadWorld(CENTER, voidHeight, topBorder, borderRadius, RADIUS_SHRINK_AMOUNT, HEIGHT_SHRINK_AMOUNT);
    }

    @Override
    public void resetMap() {
        SKYBATTLE.resetMaps();
        setBorderHeight(120);
        setBorderRadius(borderRadius);

        // reset world (center @ 300 75 -300)
        int x = 933;
        int y = 65;
        int z = 922;
        World world = getWorld(); // convenience
        for (int mapX = -67; mapX <= 67; mapX++) {
            for (int mapY = 65; mapY <= 109; mapY++) {
                for (int mapZ = -78; mapZ <= 78; mapZ++) {
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
                z = 922;
                y++;
            }
            y = 65;
            x++;
        }
        removeEntities();
    }

    @Override
    public void Border() {
        for (int y = 50; y <= 100; y += 5) {
            for (double t = 0; t < 50; t+=0.5) {
                double x = (getBorderRadius() * (float) Math.cos(t)) + CENTER.getX();
                double z = (getBorderRadius() * (float) Math.sin(t)) + CENTER.getZ();
                getWorld().spawnParticle(Particle.DUST, x, y, z, 1, SKYBATTLE.BORDER_PARTICLE);
            }
        }

        for (int x = -28; x < 28; x+=2) {
            for (int z = -28; z < 28; z+=2) {
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
                    } else if (i.getType() == Material.LEATHER_CHESTPLATE) {
                        p.getInventory().setChestplate(p.getTeam().getColoredLeatherArmor(i));
                    } else if (i.getType() == Material.LEATHER_BOOTS) {
                        p.getInventory().setBoots(p.getTeam().getColoredLeatherArmor(i));
                    } else if (i.getType() == Material.LEATHER_HELMET) {
                        p.getInventory().setHelmet(p.getTeam().getColoredLeatherArmor(i));
                    } else if (i.getType() == Material.LEATHER_LEGGINGS) {
                        p.getInventory().setLeggings(p.getTeam().getColoredLeatherArmor(i));
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
        Border();
        reduceBorderHeight(HEIGHT_SHRINK_AMOUNT);
        reduceBorderRadius(RADIUS_SHRINK_AMOUNT);
    }

    @Override
    public void removeBarriers() {
        for (Location l : SPAWNS) {
            for (int x_offset = -2; x_offset <= 2; x_offset++) {
                for (int y_offset = 0; y_offset <= 3; y_offset++) {
                    for (int z_offset = -2; z_offset <= 2; z_offset++) {
                        if (getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).getType().equals(Material.BARRIER)) {
                            getWorld().getBlockAt(l.getBlockX() + x_offset, l.getBlockY() + y_offset, l.getBlockZ() + z_offset).setType(Material.AIR);
                        }
                    }
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
                new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_HELMET),
                new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_LEGGINGS)
        );
    }

    private void colorBalloon(Location l, ChatColor color) {
        for (int y = 0; y <= 5; y++) {
            for (int x = 0; x <= 3; x++) {
                for (int z = 0; z <= 3; z++) {
                    Block b = getWorld().getBlockAt(l.getBlockX()+1-x, l.getBlockY()+32-y, l.getBlockZ()+1-z);
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
