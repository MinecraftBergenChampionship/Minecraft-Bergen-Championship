package gg.mbc.event;

import gg.mbc.event.teams.EventTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

/**
 * Utility class for MBC Events.
 */
public final class MBCUtils {
    // Resource Pack
    public static final Character RED_ICON = 'Ⓡ';
    public static final Character YELLOW_ICON = 'Ⓨ';
    public static final Character GREEN_ICON = 'Ⓖ';
    public static final Character BLUE_ICON = 'Ⓑ';
    public static final Character PURPLE_ICON = 'Ⓤ';
    public static final Character PINK_ICON = 'Ⓟ';
    public static final Character SPECTATOR_ICON = 's';
    public static final Character CROWN_ICON = '④';

    public static final Character RED_ICON_HALLOWEEN = 'Ⓛ';
    public static final Character YELLOW_ICON_HALLOWEEN = 'Ⓜ';
    public static final Character GREEN_ICON_HALLOWEEN = 'ⓖ';
    public static final Character BLUE_ICON_HALLOWEEN = 'Ⓗ';
    public static final Character PURPLE_ICON_HALLOWEEN = 'Ⓥ';
    public static final Character PINK_ICON_HALLOWEEN = 'Ⓕ';

    public static final Character RED_ICON_CHRISTMAS = 'ⓡ';
    public static final Character YELLOW_ICON_CHRISTMAS = 'ⓨ';
    public static final Character GREEN_ICON_CHRISTMAS = 'Ⓐ';
    public static final Character BLUE_ICON_CHRISTMAS = 'ⓑ';
    public static final Character PURPLE_ICON_CHRISTMAS = 'ⓤ';
    public static final Character PINK_ICON_CHRISTMAS = 'ⓟ';

    private static final Map<String, Character> emojis = new HashMap<>(Map.of(
            ":red:", RED_ICON, ":yellow:", YELLOW_ICON, ":green:", GREEN_ICON,
            ":blue:", BLUE_ICON, ":purple:", PURPLE_ICON, ":pink:", PINK_ICON,
            ":w:", CROWN_ICON, ":crown:", CROWN_ICON, ":dub:", CROWN_ICON, ":win:", CROWN_ICON
    ));

    // Movement
    public static final Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public static final Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public static final Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;
    public static final Material SPEED_PAD = Material.OBSERVER;
    public static final double JUMP_BUFFER_ROOM = 0.35;
    public static final Vector ZERO = new Vector(0, 0, 0);

    // SFX
    public static final String SPEED_PAD_SFX = "sfx.speed_pad";
    public static final String JUMP_PAD_SFX = "sfx.green_jump_pad";
    public static final String BOOST_PAD_SFX = "sfx.orange_red_jump_pad";

    // World
    public static final List<Material> BLOCKED_RECIPES = Arrays.asList(Material.WOODEN_AXE, Material.SHIELD, Material.SNOW_BLOCK, Material.FLINT_AND_STEEL);
    /**
     * Alias for Mini Message deserialization; see more detail below
     * <a href="https://docs.papermc.io/paper/dev/component-api/introduction/">Documentation</a>
     * @param message Supports Mini Message Syntax
     * @return Component representation of the provided message
     */
    public static Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component getDisplayName(EventTeam t, String name) {
        return text(t.icon(), NamedTextColor.WHITE).append(text(" " + name, t.textColor()));
    }

    public static Map<String, Character> getEmojis() {
        return Collections.unmodifiableMap(emojis);
    }
}
