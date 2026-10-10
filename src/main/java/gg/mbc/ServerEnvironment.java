package gg.mbc;

import gg.mbc.util.MBCUtils;
import gg.mbc.event.ServerListener;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.*;

/**
 * Singleton class representing
 */
public class ServerEnvironment {
    private final EventPlugin plugin;
    private final ServerListener globalListener;
    private final World world;
    private final List<NamespacedKey> recipes;

    ServerEnvironment(EventPlugin plugin) {
        this.plugin = plugin;
        this.recipes = new ArrayList<>();

        // prevent crafting certain items
        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe == null) continue;
            if (MBCUtils.BLOCKED_RECIPES.contains(recipe.getResult().getType())) {
                it.remove();
            } else {
                if (recipe instanceof Keyed key) {
                    recipes.add(key.getKey());
                }
            }
        }

        this.globalListener = new ServerListener(plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.discoverRecipes(recipes);
        }

        world = Objects.requireNonNull(Bukkit.getWorld(MBCUtils.LOBBY_WORLD_NAME));
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

    /**
     * @return all global crafting recipes.
     */
    public List<NamespacedKey> getRecipes() {
        return Collections.unmodifiableList(recipes);
    }
}
