package gg.mbc.event.teams;

import gg.mbc.util.MBCUtils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;

public enum TeamType {
    RED("Red Rabbits", MBCUtils.RED_ICON, Color.RED, NamedTextColor.RED, "RED", 1),
    YELLOW("Yellow Yaks", MBCUtils.YELLOW_ICON, Color.YELLOW, NamedTextColor.YELLOW, "YELLOW", 2),
    GREEN("Green Guardians", MBCUtils.GREEN_ICON, Color.GREEN, NamedTextColor.GREEN, "GREEN", 3),
    BLUE("Blue Bats", MBCUtils.BLUE_ICON, Color.BLUE, NamedTextColor.BLUE, "BLUE", 4),
    PURPLE("Purple Pandas", MBCUtils.PURPLE_ICON, Color.PURPLE, NamedTextColor.DARK_PURPLE, "PURPLE", 5),
    PINK("Pink Piglets", MBCUtils.PINK_ICON, Color.fromRGB(243, 139, 170), NamedTextColor.LIGHT_PURPLE, "PINK", 6),
    SPECTATOR("Spectator", MBCUtils.SPECTATOR_ICON, Color.WHITE, NamedTextColor.GRAY, "SPECTATOR", 0);

    private final String name;
    private final String scoreboardName;
    private final Character icon;
    private final Color color;
    private final NamedTextColor textColor;
    public final int sortID;

    TeamType(String name, Character icon, Color color, NamedTextColor textColor, String scoreboardName, int sortID) {
        this.name = name;
        this.scoreboardName = scoreboardName;
        this.icon = icon;
        this.color = color;
        this.textColor = textColor;
        this.sortID = sortID;
    }

    public String teamName() { return name; }
    public String scoreboardName() { return scoreboardName; }
    public Character icon() { return icon; }
    public Color color() { return color; }
    public NamedTextColor textColor() { return textColor; }
}
