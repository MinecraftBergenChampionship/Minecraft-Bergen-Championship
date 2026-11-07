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
     * Cleanup tasks to run when an event must be stopped unexpectedly.
     */
    void onCancel();
}
