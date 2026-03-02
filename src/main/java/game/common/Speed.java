package game.common;

public enum Speed implements Comparable<Speed> {
    IMMOBILE(0),
    SLOW(1),
    FAST(2),
    VERY_FAST(4);

    public final int factor;

    private Speed(final int factor) {
        this.factor = factor;
    }


}
