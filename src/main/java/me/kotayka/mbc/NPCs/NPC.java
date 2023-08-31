package me.kotayka.mbc.NPCs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kotayka.mbc.Participant;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NPC {

    private JavaPlugin plugin;
    private UUID uuid = UUID.randomUUID();

    private String name;
    private String playerName;
    private String skin;
    private Location location;

    private List<Participant> viewers = new ArrayList<>();

    public NPC(JavaPlugin plugin, String name, String playerName, String skin, Location loc) {
        this.plugin = plugin;
        this.name = name;
        this.playerName = playerName;
        this.skin = skin;
        this.location = loc;

        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) this.location.getWorld()).getHandle();
        GameProfile gameProfile = new GameProfile(this.uuid, this.playerName);

        gameProfile.getProperties().put("textures", new Property("textures", "textures", "signature"));


        EntityPlayer npc = new EntityPlayer(minecraftServer, worldServer, gameProfile);
    }

}
