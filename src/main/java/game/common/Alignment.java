package game.common;

public enum Alignment {
    LAWFUL_GOOD,
    NEUTRAL_GOOD,
    CHAOTIC_GOOD,
    LAWFUL_NEUTRAL,
    NEUTRAL,
    CHAOTIC_NEUTRAL,
    LAWFUL_EVIL,
    NEUTRAL_EVIL,
    CHAOTIC_EVIL;

    public final boolean isSelfless() {
        return this == LAWFUL_GOOD || this == NEUTRAL_GOOD || this == CHAOTIC_GOOD;
    }

    public final boolean isSelfish() {
        return this == LAWFUL_EVIL || this == NEUTRAL_EVIL || this == CHAOTIC_EVIL;
    }

    public final boolean isOrderly() {
        return this == LAWFUL_GOOD || this == LAWFUL_NEUTRAL || this == LAWFUL_EVIL;
    }

    public final boolean isChaotic() {
        return this == CHAOTIC_GOOD || this == CHAOTIC_NEUTRAL || this == CHAOTIC_EVIL;
    }
}
