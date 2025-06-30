package shop.wannab.order_payment_service.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CouponUsageRequestDto {

    private Long orderId;

    // 사용된 쿠폰 목록
    private List<UsedCouponInfo> usedCoupons;

    @Getter
    @Setter
    public static class UsedCouponInfo {
        private Long couponId;

        private Long bookId;
    }
}