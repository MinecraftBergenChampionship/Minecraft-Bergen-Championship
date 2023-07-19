package me.kotayka.mbc;

import me.kotayka.mbc.commands.changeTeam;
import me.kotayka.mbc.commands.pause;
import me.kotayka.mbc.commands.start;
import me.kotayka.mbc.commands.tab.changeTeamTabCompletion;
import me.kotayka.mbc.commands.tab.startTabCompletion;
import me.kotayka.mbc.commands.unpause;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public class Plugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        MBC.getInstance(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            MBC.getInstance().players.add(new Participant(p));
        }
        Bukkit.getLogger().info("MBC enabled");

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
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("MBC disabled");
    }
}
