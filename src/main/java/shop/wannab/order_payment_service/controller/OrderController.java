package shop.wannab.order_payment_service.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.RefundReason;
import shop.wannab.order_payment_service.entity.dto.*;
import shop.wannab.order_payment_service.service.OrderService;

import java.util.Objects;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final BookClient bookClient;
    private final OrderService orderService;

    @PostMapping
    public OrderPageRequestDto getNecesaryOrderInfo(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam Long guestId, @RequestBody OrderItemListDto orderItemListDto) {
        try {
            bookClient.validateOrderItems(orderItemListDto);
        } catch (FeignException.BadRequest e) {
            throw e;
        }
        if (Objects.nonNull(userId)) {
            return orderService.createOrderPageRequestDto(userId, orderItemListDto);
        }
        return orderService.createOrderPageRequestDto(guestId, orderItemListDto);

    }


    @PostMapping("/orders/new")
    OrderInfoForPayment processOrder(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam Long guestId, @RequestBody OrderSubmitDto orderSubmitDto) {
        if (Objects.nonNull(userId)) {
            return orderService.createOrder(orderSubmitDto, userId);
        }
        return orderService.createOrder(orderSubmitDto, guestId);
    }


    //주문목록조회 (회원)
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderLookupResponse>> getOrdersByUser(@RequestHeader("X-USER-ID") Long userId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size){

        Page<OrderLookupResponse> orders = orderService.getOrdersByUser(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문전체조회(관리자)
    @GetMapping("/admin/orders")
    public ResponseEntity<Page<OrderLookupResponse>> getAllOrders(@RequestHeader("X-USER-ID") Long userId,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size){
        Page<OrderLookupResponse> orders = orderService.getOrders(userId, page, size);
        return ResponseEntity.ok(orders);
    }

    //주문상세조회(회원)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@RequestHeader("X-USER-ID") Long userId,
                                                              @PathVariable Long orderId){
        OrderDetailResponse detail = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(detail);
    }

    //주문상세조회(비회원)
    @GetMapping("/orders/guest")
    public ResponseEntity<OrderDetailResponse> getGuestOrderDetail(@RequestParam Long orderId,
                                                                   @RequestParam String password){

        return ResponseEntity.ok(orderService.getOrderForGuest(orderId, password));
    }

    //주문취소(결제취소) 회원
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@RequestHeader("X-USER-ID") Long userId,
                                            @PathVariable Long orderId){
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok().build();
    }

    //주문취소(결제취소) 비회원
    @PostMapping("/orders/guest/cancel")
    public ResponseEntity<Void> cancelGuestOrder(@RequestParam Long orderId,
                                                 @RequestParam String password){
        orderService.cancelGuestOrder(orderId, password);
        return ResponseEntity.ok().build();
    }

    //주문상태변경 (관리자)
    @PostMapping("/admin/orders/{orderId}")
    public ResponseEntity<Void> updateStatus(@RequestHeader("X-USER-ID") Long userId,
                                             @PathVariable Long orderId,
                                             @RequestParam OrderStatus newStatus){
        orderService.updateStatus(userId, orderId, newStatus);
        return ResponseEntity.ok().build();
    }

    //반품(회원)
    @PostMapping("/orders/{orderId}/refund")
    public ResponseEntity<Void> refundOrder(@RequestHeader("X-USER-ID") Long userId,
                                            @PathVariable Long orderId,
                                            @RequestParam RefundReason reason){   //reason은 스크롤로 (제품불량, 단순변심)
        orderService.refundOrder(userId, orderId, reason);
        return ResponseEntity.ok().build();
    }

    //반품(비회원)
    @PostMapping("/orders/guest/refund")
    public ResponseEntity<Void> refundGuestOrder(@RequestParam Long orderId,
                                                 @RequestParam String password,
                                                 @RequestParam RefundReason reason){
        orderService.refundGuestOrder(orderId, password, reason);
        return ResponseEntity.ok().build();
    }


}
