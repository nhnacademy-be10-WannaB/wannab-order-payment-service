package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPolicyResponse {
    private Long id;    // 정책id
    private String name;    // 정책명
    private int minPrice;   // 최소주문금액 (이 금액을 넘어야 정책이 적용)
    private int fee;    // 배송비
}
