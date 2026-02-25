package game.common;

import java.util.Optional;

public class Path {
    public int distance;
    public Optional<Integer> lockDc;
    public Optional<Integer> stealthDc;
    public String title;

    public Path() {
        distance = 30;
        lockDc = Optional.empty();
        stealthDc = Optional.empty();
        title = "Path";
    }

    public Path(final String title) {
        this();
        this.title = title;
    }

}
