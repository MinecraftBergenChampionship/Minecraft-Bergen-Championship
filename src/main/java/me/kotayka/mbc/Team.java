package me.kotayka.mbc;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class Team {

    protected String name;
    protected String fullName;
    protected Character icon;
    protected ChatColor chatColor;
    protected Color color;

    private int unMultipliedScore = 0;
    private int score = 0;
    private int roundScore = 0;
    private int roundUnMultipliedScore = 0;

    public Team(String name, String fullName, Character icon, ChatColor chatColor) {
        this.name = name;
        this.fullName = fullName;
        this.icon = icon;
        this.chatColor = chatColor;
        switch(chatColor) {
            case RED:
                this.color = Color.RED;
                break;
            case GREEN:
                this.color = Color.GREEN;
                break;
            case YELLOW:
                this.color = Color.YELLOW;
                break;
            case BLUE:
                this.color = Color.BLUE;
                break;
            case DARK_PURPLE:
                this.color = Color.PURPLE;
                break;
            case LIGHT_PURPLE:
                this.color = Color.fromRGB(243, 139, 170);
                break;
            default:
                this.color = Color.WHITE;
                break;
        }
    }

    public List<Participant> teamPlayers = new ArrayList<>(4);

    public int getScore() {
        return score;
    }

    public int getUnMultipliedScore() {
        return unMultipliedScore;
    }

    public int getRoundScore() {
        return roundScore;
    }

    public String getTeamName() {
        return name;
    }

    public String getTeamFullName() {
        return fullName;
    }

    public Character getIcon() {
        return icon;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Color getColor() { return color; }

    public void addPlayer(Participant p) {
        teamPlayers.add(p);
    }

    public void removePlayer(Participant p) {
        teamPlayers.remove(p);
    }

    /**
     *
     * @param leatherArmor uncolored leather armor
     * @return colored leather armor
     */
    public ItemStack getColoredLeatherArmor(ItemStack leatherArmor) {
        try {
            LeatherArmorMeta meta = (LeatherArmorMeta) leatherArmor.getItemMeta();
            assert meta != null;
            meta.setColor(color);
            meta.setUnbreakable(true);
            leatherArmor.setItemMeta(meta);
            return leatherArmor;
        } catch (ClassCastException e) {
            Bukkit.broadcastMessage("Passed Item Stack was not leather armor!");
            return leatherArmor;
        }
    }

    public ItemStack getColoredWool() {
        switch (getChatColor()) {
            case RED:
                return new ItemStack(Material.RED_WOOL);
            case YELLOW:
                return new ItemStack(Material.YELLOW_WOOL);
            case GREEN:
                return new ItemStack(Material.GREEN_WOOL);
            case BLUE:
                return new ItemStack(Material.BLUE_WOOL);
            case DARK_PURPLE:
                return new ItemStack(Material.PURPLE_WOOL);
            case LIGHT_PURPLE:
                return new ItemStack(Material.PINK_WOOL);
            default:
                return new ItemStack(Material.WHITE_WOOL);
        }
    }

    public ItemStack getConcrete() {
        return switch (getChatColor()) {
            case RED -> new ItemStack(Material.RED_CONCRETE);
            case YELLOW -> new ItemStack(Material.YELLOW_CONCRETE);
            case GREEN -> new ItemStack(Material.GREEN_CONCRETE);
            case BLUE -> new ItemStack(Material.BLUE_CONCRETE);
            case DARK_PURPLE -> new ItemStack(Material.PURPLE_CONCRETE);
            case LIGHT_PURPLE -> new ItemStack(Material.PINK_CONCRETE);
            default -> new ItemStack(Material.WHITE_CONCRETE);
        };
    }

    public static Team getTeam(String team) {
        switch (team.toLowerCase()) {
            case "redrabbits":
            case "redrabbit":
            case "red":
                return MBC.getInstance().red;
            case "yellowyaks":
            case "yellowyak":
            case "yellow":
                return MBC.getInstance().yellow;
            case "greenguardians":
            case "greenguardian":
            case "green":
                return MBC.getInstance().green;
            case "bluebats":
            case "bluebat":
            case "blue":
                return MBC.getInstance().blue;
            case "purplepandas":
            case "purplepanda":
            case "purple":
                return MBC.getInstance().purple;
            case "pinkpiglets":
            case "pinkpiglet":
            case "pink":
                return MBC.getInstance().pink;
            case "spectator":
            case "spectators":
            case "spec":
                return MBC.getInstance().spectator;
            default:
                return null;
        }
    }

    public void addRoundScore(int score) {
        roundScore+=score;
        roundUnMultipliedScore+=score*MBC.getInstance().multiplier;
        MBC.getInstance().currentGame.updateTeamRoundScore(this);
    }

    public void addGameScore(int score) {
        this.score+=score;
        unMultipliedScore+=score*MBC.getInstance().multiplier;
        MBC.getInstance().currentGame.updateTeamGameScore(this);
    }

    /**
     * @return Formatted team name with icon and color.
     * No hanging space.
     */
    public String teamNameFormat() {
        return getIcon() + " " + this.chatColor + getTeamFullName() + ChatColor.WHITE;
    }

    /**
     * Announces team death.
     * TODO: maybe a sound effect
     */
    public void announceTeamDeath() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(teamNameFormat() + " have been eliminated!");
            }
        }, 10L);
    }
}

class TeamUnMultipliedScoreSorter implements Comparator<Team> {

    public int compare(Team a, Team b)
    {
        return a.getUnMultipliedScore() - b.getUnMultipliedScore();
    }
}

class TeamScoreSorter implements Comparator<Team> {
    public TeamScoreSorter() {}

    public int compare(Team a, Team b)
    {
        return a.getScore() - b.getScore();
    }
}

class TeamRoundSorter implements Comparator<Team> {
    public TeamRoundSorter() {}

    public int compare(Team a, Team b)
    {
        return a.getRoundScore() - b.getRoundScore();
    }
}