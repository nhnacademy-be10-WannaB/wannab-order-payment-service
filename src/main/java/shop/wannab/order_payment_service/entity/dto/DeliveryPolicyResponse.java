package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryPolicyResponse {
    private final Long id;    // 정책id
    private final String name;    // 정책명
    private final int minPrice;   // 최소주문금액 (이 금액을 넘어야 정책이 적용)
    private final int fee;    // 배송비
}
