package shop.wannab.order_payment_service.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.RefundReason;
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
                                                     @RequestHeader("X-User-Id") Long id){
        OrderResponse response = orderService.createOrder(request, id);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //주문목록조회 (회원)
    @GetMapping
    public ResponseEntity<Page<OrderListResponse>> getOrdersByUser(@RequestHeader("X-User-Id") Long userId,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size){

        Page<OrderListResponse> orders = orderService.getOrdersByUser(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문전체조회(관리자)
    @GetMapping("/all")
    public ResponseEntity<Page<OrderListResponse>> getAllOrders(@RequestHeader("X-User-Id") Long userId,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size){
        Page<OrderListResponse> orders = orderService.getOrders(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문상세조회(회원)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@RequestHeader("X-User-Id") Long userId,
                                                              @PathVariable Long orderId){
        OrderDetailResponse detail = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(detail);
    }

    //주문상세조회(비회원)
    @GetMapping("/guest")
    public ResponseEntity<OrderDetailResponse> getGuestOrderDetail(@RequestParam Long orderId,
                                                                   @RequestParam String password){

        return ResponseEntity.ok(orderService.getOrderForGuest(orderId, password));
    }

    //주문취소(결제취소) 회원
    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long orderId){
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok().build();
    }

    //주문취소(결제취소) 비회원
    @PatchMapping("/guest")
    public ResponseEntity<Void> cancelGuestOrder(@RequestParam Long orderId,
                                                 @RequestParam String password){
        orderService.cancelGuestOrder(orderId, password);
        return ResponseEntity.ok().build();
    }

    //주문상태변경 (관리자)
    @PatchMapping("{orderId}/status")
    public ResponseEntity<Void> updateStatus(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable Long orderId,
                                             @RequestParam OrderStatus newStatus){
        orderService.updateStatus(userId, orderId, newStatus);
        return ResponseEntity.ok().build();
    }

    //반품(회원)
    @PatchMapping("/{orderId}/refund")
    public ResponseEntity<Void> refundOrder(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long orderId,
                                            @RequestParam RefundReason reason){   //reason은 스크롤로 (제품불량, 단순변심)
        orderService.refundOrder(userId, orderId, reason);
        return ResponseEntity.ok().build();
    }

    //반품(비회원)
    @PatchMapping("guest/refund")
    public ResponseEntity<Void> refundGuestOrder(@RequestParam Long orderId,
                                                 @RequestParam String password,
                                                 @RequestParam RefundReason reason){
        orderService.refundGuestOrder(orderId, password, reason);
        return ResponseEntity.ok().build();
    }


}
