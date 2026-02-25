package game.unit;

import java.util.Arrays;
import java.util.Set;

public enum Job {
    ARTIFICER(13, 14, 12, 15, 8, 10, true),
    BARBARIAN(15, 13, 14, 8, 10, 12, false),
    BARD(10, 14, 12, 13, 8, 15, true),
    CLERIC(13, 8, 14, 10, 15, 12, true),
    DRUID(12, 13, 14, 10, 15, 8, true),
    FIGHTER(15, 14, 13, 8, 12, 10, false),
    MONK(13, 14, 10, 8, 15, 12, false),
    PALADIN(15, 10, 13, 8, 12, 14, true),
    RANGER(13, 15, 12, 10, 14, 8, true),
    ROGUE(8, 15, 10, 14, 12, 13, false),
    SORCERER(8, 14, 12, 13, 10, 15, true),
    WARLOCK(12, 14, 13, 10, 8, 15, true),
    WIZARD(8, 12, 10, 15, 14, 3, true),
    SICKLY(8, 8, 8, 8, 8, 8, false),

    // These are common jobs for monsters
    COMMONER(10, 10, 10, 10, 10, 10, false),
    MINION(12, 12, 12, 12, 12, 12, false),
    TOUGH(14, 14, 14, 14, 14, 14, false),
    ELITE(16, 16, 16, 16, 16, 16, false),
    BOSS(18, 18, 18, 18, 18, 18, true),
    DIETY(20, 20, 20, 20, 20, 20, true)


    ;

    public final int stremgth, dexterity, constitution, intelligence, wisdom, charisma;
    public final boolean isCaster;

    private Job(
            final int strength,
            final int dexterity,
            final int constitution,
            final int intelligence,
            final int wisdom,
            final int charisma,
            final boolean isCaster
    ) {
        this.stremgth = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.isCaster = isCaster;
    }

    public static Job[] getNpcJobs() {
        return new Job[]{COMMONER, MINION, TOUGH, ELITE, BOSS, DIETY};
    }

    public static Job[] getPcJobs() {
        return new Job[]{ARTIFICER, BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WARLOCK, WIZARD};
    }

}
