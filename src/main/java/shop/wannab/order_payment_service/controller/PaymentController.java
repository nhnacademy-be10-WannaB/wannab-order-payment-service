package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.client.TossPaymentsApiClient;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<FinalOrderResultDto> confirmAndProcessPayment(@RequestBody TossConfirmRequestDto requestDto) {
            log.debug("confirmAndProcessPayment requestDto: {}", requestDto);
            FinalOrderResultDto result = paymentService.confirmAndProcessPayment(requestDto);
            log.debug("결제 승인 완료 : {}", result.getPaymentKey());
            return ResponseEntity.ok(result);
    }
}
