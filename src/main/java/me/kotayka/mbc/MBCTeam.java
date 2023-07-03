package me.kotayka.mbc;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class MBCTeam {
    protected String name;
    protected String fullName;
    protected Character icon;
    protected ChatColor chatColor;
    protected Color color;
    protected Team scoreboardTeam;

    private int rawTotalScore = 0;
    private int multipliedTotalScore = 0;
    private int rawCurrentScore = 0;
    private int multipliedCurrentScore = 0;

    public MBCTeam(String name, String fullName, Character icon, ChatColor chatColor) {
        this.name = name;
        this.fullName = fullName;
        this.icon = icon;
        this.chatColor = chatColor;
        switch(chatColor) {
            case RED:
                this.color = Color.RED;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Red Rabbits");
                break;
            case GREEN:
                this.color = Color.GREEN;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Green Guardians");
                break;
            case YELLOW:
                this.color = Color.YELLOW;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Yellow Yaks");
                break;
            case BLUE:
                this.color = Color.BLUE;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Blue Bats");
                break;
            case DARK_PURPLE:
                this.color = Color.PURPLE;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Purple Pandas");
                break;
            case LIGHT_PURPLE:
                this.color = Color.fromRGB(243, 139, 170);
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Pink Piglets");
                break;
            default:
                this.color = Color.WHITE;
                scoreboardTeam = Bukkit.getScoreboardManager().getNewScoreboard().registerNewTeam("Spectators");
                break;
        }
        scoreboardTeam.setAllowFriendlyFire(false); // keeping scoreboard teams in just for this for now
                                                    // other possibilities like glowing for the future
    }

    public List<Participant>teamPlayers = new ArrayList<>(4);

    public int getMultipliedTotalScore() {
        return multipliedTotalScore;
    }

    public int getRawTotalScore() {
        return rawTotalScore;
    }

    public int getMultipliedCurrentScore() {
        return multipliedCurrentScore;
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
        scoreboardTeam.addEntity(p.getPlayer());
    }
    public List<Participant> getPlayers() { return teamPlayers; }
    public void removePlayer(Participant p) {
        teamPlayers.remove(p);
        scoreboardTeam.removeEntity(p.getPlayer());
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

    public static MBCTeam getTeam(String team) {
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

    /**
     * Adds specified amount to current team score
     * Calls updateInGameTeamScoreboard()
     * @param score amount to increment team's score
     * @see Game updateInGameTeamScoreboard()
     */
    public void addCurrentTeamScore(int score) {
        rawCurrentScore +=score;
        multipliedCurrentScore +=score*MBC.getInstance().multiplier;
        MBC.getInstance().currentGame.updateInGameTeamScoreboard();
    }

    /**
     * Adds specified amount to total team score; called per player
     * @param score amount to increment team's score
     * @see Game updateTeamStandings()
     * @see MBCTeam addCurrentScoreToTotal()
     */
    private void addTotalTeamScore(int score) {
        this.rawTotalScore +=score;
        multipliedTotalScore +=score*MBC.getInstance().multiplier;
        MBC.getInstance().currentGame.updateTeamStandings();
    }

    /**
     * Transfers the current raw score to total score, resets total score after.
     * Called per player.
     * @see Game updateTeamStandings()
     */
    public void addCurrentScoreToTotal() {
        addTotalTeamScore(rawCurrentScore);
        resetCurrentScores();
    }

    private void resetCurrentScores() {
        rawCurrentScore = 0;
        multipliedCurrentScore = 0;
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
        // small delay to sync messages correctly
        Bukkit.getScheduler().scheduleSyncDelayedTask(MBC.getInstance().plugin, new Runnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(teamNameFormat() + " have been eliminated!");
            }
        }, 10L);
    }
}

class TeamUnMultipliedScoreSorter implements Comparator<MBCTeam> {

    public int compare(MBCTeam a, MBCTeam b)
    {
        return a.getRawTotalScore() - b.getRawTotalScore();
    }
}

class TeamScoreSorter implements Comparator<MBCTeam> {
    public TeamScoreSorter() {}

    public int compare(MBCTeam a, MBCTeam b)
    {
        return a.getMultipliedTotalScore() - b.getMultipliedTotalScore();
    }
}

class TeamRoundSorter implements Comparator<MBCTeam> {
    public TeamRoundSorter() {}

    public int compare(MBCTeam a, MBCTeam b)
    {
        return a.getMultipliedCurrentScore() - b.getMultipliedCurrentScore();
    }
}