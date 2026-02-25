package game.builder;

import game.common.Alignment;
import game.common.Party;
import game.common.Rarity;

import java.util.ArrayList;
import java.util.List;

import static game.util.Func.*;

public class EncounterBuilder {

    private final PartyBuilder partyBuilder = new PartyBuilder();

    private int expectedNumberOfParties(final Rarity encounterSize) {
        switch(encounterSize) {
            case RARE -> {
                return d(3) + 3;
            }
            case UNCOMMON -> {
                return d(2) + 1;
            }
            default -> {
                return 2;
            }
        }
    }

    public List<Party> build(final int avgNumPc, final int avgPartyLevel) {
        return build(avgNumPc, avgPartyLevel, expectedNumberOfParties(rollRarity()));
    }

    public List<Party> build(final int avgNumPc, final int avgPartyLevel, final int numParties) {
        final List<Party> parties = new ArrayList<>();

        for(int i=0; i<numParties; i++) {
            final String title = "Party " + (i+1);
            final Party party = partyBuilder.build(title, avgNumPc, avgPartyLevel);
            parties.add(party);
        }

        return parties;
    }
}
