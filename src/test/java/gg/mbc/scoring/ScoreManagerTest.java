package gg.mbc.scoring;

import gg.mbc.EventPlugin;
import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.scoring.ScoreManager;
import gg.mbc.util.MBCUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;

public class ScoreManagerTest extends EventPluginTest {
    ScoreManager scoreManager;

    @Override
    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        // Server environment assumes the existence of a world "world"
        this.world = server.addSimpleWorld(MBCUtils.LOBBY_WORLD_NAME);

        this.plugin = MockBukkit.load(EventPlugin.class);
        Assertions.assertNotNull(MBCEvent.getInstance());
        this.scoreManager = MBCEvent.getInstance().getScoreManager();
        Assertions.assertNotNull(scoreManager);
    }
}
