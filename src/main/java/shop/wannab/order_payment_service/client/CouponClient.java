package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.wannab.order_payment_service.entity.dto.*;

import java.util.List;

@FeignClient(name = "coupon-service")
public interface CouponClient {

    @PostMapping("/api/coupons/order")
    ResponseEntity<ApplicableCouponsDto> getApplicableCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody OrderCouponsRequestDto orderCouponsRequestDtoList);

    @PostMapping("/api/coupons/order/apply")
    ResponseEntity<List<TryApplyCouponsResponseDto>> getApplyCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody TryApplyCouponsRequestDto tryApplyCouponsRequestDtoMap
            );

    @PostMapping("/api/coupons/order/success")
    ResponseEntity<Void> processUsedCoupons(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CouponUsageRequestDto requestDto);
}
