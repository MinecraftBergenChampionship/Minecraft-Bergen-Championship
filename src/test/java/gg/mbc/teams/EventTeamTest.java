package gg.mbc.teams;

import gg.mbc.EventPlugin;
import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import gg.mbc.util.MBCUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.Arrays;
import java.util.Collections;

public class EventTeamTest extends EventPluginTest {
    TeamManager manager;

    @Override
    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        // Server environment assumes the existence of a world "world"
        this.world = server.addSimpleWorld(MBCUtils.LOBBY_WORLD_NAME);

        this.plugin = MockBukkit.load(EventPlugin.class);
        Assertions.assertNotNull(MBCEvent.getInstance());
        this.manager = MBCEvent.getInstance().getTeamManager();
        Assertions.assertNotNull(manager);
    }

    @Test
    @DisplayName("Test changeTeamName to valid names")
    public void testChangeTeamNameValid() {
        PlayerMock player = server.addPlayer();
        Assertions.assertEquals(TeamType.SPECTATOR, manager.getTeam(player).type());
        String[] validNames = {
            "Regular Name", "Red Rabbits", "SixteenCharacter", "@awesome",
            "x", ":mfw:", "123a", "a123", "#awesome", "冰淇淋", "Two Spaces Here?", "     trimmed     ",
        };
        for (TeamType t : TeamType.values()) {
            manager.changeTeam(player, manager.getTeam(t));
            EventTeam team = manager.getTeam(player);
            Assertions.assertEquals(t, team.type());

            Collections.shuffle(Arrays.asList(validNames));
            for (String name : validNames) {
                Assertions.assertTrue(team.changeName(name));
                Assertions.assertEquals(name.trim(), team.name());
                team = manager.getTeam(player);
                Assertions.assertEquals(t, team.type());
            }
        }
    }

    @Test
    @DisplayName("Test changeTeamName to invalid names")
    public void testChangeTeamNameInValid() {
        PlayerMock player = server.addPlayer();
        Assertions.assertEquals(TeamType.SPECTATOR, manager.getTeam(player).type());
        String[] invalidNames = {
            "", " ", "67", "thisnameisridiculouslylongandnotsupported",
            ":#:", "@@@@@",
        };
        for (TeamType t : TeamType.values()) {
            manager.changeTeam(player, manager.getTeam(t));
            EventTeam team = manager.getTeam(player);
            String oldName = team.name();
            Assertions.assertEquals(t, team.type());

            Arrays.sort(invalidNames);
            for (String name : invalidNames) {
                Assertions.assertFalse(team.changeName(name));
                Assertions.assertNotEquals(name, team.name());
                Assertions.assertEquals(oldName, team.name());
                team = manager.getTeam(player);
                Assertions.assertEquals(t, team.type());
            }
        }
    }
}
