package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;

@FeignClient(name="toss-payments-api",url="https://api.tosspayments.com")
public interface TossPaymentsApiClient {

    @PostMapping(value = "/v1/payments/confirm", consumes = "application/json")
    TossConfirmResponseDto confirmPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestBody TossConfirmRequestDto requestDto
    );
}
