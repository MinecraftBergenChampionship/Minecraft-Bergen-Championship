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
import java.util.Collection;

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
                Collection<String> teamNames = manager.getTeamNames();
                Assertions.assertTrue(team.changeName(name, teamNames));
                Assertions.assertEquals(name.trim(), team.name());
                team = manager.getTeam(player);
                Assertions.assertEquals(t, team.type());
                team.resetName(); // Resets name after each test so tests do not run into duplicate names from previous tests when using teamNames collection
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
            Collection<String> teamNames = manager.getTeamNames();

            Arrays.sort(invalidNames);
            for (String name : invalidNames) {
                Assertions.assertFalse(team.changeName(name, teamNames));
                Assertions.assertNotEquals(name, team.name());
                Assertions.assertEquals(oldName, team.name());
                team = manager.getTeam(player);
                Assertions.assertEquals(t, team.type());
            }
        }
    }

    //Creates two players and adds them to two different teams.
    //Team 1 has set names, team 2 attempts to change name to something identical
    //May be redundant but good to check
    //This test breaks when testing for changing name to Red Rabbits
    @Test
    @DisplayName("Test changeTeamName to existing names")
    public void testChangeTeamNameExisting(){
        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        Assertions.assertEquals(TeamType.SPECTATOR, manager.getTeam(player1).type());
        Assertions.assertEquals(TeamType.SPECTATOR, manager.getTeam(player2).type());
        String[] setTeamNames = {"Player1's Team", "Green Grabbits", "HeeHeeMcHaw"};
        TeamType[] teams = TeamType.values();

        for (int i = 0; i < teams.length-1; i++){
            System.out.println(i);
            manager.changeTeam(player1, manager.getTeam(teams[i]));
            EventTeam team1 = manager.getTeam(player1);
            Assertions.assertEquals(teams[i], team1.type());
            manager.changeTeam(player2, manager.getTeam(teams[i+1]));
            EventTeam team2 = manager.getTeam(player2);
            Assertions.assertEquals(teams[i+1], team2.type());
            String oldName1 = team1.name();
            String oldName2 = team2.name();
            for (String name : setTeamNames){
                Collection<String> teamNames = manager.getTeamNames();
                Assertions.assertTrue(team1.changeName(name, teamNames));
                teamNames = manager.getTeamNames(); //Resets so it has the new name
                Assertions.assertTrue(teamNames.contains(name));
                Assertions.assertFalse(team2.changeName(name, teamNames));
                Assertions.assertNotEquals(oldName1, team1.name());
                Assertions.assertEquals(oldName2, team2.name());
            }
            team1.resetName();
            team2.resetName();
        }
    }
}
