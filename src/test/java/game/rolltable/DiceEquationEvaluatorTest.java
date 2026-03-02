package game.rolltable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DiceEquationEvaluator
 */
@DisplayName("Dice Equation Evaluator Tests")
public class DiceEquationEvaluatorTest {

    @Test
    @DisplayName("Should evaluate simple d20 roll")
    public void testSimpleD20() {
        final int result = DiceEquationEvaluator.evaluate("1d20");
        assertTrue(result >= 1 && result <= 20, "Result should be between 1 and 20");
    }

    @Test
    @DisplayName("Should evaluate multiple dice")
    public void testMultipleDice() {
        final int result = DiceEquationEvaluator.evaluate("2d6");
        assertTrue(result >= 2 && result <= 12, "Result should be between 2 and 12 for 2d6");
    }

    @Test
    @DisplayName("Should evaluate dice with positive modifier")
    public void testDiceWithPositiveModifier() {
        final int result = DiceEquationEvaluator.evaluate("1d20 + 5");
        assertTrue(result >= 6 && result <= 25, "Result should be between 6 and 25 for 1d20+5");
    }

    @Test
    @DisplayName("Should evaluate dice with negative modifier")
    public void testDiceWithNegativeModifier() {
        final int result = DiceEquationEvaluator.evaluate("1d20 - 3");
        assertTrue(result >= -2 && result <= 17, "Result should be between -2 and 17 for 1d20-3");
    }

    @Test
    @DisplayName("Should evaluate complex equation with multiple dice and modifiers")
    public void testComplexEquation() {
        final int result = DiceEquationEvaluator.evaluate("2d20 + 1d6 + 2");
        assertTrue(result >= 5 && result <= 48, "Result should be between 5 and 48 for 2d20+1d6+2");
    }

    @Test
    @DisplayName("Should evaluate equation with only modifier")
    public void testEquationWithOnlyModifier() {
        final int result = DiceEquationEvaluator.evaluate("+10");
        assertEquals(10, result, "Equation with only modifier should return the modifier value");
    }

    @Test
    @DisplayName("Should return non-negative result for standard roll")
    public void testNonNegativeResult() {
        for (int i = 0; i < 10; i++) {
            final int result = DiceEquationEvaluator.evaluate("1d20");
            assertTrue(result >= 1, "Dice roll result should always be positive");
        }
    }

    @Test
    @DisplayName("Should handle spaces in equation")
    public void testEquationWithSpaces() {
        final int result = DiceEquationEvaluator.evaluate("2d20 + 1d6 + 2");
        assertTrue(result >= 5 && result <= 48, "Should handle spaces in equation correctly");
    }

    @Test
    @DisplayName("Should evaluate detailed rolls")
    public void testDetailedRolls() {
        for (int i = 0; i < 10; i++) {
            final var rolls = DiceEquationEvaluator.evaluateDetailed("2d6");
            assertEquals(2, rolls.size(), "2d6 should return 2 rolls");
            assertTrue(rolls.stream().allMatch(r -> r >= 1 && r <= 6), "Each roll should be 1-6");
        }
    }

    @Test
    @DisplayName("Should select weighted index with uniform weights")
    public void testWeightedSelectionUniform() {
        final int[] weights = {1, 1, 1, 1};
        for (int i = 0; i < 20; i++) {
            final int index = DiceEquationEvaluator.selectWeightedIndex(weights);
            assertTrue(index >= 0 && index < 4, "Index should be between 0 and 3 for 4 items with uniform weights");
        }
    }

    @Test
    @DisplayName("Should select weighted index with varied weights")
    public void testWeightedSelectionVaried() {
        final int[] weights = {1, 2, 3, 4};
        int[] selectionCounts = {0, 0, 0, 0};
        final int iterations = 1000;
        
        for (int i = 0; i < iterations; i++) {
            final int index = DiceEquationEvaluator.selectWeightedIndex(weights);
            selectionCounts[index]++;
        }
        
        // With weights 1:2:3:4, we expect roughly 10%, 20%, 30%, 40% distribution
        // Allow 5% variance
        final int totalWeight = 10;
        final int minThreshold = (int)(iterations * 0.05);
        final int maxThreshold = (int)(iterations * 0.45);
        
        for (int i = 0; i < weights.length; i++) {
            final int expected = (weights[i] * iterations) / totalWeight;
            assertTrue(selectionCounts[i] >= expected - minThreshold && selectionCounts[i] <= expected + minThreshold,
                String.format("Index %d should have ~%d selections, got %d", i, expected, selectionCounts[i]));
        }
    }

    @Test
    @DisplayName("Should select first item with single weight")
    public void testWeightedSelectionSingleItem() {
        final int[] weights = {5};
        for (int i = 0; i < 10; i++) {
            final int index = DiceEquationEvaluator.selectWeightedIndex(weights);
            assertEquals(0, index, "Single item should always return index 0");
        }
    }

    @Test
    @DisplayName("Should return -1 for null weights")
    public void testWeightedSelectionNullWeights() {
        final int index = DiceEquationEvaluator.selectWeightedIndex(null);
        assertEquals(-1, index, "Null weights should return -1");
    }

    @Test
    @DisplayName("Should return -1 for empty weights")
    public void testWeightedSelectionEmptyWeights() {
        final int[] weights = {};
        final int index = DiceEquationEvaluator.selectWeightedIndex(weights);
        assertEquals(-1, index, "Empty weights should return -1");
    }

    @Test
    @DisplayName("Should favor higher weighted items")
    public void testWeightedSelectionFavorsHigherWeights() {
        final int[] weights = {1, 10};
        int[] selectionCounts = {0, 0};
        final int iterations = 1000;
        
        for (int i = 0; i < iterations; i++) {
            final int index = DiceEquationEvaluator.selectWeightedIndex(weights);
            selectionCounts[index]++;
        }
        
        // Item 1 should be selected roughly 10 times more often
        assertTrue(selectionCounts[1] > selectionCounts[0], "Higher weighted item should be selected more often");
        assertTrue(selectionCounts[1] > selectionCounts[0] * 5, "Item with weight 10 should be selected significantly more");
    }
}
