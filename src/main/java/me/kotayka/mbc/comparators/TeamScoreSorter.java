package me.kotayka.mbc.comparators;

import me.kotayka.mbc.MBCTeam;

import java.util.Comparator;

public class TeamScoreSorter implements Comparator<MBCTeam> {
    public TeamScoreSorter() {}

    public int compare(MBCTeam a, MBCTeam b)
    {
        if (a.getMultipliedTotalScore() == b.getMultipliedTotalScore()) {
            if (a.getRawTotalScore() != b.getRawTotalScore()) {
                // compare by unmultiplied score before colors; this will be the tiebreaker for Dodgebolt as well
                return (a.getRawTotalScore() - b.getRawTotalScore());
            }

                // compare colors
            return b.getSortID() - a.getSortID();
        }
        return (int) (a.getMultipliedTotalScore() - b.getMultipliedTotalScore()); // reverse so bigger numbers are at the top when sorted
    }
}
