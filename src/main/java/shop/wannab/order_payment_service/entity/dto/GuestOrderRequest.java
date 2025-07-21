package shop.wannab.order_payment_service.entity.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 게스트 주문조회 및 반품,취소 시 필요
 */
@Data
public class GuestOrderRequest {
    @Min(value = 0, message = "주문번호는 0 이상이어야 합니다.")
    private Long orderId;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "\\d{4,6}", message = "비밀번호는 숫자 4~6자리여야 합니다.")
    private String password;
}