package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryPolicyRequest {
    @NotBlank(message = "정책명은 필수로 입력해야 합니다")
    private String name;    // 정책명

    @PositiveOrZero(message = "최소주문금액은 0보다 작을 수 없습니다.")
    private int minPrice;   // 최소주문금액 (이 금액을 넘어야 정책이 적용)

    @PositiveOrZero(message = "배송비는 0보다 작을 수 없습니다.")
    private int fee;    // 배송비

}
