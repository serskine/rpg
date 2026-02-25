package game.builder;

import game.common.*;

import java.util.List;
import java.util.Map;

import static game.common.CreatureSize.*;
import static game.util.Func.*;

public class CreatureBuilder {

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

    private CreatureSize expectedCreatureSizeFor(Rarity rarity) {
        switch(rarity) {
            case RARE -> {
                return LARGE;
            }
            case UNCOMMON -> {
                return SMALL;
            }
            default -> {
                return MEDIUM;
            }
        }
    }

    private Job[] expectedAvailableJobsFor(Rarity rarity) {
        final Map<Rarity, List<Job>> jobMap = Job.getWeightedClassMap();
        final List<Job> availableJobs = jobMap.get(rarity);
        return availableJobs.toArray(new Job[availableJobs.size()]);
    }

    public final Creature  buildCreature(final int partyLevel, final Rarity difficulty) {
        final Job[] availableJobs = expectedAvailableJobsFor(rollRarity());
        return buildCreature(partyLevel, difficulty, availableJobs);
    }

    public final Creature buildCreature(final int partyLevel, final Rarity difficulty, final Job... availableJobs) {
        final int level = expectedLevelFor(partyLevel, difficulty);
        final Job job = chooseFromRandomly(availableJobs);
        final Alignment alignment = chooseFromRandomly(Alignment.values());
        final CreatureSize creatureSize = expectedCreatureSizeFor(rollRarity());

        final Creature creature = new Creature();

        creature.level = level;
        creature.job = job;
        creature.alignment = alignment;
        creature.size = creatureSize;
        creature.title = alignment.name() + " " + creatureSize.name() + " " + job.name();

        return creature;
    }
}
