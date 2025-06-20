package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import shop.wannab.order_payment_service.entity.dto.UserAddressResponse;

import java.util.List;

@FeignClient(name = "wannab-user-service")
public interface UserClient {
    @GetMapping("/api/users/{user-id}/points")
    int getUserPoints(@PathVariable("user-id") Long userId, @RequestHeader("X-USER-ID") Long headerUserId);

    @GetMapping("/api/users/{user-id}/addresses")
    List<UserAddressResponse> getAllAddresses(@RequestHeader("X-USER-ID") Long headerUserId,
                                              @PathVariable("user-id") Long userId);

    //사용한 포인트 차감
    @PatchMapping("/api/users/{user-id}/points")
    void usePoint(@RequestHeader("X-USER-ID") Long headerUserId,
                  @PathVariable("user-id") Long userId,
                  @RequestParam("used") int usedPoint);


    // 이메일 (임시)
    @GetMapping("/api/users/{user-id}/email")
    String getUserEmail(@PathVariable("user-id") Long userId);
}
