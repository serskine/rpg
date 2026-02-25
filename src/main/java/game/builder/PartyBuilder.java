package game.builder;

import game.common.Creature;
import game.common.Job;
import game.common.Party;
import game.util.Func;
import game.common.Rarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static game.util.Func.*;

public class PartyBuilder {

    private final CreatureBuilder creatureBuilder = new CreatureBuilder();

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

    private Set<Job> selectGroupTypes(final int numGroups) {
        final Set<Job> jobs = new HashSet<>();
        while(jobs.size() < numGroups) {
            jobs.add(chooseFromRandomly(Job.values()));
        }
        return jobs;
    }

    private List<Creature> buildGroup(final int groupSize, Creature template) {
        final List<Creature> creatures = new ArrayList<>();
        for(int i=0; i<groupSize; i++) {
            final Creature creature = template.copy();
            creature.title = creature.title + "-" + (i+1);
            creatures.add(creature);
        }
        return creatures;
    }



    private final List<Creature> buildCreatureList(final int numPc, final int partyLevel) { return buildCreatureList(numPc, partyLevel, Func.rollRarity()); }

    private final List<Creature> buildCreatureList(final int numPc, final int partyLevel, final Rarity difficulty) {
        final int numGroups = expectedNumberGroups(difficulty);
        final List<Creature> creatures = new ArrayList<>();

        for(int i=0; i<numGroups; i++) {

            final Set<Job> jobs = selectGroupTypes(numGroups);
            for(Job job : jobs) {
                final Creature template = creatureBuilder.buildCreature(partyLevel, difficulty, job);
                final List<Creature> group = buildGroup(numPc, template);
                creatures.addAll(group);
            }
        }
        return creatures;
    }

    public final Party build(final String title, final int numPc, final int partyLevel) {
        final Party party = new Party(title);
        party.creatures.addAll(buildCreatureList(numPc, partyLevel));
        return party;
    }

}
