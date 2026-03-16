package game.builder;

import game.common.Path;
import game.common.PathDistance;
import game.common.Rarity;

import java.util.Optional;

import static game.util.Func.*;

public class PathBuilder {

    public PathDistance expectedDistanceFor(final Rarity rarity) {
        switch(rarity) {
            case UNCOMMON -> {
                return PathDistance.MELEE;
            }
            case RARE -> {
                return PathDistance.FAR;
            }
            default -> {
                return PathDistance.SHORT;
            }
        }
    }

    public Optional<Integer> expectedOptionalDcFor(final Rarity rarity) {
        switch(rarity) {
            case UNCOMMON -> {
                return Optional.of(10 + d(6));
            }
            case RARE -> {
                return Optional.of(15 + d(12));
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    public Path build(final String title) {
        final Path path = new Path(title);

        path.distance = expectedDistanceFor(rollRarity());
        path.stealthDc = expectedOptionalDcFor(rollRarity());
        path.lockDc = expectedOptionalDcFor(rollRarity());
        path.title = title;

        return path;
    }
}
