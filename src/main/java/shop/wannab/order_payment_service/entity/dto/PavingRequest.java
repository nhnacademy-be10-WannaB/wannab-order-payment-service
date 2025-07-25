package shop.wannab.order_payment_service.entity.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PavingRequest {
    @NotBlank(message = "포장지 이름은 필수입니다.")
    private String name;
    @PositiveOrZero(message = "포장지의 가격은 0보다 작을 수 없습니다.")
    private int price;
}
