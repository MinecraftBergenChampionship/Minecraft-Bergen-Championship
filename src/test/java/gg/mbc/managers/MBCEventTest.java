package gg.mbc.managers;

import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import org.junit.jupiter.api.*;

public class MBCEventTest extends EventPluginTest {
    @Test
    @DisplayName("Internal Setup")
    void testManagersLoaded() {
        Assertions.assertNotNull(MBCEvent.getInstance());
        Assertions.assertNotNull(MBCEvent.getInstance().getTeamManager());
        Assertions.assertNotNull(MBCEvent.getInstance().getScoreboardManager());
        Assertions.assertNotNull(MBCEvent.getInstance().getScoreManager());
    }
}
