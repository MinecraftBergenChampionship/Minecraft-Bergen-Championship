package gg.mbc;

import gg.mbc.commands.*;
import gg.mbc.commands.tab.changeTeamTab;
import gg.mbc.event.MBCEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class EventPlugin extends JavaPlugin {
    // Manages the singular server instance
    private static ServerEnvironment serverEnvironment = null;

    @Override
    public void onEnable() {
        initializeCommands();
        MBCEvent.createEvent(this);
        if (serverEnvironment == null) {
            serverEnvironment = new ServerEnvironment(this);
        }

        serverEnvironment.resetPlayerStatus();
        Bukkit.broadcast(Component.text("MBC has been enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        // Stop all games that are occurring

        // Destroy current instance of event
        MBCEvent.getInstance().stopEvent();

        // Reset server environment
        serverEnvironment = null;
        Bukkit.broadcast(Component.text("MBC has been disabled!", NamedTextColor.RED));
    }

    public void reloadPlugin() {
        onDisable();
        onEnable();
    }

    @SuppressWarnings("null")
    private void initializeCommands() {
        PluginCommand changeTeam = getCommand("changeteam");
        changeTeam.setExecutor(new changeteam());
        changeTeam.setTabCompleter(new changeTeamTab());

        getCommand("debug").setExecutor(new debug());
        getCommand("reload").setExecutor(new reload());
        getCommand("ping").setExecutor(new ping());
        getCommand("teamname").setExecutor(new teamname());
    }
}
