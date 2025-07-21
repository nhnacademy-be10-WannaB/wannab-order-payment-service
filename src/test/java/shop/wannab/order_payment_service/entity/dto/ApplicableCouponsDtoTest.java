package shop.wannab.order_payment_service.entity.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.DiscountType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicableCouponsDtoTest {

    @Test
    @DisplayName("ApplicableCouponsDto 생성자, getter 확인")
    void testApplicableCouponsDtoCreation() {
        BookCouponDto bookCoupon = new BookCouponDto(1L, "도서 쿠폰", 1000, DiscountType.FIXED);
        OrderCouponDto orderCoupon = new OrderCouponDto(2L, "주문 쿠폰", 2000, DiscountType.PERCENT);

        Map<Long, List<BookCouponDto>> itemCoupons = Map.of(1L, List.of(bookCoupon));
        List<OrderCouponDto> orderCoupons = List.of(orderCoupon);

        ApplicableCouponsDto dto = new ApplicableCouponsDto(itemCoupons, orderCoupons);

        assertThat(dto.getItemCoupons()).containsKey(1L);
        assertThat(dto.getItemCoupons().get(1L)).contains(bookCoupon);
        assertThat(dto.getOrderCoupons()).contains(orderCoupon);
    }
}