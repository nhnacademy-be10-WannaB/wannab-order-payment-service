package shop.wannab.order_payment_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.client.CouponClient;
import shop.wannab.order_payment_service.client.UserClient;
import shop.wannab.order_payment_service.entity.*;
import shop.wannab.order_payment_service.entity.dto.*;
import shop.wannab.order_payment_service.repository.*;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock private DeliveryPolicyService deliveryPolicyService;
    @Mock private UserClient userClient;
    @Mock private BookClient bookClient;
    @Mock private PavingService pavingService;
    @Mock private PaymentService paymentService;
    @Mock private CouponClient couponClient;

    @Mock private OrderRepository orderRepository;
    @Mock private GuestRepository guestRepository;
    @Mock private OrderBookRepository orderBookRepository;
    @Mock private PavingRepository pavingRepository;
    @Mock private OrderItemTempRedisRepository orderItemTempRedisRepository;
    @Mock private CouponUsageTempRedisRepository couponUsageTempRedisRepository;
    @Mock private PointHistoryCreateDtoRepository pointHistoryCreateDtoRepository;

    @InjectMocks
    private OrderService orderService;

    private Long userId = 1L;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void testGetOrdersByUser() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderAt").descending());
        List<Order> orders = List.of(mock(Order.class));
        Page<Order> pageResult = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAllByUserIdAndOrderStatusNot(eq(userId), eq(OrderStatus.FAILED), eq(pageable)))
                .thenReturn(pageResult);

        Page<OrderLookupResponse> result = orderService.getOrdersByUser(userId, page, size);

        assertNotNull(result);
        assertEquals(orders.size(), result.getTotalElements());
        verify(orderRepository, times(1))
                .findAllByUserIdAndOrderStatusNot(eq(userId), eq(OrderStatus.FAILED), eq(pageable));
    }

    @Test
    public void testGetOrder() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(java.util.Optional.of(order));
        when(order.getUserId()).thenReturn(userId);

        OrderBookInfoListDto orderBookInfoListDto = mock(OrderBookInfoListDto.class);
        when(orderBookInfoListDto.getOrderBookInfos()).thenReturn(new ArrayList<>());
        when(bookClient.getOrderBookInfos(any())).thenReturn(orderBookInfoListDto);

        OrderDetailResponse result = orderService.getOrder(1L, userId);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testCancelOrder() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(java.util.Optional.of(order));
        when(order.getUserId()).thenReturn(userId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);

        orderService.cancelOrder(1L, userId);

        verify(orderRepository, times(1)).findById(anyLong());
        verify(order, times(1)).setOrderStatus(OrderStatus.CANCELLED);
    }

    @Test
    public void testRefundOrder() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(java.util.Optional.of(order));
        when(order.getUserId()).thenReturn(userId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED);
        when(order.getShippedAt()).thenReturn(java.time.LocalDateTime.now().minusDays(5));

        orderService.refundOrder(userId, 1L, RefundReason.JUST);

        verify(orderRepository, times(1)).findById(anyLong());
        verify(order, times(1)).setOrderStatus(OrderStatus.RETURNED);
    }

    @Test
    public void testUpdateStatus() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(java.util.Optional.of(order));

        orderService.updateStatus(userId, 1L, OrderStatus.SHIPPING);

        verify(orderRepository, times(1)).findById(anyLong());
        verify(order, times(1)).setOrderStatus(OrderStatus.SHIPPING);
    }

    @Test
    public void testIsReviewable() {
        when(orderBookRepository.existsByObIdAndOrder_OrderStatus(anyLong(), eq(OrderStatus.COMPLETED)))
                .thenReturn(true);

        boolean result = orderService.isReviewable(1L);

        assertTrue(result);
        verify(orderBookRepository, times(1)).existsByObIdAndOrder_OrderStatus(anyLong(), eq(OrderStatus.COMPLETED));
    }

    @Test
    public void testSaveTemporaryOrderInfo() {
        OrderItemListDto dto = mock(OrderItemListDto.class);
        orderService.saveTemporaryOrderInfo(1L, dto);

        verify(orderItemTempRedisRepository, times(1)).saveTemporaryOrderInfo(anyLong(), any());
    }

    @Test
    public void testConsumeTemporaryOrderInfo() {
        when(orderItemTempRedisRepository.getOrderItems(anyLong())).thenReturn(List.of(new CartItem(1L, 1)));

        OrderItemListDto result = orderService.consumeTemporaryOrderInfo(1L);

        assertNotNull(result);
        verify(orderItemTempRedisRepository, times(1)).getOrderItems(anyLong());
    }

    @Test
    public void testCreateOrder() {
        // Given
        BookOrderSubmitDto bookOrder = new BookOrderSubmitDto(1L, 2, null, null);
        OrderSubmitDto orderSubmitDto = new OrderSubmitDto(
                List.of(bookOrder),
                "1",
                100,
                LocalDate.of(2025, 7, 19),
                "tttt@tttmp.com",
                "010-1234-5678",
                "정민수",
                "광주어딘가",
                "1234",
                null
        );

        doNothing().when(bookClient).validateOrderItems(any());

        BookIdTitlePriceDto bookInfo = new BookIdTitlePriceDto(1L, "테스트책", 10000);
        BookIdTitlePriceListDto bookInfoList = new BookIdTitlePriceListDto(List.of(bookInfo));
        when(bookClient.getBookSimpleInfos(any())).thenReturn(bookInfoList);

        DeliveryPolicy policy = new DeliveryPolicy();
        policy.setFee(2500);
        policy.setMinPrice(0);
        policy.setName("기본배송");
        when(deliveryPolicyService.findApplicablePolicy(anyInt())).thenReturn(policy);

        when(couponClient.getApplyCoupons(any(), any())).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(orderBookRepository.saveAll(any())).thenReturn(Collections.emptyList());

        doNothing().when(pointHistoryCreateDtoRepository).save(any(PointHistoryCreateDTO.class));

        OrderInfoForPayment result = orderService.createOrder(orderSubmitDto, 1L);

        assertNotNull(result);
    }

    @Test
    public void testGetOrders_admin() {
        OrderSearchDto searchDto = new OrderSearchDto();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("orderAt").descending());
        Page<OrderLookupResponse> page = new PageImpl<>(List.of());
        when(orderRepository.searchOrders(any(), any())).thenReturn(page);

        Page<OrderLookupResponse> result = orderService.getOrders(searchDto, 0, 10);

        assertNotNull(result);
    }
    @Test
    public void testGetOrderForGuest_valid() {
        Long orderId = 1L;
        String password = "1234";
        Order order = mock(Order.class);
        Guest guest = new Guest(password, order);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(guestRepository.findByOrder_Id(orderId)).thenReturn(Optional.of(guest));

        OrderBookInfoListDto bookInfoListDto = new OrderBookInfoListDto(List.of());
        when(bookClient.getOrderBookInfos(any())).thenReturn(bookInfoListDto);

        OrderDetailResponse result = orderService.getOrderForGuest(orderId, password);
        assertNotNull(result);
    }

    @Test
    public void testCancelGuestOrder_valid() {
        Long orderId = 1L;
        String password = "1234";
        Order order = mock(Order.class);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);
        Guest guest = new Guest(password, order);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(guestRepository.findByOrder_Id(orderId)).thenReturn(Optional.of(guest));

        orderService.cancelGuestOrder(orderId, password);

        verify(order, times(1)).setOrderStatus(OrderStatus.CANCELLED);
        verify(paymentService, times(1)).paymentCancel(eq(orderId), anyInt());
    }

    @Test
    public void testRefundGuestOrder_damaged_within30days() {
        Long orderId = 1L;
        String password = "1234";
        Order order = mock(Order.class);
        Guest guest = new Guest(password, order);

        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED);
        when(order.getShippedAt()).thenReturn(LocalDateTime.now().minusDays(5));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(guestRepository.findByOrder_Id(orderId)).thenReturn(Optional.of(guest));

        orderService.refundGuestOrder(orderId, password, RefundReason.DAMAGED);

        verify(order, times(1)).setOrderStatus(OrderStatus.RETURNED);
        verify(paymentService, times(1)).paymentCancel(eq(orderId), anyInt());
    }


}