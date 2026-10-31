package gg.mbc.event.managers;

import gg.mbc.EventPlugin;
import gg.mbc.event.scheduling.EventStage;
import gg.mbc.event.scheduling.TimedEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * Scheduling Manager for large-form timed events.
 * This class is meant to handle scheduling and the core game loops behind main event minigames.
 * This class should not be used for scheduling small BukkitTasks; instead use Bukkit's native scheduling tools.
 */
public class EventScheduler {
    private final EventPlugin plugin;
    private final BukkitScheduler scheduler;
    private BukkitTask eventTimer;
    private EventStage activeEvent;

    public EventScheduler(EventPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void clearEvents() {
        scheduler.cancelTasks(plugin);
    }

    public void startTimer(int duration, TimedEvent event) {
        event.onStart();

        eventTimer = new BukkitRunnable() {
            private int timeLeft = duration;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    event.onEnd();
                    cancel();
                    return;
                }

                event.update(timeLeft);
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopTimer() {
        eventTimer.cancel();
        activeEvent.onCancel();
    }
}
