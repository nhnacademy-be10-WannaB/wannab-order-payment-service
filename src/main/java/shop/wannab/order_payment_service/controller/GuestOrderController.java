package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.entity.dto.OrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;
import shop.wannab.order_payment_service.service.OrderService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/guest/orders")
public class GuestOrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createGuestOrder(@RequestBody OrderRequest request,
                                                          @RequestHeader("X-UserId") Long id){
        OrderResponse response = orderService.createOrder(request, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
