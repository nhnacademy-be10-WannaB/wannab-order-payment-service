package shop.wannab.order_payment_service.entity.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.strategy.TossPaymentStrategy;
import shop.wannab.order_payment_service.service.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TossPaymentStrategyTest {

    private PaymentService paymentService;
    private TossPaymentStrategy tossStrategy;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        tossStrategy = new TossPaymentStrategy(paymentService);
    }



    @Test
    @DisplayName("getProviderName()이 'TOSS'를 반환하는지 테스트")
    void getProviderName_returnsToss() {
        assertEquals("TOSS", tossStrategy.getProviderName());
    }
}