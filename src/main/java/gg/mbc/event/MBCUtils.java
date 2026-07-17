package gg.mbc.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;

/**
 * Utility class for MBC Events.
 */
public final class MBCUtils {
    public static final Material BOOST_PAD = Material.WAXED_EXPOSED_CUT_COPPER;
    public static final Material MEGA_BOOST_PAD = Material.WAXED_WEATHERED_CUT_COPPER;
    public static final Material JUMP_PAD = Material.WAXED_WEATHERED_COPPER;
    private static final double JUMP_BUFFER_ROOM = 0.35;

    // Resource Pack
    public static final Character RED_ICON = 'Ⓡ';
    public static final Character YELLOW_ICON = 'Ⓨ';
    public static final Character GREEN_ICON = 'Ⓖ';
    public static final Character BLUE_ICON = 'Ⓑ';
    public static final Character PURPLE_ICON = 'Ⓤ';
    public static final Character PINK_ICON = 'Ⓟ';
    public static final Character SPECTATOR_ICON = 's';

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


    /**
     * Alias for Mini Message deserialization; see more detail below
     * <a href="https://docs.papermc.io/paper/dev/component-api/introduction/">Documentation</a>
     * @param message Supports Mini Message Syntax
     * @return Component representation of the provided message
     */
    public static Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}
