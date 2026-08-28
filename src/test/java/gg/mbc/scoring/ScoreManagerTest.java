package gg.mbc.scoring;

import gg.mbc.EventPlugin;
import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.scoring.ScoreManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

public class ScoreManagerTest extends EventPluginTest {
    ScoreManager scoreManager;

    @Override
    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        // Server environment assumes the existence of a world "world"
        this.world = server.addSimpleWorld("world");

        this.plugin = MockBukkit.load(EventPlugin.class);
        Assertions.assertNotNull(MBCEvent.getInstance());
        this.scoreManager = MBCEvent.getInstance().getScoreManager();
        Assertions.assertNotNull(scoreManager);
    }
}
