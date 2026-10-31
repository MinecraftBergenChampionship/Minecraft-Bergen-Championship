package gg.mbc.event.scheduling;

/**
 *
 */
public interface TimedEvent {
    /**
     * Events whenever this event starts.
     */
    void onStart();

    /**
     * Events whenever this event ends.
     */
    void onEnd();

    /**
     * Core event game-loop; called every second.
     * @param timeLeft Integer amount of time, in seconds, remaining.
     * @implSpec Events will end when timeLeft == 0.
     */
    void update(int timeLeft);

    /**
     * Cleanup tasks to run when an event must be stopped unexpectedly.
     */
    void onCancel();
}
