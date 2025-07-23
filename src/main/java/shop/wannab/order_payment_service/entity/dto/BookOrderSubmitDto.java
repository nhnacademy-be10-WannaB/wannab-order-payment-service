package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class BookOrderSubmitDto {
    private long bookId;
    @Min(value = 1)
    private int bookQuantity;
    private Long pavingId;
    private Long appliedCouponId;

}