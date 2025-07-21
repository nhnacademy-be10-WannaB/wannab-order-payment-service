package shop.wannab.order_payment_service.entity.payment.strategy;

import org.springframework.stereotype.Component;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;

@Component
public class KakaoPaymentStrategy implements PaymentStrategy {

    @Override
    public FinalOrderResultDto confirmAndProcessPayment(TossConfirmRequestDto requestDto) {
        throw new UnsupportedOperationException("카카오 페이는 아직 지원하지 않습니다");
    }

    @Override
    public String getProviderName() {
        return "KAKAO";
    }
}
