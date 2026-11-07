package gg.mbc.event.scheduling;

public interface EventStage extends TimedEvent {
    /**
     * Core event game-loop; called every second.
     * @param timeLeft Integer amount of time, in seconds, remaining.
     * @implSpec Events will end when timeLeft == 0.
     */
    void update(int timeLeft);
}
