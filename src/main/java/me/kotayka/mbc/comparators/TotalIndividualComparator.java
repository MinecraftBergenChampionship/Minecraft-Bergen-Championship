package me.kotayka.mbc.comparators;

import me.kotayka.mbc.Participant;

import java.util.Comparator;

public class TotalIndividualComparator implements Comparator<Participant> {
    @Override
    public int compare(Participant o1, Participant o2) {
        if (o1.getRawTotalScore() == o2.getRawTotalScore()) {
            if (o1.getMultipliedTotalScore() != o2.getMultipliedTotalScore()) {
                return (int) (o2.getMultipliedTotalScore() - o1.getMultipliedTotalScore());
            }

            return o1.getTeam().getSortID() - o2.getTeam().getSortID();
        }
        return o2.getRawTotalScore() - o1.getRawTotalScore();
    }
}