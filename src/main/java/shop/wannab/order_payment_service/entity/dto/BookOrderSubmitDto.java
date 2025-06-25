package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookOrderSubmitDto {
    private long bookId;
    @Min(value = 1)
    private int bookQuantity;
    private Long wrappingPaperId;
    //TODO: coupon 관련 정보 추가해야 함

}