package gg.mbc.teams;

import gg.mbc.EventPlugin;
import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

public class EventTeamTest extends EventPluginTest {
    TeamManager manager;

    @Override
    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        // Server environment assumes the existence of a world "world"
        this.world = server.addSimpleWorld("world");

        this.plugin = MockBukkit.load(EventPlugin.class);
        Assertions.assertNotNull(MBCEvent.getInstance());
        this.manager = MBCEvent.getInstance().getTeamManager();
        Assertions.assertNotNull(manager);
    }

    @Test
    @DisplayName("Test changeName")
    public void testChangeTeamName() {

    }
}
