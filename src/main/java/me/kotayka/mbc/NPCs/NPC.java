package me.kotayka.mbc.NPCs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kotayka.mbc.Participant;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NPC {

    private JavaPlugin plugin;
    private Location location;
    private Player player;
    private List<Player> viewers = new ArrayList<>();
    private EntityPlayer npc;
    private int entityID;

    public NPC(JavaPlugin plugin, Player player, Location loc) {
        this.plugin = plugin;
        this.player = player;
        this.location = loc;
    }

    public void create() {
        this.create("NPC");
    }

    public void create(String name) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) Objects.requireNonNull((player).getWorld())).getHandle();

        String[] things = getRequest(player.getUniqueId().toString());

        String texture = things[0];
        String signature = things[1];

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);

        gameProfile.getProperties().put("textures", new Property("textures", texture, signature));

        EntityPlayer npcPlayer = new EntityPlayer(server, world, gameProfile);
        Location npcLocation = this.location;
        npcPlayer.b(npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(), npcLocation.getYaw(), npcLocation.getPitch());

        this.entityID = npcPlayer.getBukkitEntity().getEntityId();
        this.npc = npcPlayer;
    }

    public void show(Player p) {
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
        connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, this.npc));
        connection.a(new PacketPlayOutNamedEntitySpawn(this.npc));
        connection.a(new PacketPlayOutEntityHeadRotation(this.npc, (byte) (this.location.getYaw() * 256 / 360)));

        EntityPlayer npc = this.npc;

        viewers.add(p);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, npc));
            }
        }, 100);
    }

    public void remove(Player p) {
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
        connection.a(new PacketPlayOutEntityDestroy(this.npc.ae()));

        this.viewers.remove(p);
    }

    public String[] getRequest(String uuid)  {
        try {
            URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
            JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            String signature = textureProperty.get("signature").getAsString();

            return new String[] {texture, signature};
        } catch (IOException e) {
            System.err.println("Could not get skin data from session servers!");
            e.printStackTrace();
            return null;
        }
    }

}
