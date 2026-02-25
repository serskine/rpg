package game.builder;

import game.common.Path;
import game.common.Rarity;

import java.util.Optional;

import static game.util.Func.*;

public class PathBuilder {

    public int expectedDistanceFor(final Rarity rarity) {
        switch(rarity) {
            case UNCOMMON -> {
                return 30 + d(6) * 5;
            }
            case RARE -> {
                return 60 + d(12) * 5;
            }
            default -> {
                return d(6) * 5;
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
