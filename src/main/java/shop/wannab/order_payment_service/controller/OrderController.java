package shop.wannab.order_payment_service.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    //주문목록조회 (회원)
    @GetMapping
    public ResponseEntity<Page<OrderListResponse>> getOrdersByUser(@RequestHeader("User-Id") Long userId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size){

        Page<OrderListResponse> orders = orderService.getOrdersByUser(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문전체조회(관리자)
    @GetMapping("/all")
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(@RequestHeader("User-Id") Long userId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size){
        Page<OrderListResponse> orders = orderService.getOrders(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문상세조회(회원)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(//@RequestHeader("User-Id") Long userId,
                                                              @PathVariable Long orderId){
        OrderDetailResponse detail = orderService.getOrder(orderId);
        return ResponseEntity.ok(detail);
    }

    //주문상세조회(비회원)
    @GetMapping("/orders/guest")
    public ResponseEntity<OrderDetailResponse> getGuestOrderDetail(@RequestParam Long orderId,
                                                                   @RequestParam String password){

        return ResponseEntity.ok(orderService.getOrderForGuest(orderId, password));
    }

    //주문취소

    //주문상태변경
}
