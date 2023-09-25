package me.kotayka.mbc;

import me.kotayka.mbc.NPCs.NPCManager;
import me.kotayka.mbc.commands.*;
import me.kotayka.mbc.commands.tab.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        MBC.getInstance(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            MBC.getInstance().players.add(new Participant(p));
            p.setInvulnerable(false);
        }
        Bukkit.getLogger().info("MBC enabled");
        Bukkit.broadcastMessage("Enable stat logging with /statlogs set true");

        getServer().getPluginManager().registerEvents(MBC.getInstance(), this);
        getServer().getPluginManager().registerEvents(MBC.getInstance().lobby, this);
        //TODO: start registering events in game start
        //getServer().getPluginManager().registerEvents(MBC.getInstance().aceRace, this);
        //getServer().getPluginManager().registerEvents(MBC.getInstance().tgttos, this);
        //getServer().getPluginManager().registerEvents(MBC.getInstance().skybattle, this);

        getCommand("changeTeam").setExecutor(new changeTeam());
        getCommand("changeTeam").setTabCompleter(new changeTeamTabCompletion());

        getCommand("start").setExecutor(new start());
        getCommand("start").setTabCompleter(new startTabCompletion());

        getCommand("pause").setExecutor(new pause());
        getCommand("unpause").setExecutor(new unpause());

        getCommand("ping").setExecutor(new ping());
        getCommand("checkbuild").setExecutor(new checkbuild());
        getCommand("placement").setExecutor(new placement());
        getCommand("individual").setExecutor(new individual());
        getCommand("gamenum").setExecutor(new gamenum());

        getCommand("playerscore").setExecutor(new playerscore());
        getCommand("playerscore").setTabCompleter(new playerscoreTabCompletion());
        getCommand("teamscore").setExecutor(new teamscore());
        getCommand("teamscore").setTabCompleter(new teamscoreTabCompletion());
        getCommand("endgame").setExecutor(new endgame());

        getCommand("statlogs").setExecutor(new statlogs());
        getCommand("statlogs").setTabCompleter(new statlogsTabCompleter());

        getCommand("spawnNPC").setExecutor(new spawnNPC());
        getCommand("invincible").setExecutor(new invincible());

        // prevent crafting wooden axes (worldedit)
        Iterator<Recipe> it = getServer().recipeIterator();
        Recipe recipe;
        while (it.hasNext()) {
            recipe = it.next();
            if (recipe != null && recipe.getResult().getType() == Material.WOODEN_AXE)
                it.remove();
            if (recipe != null && recipe.getResult().getType() == Material.SNOW_BLOCK)
                it.remove();
        }
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("MBC disabled");
    }
}
