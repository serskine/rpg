package game.common;

import java.util.Optional;

import static game.common.Alignment.NEUTRAL;
import static game.common.CreatureSize.MEDIUM;
import static game.util.Func.*;

public class Creature {
    public Job job;
    public int damage;
    public int movement;
    public int level;
    public Alignment alignment;
    public CreatureSize size;
    public String title;

    private Optional<Integer> stealthDc;

    public Creature() {
        this.job = Job.COMMONER;
        this.damage = 0;
        this.movement = speed();
        this.level = 1;
        this.alignment = NEUTRAL;
        this.size = MEDIUM;
        this.title = size.name() + "-" + job.name();
        this.stealthDc = Optional.empty();
    }

    public Creature copy() {
        final Creature copy = new Creature();

        copy.job = this.job;
        copy.damage = this.damage;
        copy.movement = this.movement;
        copy.level = this.level;
        copy.alignment = this.alignment;
        copy.size = this.size;
        copy.title = this.title;
        copy.stealthDc = this.stealthDc;

        return copy;
    }

    public int speed() {
        return modifier(body()) * 5 + 25;
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

    public void dash() {
        movement += speed();
    }

    public int move(final int distance) {
         if (distance > movement) {
             final int actualDistance = movement;
             movement  = 0;
             return actualDistance;
         } else {
             movement -= distance;
             return distance;
         }
    }

    public void resetMovement() {
        movement = speed();
    }

    public int passivePerception() {
        return 10 + modifier(intelligence());
    }

    public int passiveInsight() {
        return 10 + modifier(wisdom());
    }


    
}
