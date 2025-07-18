package shop.wannab.order_payment_service.entity.payment.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.service.PaymentService;

@Component
@RequiredArgsConstructor
public class TossPaymentStrategy implements PaymentStrategy {

    private final PaymentService paymentService;

    @Override
    public FinalOrderResultDto confirmAndProcessPayment(TossConfirmRequestDto requestDto) {
        return paymentService.confirmAndProcessTossPayment(requestDto);
    }

    @Override
    public String getProviderName() {
        return "TOSS";
    }
}
