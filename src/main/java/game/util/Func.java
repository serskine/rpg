package game.util;

import game.common.Rarity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Func {

    public static final int d(final int size) {
        return (int) (Math.random() * size + 1);
    }

    public static final int[] pool(final int... sizes) {
        final int[] pool = new int[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            pool[i] = d(sizes[i]);
        }
        return pool;
    }

    public static final int sum(int... nums) {
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        return sum;
    }

    public static final int min(int... nums) {
        int min = nums[0];
        for (int i=1; i<nums.length; i++) {
            if (nums[i] < min) {
                min = nums[i];
            }
        }
        return min;
    }

    public static final int max(int... nums) {
        int max = nums[0];
        for (int i=1; i<nums.length; i++) {
            if (nums[i] > max) {
                max = nums[i];
            }
        }
        return max;
    }

    public static final int[] roll(final int num, final int size) {
        final int[] rolls = new int[num];
        for (int i = 0; i < num; i++) {
            rolls[i] = d(size);
        }
        return rolls;
    }

    public static final int sumRoll(final int num, final int size) {
        return sum(roll(num, size));
    }

    public static final int advantage(final int num, final int size) {
        return max(roll(num, size));
    }

    public static final int disadvantage(final int num, final int size) {
        return min(roll(num, size));
    }

    public static final Rarity rollRarity() {
        final int roll = d(6);
        if (roll <= 3) {
            return Rarity.COMMON;
        } else if (roll <= 5) {
            return Rarity.UNCOMMON;
        } else {
            return Rarity.RARE;
        }
    }

    public static final int modifier(final int score) {
        return (score - 10) / 2;
    }

    public static <T> T chooseFromRandomly(T... items) {
        return items[d(items.length) - 1];
    }

    public static <T> T chooseFromRandomly(final List<T> items) {
        return items.get(d(items.size()) - 1);
    }

    public static <T> T chooseFromRandomly(final Collection<T> items) {
        if (items instanceof List<T>) {
            return chooseFromRandomly((List<T>) items);
        } else {
            final int index = d(items.size()) - 1;
            return items.stream().skip(index).findFirst().orElseThrow();
        }
    }

}
