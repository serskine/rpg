package game.common;

import java.util.Optional;

public class Path {
    public PathDistance distance;
    public Optional<Integer> lockDc;
    public Optional<Integer> stealthDc;
    public String title;

    public Path() {
        distance = PathDistance.MELEE;
        lockDc = Optional.empty();
        stealthDc = Optional.empty();
        title = "Path";
    }

    public Path(final String title) {
        this();
        this.title = title;
    }

}
