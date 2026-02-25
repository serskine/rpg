package game.unit;

import java.util.Optional;

import static game.util.Func.*;

public class Creature {
    public int damage;
    public int movement;
    public int level;
    public final Job job;

    private Optional<Integer> stealthDc = Optional.empty();

    public Creature(final Job job, final int level) {
        this.level = level;
        this.job = job;
        this.movement = speed();
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
        return (level-1) / 4 + 2;
    }

    public final int hd() {
        return modifier(body())  * 2 + 2;
    }

    public final int maxHp() {
        return hd() + (hd() - hd()/2) * (level-1) + modifier(constitution()) * level;
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
        return (strength() + constitution()) * 5;
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

    public Creature copy() {
        final Creature copy = new Creature(job, level);
        copy.damage = damage;
        copy.movement = movement;
        copy.stealthDc = stealthDc;
        return copy;
    }
    
}
