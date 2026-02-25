package game.unit;

import game.util.Func;
import game.world.Rarity;

import java.util.ArrayList;
import java.util.List;

import static game.util.Func.*;

public class EncounterBuilder {

    private int expectedLevelFor(final int partyLevel, Rarity rarity) {
        switch(rarity) {
            case RARE -> {
                return max(1, partyLevel + 2);
            }
            case UNCOMMON -> {
                return max(1, partyLevel);
            }
            default -> {
                return max(1, partyLevel - 1);
            }
        }
    }

    private int expectedNumberGroups(final Rarity encounterSize) {
        switch (encounterSize) {
            case RARE -> {
                return 3;
            }
            case UNCOMMON -> {
                return 2;
            }
            default -> {
                return 1;
            }
        }
    }

    public final Creature nextCreature(final int partyLevel, final Rarity difficulty, final Job... availableJobs) {
        final int cr = expectedLevelFor(partyLevel, difficulty);
        final Job job = chooseFromRandomly(availableJobs);
        return new Creature(job, cr);
    }

    public List<Creature> nextEncounter(final int numPc, final int partyLevel) { return nextEncounter(numPc, partyLevel, Func.rollRarity()); }

    public List<Creature> nextEncounter(final int numPc, final int partyLevel, final Rarity difficulty) {
        final int numGroups = expectedNumberGroups(rollRarity());
        final int cr = expectedLevelFor(partyLevel, difficulty);
        final List<Creature> creatures = new ArrayList<>();

        return creatures;

    }


}
