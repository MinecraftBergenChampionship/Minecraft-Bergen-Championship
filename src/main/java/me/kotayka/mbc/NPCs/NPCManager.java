package me.kotayka.mbc.NPCs;

import me.kotayka.mbc.Participant;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class NPCManager {
    public final JavaPlugin plugin;
    public static ArrayList<NPC> npcs = new ArrayList<>();

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public NPC createNPC(Player player, Location loc) {
        Participant p = Participant.getParticipant(player);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, p.getFormattedName());
        npcs.add(npc);

        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(player.getName());

        npc.spawn(loc);
        return npc;
    }

    public void remove(NPC npc) {
        CitizensAPI.getNPCRegistry().deregister(npc);
    }

    public void removeAllNPCs() {
        CitizensAPI.getNPCRegistry().deregisterAll();
    }
}