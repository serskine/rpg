package game.world;

import java.util.Optional;

public class Path {
    public final int distance;
    public final Optional<Integer> lockDc;
    public final int stealthDc;
    public final String title;
    public final String description;

    public Path(final int distance, final Optional<Integer> lockDc, final int stealthDc, final String title, final String description) {
        this.distance = distance;
        this.lockDc = lockDc;
        this.stealthDc = stealthDc;
        this.title = title;
        this.description = description;
    }

}
