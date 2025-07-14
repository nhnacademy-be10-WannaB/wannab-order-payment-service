package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.dto.UserAddressResponse;
import shop.wannab.order_payment_service.entity.dto.PointProcessRequest;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/points")
    int getUserPoints(@RequestHeader("X-USER-ID") Long headerUserId);

    @GetMapping("/api/users/addresses")
    List<UserAddressResponse> getAllAddresses(@RequestHeader("X-USER-ID") Long headerUserId);

    @PostMapping("/api/users/points/process")
    void processPoints(@RequestBody PointProcessRequest pointProcessRequest);

    @PostMapping("/api/users/points/orders/{order-id}/cancel")
    void cancleOrderPointProcess(@PathVariable("order-id") Long orderId);

    //주문 취소시 포인트 반환(반환받을 포인트값만 던져줌)
    @PostMapping("/api/users/points/refund")
    void refundPoint(@RequestParam("order-id") Long orderId,
                     @RequestParam("amount") int refundPoint);

//    // 이메일 (보류)
//    @GetMapping("/api/users/{user-id}/email")
//    String getUserEmail(@PathVariable("user-id") Long userId);
//
//    //유저역할 (보류)
//    @GetMapping("/users/{userId}/role")
//    String getUserRole(@PathVariable("user-id") Long userId);
}
