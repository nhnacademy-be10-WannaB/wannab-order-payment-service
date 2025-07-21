package shop.wannab.order_payment_service.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    @Test
    public void testConstructorWithPaving() {
        Order order = new Order();
        Paving paving = new Paving();
        paving.setPrice(1500);

        long bookId = 10L;
        int quantity = 2;
        int bookPrice = 10000;

        OrderBook orderBook = new OrderBook(order, bookId, paving, quantity, bookPrice);

        assertEquals(order, orderBook.getOrder());
        assertEquals(bookId, orderBook.getBookId());
        assertEquals(paving, orderBook.getPaving());
        assertEquals(quantity, orderBook.getQuantity());
        assertEquals(bookPrice, orderBook.getBookPrice());
        assertEquals(1500, orderBook.getPavingPrice());
    }

    @Test
    public void testConstructorWithoutPaving() {
        Order order = new Order();
        long bookId = 20L;
        int quantity = 3;
        int bookPrice = 12000;

        OrderBook orderBook = new OrderBook(order, bookId, null, quantity, bookPrice);

        assertEquals(order, orderBook.getOrder());
        assertEquals(bookId, orderBook.getBookId());
        assertNull(orderBook.getPaving());
        assertEquals(quantity, orderBook.getQuantity());
        assertEquals(bookPrice, orderBook.getBookPrice());
        assertEquals(0, orderBook.getPavingPrice());
    }

    @Test
    public void testSettersAndGetters() {
        OrderBook orderBook = new OrderBook();
        orderBook.setObId(1L);
        orderBook.setQuantity(5);
        orderBook.setBookPrice(20000);
        orderBook.setPavingPrice(500);

        Order order = new Order();
        orderBook.setOrder(order);

        orderBook.setBookId(99L);

        Paving paving = new Paving();
        orderBook.setPaving(paving);

        assertEquals(1L, orderBook.getObId());
        assertEquals(5, orderBook.getQuantity());
        assertEquals(20000, orderBook.getBookPrice());
        assertEquals(500, orderBook.getPavingPrice());
        assertEquals(order, orderBook.getOrder());
        assertEquals(99L, orderBook.getBookId());
        assertEquals(paving, orderBook.getPaving());
    }
}