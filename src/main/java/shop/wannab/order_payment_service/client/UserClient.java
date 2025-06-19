package shop.wannab.order_payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import shop.wannab.order_payment_service.entity.dto.UserAddressResponse;

import java.util.List;

@FeignClient(name = "wannab-user-service")
public interface UserClient {
    @GetMapping("/api/users/{user-id}/points")
    int getUserPoints(@PathVariable("user-id") Long userId, @RequestHeader("X-USER-ID") Long headerUserId);

    @GetMapping
    List<UserAddressResponse> getAllAddresses(@RequestHeader("X-USER-ID") Long userId);
}
