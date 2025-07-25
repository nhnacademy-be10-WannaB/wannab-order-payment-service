package shop.wannab.order_payment_service.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import shop.wannab.order_payment_service.entity.payment.Cancel;
import shop.wannab.order_payment_service.entity.payment.Payment;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("ci")
class CancelRepositoryTest {

    @Test
    void testCancelConstructor() {
        Payment payment = new Payment();
        payment.setPaymentKey("payment_key_123");
        LocalDateTime now = LocalDateTime.now();
        Cancel cancel = new Cancel(payment, 1000, now);

        assertEquals(payment, cancel.getPayment());
        assertEquals(1000, cancel.getCancelAmount());
        assertEquals(now, cancel.getCancelAt());
    }

    @Test
    void testGettersAndSetters() {
        Cancel cancel = new Cancel();
        Payment payment = new Payment();
        payment.setPaymentKey("payment_key_456");
        LocalDateTime now = LocalDateTime.now();

        cancel.setId("cancel_id_123");
        cancel.setPayment(payment);
        cancel.setCancelAmount(2000);
        cancel.setCancelAt(now);

        assertEquals("cancel_id_123", cancel.getId());
        assertEquals(payment, cancel.getPayment());
        assertEquals(2000, cancel.getCancelAmount());
        assertEquals(now, cancel.getCancelAt());
    }
}