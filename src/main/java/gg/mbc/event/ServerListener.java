package gg.mbc.event;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import gg.mbc.event.players.EventPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class ServerListener implements Listener {
    final Plugin plugin;

    public ServerListener(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Immediately be placed on a team upon joining.
     * Default team is Spectator team.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        MBCEvent mbc = MBCEvent.getInstance();
        Player player = event.getPlayer();
        if (mbc.getPlayer(player.getUniqueId()) == null) {
            // Player is new
            mbc.addPlayer(player);

            // TODO: game scoreboard
        } else {
            // Player is relogging
            EventPlayer eventPlayer = mbc.getPlayer(player.getUniqueId());
            eventPlayer.setPlayer(player);

            // TODO: game scoreboard
        }
    }

    /**
     * Prevent getting kicked by flying.
     */
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.reason().toString().equalsIgnoreCase("Flying is not enabled on this server"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onJump(PlayerJumpEvent e) {
        if (isOnBlockWithBuffer(e.getPlayer(), MBCUtils.MEGA_BOOST_PAD)) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Player p = e.getPlayer();
                    Location l = p.getLocation();
                    l.setPitch(-30);
                    p.setVelocity(p.getVelocity().add(l.getDirection().multiply(4.0).setY(1.25)));
                    p.playSound(p, "sfx.orange_red_jump_pad", SoundCategory.BLOCKS, 1, 1);
                }
            }, 1);
            return;
        }

        if (isOnBlockWithBuffer(e.getPlayer(), MBCUtils.BOOST_PAD)) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Player p = e.getPlayer();
                    Location l = p.getLocation();
                    l.setPitch(-30);
                    p.setVelocity(p.getVelocity().add(l.getDirection().multiply(2.0)));
                    p.playSound(p, "sfx.orange_red_jump_pad", SoundCategory.BLOCKS, 1, 1);
                }
            }, 1);
            return;
        }

        if (isOnBlockWithBuffer(e.getPlayer(), MBCUtils.JUMP_PAD)) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    Player p = e.getPlayer();
                    p.setVelocity(p.getVelocity().add(new Vector(p.getVelocity().getX(), p.getVelocity().getY()*1.75, p.getVelocity().getZ())));
                    p.playSound(p, "sfx.green_jump_pad", SoundCategory.BLOCKS, 1, 1);
                }
            }, 1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (isOnBlockWithBuffer(e.getPlayer(), MBCUtils.SPEED_PAD)) {
            PotionEffect s = e.getPlayer().getPotionEffect(PotionEffectType.SPEED);

            if (s == null) e.getPlayer().playSound(e.getPlayer(), "sfx.speed_pad", SoundCategory.BLOCKS, 1, 1);
            else if (s.isShorterThan(new PotionEffect(PotionEffectType.SPEED, 70, 3, false, false))) e.getPlayer().playSound(e.getPlayer(), "sfx.speed_pad", SoundCategory.BLOCKS, 1, 1);
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 3, false, false));
        }
    }

    private boolean isOnBlockWithBuffer(Player player, Material padType) {
        Location loc = player.getLocation();
        double radius = MBCUtils.JUMP_BUFFER_ROOM;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Location checkLocation = loc.clone().add(x*radius, -1, z*radius);
                Block blockBelow = checkLocation.getBlock();

                if (blockBelow.getType() == padType) {
                    return true;
                }
            }
        }
        return false;
    }


}
