package gg.mbc.event.managers;

import gg.mbc.EventPlugin;
import gg.mbc.event.EventState;
import gg.mbc.event.scheduling.EventStage;

/**
 * The Event Manager class coordinates the lifecycle of the event, primarily
 * transitioning of EventState, handling both data mutation and server-effects,
 * as well as handling reloading.
 */
public class EventManager {
    private EventPlugin plugin;
    private EventStage activeEvent;
    private EventState eventState = EventState.INACTIVE;
    private int numGamesPlayed = 0;

    public EventManager(EventPlugin plugin) {
        this.plugin = plugin;
    }

    void transitionTo(EventState newState) {

    }

    public EventState getState() {
        return eventState;
    }
}
