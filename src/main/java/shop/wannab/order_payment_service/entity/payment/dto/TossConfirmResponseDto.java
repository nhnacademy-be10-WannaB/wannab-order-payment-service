package shop.wannab.order_payment_service.entity.payment.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TossConfirmResponseDto {
    private String paymentKey;
    private String type;
    private String method;
    private Integer totalAmount;
    private Integer balanceAmount;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private String orderId;

    private TossFailureDto failure;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TossFailureDto {
        private String code;
        private String message;
    }
}
