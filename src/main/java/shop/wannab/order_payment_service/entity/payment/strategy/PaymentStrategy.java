package shop.wannab.order_payment_service.entity.payment.strategy;

import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;

public interface PaymentStrategy {
    FinalOrderResultDto confirmAndProcessPayment(TossConfirmRequestDto requestDto);
    String getProviderName();
}
