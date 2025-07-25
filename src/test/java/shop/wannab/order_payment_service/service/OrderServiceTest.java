package shop.wannab.order_payment_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

import static org.assertj.core.api.Assertions.assertThat;
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
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
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
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(order.getUserId()).thenReturn(userId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.PAID);

        orderService.cancelOrder(1L, userId);

        verify(orderRepository, times(1)).findById(anyLong());
        verify(order, times(1)).setOrderStatus(OrderStatus.CANCELLED);
    }

    @Test
    public void testRefundOrder() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(order.getUserId()).thenReturn(userId);
        when(order.getOrderStatus()).thenReturn(OrderStatus.COMPLETED);
        when(order.getShippedAt()).thenReturn(LocalDateTime.now().minusDays(5));

        orderService.refundOrder(userId, 1L, RefundReason.JUST);

        verify(orderRepository, times(1)).findById(anyLong());
        verify(order, times(1)).setOrderStatus(OrderStatus.RETURNED);
    }

    @Test
    public void testUpdateStatus() {
        Order order = mock(Order.class);
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

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
    void testTotalPavingPriceApplied() {
    // Given
    BookOrderSubmitDto bookOrder = new BookOrderSubmitDto(1L, 1, 5L, null); // pavingId = 5L
    OrderSubmitDto orderSubmitDto = new OrderSubmitDto(
            List.of(bookOrder),
            "1",
            0,
            LocalDate.now(),
            "abc@abc.com",
            "010-1111-2222",
            "홍길동",
            "서울시 어딘가",
            "1234",
            null
    );

    doNothing().when(bookClient).validateOrderItems(any());

    BookIdTitlePriceDto bookInfo = new BookIdTitlePriceDto(1L, "테스트책", 10000);
    when(bookClient.getBookSimpleInfos(any())).thenReturn(new BookIdTitlePriceListDto(List.of(bookInfo)));

    DeliveryPolicy policy = new DeliveryPolicy();
    policy.setFee(2500);
    policy.setMinPrice(0);
    policy.setName("기본배송");
    when(deliveryPolicyService.findApplicablePolicy(anyInt())).thenReturn(policy);

    when(couponClient.getApplyCoupons(any(), any())).thenReturn(ResponseEntity.ok(Collections.emptyList()));
    when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(orderBookRepository.saveAll(any())).thenReturn(Collections.emptyList());
    doNothing().when(pointHistoryCreateDtoRepository).save(any());

    Paving paving = new Paving(5L,"flowerPaving", 2000);
    when(pavingRepository.findById(5L)).thenReturn(Optional.of(paving));

    // When
    OrderInfoForPayment result = orderService.createOrder(orderSubmitDto, 1L);

    // Then
    assertNotNull(result);
}

    @Test
    void testCreateOrder_withDiscount() {
    BookOrderSubmitDto bookOrder = new BookOrderSubmitDto(1L, 2, null, 101L); // 책에 쿠폰 적용
    OrderSubmitDto orderSubmitDto = new OrderSubmitDto(
            List.of(bookOrder),
            null,
            10000,
            LocalDate.of(2025, 7, 19),
            "test@example.com",
            "010-1234-5678",
            "홍길동",
            "서울시",
            "1234",
            null
    );

    doNothing().when(bookClient).validateOrderItems(any());
    when(bookClient.getBookSimpleInfos(any()))
            .thenReturn(new BookIdTitlePriceListDto(List.of(new BookIdTitlePriceDto(1L, "테스트책", 5000))));

    DeliveryPolicy policy = new DeliveryPolicy();
    policy.setFee(2500);
    policy.setMinPrice(0);
    policy.setName("기본배송");
    when(deliveryPolicyService.findApplicablePolicy(anyInt())).thenReturn(policy);

    List<TryApplyCouponsResponseDto> couponDiscountInfos = List.of(
            new TryApplyCouponsResponseDto(101L, 1000, DiscountType.FIXED, 1L)
    );
    when(couponClient.getApplyCoupons(eq(userId), any()))
        .thenReturn(ResponseEntity.ok(couponDiscountInfos));

    when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(orderBookRepository.saveAll(any())).thenReturn(Collections.emptyList());
    doNothing().when(pointHistoryCreateDtoRepository).save(any());

    // When
    OrderInfoForPayment result = orderService.createOrder(orderSubmitDto, userId);

    // Then
    assertNotNull(result);
    // 할인 금액이 반영된 상태나 로그, 혹은 내부 필드가 있다면 검증
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

    @Test
    void createOrderPageRequestDto_guest_returnsDtoWithDefaults() {
        // given
        Long guestId = -1L;
        OrderItemListDto orderItemListDto = new OrderItemListDto(List.of(
                new CartItem(101L, 2)
        ));

        OrderBookInfo orderBookInfo = new OrderBookInfo();
        orderBookInfo.setBookId(1L);
        orderBookInfo.setTitle("Effective Java");
        orderBookInfo.setOriginPrice(50000);
        orderBookInfo.setSalesPrice(40000);
        orderBookInfo.setQuantity(2);
        orderBookInfo.setThumbnailUrl("https://example.com/thumbnail.jpg");

        List<OrderBookInfo> orderBookInfoList = List.of(
                orderBookInfo
        );
        OrderBookInfoListDto orderBookInfos = new OrderBookInfoListDto(orderBookInfoList);
        List<PavingResponse> pavingList = List.of(new PavingResponse(1L, "포장1", 500));

        // mock
        when(bookClient.getOrderBookInfos(orderItemListDto)).thenReturn(orderBookInfos);
        when(pavingService.getPavingList()).thenReturn(pavingList);

        // 배송비가 100원인 정책 객체 생성
        DeliveryPolicy mockPolicy = new DeliveryPolicy();
        mockPolicy.setFee(100);

        // deliveryPolicyService가 호출될 때 mockPolicy를 반환하도록 설정
        when(deliveryPolicyService.findApplicablePolicy(anyInt())).thenReturn(mockPolicy);

        // when
        OrderPageRequestDto result = orderService.createOrderPageRequestDto(guestId, orderItemListDto);

        // then
        assertThat(result.getUserPoints()).isZero();
        assertThat(result.getPavingList()).isEqualTo(pavingList);
        assertThat(result.getOrderCoupons()).isEmpty();
        assertEquals(100, result.getShippingFee());
    }

    @Test
    void createOrderPageRequestDto_User_returnsPopulatedDto() {
        // given
        List<CartItem> items = List.of(new CartItem(10L, 2));
        OrderItemListDto orderItemListDto = new OrderItemListDto(items);

        // bookClient.getOrderBookInfos
        OrderBookInfo bookInfo = new OrderBookInfo();
        bookInfo.setBookId(1L);
        bookInfo.setTitle("Effective Java");
        bookInfo.setOriginPrice(50000);
        bookInfo.setSalesPrice(40000);
        bookInfo.setQuantity(2);
        bookInfo.setThumbnailUrl("https://example.com/thumbnail.jpg");
        OrderBookInfoListDto bookInfos = new OrderBookInfoListDto(List.of(bookInfo));
        when(bookClient.getOrderBookInfos(orderItemListDto)).thenReturn(bookInfos);

        // userClient.getUserPoints
        when(userClient.getUserPoints(userId)).thenReturn(5000);

        // userClient.getAllAddresses
        List<UserAddressResponse> addresses = List.of(new UserAddressResponse());
        when(userClient.getAllAddresses(userId)).thenReturn(addresses);

        // pavingService.getPavingList
        List<PavingResponse> pavingList = List.of(new PavingResponse());
        when(pavingService.getPavingList()).thenReturn(pavingList);

        // couponClient.getApplicableCoupons
        Map<Long, List<BookCouponDto>> itemCoupons = Map.of(10L, List.of());
        List<OrderCouponDto> orderCoupons = List.of(new OrderCouponDto());
        ApplicableCouponsDto couponsDto = new ApplicableCouponsDto(itemCoupons, orderCoupons);
        when(couponClient.getApplicableCoupons(eq(userId), any(OrderCouponsRequestDto.class)))
                .thenReturn(ResponseEntity.ok(couponsDto));

        // deliveryPolicyService.getShippingFee
        when(deliveryPolicyService.findApplicablePolicy(anyInt()))
                .thenReturn(new DeliveryPolicy(1L, "기본배송비", 3000, 0));

        // when
        OrderPageRequestDto result = orderService.createOrderPageRequestDto(userId, orderItemListDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserPoints()).isEqualTo(5000);
        assertThat(result.getPavingList()).isEqualTo(pavingList);
    }

}