package game.common;

public enum PathDistance {
    MELEE(15),
    SHORT(30),
    FAR(60),
    VERY_FAR(120);

    public final int minimum;

    private PathDistance(final int factor) {
        this.minimum = factor;
    }
}
