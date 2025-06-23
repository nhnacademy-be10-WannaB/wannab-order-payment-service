package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrappingPaperRequest {

    @NotBlank(message = "포장지 이름은 필수로 입력해야 합니다")
    private String name; // 포장지 이름

    @Positive(message = "가격은 0보다 크게 입력해야 합니다")
    private int price; // 포장지 가격
}
