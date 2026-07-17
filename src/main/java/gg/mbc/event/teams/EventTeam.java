package gg.mbc.event.teams;

import gg.mbc.event.MBCUtils;
import gg.mbc.event.players.EventPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;

import java.util.ArrayList;

import static net.kyori.adventure.text.Component.text;

public class EventTeam {
    // Display
    private final String name;
    private final String scoreboardName;
    private final Character icon;
    private final NamedTextColor textColor;
    private final Color color;
    private final Component displayName;

    // Data
    private final ArrayList<EventPlayer> players = new ArrayList<>();

    public EventTeam(TeamType type) {
        this.name = type.teamName();
        this.icon = type.icon();
        this.color = type.color();
        this.textColor = type.textColor();
        this.scoreboardName = type.scoreboardName();

        this.displayName = text().content(icon + " ").append(text(name, textColor)).build();
    }

    public Component displayName() { return displayName; }
    public String name() { return name; }
    public NamedTextColor textColor() { return textColor; }
    public String scoreboardName() { return scoreboardName; }
}
