package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.entity.dto.GuestOrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;
import shop.wannab.order_payment_service.service.GuestOrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest/orders")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createGuestOrder(@RequestBody GuestOrderRequest request){
        OrderResponse response = guestOrderService.createGuestOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
