package gg.mbc.event.teams;

import gg.mbc.event.MBCUtils;
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
    // Display
    private final String name;
    private final String scoreboardName;
    private final Character icon;
    private final NamedTextColor textColor;
    private final Color color;
    private final Component displayName;

    // Players
    private final Set<EventPlayer> players;

    public EventTeam(TeamType type) {
        this.name = type.teamName();
        this.icon = type.icon();
        this.color = type.color();
        this.textColor = type.textColor();
        this.scoreboardName = type.scoreboardName();
        players = new HashSet<>();

        this.displayName = text().content(icon + " ").append(text(name, textColor)).build();
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
}
