package gg.mbc;

import gg.mbc.event.MBCEvent;
import org.bukkit.World;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public class EventPluginTest {
    private ServerMock server;
    private EventPlugin plugin;
    private World world;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();

        // Server environment assumes the existence of a world "world"
        this.world = server.addSimpleWorld("world");

        this.plugin = MockBukkit.load(EventPlugin.class);

    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Server Environment Setup")
    void testEventEnvironmentNotNull() {
        Assertions.assertNotNull(MBCEvent.getInstance());
    }
}
