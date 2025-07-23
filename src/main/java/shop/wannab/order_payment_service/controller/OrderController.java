package shop.wannab.order_payment_service.controller;

import feign.FeignException;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.RefundReason;
import shop.wannab.order_payment_service.entity.dto.GuestOrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderDetailResponse;
import shop.wannab.order_payment_service.entity.dto.OrderInfoForPayment;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderPageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;
import shop.wannab.order_payment_service.entity.dto.OrderSubmitDto;
import shop.wannab.order_payment_service.service.OrderService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final BookClient bookClient;
    private final OrderService orderService;

    @PostMapping("/orders/items")
    void produceOrderPageDto(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam(required = false) Long guestId, @RequestBody OrderItemListDto orderItemListDto) {
        if (Objects.nonNull(userId)) {
            orderService.saveTemporaryOrderInfo(userId, orderItemListDto);
        } else if (Objects.isNull(userId) && Objects.nonNull(guestId)) {
            orderService.saveTemporaryOrderInfo(guestId, orderItemListDto);
        }
    }

    @PostMapping("/orders")
    public OrderPageRequestDto consumeOrderPageDto(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam(required = false) Long guestId) {
        OrderItemListDto orderItemListDto = null;

        if (Objects.nonNull(userId)) {
            orderItemListDto = orderService.consumeTemporaryOrderInfo(userId);
        } else if (Objects.isNull(userId) && Objects.nonNull(guestId)) {
            orderItemListDto = orderService.consumeTemporaryOrderInfo(guestId);
        }

        try {
            bookClient.validateOrderItems(orderItemListDto);
        } catch (FeignException.BadRequest e) {
            log.debug("FeignException : {}", e.getMessage());
            throw e;
        }
        if (Objects.nonNull(userId)) {
            return orderService.createOrderPageRequestDto(userId, orderItemListDto);
        }
        return orderService.createOrderPageRequestDto(guestId, orderItemListDto);

    }


    @PostMapping(path = "/orders/new")
    OrderInfoForPayment processOrder(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam(required = false) Long guestId, @RequestBody @Valid OrderSubmitDto orderSubmitDto) {
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
                                                                  @Valid @ModelAttribute OrderSearchDto orderSearchDto,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size) {
        Page<OrderLookupResponse> orders = orderService.getOrders(orderSearchDto, page, size);
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
    @PostMapping("/orders/guest")
    public ResponseEntity<OrderDetailResponse> getGuestOrderDetail(@RequestBody @Valid GuestOrderRequest request){

        return ResponseEntity.ok(orderService.getOrderForGuest(request.getOrderId(), request.getPassword()));
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
    public ResponseEntity<Void> cancelGuestOrder(@RequestBody @Valid GuestOrderRequest request){
        orderService.cancelGuestOrder(request.getOrderId(), request.getPassword());
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
    public ResponseEntity<Void> refundGuestOrder(@RequestBody @Valid GuestOrderRequest request,
                                                 @RequestParam RefundReason reason){
        orderService.refundGuestOrder(request.getOrderId(), request.getPassword(), reason);
        return ResponseEntity.ok().build();
    }


    //리뷰가능여부확인
    @GetMapping("/orders/review-check")
    public boolean isReviewable(@RequestParam Long obId) {
        return orderService.isReviewable(obId);
    }

}
