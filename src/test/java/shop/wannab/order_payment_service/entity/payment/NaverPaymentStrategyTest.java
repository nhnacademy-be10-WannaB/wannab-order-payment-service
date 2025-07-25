package shop.wannab.order_payment_service.entity.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.strategy.NaverPaymentStrategy;

import static org.junit.jupiter.api.Assertions.*;

class NaverPaymentStrategyTest {

    private final NaverPaymentStrategy naverPaymentStrategy = new NaverPaymentStrategy();

    @Test
    @DisplayName("confirmAndProcessPayment 호출 시 UnsupportedOperationException 발생")
    void confirmAndProcessPayment_shouldThrowUnsupportedOperationException() {
        TossConfirmRequestDto dummyRequest = new TossConfirmRequestDto("testKey", "orderId", 5000);

        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> naverPaymentStrategy.confirmAndProcessPayment(dummyRequest)
        );

        assertEquals("네이버 페이는 아직 지원하지 않습니다", exception.getMessage());
    }

    @Test
    @DisplayName("getProviderName은 NAVER 반환")
    void getProviderName_shouldReturnNAVER() {
        assertEquals("NAVER", naverPaymentStrategy.getProviderName());
    }
}