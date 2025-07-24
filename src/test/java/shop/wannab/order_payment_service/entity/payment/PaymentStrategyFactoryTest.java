package shop.wannab.order_payment_service.entity.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Set;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategy;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategyFactory;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStrategyFactoryTest {

    private PaymentStrategy kakaoStrategy;
    private PaymentStrategy naverStrategy;
    private PaymentStrategyFactory factory;

    @BeforeEach
    void setUp() {
        kakaoStrategy = Mockito.mock(PaymentStrategy.class);
        naverStrategy = Mockito.mock(PaymentStrategy.class);

        Mockito.when(kakaoStrategy.getProviderName()).thenReturn("KAKAO");
        Mockito.when(naverStrategy.getProviderName()).thenReturn("NAVER");

        factory = new PaymentStrategyFactory(Set.of(kakaoStrategy, naverStrategy));
    }

    @Test
    @DisplayName("정상적으로 provider 이름으로 전략 반환")
    void getStrategy_success() {
        assertEquals(kakaoStrategy, factory.getStrategy("KAKAO"));
        assertEquals(naverStrategy, factory.getStrategy("NAVER"));
    }

    @Test
    @DisplayName("소문자로 입력해도 정상 동작")
    void getStrategy_caseInsensitive() {
        assertEquals(kakaoStrategy, factory.getStrategy("kakao"));
    }

    @Test
    @DisplayName("등록되지 않은 전략 조회 시 예외 발생")
    void getStrategy_notFound() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.getStrategy("UNKNOWN")
        );
        assertEquals("지원하지 않는 결제 대행사: UNKNOWN", ex.getMessage());
    }
}