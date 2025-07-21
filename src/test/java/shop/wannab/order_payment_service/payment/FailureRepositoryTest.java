package shop.wannab.order_payment_service.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import shop.wannab.order_payment_service.entity.payment.Failure;
import shop.wannab.order_payment_service.entity.payment.Payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("ci")
class FailureRepositoryTest {

    @Test
    void testFailureConstructor() {
        Payment payment = new Payment();
        payment.setPaymentKey("payment_key_789");
        Failure failure = new Failure(payment, "E001", "Test Error");

        assertEquals(payment, failure.getPayment());
        assertEquals("E001", failure.getErrorCode());
        assertEquals("Test Error", failure.getMessage());
    }

    @Test
    void testGettersAndSetters() {
        Failure failure = new Failure();
        Payment payment = new Payment();
        payment.setPaymentKey("payment_key_101");

        failure.setId("failure_id_123");
        failure.setPayment(payment);
        failure.setErrorCode("E002");
        failure.setMessage("Another Test Error");

        assertEquals("failure_id_123", failure.getId());
        assertEquals(payment, failure.getPayment());
        assertEquals("E002", failure.getErrorCode());
        assertEquals("Another Test Error", failure.getMessage());
    }
}