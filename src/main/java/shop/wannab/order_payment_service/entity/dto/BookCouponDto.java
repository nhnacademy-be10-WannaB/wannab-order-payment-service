package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import shop.wannab.order_payment_service.entity.DiscountType;

@Getter
@Setter
@AllArgsConstructor
public class BookCouponDto {
    private Long couponId;
    private String couponName;
    private int discountValue;
    private DiscountType discountType;
}
