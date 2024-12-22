package me.kotayka.mbc.gameMaps.sgMaps;

import me.kotayka.mbc.MBC;
import me.kotayka.mbc.Participant;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

public class JesuscraftTwo extends SurvivalGamesMap {
    int[][] spawns = {{9996,10005},{9996,9998},{10003,9998},{10003,10005},{10001,10003},{9998,10003},{9998,10000},{10001,10000}};

    Location[] middleChests = {};
    public final Particle.DustOptions HEIGHT_BORDER_PARTICLE = new Particle.DustOptions(Color.ORANGE, 3);

    public JesuscraftTwo() {
        //super("Jesuscraft2");
        super.spawns = this.spawns;
        super.middleChests = this.middleChests;
        super.spawnY = 151;
        super.mapName = "Jesuscraft II";
        super.type = "Elytra";
        super.CENTER = new Location(Bukkit.getWorld("Survival_Games"), 10000, 63, 10001);
        super.hasElevationBorder = true;
        super.borderHeight = 45;
        //super.airdrops = false;

        resetBorder();
    }

    @Override
    public void setBarriers(boolean barriers) {
        Material block = (barriers) ? Material.BARRIER : Material.AIR;

        for (int x = 9994; x <= 10005; x++) {
            for (int z = 9996; z <= 10007; z++) {
                // directly north / south
                getWorld().getBlockAt(x, 157, z).setType(block);
                if (x == 9994 || x == 10005 || z == 9996 || z == 10007) {
                    for (int y = 151; y <= 156; y++) {
                        getWorld().getBlockAt(x, y, z).setType(block);
                    }
                }
                getWorld().getBlockAt(x, 150, z).setType(block);
            }
        }
    }

    public void Border() {
        borderHeight++;
        for (int x = 9986; x <= 10016; x+=3) {
            for (int z = 9986; z <= 10016; z+=3) {
                getWorld().spawnParticle(Particle.DUST, x, borderHeight, z, 1, HEIGHT_BORDER_PARTICLE);
            }
        }

        // the lazy way
        for (Participant p : MBC.getInstance().getPlayers()) {
            if (p.getPlayer().getGameMode() != GameMode.SURVIVAL || p.getPlayer().getWorld() != getWorld()) continue;

            Player player = p.getPlayer();
            if (player.getLocation().getY() <= borderHeight) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_RED + "You're in the border!"));
                player.damage(0.5*Math.abs(player.getLocation().getY()-borderHeight+0.5));
            }
        }
    }

    @Override
    public boolean checkChest(Chest chest) {
        Location l = chest.getLocation();

        return l.getX() > 9798 && l.getX() < 10202
            && l.getZ() > 9798 && l.getZ() < 10202;
    }

    @Override
    public void resetBorder() {
        borderHeight = 40;
        border.setCenter(10000, 10001);
        border.setSize(400);
    }

    @Override
    public void startBorder() {
        border.setSize(30, 420);
    }

    @Override
    public void Overtime() {
        border.setSize(12, 15);
    }
}
