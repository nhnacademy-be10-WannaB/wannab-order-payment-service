package shop.wannab.order_payment_service.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class CouponUsageRequestDto implements Serializable {

    private Long orderId;
    private Long userId;
    // 사용된 쿠폰 목록
    private List<UsedCouponInfo> usedCoupons;

    @Getter
    @Setter
    public static class UsedCouponInfo implements Serializable{
        private Long couponId;

        private Long bookId;
    }
}