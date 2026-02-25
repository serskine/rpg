package game.common;

import java.util.ArrayList;
import java.util.List;

public class Party {
    public final List<Creature> creatures = new ArrayList<>();
    public final String title;

    public Party(final String title) {
        this.title = title;
    }

    public void setPartyAlignment(final Alignment alignment) {
        creatures.forEach(c -> c.alignment = alignment);
    }
}
