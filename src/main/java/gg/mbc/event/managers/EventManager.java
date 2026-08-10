package gg.mbc.event.managers;

import gg.mbc.EventPlugin;
import gg.mbc.event.EventState;

/**
 * The Event Manager class coordinates the lifecycle of the event, primarily
 * transitioning of EventState, handling both data mutation and server-effects,
 * as well as handling reloading.
 */
public class EventManager {
    private EventPlugin plugin;
    private EventState eventState = EventState.INACTIVE;

    public EventManager(EventPlugin plugin) {
        this.plugin = plugin;

    }

    public EventState getState() {
        return eventState;
    }
}
