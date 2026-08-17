package gg.mbc.event;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.players.EventPlayer;
import gg.mbc.event.teams.TeamType;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

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
        UUID id = player.getUniqueId();
        if (mbc.getPlayer(id) == null) {
            // Player is new
            TeamManager tm = mbc.getTeamManager();
            mbc.addPlayer(player);
            tm.changeTeam(player, tm.getTeam(TeamType.SPECTATOR));

            // TODO: game scoreboard
        } else {
            // Player is relogging
            EventPlayer eventPlayer = mbc.getPlayer(id);
            eventPlayer.setPlayer(player);
            TeamManager tm = mbc.getTeamManager();
            tm.changeTeam(player, tm.getTeam(id));

            // TODO: game scoreboard
        }
        EventPlayer eventPlayer = mbc.getPlayer(id);
        event.joinMessage(eventPlayer.getEventName().append(text(" joined the game", NamedTextColor.WHITE)));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        MBCEvent mbc = MBCEvent.getInstance();
        Player player = event.getPlayer();
        EventPlayer eventPlayer = mbc.getPlayer(player.getUniqueId());
        event.quitMessage(eventPlayer.getEventName().append(text(" left the game", NamedTextColor.WHITE)));

        // TODO: logistics in game
        // Internally set to spectator
        /*
        TeamManager tm = mbc.getTeamManager();
        tm.changeTeam(player, tm.getTeam(TeamType.SPECTATOR)); */
    }


    /**
     * Formatting for chat display, as well as emoji support
     * @see MBCUtils::getEmojis()
     * https://github.com/MrQuackDuck/ImageEmojis/blob/705197c486f68a8f497a15356eb766701cde6dbc/src/main/java/mrquackduck/imageemojis/server/listeners/SendMessageListener.java#L35
     * */
    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        TextComponent msg = (TextComponent) event.originalMessage();
        Component name = MBCEvent.getInstance().getPlayer(player.getUniqueId()).getEventName();
        for (Map.Entry<String, Character> emoji : MBCUtils.getEmojis().entrySet()) {
            TextComponent replacement = Component.text(emoji.getValue());
            TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                    .match(emoji.getKey())
                    .replacement(replacement)
                    .build();
            msg = (TextComponent) msg.replaceText(replacementConfig);
        }
        event.setCancelled(true);
        Bukkit.broadcast(name.append(text(": ").append(msg)));
    }

    /**
     * Prevent getting kicked by flying.
     */
    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (event.reason().toString().equalsIgnoreCase("Flying is not enabled on this server"))
            event.setCancelled(true);
    }

    /**
     * Handle Global Jump/Speed pad Effects
     */
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
                    p.playSound(p, MBCUtils.BOOST_PAD_SFX, SoundCategory.BLOCKS, 1, 1);
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
                    p.playSound(p, MBCUtils.BOOST_PAD_SFX, SoundCategory.BLOCKS, 1, 1);
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
                    p.playSound(p, MBCUtils.JUMP_PAD_SFX, SoundCategory.BLOCKS, 1, 1);
                }
            }, 1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (isOnBlockWithBuffer(e.getPlayer(), MBCUtils.SPEED_PAD)) {
            PotionEffect s = e.getPlayer().getPotionEffect(PotionEffectType.SPEED);

            if (s == null) e.getPlayer().playSound(e.getPlayer(), MBCUtils.SPEED_PAD_SFX, SoundCategory.BLOCKS, 1, 1);
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
