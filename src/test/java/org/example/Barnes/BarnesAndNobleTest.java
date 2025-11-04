package org.example.Barnes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Assignment 5 - Unit, Mocking and Integration Testing 1
 * Tests are labeled with @DisplayName("specification-based") and @DisplayName("structural-based")
 */
class BarnesAndNobleTest {

    // ---- Helpers ------------------------------------------------------------

    private BarnesAndNoble newSut(BookDatabase db, BuyBookProcess proc) {
        return new BarnesAndNoble(db, proc);
    }

    private Book book(int price, int quantity) {
        Book b = mock(Book.class);
        when(b.getPrice()).thenReturn(price);
        when(b.getQuantity()).thenReturn(quantity);
        return b;
    }

    // Assumptions about provided model classes in the project:
    //  - PurchaseSummary has: int/double getTotalPrice(), Map<Book,Integer> getUnavailable()
    //    and BarnesAndNoble internally uses addToTotalPrice(...) and addUnavailable(...).
    // ------------------------------------------------------------------------

    @Nested
    @DisplayName("specification-based")
    class SpecificationBased {

        @Test
        @DisplayName("null cart returns null and makes no calls")
        void nullOrder_returnsNull_noCalls() {
            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            BarnesAndNoble sut = newSut(db, proc);
            PurchaseSummary result = sut.getPriceForCart(null);

            assertNull(result);
            verifyNoInteractions(db, proc);
        }

        @Test
        @DisplayName("sufficient stock: charges requested qty, no unavailable")
        void sufficientStock_exactlyRequested() {
            String A = "A";
            Map<String, Integer> order = new LinkedHashMap<>();
            order.put(A, 2);

            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            Book bookA = book(20, 5); // enough stock
            when(db.findByISBN(A)).thenReturn(bookA);

            BarnesAndNoble sut = newSut(db, proc);
            PurchaseSummary summary = sut.getPriceForCart(order);

            assertNotNull(summary);
            // total = 2 * 20 = 40
            assertEquals(40, summary.getTotalPrice());
            assertTrue(summary.getUnavailable().isEmpty());

            verify(proc).buyBook(bookA, 2);
            verifyNoMoreInteractions(proc);
        }

        @Test
        @DisplayName("insufficient stock: charges available qty, records shortfall")
        void insufficientStock_recordsShortfall() {
            String B = "B";
            Map<String, Integer> order = new LinkedHashMap<>();
            order.put(B, 4); // request 4

            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            Book bookB = book(10, 2); // only 2 available => shortfall 2
            when(db.findByISBN(B)).thenReturn(bookB);

            BarnesAndNoble sut = newSut(db, proc);
            PurchaseSummary summary = sut.getPriceForCart(order);

            assertNotNull(summary);
            // charged only available: 2 * 10 = 20
            assertEquals(20, summary.getTotalPrice());
            assertEquals(1, summary.getUnavailable().size());
            assertEquals(2, summary.getUnavailable().get(bookB));

            verify(proc).buyBook(bookB, 2);
            verifyNoMoreInteractions(proc);
        }

        @Test
        @DisplayName("zero quantity request: no shortage, price unchanged, still calls process with 0")
        void zeroQuantity_noShortage_priceUnchanged() {
            String Z = "Z";
            Map<String, Integer> order = new LinkedHashMap<>();
            order.put(Z, 0);

            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            Book bookZ = book(99, 10);
            when(db.findByISBN(Z)).thenReturn(bookZ);

            BarnesAndNoble sut = newSut(db, proc);
            PurchaseSummary summary = sut.getPriceForCart(order);

            assertNotNull(summary);
            assertEquals(0, summary.getTotalPrice());
            assertTrue(summary.getUnavailable().isEmpty());

            verify(proc).buyBook(bookZ, 0);
            verifyNoMoreInteractions(proc);
        }
    }

    @Nested
    @DisplayName("structural-based")
    class StructuralBased {

        @Test
        @DisplayName("non-null order hits loop path and handles mixed branches")
        void iteratesAllKeys_mixedBranches() {
            String A = "A";
            String B = "B";
            Map<String, Integer> order = new LinkedHashMap<>();
            order.put(A, 3); // sufficient
            order.put(B, 5); // insufficient

            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            Book bookA = book(15, 10); // enough stock
            Book bookB = book(7, 2);   // shortage 3
            when(db.findByISBN(A)).thenReturn(bookA);
            when(db.findByISBN(B)).thenReturn(bookB);

            BarnesAndNoble sut = newSut(db, proc);
            PurchaseSummary summary = sut.getPriceForCart(order);

            // total: A -> 3*15=45; B -> 2*7=14; sum = 59
            assertEquals(59, summary.getTotalPrice());
            assertEquals(1, summary.getUnavailable().size());
            assertEquals(3, summary.getUnavailable().get(bookB));

            // Verify order of operations (A then B)
            var inOrder = inOrder(db, proc);
            inOrder.verify(db).findByISBN(A);
            inOrder.verify(proc).buyBook(bookA, 3);
            inOrder.verify(db).findByISBN(B);
            inOrder.verify(proc).buyBook(bookB, 2);
            inOrder.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("single item path still exercises non-null branch")
        void singleItem_nonNullBranch() {
            String A = "A";
            Map<String, Integer> order = new LinkedHashMap<>();
            order.put(A, 1);

            BookDatabase db = mock(BookDatabase.class);
            BuyBookProcess proc = mock(BuyBookProcess.class);

            Book bookA = book(5, 1);
            when(db.findByISBN(A)).thenReturn(bookA);

            PurchaseSummary s = newSut(db, proc).getPriceForCart(order);
            assertEquals(5, s.getTotalPrice());
            verify(proc).buyBook(bookA, 1);
        }
    }
}
