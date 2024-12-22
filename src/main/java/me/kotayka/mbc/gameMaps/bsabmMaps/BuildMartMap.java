package me.kotayka.mbc.gameMaps.bsabmMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import me.kotayka.mbc.gameTeams.BuildMartTeam;
import me.kotayka.mbc.games.BuildMart;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

// The MCC map is just called *Build Mart* but I doubt we're gonna have more maps;
// either way, keeping this in a separate file as it was before
public class BuildMartMap extends AbstractBuildMartMap {
    public final Location EXIT_PORTAL_LOC = new Location(Bukkit.getWorld("BSABM"), 10, 1, 0, -90, 0);
    private final BuildMart BUILD_MART;

    public BuildMartMap(BuildMart buildMart) {
        super(-20, 1, new Location(Bukkit.getWorld("BSABM"), 0, 161, 0));
        World w = Bukkit.getWorld("BSABM");
        super.replicationLocations.put(BreakAreaType.JUNGLE_LOGS, new Location(w, -13, -16, 159));
        super.replicationLocations.put(BreakAreaType.ACACIA_LOGS, new Location(w, -22, -16, 159));
        super.replicationLocations.put(BreakAreaType.SPRUCE_LOGS, new Location(w, -31, -16, 159));
        super.replicationLocations.put(BreakAreaType.BIRCH_LOGS, new Location(w, -40, -16, 159));
        super.replicationLocations.put(BreakAreaType.OAK_LOGS, new Location(w, -49, -16, 159));
        super.replicationLocations.put(BreakAreaType.DARK_OAK_LOGS, new Location(w, -4, -16, 159));
        super.replicationLocations.put(BreakAreaType.CHERRY_LOGS, new Location(w, 5, -16, 159));
        super.replicationLocations.put(BreakAreaType.SAND, new Location(w, -60, -16, 159));
        super.replicationLocations.put(BreakAreaType.ORES, new Location(w, -78, -16, 159));
        this.BUILD_MART = buildMart;
    }

    @Override
    public void loadBreakAreas() {
        // yes this looks bad
        World w = Bukkit.getWorld("BSABM");

        // Concrete & Glass (thank you iDrg!)
        addBreakArea(new BreakArea(Material.WHITE_CONCRETE, new Location(w, 14, 13, 77), new Location(w, 22, 18, 85), new Location(w, 11, 13, 76, -70, 0)));
        addBreakArea(new BreakArea(Material.WHITE_CONCRETE, new Location(w, 14, 22, 77), new Location(w, 22, 27, 85), new Location(w, 11, 13, 76, -70, 0)));
        addBreakArea(new BreakArea(Material.PINK_CONCRETE, new Location(w, 40, 13, 72), new Location(w, 48, 18, 80), new Location(w, 51, 13, 69, 45, 0)));
        addBreakArea(new BreakArea(Material.PINK_CONCRETE, new Location(w, 40, 22, 72), new Location(w, 48, 27, 80), new Location(w, 51, 13, 69, 45, 0)));
        addBreakArea(new BreakArea(Material.PURPLE_CONCRETE, new Location(w, 77, 13, 10), new Location(w, 85, 18, 18), new Location(w, 74, 13, 21, -135, 0)));
        addBreakArea(new BreakArea(Material.PURPLE_CONCRETE, new Location(w, 77, 22, 10), new Location(w, 85, 27, 18), new Location(w, 74, 13, 21, -135, 0)));
        addBreakArea(new BreakArea(Material.BLUE_CONCRETE, new Location(w, 77, 13, -18), new Location(w, 85, 18, -10), new Location(w, 74, 13, -21, -45, 0)));
        addBreakArea(new BreakArea(Material.BLUE_CONCRETE, new Location(w, 77, 22, -18), new Location(w, 85, 27, -10), new Location(w, 74, 13, -21, -45, 0)));
        addBreakArea(new BreakArea(Material.LIGHT_BLUE_CONCRETE, new Location(w, 40, 13, -80), new Location(w, 48, 18, -72), new Location(w, 51, 13, -69, 135, 0)));
        addBreakArea(new BreakArea(Material.LIGHT_BLUE_CONCRETE, new Location(w, 40, 22, -80), new Location(w, 48, 27, -72), new Location(w, 51, 13, -69, 135, 0)));
        addBreakArea(new BreakArea(Material.BLACK_CONCRETE, new Location(w, -22, 13, -85), new Location(w, -14, 18, -77), new Location(w, -11, 13, -76, 110, 0)));
        addBreakArea(new BreakArea(Material.BLACK_CONCRETE, new Location(w, -22, 22, -85), new Location(w, -14, 27, -77), new Location(w, -11, 13, -76, 110, 0)));
        addBreakArea(new BreakArea(Material.LIME_CONCRETE, new Location(w, -48, 13, -80), new Location(w, -40, 18, -72), new Location(w, -51, 13, -69, -135, 0)));
        addBreakArea(new BreakArea(Material.LIME_CONCRETE, new Location(w, -48, 22, -80), new Location(w, -40, 27, -72), new Location(w, -51, 13, -69, -135, 0)));
        addBreakArea(new BreakArea(Material.YELLOW_CONCRETE, new Location(w, -85, 13, -18), new Location(w, -77, 18, -10), new Location(w, -74, 13, -21, 45, 0)));
        addBreakArea(new BreakArea(Material.YELLOW_CONCRETE, new Location(w, -85, 22, -18), new Location(w, -77, 27, -10), new Location(w, -74, 13, -21, 45, 0)));
        addBreakArea(new BreakArea(Material.ORANGE_CONCRETE, new Location(w, -85, 13, 10), new Location(w, -77, 18, 18), new Location(w, -74, 13, 21, 135, 0)));
        addBreakArea(new BreakArea(Material.ORANGE_CONCRETE, new Location(w, -85, 22, 10), new Location(w, -77, 27, 18), new Location(w, -74, 13, 21, 135, 0)));
        addBreakArea(new BreakArea(Material.RED_CONCRETE, new Location(w, -48, 13, 72), new Location(w, -40, 18, 80), new Location(w, -51, 13, 69, -45, 0)));
        addBreakArea(new BreakArea(Material.RED_CONCRETE, new Location(w, -48, 22, 72), new Location(w, -40, 27, 80), new Location(w, -51, 13, 69, -45, 0)));

        addBreakArea(new BreakArea(Material.WHITE_STAINED_GLASS, new Location(w, -22, 13, 77), new Location(w, -14, 18, 85), new Location(w, -11, 13, 76, 70, 0)));
        addBreakArea(new BreakArea(Material.WHITE_STAINED_GLASS, new Location(w, -22, 22, 77), new Location(w, -14, 27, 85), new Location(w, -11, 13, 76, 70, 0)));
        addBreakArea(new BreakArea(Material.PINK_STAINED_GLASS, new Location(w, 58, 13, 54), new Location(w, 66, 18, 62), new Location(w, 55, 13, 65, -135, 0)));
        addBreakArea(new BreakArea(Material.PINK_STAINED_GLASS, new Location(w, 58, 22, 54), new Location(w, 66, 27, 62), new Location(w, 55, 13, 65, -135, 0)));
        addBreakArea(new BreakArea(Material.PURPLE_STAINED_GLASS, new Location(w, 59, 13, 28), new Location(w, 67, 18, 36), new Location(w, 70, 13, 25, 45, 0)));
        addBreakArea(new BreakArea(Material.PURPLE_STAINED_GLASS, new Location(w, 59, 22, 28), new Location(w, 67, 27, 36), new Location(w, 70, 13, 25, 45, 0)));
        addBreakArea(new BreakArea(Material.BLUE_STAINED_GLASS, new Location(w, 59, 13, -36), new Location(w, 67, 18, -28), new Location(w, 70, 13, -25, 135, 0)));
        addBreakArea(new BreakArea(Material.BLUE_STAINED_GLASS, new Location(w, 59, 22, -36), new Location(w, 67, 27, -28), new Location(w, 70, 13, -25, 135, 0)));
        addBreakArea(new BreakArea(Material.LIGHT_BLUE_STAINED_GLASS, new Location(w, 58, 13, -62), new Location(w, 66, 18, -54), new Location(w, 55, 13, -65, -45, 0)));
        addBreakArea(new BreakArea(Material.LIGHT_BLUE_STAINED_GLASS, new Location(w, 58, 22, -62), new Location(w, 66, 27, -54), new Location(w, 55, 13, -65, -45, 0)));
        addBreakArea(new BreakArea(Material.BLACK_STAINED_GLASS, new Location(w, 14, 13, -85), new Location(w, 22, 18, -77), new Location(w, 11, 13, -76, -110, 0)));
        addBreakArea(new BreakArea(Material.BLACK_STAINED_GLASS, new Location(w, 14, 22, -85), new Location(w, 22, 27, -77), new Location(w, 11, 13, -76, -110, 0)));
        addBreakArea(new BreakArea(Material.LIME_STAINED_GLASS, new Location(w, -66, 13, -62), new Location(w, -58, 18, -54), new Location(w, -55, 13, -65, 45, 0)));
        addBreakArea(new BreakArea(Material.LIME_STAINED_GLASS, new Location(w, -66, 22, -62), new Location(w, -58, 27, -54), new Location(w, -55, 13, -65, 45, 0)));
        addBreakArea(new BreakArea(Material.YELLOW_STAINED_GLASS, new Location(w, -67, 13, -36), new Location(w, -59, 18, -28), new Location(w, -70, 13, -25, -135, 0)));
        addBreakArea(new BreakArea(Material.YELLOW_STAINED_GLASS, new Location(w, -67, 22, -36), new Location(w, -59, 27, -28), new Location(w, -70, 13, -25, -135, 0)));
        addBreakArea(new BreakArea(Material.ORANGE_STAINED_GLASS, new Location(w, -67, 13, 28), new Location(w, -59, 18, 36), new Location(w, -70, 13, 25, -45, 0)));
        addBreakArea(new BreakArea(Material.ORANGE_STAINED_GLASS, new Location(w, -67, 22, 28), new Location(w, -59, 27, 36), new Location(w, -70, 13, 25, -45, 0)));
        addBreakArea(new BreakArea(Material.RED_STAINED_GLASS, new Location(w, -66, 13, 54), new Location(w, -58, 18, 62), new Location(w, -55, 13, 65, 135, 0)));
        addBreakArea(new BreakArea(Material.RED_STAINED_GLASS, new Location(w, -66, 22, 54), new Location(w, -58, 27, 62), new Location(w, -55, 13, 65, 135, 0)));

        // Ores
        addBreakArea(new BreakArea(Material.QUARTZ_BLOCK, new Location(w, 66, 74, 47), new Location(w, 72, 80, 53), BreakAreaType.ORES, new Location(w, 64, 74, 45, -45, 0)));
        addBreakArea(new BreakArea(Material.EMERALD_BLOCK, new Location(w, 78, 74, 32), new Location(w, 84, 80, 38), BreakAreaType.ORES, new Location(w, 75, 74, 35, -90, 0)));
        addBreakArea(new BreakArea(Material.GOLD_BLOCK, new Location(w, 78, 74, 40), new Location(w, 84, 80, 46), BreakAreaType.ORES, new Location(w, 75, 74, 43, -90, 0)));
        addBreakArea(new BreakArea(Material.DIAMOND_BLOCK, new Location(w, 78, 74, 48), new Location(w, 84, 80, 54), BreakAreaType.ORES, new Location(w, 75, 74, 51, -90, 0)));
        addBreakArea(new BreakArea(Material.IRON_BLOCK, new Location(w,51,74,59), new Location(w,57,80,65), BreakAreaType.ORES, new Location(w, 54, 74, 56, 0, 0)));
        addBreakArea(new BreakArea(Material.REDSTONE_BLOCK, new Location(w, 59, 74, 59), new Location(w, 65, 80, 65), BreakAreaType.ORES, new Location(w, 62, 74, 56, 0, 0)));
        addBreakArea(new BreakArea(Material.COAL_BLOCK, new Location(w, 67, 74, 59), new Location(w, 73, 80, 65), BreakAreaType.ORES, new Location(w, 70, 80, 56, 0, 0)));
        addBreakArea(new BreakArea(Material.AMETHYST_BLOCK, new Location(w, 78, 74, 59), new Location(w, 84, 80,65), BreakAreaType.ORES, new Location(w, 70, 80, 56, 0, 0)));

        // Sand
        addBreakArea(new BreakArea(Material.SANDSTONE, new Location(w, -74, 74, -56), new Location(w, -64, 84, -46), BreakAreaType.SAND, new Location(w, -69, 74, -43, -180, 0)));
        addBreakArea(new BreakArea(Material.RED_SANDSTONE, new Location(w, -98, 74, -56), new Location(w, -88, 84, -46), BreakAreaType.SAND, new Location(w, -93, 73, -43, -180, 0)));
        addBreakArea(new BreakArea(Material.RED_SAND, new Location(w, -86, 67, -36), new Location(w, -76, 73, -26), new Location(w, -87, 73, -39, 0, 0)));
        addBreakArea(new BreakArea(Material.SAND, new Location(w, -98, 67, -36), new Location(w, -88, 73, -26), new Location(w, -87, 73, -39, 0, 0)));


        // Logs
        addBreakArea(new BreakArea(Material.OAK_LOG, new Location(w, -79, 73, 24), new Location(w, -71, 79, 32), BreakAreaType.OAK_LOGS, new Location(w, -81, 72, 21, 0, 0)));
        addBreakArea(new BreakArea(Material.OAK_LOG, new Location(w, -79, 82, 24), new Location(w, -71, 88, 32), BreakAreaType.OAK_LOGS, new Location(w, -81, 81, 22, 0, 0)));
        addBreakArea(new BreakArea(Material.BIRCH_LOG, new Location(w, -91, 73, 24), new Location(w, -83, 79, 32), BreakAreaType.BIRCH_LOGS, new Location(w, -81, 72, 21, 0, 0)));
        addBreakArea(new BreakArea(Material.BIRCH_LOG, new Location(w, -91, 82, 24), new Location(w, -83, 88, 32), BreakAreaType.BIRCH_LOGS, new Location(w, -81, 81, 22, 0, 0)));
        addBreakArea(new BreakArea(Material.JUNGLE_LOG, new Location(w, -79, 73, -4), new Location(w, -71, 79, 4), BreakAreaType.JUNGLE_LOGS, new Location(w, -81, 72, 7, -180, 0)));
        addBreakArea(new BreakArea(Material.CHERRY_LOG, new Location(w, -79, 82, -4), new Location(w, -71, 88, 4), BreakAreaType.CHERRY_LOGS, new Location(w, -81, 81, 6, -180, 0)));
        addBreakArea(new BreakArea(Material.DARK_OAK_LOG, new Location(w, -91, 73, -4), new Location(w, -83, 79, 4), BreakAreaType.DARK_OAK_LOGS, new Location(w, -81, 72, 7, -180, 0)));
        addBreakArea(new BreakArea(Material.ACACIA_LOG, new Location(w, -91, 82, -4), new Location(w, -83, 88, 4), BreakAreaType.ACACIA_LOGS, new Location(w, -81, 81, 6, -180, 0)));
        addBreakArea(new BreakArea(Material.SPRUCE_LOG, new Location(w, -105, 73, 10), new Location(w, -97, 79, 18), BreakAreaType.SPRUCE_LOGS, new Location(w, -94, 72, 14, 90, 0)));
        addBreakArea(new BreakArea(Material.SPRUCE_LOG, new Location(w, -105, 82, 10), new Location(w, -97, 88, 18), BreakAreaType.SPRUCE_LOGS, new Location(w, -96, 81, 14, 90, 0)));

        // stone
        addBreakArea(new BreakArea(Material.STONE, new Location(w, -8, 67, -84), new Location(w, 2, 73, -74), new Location(w, -11, 74, -87, -90, 0)));
        addBreakArea(new BreakArea(Material.DIORITE, new Location(w, -8, 67, -100), new Location(w, 2, 73, -90), new Location(w, -11, 74, -87, -90, 0)));
        addBreakArea(new BreakArea(Material.GRANITE, new Location(w, -30, 67, -84), new Location(w, -20, 73, -74), new Location(w, -17, 74, -87, 90, 0)));
        addBreakArea(new BreakArea(Material.ANDESITE, new Location(w, -30, 67, -100), new Location(w, -20, 73, -90), new Location(w, -17, 74, -87, 90, 0)));
        addBreakArea(new BreakArea(Material.MOSSY_COBBLESTONE, new Location(w, -19, 67, -115), new Location(w, -9, 73, -105), new Location(w, -14, 74, -101, 180, 0)));

        // bricks
        Location spawn_bricks = new Location(w, -43, 73, 63, 45, 0);
        addBreakArea(new BreakArea(Material.STONE_BRICKS, new Location(w, -38, 66, 71), new Location(w, -28, 72, 81), spawn_bricks));
        addBreakArea(new BreakArea(Material.BRICKS, new Location(w, -50, 66, 71), new Location(w, -40, 72, 81), spawn_bricks));
        addBreakArea(new BreakArea(Material.CHISELED_STONE_BRICKS, new Location(w, -62, 66, 71), new Location(w, -52, 72, 81), spawn_bricks));
        addBreakArea(new BreakArea(Material.PRISMARINE_BRICKS, new Location(w, -62, 66, 59), new Location(w, -52, 72, 69), spawn_bricks));
        addBreakArea(new BreakArea(Material.NETHER_BRICKS, new Location(w, -62, 66, 47), new Location(w, -52, 72, 57), spawn_bricks));

        // nether
        addBreakArea(new BreakArea(Material.WARPED_WART_BLOCK, new Location(w, 20, 66, 69), new Location(w, 30, 72, 79), new Location(w, 17, 73, 82, -90, 0)));
        addBreakArea(new BreakArea(Material.BLACKSTONE, new Location(w, 20, 66, 85), new Location(w, 30, 72, 95), new Location(w, 17, 73, 82, -90, 0)));
        addBreakArea(new BreakArea(Material.NETHER_WART_BLOCK, new Location(w, -2, 66, 69), new Location(w, 8, 72, 79), new Location(w, 11, 73, 82, 90, 0)));
        addBreakArea(new BreakArea(Material.NETHER_BRICKS, new Location(w, -2, 66, 85), new Location(w, 8, 72, 95), new Location(w, 11, 73, 82, 90, 0)));
        addBreakArea(new BreakArea(Material.QUARTZ_BLOCK, new Location(w, 9, 66, 100), new Location(w, 19, 72, 110), new Location(w, 14,73, 97, 180, 0)));
    }

    @Override
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().getLocation().getY() <= deathY) {
            if (e.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) {
                Location l = e.getPlayer().getLocation();
                l.add(0, 120, 0);
                e.getPlayer().teleport(l);
            } else {
                BUILD_MART.getBuildMartPlayer(e.getPlayer()).respawn();
            }
        }

        // Cause player to fly when they are close to the central fan
        if (Math.sqrt(Math.pow((e.getPlayer().getLocation().getX()+3),2)+(Math.pow(e.getPlayer().getLocation().getZ(),2))) <= 8 && e.getPlayer().getLocation().getY() < 10) {
            e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 1000, e.getPlayer().getVelocity().getZ()));
            MBC.getInstance().plugin.getServer().getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, new Runnable() {
                @Override
                public void run() {
                    e.getPlayer().setVelocity(new Vector(e.getPlayer().getVelocity().getX(), 3, e.getPlayer().getVelocity().getZ()));
                }
            }, 17);
        }

        if (!(e.getPlayer().getLocation().getBlock().getType().equals(Material.NETHER_PORTAL))) return;

        warp(e.getPlayer());
    }

    public void warp(Player player) {
        if (player.getLocation().getZ() >= -10 && player.getLocation().getZ() <= 10) {
            // warping to team plots
            player.setAllowFlight(true);
            Participant p = Participant.getParticipant(player);
            BuildMartTeam team = BUILD_MART.getTeam(p);
            Location l = team.getSPAWN();
            l.setPitch(player.getLocation().getPitch());
            l.setYaw(player.getLocation().getYaw());
            player.teleport(l);
        } else {
           // warping to mart
           player.setAllowFlight(false);
           Location l = EXIT_PORTAL_LOC;
           l.setYaw(player.getLocation().getYaw());
           l.setPitch(player.getLocation().getPitch());
           player.teleport(l);
        }
    }

    @Override
    public void openPortals(boolean b) {
        Material m = b ? Material.AIR : Material.BARRIER;
        for (int y = 1; y <= 5; y++) {
            // main portal
            for (int z = -2; z <= 2; z++) {
                getWorld().getBlockAt(13, y, z).setType(m);
            }

            //team portals
            for (int z = 148; z <= 152; z++) {
                for (int x = -108; x <= 87; x+=39) {
                    getWorld().getBlockAt(x, y, z).setType(m);
                }
            }
        }
    }

    @Override
    public void loadTeamPlots(BuildMartTeam[] teams) {
        World world = Bukkit.getWorld("BSABM");
        int x = -85;
        int z = 142;
        int i = 0, n = 0; // i tracks teams, n tracks plot num
        for (; x <= 110; x += 39) {
            for (; z <= 164; z += 11) {
                BuildPlot example = new BuildPlot(new Location(world, x, 1, z), true, n);
                BuildPlot replica = new BuildPlot(new Location(world, x-11, 1, z), false, n);
                replica.setBuild(BUILD_MART.getOrder(n).get(0));
                example.setBuild(BUILD_MART.getOrder(n).get(0));
                teams[i].addBuildPlot(replica, n);
                teams[i].addExamplePlot(example, n);
                replica.setAir();
                n++;
            }
            i++;
            z = 142;
            n = 0;
        }
    }

    @Override
    public void resetBlockInventories() {
        for (int x = -104; x <= 91; x+= 39) {
            // reset chests
            for (int z = 139; z <= 161; z+=11) {
                Chest c = (Chest) getWorld().getBlockAt(x, 1, z).getState();
                c.getInventory().clear();
            }

            // reset furnaces
            for (int z = 140; z <= 158; z+=18) {
                for (int y = 2; y <= 3; y++) {
                    Furnace f1 = (Furnace) getWorld().getBlockAt(x-3, y, z).getState();
                    Furnace f2 = (Furnace) getWorld().getBlockAt(x-3, y, z+2).getState();
                    f1.getInventory().clear();
                    f2.getInventory().clear();
                }
            }
        }
    }

    @Override
    public int getVolume(BreakAreaType type) {
        return switch (type) {
            case ORES -> 339;
            case SAND -> 1315;
            default -> 252; // for all `logs` types; shouldn't use getVolume for regular fill
        };
    }
}