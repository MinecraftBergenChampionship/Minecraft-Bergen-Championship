package gg.mbc;

import gg.mbc.event.MBCEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EventPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Create Event
        MBCEvent.createEvent(this);

    }

    @Override
    public void onDisable() {
        // Stop all games that are occurring

        // Destroy current instance of event
        MBCEvent.stopEvent();
    }
}
