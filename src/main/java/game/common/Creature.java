package game.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static game.common.Alignment.NEUTRAL;
import static game.common.CreatureSize.MEDIUM;
import static game.common.Speed.IMMOBILE;
import static game.util.Func.*;
import static game.util.Text.capitalized;

public class Creature {
    public Job job;
    public int damage;
    public int level;
    public Alignment alignment;
    public CreatureSize size;
    public String title;
    private final Map<MovementType, Speed> movementMap;
    private int distanceTravelled = 0;

    private Optional<Integer> stealthDc;

    public Creature() {
        this.job = Job.COMMONER;
        this.damage = 0;
        this.level = 1;
        this.alignment = NEUTRAL;
        this.size = MEDIUM;
        this.title = size.name() + "-" + job.name();
        this.stealthDc = Optional.empty();
        this.movementMap = new HashMap<>();
        this.distanceTravelled = 0;
    }

    public Creature copy() {
        final Creature copy = new Creature();

        copy.job = this.job;
        copy.damage = this.damage;
        copy.distanceTravelled = this.distanceTravelled;
        copy.level = this.level;
        copy.alignment = this.alignment;
        copy.size = this.size;
        copy.title = this.title;
        copy.stealthDc = this.stealthDc;

        return copy;
    }

    public Speed speed(final MovementType movementType) {
        return movementMap.getOrDefault(movementType, IMMOBILE);
    }

    public int hp() {
        return maxHp() - damage;
    }

    public final int strength()     {   return job.stremgth;        }
    public final int dexterity()    {   return job.dexterity;       }
    public final int constitution() {   return job.constitution;    }
    public final int intelligence() {   return job.intelligence;    }
    public final int wisdom()       {   return job.wisdom;          }
    public final int charisma()     {   return job.charisma;        }


    public final int body() {
        return (strength() + constitution() + dexterity()) / 3;
    }

    public final int mind() {
        return (intelligence() + wisdom() + charisma()) / 3;
    }

    public final int prof() {
        return (level -1) / 4 + 2;
    }

    public final int hd() {
        return modifier(body())  * 2 + 2;
    }

    public final int maxHp() {
        return hd() + (hd() - hd()/2) * (level -1) + modifier(constitution()) * level;
    }

    public final int ac() {
        return 10 + modifier(dexterity());
    }

    public final int carry() {
        return strength() * 15;
    }

    public final int drag() {
        return carry() * 2;
    }

    public final int bodyWeight() {

        final int mediumWeight = (strength() + constitution()) * 5;
        int calculatedWeight;
        switch(size) {
            case TINY -> {
                calculatedWeight = mediumWeight / 4;
            }
            case SMALL -> {
                calculatedWeight = mediumWeight / 2;
            }
            case LARGE -> {
                calculatedWeight = mediumWeight * 4;
            }
            case HUGE -> {
                calculatedWeight = mediumWeight * 9;
            }
            case GARGANTUAN -> {
                calculatedWeight = mediumWeight * 16;
            }
            default -> {
                calculatedWeight = mediumWeight;
            }
        }
        return max(1, calculatedWeight);    // Minimum of one pound.
    }

    public final Optional<Integer> spellDc() {
        if (job.isCaster) {
            return Optional.of(8 + prof() + modifier(mind()));
        } else {
            return Optional.empty();
        }
    }

    public final Optional<Integer> spellAttack() {
        if (job.isCaster) {
            return Optional.of(prof() + modifier(mind()));
        } else {
            return Optional.empty();
        }
    }

    public final int meleeAttack() {
        return prof() + modifier(max(strength(), (strength() + dexterity()) / 2));
    }

    public final int rangedAttack() {
        return prof() + modifier(max(dexterity(), (strength() + dexterity()) / 2));
    }

    public void hide() {
        stealthDc = Optional.of(d(20) + modifier(dexterity()));
    }

    public void resetMovement() {
        distanceTravelled = 0;
    }

    public boolean canMove(final PathDistance pathDistance, final MovementType movementType) {
        if (!movementMap.containsKey(movementType)) {
            return false;
        } else {

            final Speed speed = movementMap.get(movementType);
            final int movementRemaining = speed.factor - distanceTravelled;
            return (movementRemaining >= pathDistance.minimum);
        }
    }

    public boolean move(final PathDistance pathDistance, final MovementType movementType) {
        if (canMove(pathDistance, movementType)) {
            distanceTravelled += pathDistance.minimum;
            return true;
        } else {
            return false;
        }
    }

    public int passivePerception() {
        return 10 + modifier(intelligence());
    }

    public int passiveInsight() {
        return 10 + modifier(wisdom());
    }

    @Override
    public String toString() {
        return capitalized(title)
                + " (Level " + level + " "
                + ((size == MEDIUM) ? "" : capitalized(size.name())) + " "
                + capitalized(job.name()) + ")";
    }
    
}
