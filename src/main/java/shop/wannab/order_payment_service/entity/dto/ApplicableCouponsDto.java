package shop.wannab.order_payment_service.entity.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicableCouponsDto {
    //프론트에 뿌리는거 고려해서 맵으로 감싸서 던져주기
    private Map<Long,List<BookCouponDto>> itemCoupons;
    private List<OrderCouponDto> orderCoupons;

    public ApplicableCouponsDto(Map<Long,List<BookCouponDto>> itemCoupons, List<OrderCouponDto> orderCoupons) {
        this.itemCoupons = itemCoupons;
        this.orderCoupons = orderCoupons;
    }
}
