package shop.wannab.order_payment_service.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.dto.*;
import shop.wannab.order_payment_service.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final BookClient bookClient;
    private final OrderService orderService;


    @PostMapping
    public OrderPageRequestDto getNecesaryOrderInfo(@RequestHeader("X-User-Id") Long userId, @RequestBody OrderItemListDto orderItemListDto) {
        try {
            bookClient.validateOrderItems(orderItemListDto);
        } catch (FeignException.BadRequest e) {
            throw e;
        }
        return orderService.createOrderPageRequestDto(userId, orderItemListDto);
    }


    //주문생성
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request,
                                                     @RequestHeader("X-UserId") Long id){
        OrderResponse response = orderService.createOrder(request, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
