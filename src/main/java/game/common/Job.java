package game.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static game.common.Rarity.*;

public enum Job {
    ARTIFICER(13, 14, 12, 15, 8, 10, true, RARE),
    BARBARIAN(15, 13, 14, 8, 10, 12, false, COMMON),
    BARD(10, 14, 12, 13, 8, 15, true, COMMON),
    CLERIC(13, 8, 14, 10, 15, 12, true, COMMON),
    DRUID(12, 13, 14, 10, 15, 8, true, UNCOMMON),
    FIGHTER(15, 14, 13, 8, 12, 10, false, COMMON),
    MONK(13, 14, 10, 8, 15, 12, false, UNCOMMON),
    PALADIN(15, 10, 13, 8, 12, 14, true, COMMON),
    RANGER(13, 15, 12, 10, 14, 8, true, COMMON),
    ROGUE(8, 15, 10, 14, 12, 13, false, COMMON),
    SORCERER(8, 14, 12, 13, 10, 15, true, RARE),
    WARLOCK(12, 14, 13, 10, 8, 15, true, RARE),
    WIZARD(8, 12, 10, 15, 14, 3, true, RARE),
    SICKLY(8, 8, 8, 8, 8, 8, false, COMMON),

    // These are common jobs for monsters
    COMMONER(10, 10, 10, 10, 10, 10, false, COMMON),
    MINION(12, 12, 12, 12, 12, 12, false, COMMON),
    TOUGH(14, 14, 14, 14, 14, 14, false, COMMON),
    ELITE(16, 16, 16, 16, 16, 16, false, UNCOMMON),
    BOSS(18, 18, 18, 18, 18, 18, true, UNCOMMON),
    DIETY(20, 20, 20, 20, 20, 20, true, RARE)
    ;

    public final int stremgth, dexterity, constitution, intelligence, wisdom, charisma;
    public final boolean isCaster;
    public final Rarity rarity;

    private Job(
            final int strength,
            final int dexterity,
            final int constitution,
            final int intelligence,
            final int wisdom,
            final int charisma,
            final boolean isCaster,
            final Rarity rarity
    ) {
        this.stremgth = strength;
        this.dexterity = dexterity;
        this.constitution = constitution;
        this.intelligence = intelligence;
        this.wisdom = wisdom;
        this.charisma = charisma;
        this.isCaster = isCaster;
        this.rarity = rarity;
    }

    public static Job[] getNpcJobs() {
        return new Job[]{COMMONER, MINION, TOUGH, ELITE, BOSS, DIETY};
    }

    public static Job[] getPcJobs() {
        return new Job[]{ARTIFICER, BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WARLOCK, WIZARD};
    }

    public static Job[] getRarePcJobs() {
        return new Job[]{ARTIFICER, WIZARD, SORCERER};
    }

    public static Job[] getUncommonPcJobs() {
        return new Job[]{DRUID, MONK, WARLOCK};
    }

    public static Job[] getCommonPcJobs() {
        return new Job[]{BARBARIAN, BARD, CLERIC, FIGHTER, PALADIN, RANGER, ROGUE};
    }


    public static Map<Rarity, List<Job>> getWeightedClassMap() {

        final Map<Rarity, List<Job>> map = new HashMap<>();
        for(Rarity rarity : Rarity.values()) {
            map.put(rarity, new ArrayList());
        }

        for (Job job : values()) {
            map.get(job.rarity).add(job);
        }
        return map;
    }
}
