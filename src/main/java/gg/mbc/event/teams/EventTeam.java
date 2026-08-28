package gg.mbc.event.teams;

import gg.mbc.event.players.EventPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static net.kyori.adventure.text.Component.text;

public class EventTeam {
    public static final int MAX_NAME_LENGTH = 16;

    // Display
    private final TeamType type;
    private String name;
    private Component displayName;
    private final String scoreboardName;
    private final Character icon;
    private final NamedTextColor textColor;
    private final Color color;

    // Players
    private final Set<EventPlayer> players;

    public EventTeam(TeamType type) {
        this.type = type;
        this.name = type.teamName();
        this.icon = type.icon();
        this.color = type.color();
        this.textColor = type.textColor();
        this.scoreboardName = type.scoreboardName();
        players = new HashSet<>();

        this.displayName = text(icon + " ", NamedTextColor.WHITE).append(text(name, textColor));
    }

    // Add and Remove players from team
    public void addPlayer(EventPlayer p) { players.add(p); }
    public void removePlayer(EventPlayer p) { players.remove(p); }

    // View Players
    public Collection<EventPlayer> players() { return Collections.unmodifiableCollection(players); }

    public Component displayName() { return displayName; }
    public String name() { return name; }
    public NamedTextColor textColor() { return textColor; }
    public String scoreboardName() { return scoreboardName; }
    public Character icon() { return icon; }
    public TeamType type() { return type; }

    /**
     * Changes the display name of the team.
     * It is guaranteed that
     * @param name New name to change to.
     * @return boolean value indicating success of the operation
     */
    public boolean changeName(String name) {
        if (name.length() > MAX_NAME_LENGTH) return false;
        this.name = name;
        this.displayName = text(icon + " ", NamedTextColor.WHITE).append(text(name, textColor));
        return true;
    }

    /**
     * Reset the name of this team.
     */
    public void resetName() {
        this.name = type.name();
        this.displayName = text(icon + " ", NamedTextColor.WHITE).append(text(name, textColor));
    }

    @Override
    public int hashCode() { return type.sortID; }
}
