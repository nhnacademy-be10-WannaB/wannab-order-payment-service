package shop.wannab.order_payment_service.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    public void testOrderConstructorAndGetters() {
        Long userId = 10L;
        String orderName = "테스트주문";
        LocalDateTime shippedAt = LocalDateTime.of(2025, 7, 20, 12, 0);
        LocalDate deliveryRequestAt = LocalDate.of(2025, 7, 21);
        int totalBookPrice = 30000;
        int totalDiscountAmount = 5000;
        int shippingFee = 2500;
        int totalPavingPrice = 1000;
        String recipientName = "정민수";
        String recipientEmail = "tttt@tttt.com";
        String recipientPhone = "010-0000-0000";
        String recipientAddress = "광주어딘가";

        Order order = new Order(userId, orderName, shippedAt, deliveryRequestAt, totalBookPrice,
                totalDiscountAmount, shippingFee, totalPavingPrice,
                recipientName, recipientEmail, recipientPhone, recipientAddress);

        assertNotNull(order.getOrderAt());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(userId, order.getUserId());
        assertEquals(orderName, order.getOrderName());
        assertEquals(shippedAt, order.getShippedAt());
        assertEquals(deliveryRequestAt, order.getDeliveryWant());
        assertEquals(totalBookPrice, order.getTotalBookPrice());
        assertEquals(totalDiscountAmount, order.getTotalDiscountAmount());
        assertEquals(shippingFee, order.getShippingFee());
        assertEquals(totalPavingPrice, order.getTotalPavingPrice());
        assertEquals(recipientName, order.getRecipientName());
        assertEquals(recipientEmail, order.getRecipientEmail());
        assertEquals(recipientPhone, order.getRecipientPhoneNumber());
        assertEquals(recipientAddress, order.getRecipientAddress());
    }

    @Test
    public void testGetTotalPrice() {
        Order order = new Order(
                1L, "주문1", null, LocalDate.now().plusDays(1),
                20000, 3000, 2500, 1000,
                "수령인", "dddd@dddd.com", "010-9999-9999", "광주 어딘가"
        );

        int totalPrice = order.getTotalPrice();

        assertEquals(20000 + 2500 + 1000 - 3000, totalPrice);
    }

    @Test
    public void testSetShippedAtAndOrderStatus() {
        Order order = new Order();
        LocalDateTime now = LocalDateTime.now();

        order.setShippedAt(now);
        order.setOrderStatus(OrderStatus.COMPLETED);

        assertEquals(now, order.getShippedAt());
        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
    }
}