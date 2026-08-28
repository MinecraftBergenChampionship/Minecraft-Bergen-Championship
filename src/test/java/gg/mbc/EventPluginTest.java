package gg.mbc;

import org.bukkit.World;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public class EventPluginTest {
    protected ServerMock server;
    protected EventPlugin plugin;
    protected World world;

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
}
