package shop.wannab.order_payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.RefundReason;
import shop.wannab.order_payment_service.entity.dto.BookOrderSubmitDto;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.GuestOrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderDetailResponse;
import shop.wannab.order_payment_service.entity.dto.OrderInfoForPayment;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderPageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;
import shop.wannab.order_payment_service.entity.dto.OrderSubmitDto;
import shop.wannab.order_payment_service.service.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("ci")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookClient bookClient;

    @MockBean
    private OrderService orderService;

    private OrderItemListDto sampleOrderItemListDto;
    private OrderSubmitDto sampleOrderSubmitDto;
    private GuestOrderRequest sampleGuestOrderRequest;

    @BeforeEach
    void setUp() {
        sampleOrderSubmitDto = new OrderSubmitDto();

        sampleOrderSubmitDto.setUserId("1");
        sampleOrderSubmitDto.setUsedPoints(100);
        sampleOrderSubmitDto.setDeliveryRequestAt(LocalDate.now().plusDays(7)); // 희망 배송일은 미래 날짜
        sampleOrderSubmitDto.setEmail("test@example.com");
        sampleOrderSubmitDto.setRecipientPhoneNumber("010-1234-5678");
        sampleOrderSubmitDto.setRecipientName("테스트 수령인");
        sampleOrderSubmitDto.setRecipientAddress("서울시 테스트구 테스트동");
        sampleOrderSubmitDto.setGuestPassword(null);
        sampleOrderSubmitDto.setAppliedOrderCouponId(5L);

        sampleGuestOrderRequest = new GuestOrderRequest();
        sampleGuestOrderRequest.setOrderId(123L);
        sampleGuestOrderRequest.setPassword("guest123");


        sampleOrderItemListDto = new OrderItemListDto(
                List.of(new CartItem(1L, 2))
        );

    }


    @Test
    @DisplayName("임시 주문 정보 저장 - 회원 ID로 성공")
    void produceOrderPageDto_memberSuccess() throws Exception {
        Long userId = 1L;

        doNothing().when(orderService).saveTemporaryOrderInfo(eq(userId), any(OrderItemListDto.class));

        mockMvc.perform(post("/api/orders/items")
                        .header("X-USER-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrderItemListDto)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).saveTemporaryOrderInfo(eq(userId), any(OrderItemListDto.class));
    }

    @Test
    @DisplayName("임시 주문 정보 저장 - 비회원 ID로 성공")
    void produceOrderPageDto_guestSuccess() throws Exception {
        Long guestId = 100L;

        doNothing().when(orderService).saveTemporaryOrderInfo(eq(guestId), any(OrderItemListDto.class));

        mockMvc.perform(post("/api/orders/items")
                        .param("guestId", String.valueOf(guestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrderItemListDto)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).saveTemporaryOrderInfo(eq(guestId), any(OrderItemListDto.class));
    }







    @Test
    @DisplayName("회원 주문 목록 조회 성공")
    void getOrdersByUser_success() throws Exception {
        Long userId = 1L;
        int page = 0;
        int size = 10;
        // OrderLookupResponse의 수동 생성자 (id, name, orderAt, status, shippedAt, totalPrice)를 사용
        OrderLookupResponse order1 = new OrderLookupResponse(
                1L, "첫 번째 주문", LocalDateTime.now().minusDays(5), OrderStatus.COMPLETED, LocalDateTime.now().minusDays(3), 15000);
        OrderLookupResponse order2 = new OrderLookupResponse(
                2L, "두 번째 주문", LocalDateTime.now().minusDays(10), OrderStatus.SHIPPING, LocalDateTime.now().minusDays(8), 25000);

        List<OrderLookupResponse> orderList = Arrays.asList(order1, order2);
        Page<OrderLookupResponse> expectedPage = new PageImpl<>(orderList, PageRequest.of(page, size), orderList.size());

        when(orderService.getOrdersByUser(eq(userId), eq(page), eq(size))).thenReturn(expectedPage);

        mockMvc.perform(get("/api/orders")
                        .header("X-USER-ID", userId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(order1.getOrderId()))
                .andExpect(jsonPath("$.content[0].orderName").value(order1.getOrderName()))
                .andExpect(jsonPath("$.content[0].orderStatus").value(order1.getOrderStatus().name()))
                .andExpect(jsonPath("$.content[0].totalPrice").value(order1.getTotalPrice()))
                .andExpect(jsonPath("$.content[1].orderId").value(order2.getOrderId()));

        verify(orderService, times(1)).getOrdersByUser(eq(userId), eq(page), eq(size));
    }


    @Test
    @DisplayName("관리자 전체 주문 목록 조회 성공")
    void getAllOrders_success() throws Exception {
        Long adminId = 999L;
        int page = 0;
        int size = 20;
        OrderSearchDto searchDto = new OrderSearchDto();
        searchDto.setOrderStatus(OrderStatus.COMPLETED);

        OrderLookupResponse adminOrder1 = new OrderLookupResponse(
                10L, "관리자 주문1", LocalDateTime.now().minusMonths(1), OrderStatus.COMPLETED, LocalDateTime.now().minusMonths(1).plusDays(2), 50000);
        List<OrderLookupResponse> orderList = Collections.singletonList(adminOrder1);
        Page<OrderLookupResponse> expectedPage = new PageImpl<>(orderList, PageRequest.of(page, size), orderList.size());

        when(orderService.getOrders(any(OrderSearchDto.class), eq(page), eq(size))).thenReturn(expectedPage);

        mockMvc.perform(get("/api/admin/orders")
                        .header("X-USER-ID", adminId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("orderStatus", searchDto.getOrderStatus().name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value(adminOrder1.getOrderId()));

        verify(orderService, times(1)).getOrders(any(OrderSearchDto.class), eq(page), eq(size));
    }




    @Test
    @DisplayName("회원 주문 취소 성공")
    void cancelOrder_success() throws Exception {
        Long userId = 1L;
        Long orderId = 123L;

        doNothing().when(orderService).cancelOrder(eq(orderId), eq(userId));

        mockMvc.perform(post("/api/orders/{orderId}/cancel", orderId)
                        .header("X-USER-ID", userId))
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancelOrder(eq(orderId), eq(userId));
    }


    @Test
    @DisplayName("비회원 주문 취소 성공")
    void cancelGuestOrder_success() throws Exception {
        doNothing().when(orderService).cancelGuestOrder(eq(sampleGuestOrderRequest.getOrderId()), eq(sampleGuestOrderRequest.getPassword()));

        mockMvc.perform(post("/api/orders/guest/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGuestOrderRequest)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).cancelGuestOrder(eq(sampleGuestOrderRequest.getOrderId()), eq(sampleGuestOrderRequest.getPassword()));
    }


    @Test
    @DisplayName("관리자 주문 상태 변경 성공")
    void updateStatus_success() throws Exception {
        Long adminId = 999L;
        Long orderId = 123L;
        OrderStatus newStatus = OrderStatus.COMPLETED;

        doNothing().when(orderService).updateStatus(eq(adminId), eq(orderId), eq(newStatus));

        mockMvc.perform(post("/api/admin/orders/{orderId}", orderId)
                        .header("X-USER-ID", adminId)
                        .param("newStatus", newStatus.name()))
                .andExpect(status().isOk());

        verify(orderService, times(1)).updateStatus(eq(adminId), eq(orderId), eq(newStatus));
    }


    @Test
    @DisplayName("회원 반품 요청 성공")
    void refundOrder_success() throws Exception {
        Long userId = 1L;
        Long orderId = 123L;
        RefundReason reason = RefundReason.DAMAGED;

        doNothing().when(orderService).refundOrder(eq(userId), eq(orderId), eq(reason));

        mockMvc.perform(post("/api/orders/{orderId}/refund", orderId)
                        .header("X-USER-ID", userId)
                        .param("reason", reason.name()))
                .andExpect(status().isOk());

        verify(orderService, times(1)).refundOrder(eq(userId), eq(orderId), eq(reason));
    }


    @Test
    @DisplayName("비회원 반품 요청 성공")
    void refundGuestOrder_success() throws Exception {
        RefundReason reason = RefundReason.JUST;

        doNothing().when(orderService).refundGuestOrder(eq(sampleGuestOrderRequest.getOrderId()), eq(sampleGuestOrderRequest.getPassword()), eq(reason));

        mockMvc.perform(post("/api/orders/guest/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGuestOrderRequest))
                        .param("reason", reason.name()))
                .andExpect(status().isOk());

        verify(orderService, times(1)).refundGuestOrder(eq(sampleGuestOrderRequest.getOrderId()), eq(sampleGuestOrderRequest.getPassword()), eq(reason));
    }


    @Test
    @DisplayName("리뷰 가능 여부 확인 - 가능")
    void isReviewable_true() throws Exception {
        Long obId = 123L;
        when(orderService.isReviewable(eq(obId))).thenReturn(true);

        mockMvc.perform(get("/api/orders/review-check")
                        .param("obId", String.valueOf(obId)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderService, times(1)).isReviewable(eq(obId));
    }

    @Test
    @DisplayName("리뷰 가능 여부 확인 - 불가능")
    void isReviewable_false() throws Exception {
        Long obId = 456L;
        when(orderService.isReviewable(eq(obId))).thenReturn(false);

        mockMvc.perform(get("/api/orders/review-check")
                        .param("obId", String.valueOf(obId)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(orderService, times(1)).isReviewable(eq(obId));
    }
}