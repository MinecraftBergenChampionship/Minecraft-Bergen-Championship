package gg.mbc.event.managers;

import gg.mbc.EventPlugin;
import gg.mbc.event.scheduling.EventStage;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * Scheduling Manager for large-form timed events.
 * This class is meant to handle scheduling and the core game loops behind main event minigames.
 * This class should not be used for scheduling small BukkitTasks; instead use Bukkit's native scheduling tools.
 *
 * It is assumed that only one EventStage will be active at a time.
 * EventManager will handle logic for ensuring one EventStage is active at a time.
 */
public class EventScheduler {
    private final EventPlugin plugin;
    private final BukkitScheduler scheduler;
    private BukkitTask eventTimer;
    private EventStage event;

    public EventScheduler(EventPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void clearEvents() {
        scheduler.cancelTasks(plugin);
    }

    /**
     * Timer task for EventStages
     * @param duration time in seconds the task should last for
     */
    public void startTimer(int duration) {
        event.onStart();
        eventTimer = new BukkitRunnable() {
            int timeLeft = duration;

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
        event.onCancel();
    }
}