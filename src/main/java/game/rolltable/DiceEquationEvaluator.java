package game.rolltable;

import game.util.Func;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Parses and evaluates dice roll equations like "2d20 + 1d6 + 2".
 * Supports: NdS (roll N dice with S sides and sum), +/- modifiers.
 */
public class DiceEquationEvaluator {
    private static final Pattern DICE_PATTERN = Pattern.compile("(\\d+)d(\\d+)");
    private static final Pattern MODIFIER_PATTERN = Pattern.compile("[+-]\\s*\\d+");

    /**
     * Evaluates a dice equation and returns the total result.
     * Example: "2d20 + 1d6 + 2" might return 25
     *
     * @param equation the dice equation string (e.g., "2d20 + 1d6 + 2")
     * @return the total roll result
     */
    public static int evaluate(final String equation) {
        int total = 0;

        // Parse and roll all dice
        final Matcher diceMatcher = DICE_PATTERN.matcher(equation);
        while (diceMatcher.find()) {
            final int numDice = Integer.parseInt(diceMatcher.group(1));
            final int numSides = Integer.parseInt(diceMatcher.group(2));
            total += Func.sumRoll(numDice, numSides);
        }

        // Parse and apply all modifiers
        final Matcher modifierMatcher = MODIFIER_PATTERN.matcher(equation);
        while (modifierMatcher.find()) {
            final String modifier = modifierMatcher.group().replaceAll("\\s", "");
            total += Integer.parseInt(modifier);
        }

        return total;
    }

    /**
     * Evaluates a dice equation and returns the detailed rolls.
     *
     * @param equation the dice equation string
     * @return a list containing all individual rolls and modifiers
     */
    public static List<Integer> evaluateDetailed(final String equation) {
        final List<Integer> rolls = new ArrayList<>();

        // Parse and roll all dice
        final Matcher diceMatcher = DICE_PATTERN.matcher(equation);
        while (diceMatcher.find()) {
            final int numDice = Integer.parseInt(diceMatcher.group(1));
            final int numSides = Integer.parseInt(diceMatcher.group(2));
            final int[] diceRolls = Func.roll(numDice, numSides);
            for (final int roll : diceRolls) {
                rolls.add(roll);
            }
        }

        return rolls;
    }

    /**
     * Selects an item from a weighted list.
     * Weights are cumulative - each item's weight represents its probability relative to the total.
     *
     * @param weights an array of weights for each item
     * @return the 0-indexed position of the selected item
     */
    public static int selectWeightedIndex(final int[] weights) {
        if (weights == null || weights.length == 0) {
            return -1;
        }

        // Calculate total weight
        int totalWeight = 0;
        for (final int weight : weights) {
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            return 0;
        }

        // Roll a random value between 1 and totalWeight
        final int roll = Func.d(totalWeight);
        
        // Find which item the roll corresponds to
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (roll <= cumulative) {
                return i;
            }
        }

        // Fallback (should not reach here)
        return weights.length - 1;
    }
}
