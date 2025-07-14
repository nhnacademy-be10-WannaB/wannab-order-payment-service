package shop.wannab.order_payment_service.entity.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailResponseDto {
    private String errorCode;
    private String errorMessage;
    private String orderId;
    private String paymentKey;
}
