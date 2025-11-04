package org.example.Amazon;

import org.example.Amazon.Cost.PriceRule;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Assignment 5 - Unit, Mocking and Integration Testing 3
 * Integration tests: compose Amazon with multiple components working together.
 *
 * We simulate a "DB reset" by clearing a shared in-memory list before each test.
 * The cart is a stateful Mockito stub: add(item) mutates a backing list; getItems() returns it.
 * Price rules are concrete implementations (no mocks) to exercise real interactions.
 */
class AmazonIntegrationTest {

    // Shared in-memory "DB" for items; cleared before each test.
    private List<Item> backingItems;

    private ShoppingCart statefulCart() {
        // Stateful stub of ShoppingCart using Mockito with custom answers
        ShoppingCart cart = mock(ShoppingCart.class);

        // getItems() returns the same live list
        when(cart.getItems()).then(invocation -> backingItems);

        // add(item) mutates the live list
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            backingItems.add(item);
            return null;
        }).when(cart).add(any(Item.class));

        return cart;
    }

    // --- Concrete rule implementations (no mocks) ---------------------------

    /** FlatPerItemRule: returns count * unitPrice (ignores item fields; integration via size). */
    private static class FlatPerItemRule implements PriceRule {
        private final double unitPrice;
        FlatPerItemRule(double unitPrice) { this.unitPrice = unitPrice; }

        @Override
        public double priceToAggregate(List<Item> items) {
            return items.size() * unitPrice;
        }
    }

    /** BuyNGetOneFree-like rule: for every n items, one is free at unitPrice. */
    private static class BuyNGetOneFreeRule implements PriceRule {
        private final int n;          // e.g., 3 => buy 3 get 1 free
        private final double unitPrice;
        BuyNGetOneFreeRule(int n, double unitPrice) {
            this.n = n;
            this.unitPrice = unitPrice;
        }

        @Override
        public double priceToAggregate(List<Item> items) {
            int count = items.size();
            int free = (n > 0) ? count / (n + 1) : 0;
            int paid = count - free;
            return paid * unitPrice;
        }
    }

    @BeforeEach
    void resetInMemoryDb() {
        backingItems = new ArrayList<>();
    }

    // ---- SPECIFICATION-BASED TESTS -----------------------------------------

    @Nested
    @DisplayName("specification-based")
    class SpecificationBased {

        @Test
        @DisplayName("calculate() applies all rules to the current cart state")
        void calculate_appliesAllRules() {
            ShoppingCart cart = statefulCart();

            // Add 5 items (use Mockito Item mocks to avoid relying on constructors)
            for (int i = 0; i < 5; i++) cart.add(mock(Item.class));

            // Two real rules composed together
            PriceRule perItem = new FlatPerItemRule(2.0);        // 5 * 2 = 10
            PriceRule b3g1 = new BuyNGetOneFreeRule(3, 1.0);     // for each 4 items, 1 free => pay 4 of 5 => 4 * 1 = 4

            Amazon amazon = new Amazon(cart, List.of(perItem, b3g1));

            double total = amazon.calculate();

            // Expect 10 + 4 = 14
            assertEquals(14.0, total, 1e-9);
            assertEquals(5, cart.getItems().size());
        }

        @Test
        @DisplayName("addToCart() updates cart and later affects calculate()")
        void addToCart_affectsTotal() {
            ShoppingCart cart = statefulCart();

            PriceRule perItem = new FlatPerItemRule(3.0); // 3 per item

            Amazon amazon = new Amazon(cart, List.of(perItem));

            // Initially empty
            assertEquals(0.0, amazon.calculate(), 1e-9);

            // Add two items then recalculate
            amazon.addToCart(mock(Item.class));
            amazon.addToCart(mock(Item.class));

            assertEquals(2, cart.getItems().size());
            assertEquals(6.0, amazon.calculate(), 1e-9);
        }
    }

    // ---- STRUCTURAL-BASED TESTS --------------------------------------------

    @Nested
    @DisplayName("structural-based")
    class StructuralBased {

        @Test
        @DisplayName("empty rule set yields 0 regardless of items (loop not entered)")
        void noRules_returnsZero() {
            ShoppingCart cart = statefulCart();
            cart.add(mock(Item.class));
            cart.add(mock(Item.class));

            Amazon amazon = new Amazon(cart, List.of());

            assertEquals(0.0, amazon.calculate(), 1e-9);
        }

        @Test
        @DisplayName("multiple rules iterate in order and all contribute to total")
        void multipleRules_iterateAndSum() {
            ShoppingCart cart = statefulCart();
            for (int i = 0; i < 3; i++) cart.add(mock(Item.class)); // 3 items

            // Rule1: 3 * 1.5 = 4.5
            // Rule2: buy 2 get 1 free at 2.0 -> pay 2 -> 4.0
            PriceRule r1 = new FlatPerItemRule(1.5);
            PriceRule r2 = new BuyNGetOneFreeRule(2, 2.0);

            Amazon amazon = new Amazon(cart, List.of(r1, r2));
            double total = amazon.calculate();

            assertEquals(8.5, total, 1e-9);
        }
    }
}
