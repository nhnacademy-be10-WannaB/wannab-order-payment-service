package shop.wannab.order_payment_service.entity.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FinalOrderResultDto {
    private String paymentKey;
    private String orderId;
    private int amount;
}
