package shop.wannab.order_payment_service.entity.payment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.strategy.KakaoPaymentStrategy;

class KakaoPaymentStrategyTest {

    private final KakaoPaymentStrategy kakaoPaymentStrategy = new KakaoPaymentStrategy();

    @Test
    @DisplayName("confirmAndProcessPayment 호출 시 UnsupportedOperationException 발생")
    void confirmAndProcessPayment_throwsUnsupportedOperationException() {
        TossConfirmRequestDto dummyRequest = new TossConfirmRequestDto("key", "orderId", 1000);

        UnsupportedOperationException exception = assertThrows(
                UnsupportedOperationException.class,
                () -> kakaoPaymentStrategy.confirmAndProcessPayment(dummyRequest)
        );

        assertEquals("카카오 페이는 아직 지원하지 않습니다", exception.getMessage());
    }

    @Test
    @DisplayName("getProviderName 은 KAKAO 반환")
    void getProviderName_returnsKAKAO() {
        assertEquals("KAKAO", kakaoPaymentStrategy.getProviderName());
    }
}