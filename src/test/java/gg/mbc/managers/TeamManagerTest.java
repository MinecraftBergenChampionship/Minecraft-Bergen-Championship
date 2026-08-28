package gg.mbc.managers;

import gg.mbc.EventPlugin;
import gg.mbc.EventPluginTest;
import gg.mbc.event.MBCEvent;
import gg.mbc.event.managers.TeamManager;
import gg.mbc.event.teams.EventTeam;
import gg.mbc.event.teams.TeamType;
import gg.mbc.util.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.*;

public class TeamManagerTest extends EventPluginTest {
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
    @DisplayName("Test Adding to Spec on Log on")
    public void testLogOn() {
        for (int i = 0; i < MBCEvent.MAX_PLAYERS_PER_TEAM * MBCEvent.MAX_TEAMS; i++) {
            PlayerMock player = server.addPlayer();
            EventTeam team = manager.getTeam(player);
            Assertions.assertEquals(TeamType.SPECTATOR, team.type());
        }
    }

    @Test
    @DisplayName("Test getTypes")
    public void testGetTypes() {
        Set<TeamType> types = new HashSet<>(Set.of(TeamType.RED, TeamType.YELLOW, TeamType.BLUE, TeamType.PURPLE, TeamType.GREEN, TeamType.PINK, TeamType.SPECTATOR));
        for (TeamType t : manager.getTypes()) {
            Assertions.assertTrue(types.contains(t));
            types.remove(t);
        }
        Assertions.assertEquals(0, types.size());
    }

    @Test
    @DisplayName("Test getTeam")
    public void testGetTeam() {
        for (TeamType t : manager.getTypes()) {
            Assertions.assertEquals(t, manager.getTeam(t).type());
            Assertions.assertEquals(t, manager.getTeam(t.teamName()).type());
            Assertions.assertEquals(t, manager.getTeam(t.scoreboardName()).type());
            Assertions.assertEquals(t, manager.getTeam(t.name()).type());
        }
    }

    @Test
    @DisplayName("Test changeTeam")
    public void testChangeTeamOnce() {
        for (TeamType t : manager.getTypes()) {
            PlayerMock player = server.addPlayer();
            EventTeam team = manager.getTeam(player);
            Assertions.assertEquals(TeamType.SPECTATOR, team.type());
            manager.changeTeam(player, manager.getTeam(t));
            Assertions.assertEquals(t, manager.getTeam(player).type());
        }
    }

    @Test
    @DisplayName("Test changeTeam")
    public void testChangeTeamTwice() {
        for (TeamType t : manager.getTypes()) {
            PlayerMock player = server.addPlayer();
            EventTeam team = manager.getTeam(player);
            Assertions.assertEquals(TeamType.SPECTATOR, team.type());
            manager.changeTeam(player, manager.getTeam(t));
            Assertions.assertEquals(t, manager.getTeam(player).type());
            for (TeamType t2 : manager.getTypes()) {
                manager.changeTeam(player, manager.getTeam(t2));
                if (t2 != t) {
                    Assertions.assertNotEquals(t, manager.getTeam(player).type());
                }
                Assertions.assertEquals(t2, manager.getTeam(player).type());
            }
        }
    }


    @Test
    @DisplayName("Test changeTeam with string attributes")
    public void testChangeTeamString() {
        // Get string names
        List<Tuple<TeamType, String>> teamNames = new ArrayList<>();
        for (TeamType t : manager.getTypes()) {
            teamNames.add(new Tuple<>(t, t.scoreboardName()));
            teamNames.add(new Tuple<>(t, t.teamName()));
            teamNames.add(new Tuple<>(t, t.name()));

            teamNames.add(new Tuple<>(t, t.scoreboardName().toUpperCase()));
            teamNames.add(new Tuple<>(t, t.teamName().toUpperCase()));
            teamNames.add(new Tuple<>(t, t.name().toUpperCase()));

            teamNames.add(new Tuple<>(t, t.scoreboardName().toLowerCase()));
            teamNames.add(new Tuple<>(t, t.teamName().toLowerCase()));
            teamNames.add(new Tuple<>(t, t.name().toLowerCase()));
        }

        List<PlayerMock> players = new ArrayList<>();
        for (Tuple<TeamType, String> pair : teamNames) {
            PlayerMock player = server.addPlayer();
            players.add(player);
            EventTeam team = manager.getTeam(player);
            Assertions.assertEquals(TeamType.SPECTATOR, team.type());
            TeamType type = pair.first;
            String name = pair.second;
            manager.changeTeam(player, name);
            Assertions.assertEquals(type, manager.getTeam(player).type());
        }

        for (int i = 0; i < 25; i++) {
            PlayerMock player = players.get((int)(Math.random()*teamNames.size()));
            Tuple<TeamType, String> pair = teamNames.get((int)(Math.random()*teamNames.size()));
            TeamType type = pair.first;
            String name = pair.second;
            manager.changeTeam(player, name);
            Assertions.assertEquals(type, manager.getTeam(player).type());
        }
    }
}
