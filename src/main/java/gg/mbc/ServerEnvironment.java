package gg.mbc;

import gg.mbc.event.MBCUtils;
import gg.mbc.event.ServerListener;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.Objects;

public class ServerEnvironment {
    private final EventPlugin plugin;
    private final ServerListener globalListener;
    private final World world;

    ServerEnvironment(EventPlugin plugin) {
        this.plugin = plugin;
        this.globalListener = new ServerListener(plugin);

        // prevent crafting certain items
        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe == null) continue;
            if (MBCUtils.BLOCKED_RECIPES.contains(recipe.getResult().getType())) {
                it.remove();
            }
        }

        world = Objects.requireNonNull(Bukkit.getWorld("world"));
        world.setGameRule(GameRules.ADVANCE_TIME, false);
        world.setTime(6000);

        plugin.getServer().getPluginManager().registerEvents(globalListener, plugin);
    }

    /**
     * Reset the status of all players to default when reloading the server.
     */
    @SuppressWarnings("null")
    void resetPlayerStatus() {
        // Reset all player status
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
            player.setVelocity(MBCUtils.ZERO);
            player.setInvulnerable(false);

            for (Player player2 : Bukkit.getOnlinePlayers()) {
                if (player2.getUniqueId() == player.getUniqueId()) continue;
                player.showPlayer(plugin, player2);
            }
        }
    }
}
