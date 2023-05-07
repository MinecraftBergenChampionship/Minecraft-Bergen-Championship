package me.kotayka.mbc;

import me.kotayka.mbc.commands.changeTeam;
import me.kotayka.mbc.commands.start;
import me.kotayka.mbc.commands.tab.changeTeamTabCompletion;
import me.kotayka.mbc.commands.tab.startTabCompletion;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getLogger().info("MBC enabled");

        getServer().getPluginManager().registerEvents(new MBC(this), this);
        getServer().getPluginManager().registerEvents(MBC.lobby, this);
        getServer().getPluginManager().registerEvents(MBC.aceRace, this);
        getServer().getPluginManager().registerEvents(MBC.tgttos, this);

        getCommand("changeTeam").setExecutor(new changeTeam());
        getCommand("changeTeam").setTabCompleter(new changeTeamTabCompletion());

        getCommand("start").setExecutor(new start());
        getCommand("start").setTabCompleter(new startTabCompletion());
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("MBC disabled");
    }
}
