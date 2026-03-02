package game.common;

import game.util.Text;

public enum RoomFeature {
    SMALL_TABLE(2,2),
    BIG_TABLE(3, 2),
    WORK_BENCH(3,2),
    TOMB(3,2),
    COFFIN(2,1),
    THRONE(1,1),
    ALTAR(2,1),
    STATUE(1,1),
    LARGE_STATUE(2,2),
    HUGE_STATUE(3,3),
    DESK(2,1),
    SPELL_ALTAR(3,2),
    BOOK_SHELF(3,1),
    STORAGE_CABINET(3,1),
    SMALL_SHELF(3,1),
    BRAZIER(1,1),
    TELEPORTATION_CIRCLE(2,2),
    FOUNTAIN(2,2),
    TRAP_DOOR(1,1),
    DOOR(1,1),
    PIT(2,2),
    PILE_OF_RUBBLE(3,2),
    PILE_OF_TREASURE(2, 2),
    CHEST(1,1),
    TWIN_BED(2,1),
    DOUBLE_BED(3,2),
    STOVE(2,1),
    ANVIL(1,1),
    FORGE(3,2),
    CAMPFIRE(1,1),
    FIREPLACE(3,1),
    BEDROLL(2,1)
    ;

    private int width, height;
    private String title;

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    
    private RoomFeature(final int w, final int h) {
        this.width = w;
        this.height = h;
        this.title = Text.allWordsCapitalized(name());
    }
}
