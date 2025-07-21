package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategy;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategyFactory;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentStrategyFactory paymentStrategyFactory;

    @PostMapping("/{provider}/confirm")
    public ResponseEntity<FinalOrderResultDto> confirmAndProcessPayment(
            @PathVariable String provider,
            @RequestBody TossConfirmRequestDto requestDto) {
        log.debug("confirmAndProcessPayment for {} with requestDto: {}", provider, requestDto);
        PaymentStrategy paymentStrategy = paymentStrategyFactory.getStrategy(provider);
        FinalOrderResultDto result = paymentStrategy.confirmAndProcessPayment(requestDto);
        log.debug("결제 승인 완료 : {}", result.getPaymentKey());
        return ResponseEntity.ok(result);
    }
}
