package org.example.Amazon;

import org.example.Amazon.Cost.PriceRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Assignment 5 - Unit, Mocking and Integration Testing 3
 * Unit tests: test Amazon in isolation using mocks/stubs for all collaborators.
 */
class AmazonUnitTest {

    // ---- Helpers ------------------------------------------------------------

    private Amazon newSut(ShoppingCart cart, List<PriceRule> rules) {
        return new Amazon(cart, rules);
    }

    // ---- SPECIFICATION-BASED TESTS -----------------------------------------

    @Nested
    @DisplayName("specification-based")
    class SpecificationBased {

        @Test
        @DisplayName("calculate() sums results from all rules over the cart items")
        void calculate_sumsAllRules() {
            // Given a cart and two rules with known outputs
            ShoppingCart cart = mock(ShoppingCart.class);
            when(cart.getItems()).thenReturn(List.of(mock(Item.class), mock(Item.class)));

            PriceRule rule1 = mock(PriceRule.class);
            PriceRule rule2 = mock(PriceRule.class);
            when(rule1.priceToAggregate(anyList())).thenReturn(12.5);
            when(rule2.priceToAggregate(anyList())).thenReturn(7.5);

            Amazon sut = newSut(cart, List.of(rule1, rule2));

            // When
            double total = sut.calculate();

            // Then
            assertEquals(20.0, total, 1e-9);
            verify(rule1).priceToAggregate(anyList());
            verify(rule2).priceToAggregate(anyList());
            verifyNoMoreInteractions(rule1, rule2);
        }

        @Test
        @DisplayName("addToCart() delegates to ShoppingCart.add(item)")
        void addToCart_delegates() {
            ShoppingCart cart = mock(ShoppingCart.class);
            Amazon sut = newSut(cart, List.of());

            Item item = mock(Item.class);
            sut.addToCart(item);

            verify(cart).add(item);
            verifyNoMoreInteractions(cart);
        }
    }

    // ---- STRUCTURAL-BASED TESTS --------------------------------------------

    @Nested
    @DisplayName("structural-based")
    class StructuralBased {

        @Test
        @DisplayName("calculate() with empty rules returns 0 and does not touch cart (loop not entered)")
        void calculate_noRules_returnsZero() {
            ShoppingCart cart = mock(ShoppingCart.class);
            // No need to stub getItems() because it should NOT be called

            Amazon sut = newSut(cart, List.of());

            double total = sut.calculate();
            assertEquals(0.0, total, 1e-9);

            // Verify the loop body never ran → cart.getItems() never called
            verify(cart, never()).getItems();
            verifyNoMoreInteractions(cart);
        }

        @Test
        @DisplayName("calculate() with multiple rules executes loop for each rule")
        void calculate_multipleRules_loopCoverage() {
            ShoppingCart cart = mock(ShoppingCart.class);
            when(cart.getItems()).thenReturn(List.of(mock(Item.class)));

            PriceRule r1 = mock(PriceRule.class);
            PriceRule r2 = mock(PriceRule.class);
            PriceRule r3 = mock(PriceRule.class);
            when(r1.priceToAggregate(anyList())).thenReturn(1.0);
            when(r2.priceToAggregate(anyList())).thenReturn(2.0);
            when(r3.priceToAggregate(anyList())).thenReturn(3.0);

            Amazon sut = newSut(cart, List.of(r1, r2, r3));

            double total = sut.calculate();
            assertEquals(6.0, total, 1e-9);

            verify(r1).priceToAggregate(anyList());
            verify(r2).priceToAggregate(anyList());
            verify(r3).priceToAggregate(anyList());
        }
    }
}
