package shop.wannab.order_payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;

import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.RefundReason;
import shop.wannab.order_payment_service.entity.dto.*;
import shop.wannab.order_payment_service.service.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ActiveProfiles("ci")
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
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


        List<BookOrderSubmitDto> bookOrderSubmitDtos = List.of(
            new BookOrderSubmitDto(1L, 2, null, null)
            );
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
        sampleOrderSubmitDto.setBookOrderSubmitDtos(bookOrderSubmitDtos);

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
            .andExpect(status().isOk())
            .andDo(document("orders-temporary-member",
                    requestHeaders(
                            headerWithName("X-USER-ID").description("회원 ID")
                    ),
                    requestFields(
                            fieldWithPath("orderItems[].bookId").description("도서 ID"),
                            fieldWithPath("orderItems[].quantity").description("도서 수량")

                    )
            ));

    verify(orderService, times(1)).saveTemporaryOrderInfo(eq(userId), any(OrderItemListDto.class));
}


    @Test
@DisplayName("임시 주문 정보 저장 - 비회원 ID로 성공")
void produceOrderPageDto_guestSuccess() throws Exception {
    Long guestId = 100L;

    OrderItemListDto orderItemListDto = new OrderItemListDto(
        List.of(new CartItem(1L, 2))
    );

    doNothing().when(orderService).saveTemporaryOrderInfo(eq(guestId), any(OrderItemListDto.class));

    mockMvc.perform(post("/api/orders/items")
                    .param("guestId", String.valueOf(guestId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderItemListDto)))
            .andExpect(status().isOk())
            .andDo(document("order-item-save-guest",
                    queryParameters(
                            parameterWithName("guestId").description("비회원 ID")
                    ),
                    requestFields(
                            fieldWithPath("orderItems[].bookId").description("도서 ID"),
                            fieldWithPath("orderItems[].quantity").description("도서 수량")
                    )
            ));

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
            .andExpect(jsonPath("$.content[0].orderId").value(adminOrder1.getOrderId()))
            .andDo(document("admin-get-all-orders",
                    requestHeaders(
                            headerWithName("X-USER-ID").description("관리자 사용자 ID")
                    ),
                    queryParameters(
                            parameterWithName("page").description("페이지 번호"),
                            parameterWithName("size").description("페이지 크기"),
                            parameterWithName("orderStatus").description("주문 상태 필터")
                    ),
                    responseFields(
                            fieldWithPath("content[].orderId").description("주문 ID"),
                            fieldWithPath("content[].orderName").description("주문명"),
                            fieldWithPath("content[].orderAt").description("주문 생성일시"),
                            fieldWithPath("content[].orderStatus").description("주문 상태"),
                            fieldWithPath("content[].shippedAt").description("배송 완료 일시"),
                            fieldWithPath("content[].totalPrice").description("총 주문 금액"),
                            fieldWithPath("content[].thumbnailUrl").optional().description("상품 썸네일 이미지 URL"),

                            fieldWithPath("last").description("마지막 페이지 여부"),
                            fieldWithPath("totalElements").description("전체 주문 수"),
                            fieldWithPath("totalPages").description("전체 페이지 수"),
                            fieldWithPath("size").description("페이지 크기"),
                            fieldWithPath("number").description("현재 페이지 번호"),
                            fieldWithPath("first").description("첫 페이지 여부"),
                            fieldWithPath("numberOfElements").description("현재 페이지 요소 수"),
                            fieldWithPath("empty").description("현재 페이지가 비어있는지 여부"),

                            fieldWithPath("pageable.pageNumber").description("현재 페이지 번호"),
                            fieldWithPath("pageable.pageSize").description("페이지 크기"),
                            fieldWithPath("pageable.sort.empty").ignored(),
                            fieldWithPath("pageable.sort.sorted").ignored(),
                            fieldWithPath("pageable.sort.unsorted").ignored(),
                            fieldWithPath("pageable.offset").ignored(),
                            fieldWithPath("pageable.paged").ignored(),
                            fieldWithPath("pageable.unpaged").ignored(),

                            fieldWithPath("sort.empty").ignored(),
                            fieldWithPath("sort.sorted").ignored(),
                            fieldWithPath("sort.unsorted").ignored()
                    )
            ));

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
            .andExpect(status().isOk())
            .andDo(document("order-cancel-success",
                    requestHeaders(
                            headerWithName("X-USER-ID").description("회원 사용자 ID")
                    ),
                    pathParameters(
                            parameterWithName("orderId").description("취소할 주문 ID")
                    )
            ));

    verify(orderService, times(1)).cancelOrder(eq(orderId), eq(userId));
}




    @Test
@DisplayName("관리자 주문 상태 변경 성공")
void updateStatus_success() throws Exception {
    Long adminId = 999L;
    Long orderId = 123L;
    OrderStatus newStatus = OrderStatus.COMPLETED;

    doNothing().when(orderService).updateStatus(eq(adminId), eq(orderId), eq(newStatus));

    mockMvc.perform(post("/api/admin/orders/{orderId}?newStatus=" + newStatus.name(), orderId)
                .header("X-USER-ID", adminId))
        .andExpect(status().isOk())
        .andDo(document("admin-update-order-status",
                requestHeaders(
                        headerWithName("X-USER-ID").description("관리자 사용자 ID")
                ),
                pathParameters(
                        parameterWithName("orderId").description("주문 ID")
                ),
                queryParameters(
                        parameterWithName("newStatus").description("변경할 주문 상태")
                )
        ));

    verify(orderService, times(1)).updateStatus(eq(adminId), eq(orderId), eq(newStatus));
}



    @Test
@DisplayName("회원 반품 요청 성공")
void refundOrder_success() throws Exception {
    Long userId = 1L;
    Long orderId = 123L;
    RefundReason reason = RefundReason.DAMAGED;

    doNothing().when(orderService).refundOrder(eq(userId), eq(orderId), eq(reason));

    mockMvc.perform(post("/api/orders/{orderId}/refund?reason={reason}", orderId, reason.name())
                .header("X-USER-ID", userId))
        .andExpect(status().isOk())
        .andDo(document("orders-refund",
                requestHeaders(
                        headerWithName("X-USER-ID").description("회원 ID")
                ),
                pathParameters(
                        parameterWithName("orderId").description("주문 ID")
                ),
                queryParameters(
                        parameterWithName("reason").description("반품 사유")
                )
        ));
    verify(orderService, times(1)).refundOrder(eq(userId), eq(orderId), eq(reason));
}

    @Test
@DisplayName("비회원 주문 취소 성공")
void cancelGuestOrder_success() throws Exception {
    sampleGuestOrderRequest.setPassword("123456");

    doNothing().when(orderService).cancelGuestOrder(
            eq(sampleGuestOrderRequest.getOrderId()),
            eq(sampleGuestOrderRequest.getPassword())
    );

    mockMvc.perform(post("/api/orders/guest/cancel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleGuestOrderRequest)))
            .andExpect(status().isOk())
            .andDo(document("orders/guest-cancel",
                    requestFields(
                            fieldWithPath("orderId").description("주문 ID"),
                            fieldWithPath("password").description("주문 비밀번호")
                    )
            ));

    verify(orderService, times(1)).cancelGuestOrder(
            eq(sampleGuestOrderRequest.getOrderId()),
            eq(sampleGuestOrderRequest.getPassword())
    );
}

    @Test
@DisplayName("비회원 반품 요청 성공")
void refundGuestOrder_success() throws Exception {
    RefundReason reason = RefundReason.JUST;

    sampleGuestOrderRequest.setPassword("123456");

    doNothing().when(orderService).refundGuestOrder(
            eq(sampleGuestOrderRequest.getOrderId()),
            eq(sampleGuestOrderRequest.getPassword()),
            eq(reason)
    );

    mockMvc.perform(post("/api/orders/guest/refund")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleGuestOrderRequest))
                    .param("reason", reason.name()))
            .andExpect(status().isOk())
            .andDo(document("order-guest-refund",
                    requestFields(
                            fieldWithPath("orderId").description("주문 ID"),
                            fieldWithPath("password").description("비회원 비밀번호")
                    ),
                    queryParameters(
                            parameterWithName("reason").description("반품 사유 (JUST, DEFECT, etc.)")
                    )
            ));

    verify(orderService, times(1)).refundGuestOrder(
            eq(sampleGuestOrderRequest.getOrderId()),
            eq(sampleGuestOrderRequest.getPassword()),
            eq(reason)
    );
}


    @Test
@DisplayName("리뷰 가능 여부 확인 - 가능")
void isReviewable_true() throws Exception {
    Long obId = 123L;
    when(orderService.isReviewable(eq(obId))).thenReturn(true);

    mockMvc.perform(get("/api/orders/review-check")
                    .param("obId", String.valueOf(obId)))
            .andExpect(status().isOk())
            .andExpect(content().string("true"))
            .andDo(document("orders/is-reviewable",
                    queryParameters(
                            parameterWithName("obId").description("주문 도서 ID")
                    )
            ));

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
            .andExpect(content().string("false"))
            .andDo(document("order-is-reviewable-false",
                    queryParameters(
                            parameterWithName("obId").description("주문 도서 ID")
                    ),
                    responseBody()
            ));

    verify(orderService, times(1)).isReviewable(eq(obId));
}

    @Test
@DisplayName("임시 주문 정보 소비 및 주문 페이지 DTO 생성 - 회원")
void consumeOrderPageDto_member_success() throws Exception {
    Long userId = 1L;

    when(orderService.consumeTemporaryOrderInfo(eq(userId))).thenReturn(sampleOrderItemListDto);
    doNothing().when(bookClient).validateOrderItems(eq(sampleOrderItemListDto));
    when(orderService.createOrderPageRequestDto(eq(userId), eq(sampleOrderItemListDto)))
            .thenReturn(new OrderPageRequestDto());

    mockMvc.perform(post("/api/orders")
                    .header("X-USER-ID", userId))
            .andExpect(status().isOk())
            .andDo(document("orders-consume-order-page-member",
                    requestHeaders(
                            headerWithName("X-USER-ID").description("회원 ID")
                    ),
                    responseBody()
            ));

    verify(orderService).consumeTemporaryOrderInfo(eq(userId));
    verify(bookClient).validateOrderItems(eq(sampleOrderItemListDto));
    verify(orderService).createOrderPageRequestDto(eq(userId), eq(sampleOrderItemListDto));
}

@Test
@DisplayName("임시 주문 정보 소비 중 FeignException 발생")
void consumeOrderPageDto_feignException() throws Exception {
    Long userId = 1L;

    when(orderService.consumeTemporaryOrderInfo(eq(userId))).thenReturn(sampleOrderItemListDto);
    doThrow(FeignException.BadRequest.class).when(bookClient).validateOrderItems(eq(sampleOrderItemListDto));

    mockMvc.perform(post("/api/orders")
                    .header("X-USER-ID", userId))
            .andExpect(status().isInternalServerError())
            .andDo(document("orders/consume-feign-exception",
                requestHeaders(
                    headerWithName("X-USER-ID").description("회원 ID")
                ),
                responseFields(
                    fieldWithPath("errorCode").description("에러 코드").type(JsonFieldType.STRING),
                    fieldWithPath("errorMessage").description("에러 메시지").type(JsonFieldType.STRING),
                    fieldWithPath("orderId").description("주문 ID (예외 상황에서는 null)").type(JsonFieldType.NULL),
                    fieldWithPath("paymentKey").description("결제 키 (예외 상황에서는 null)").type(JsonFieldType.NULL)
                )
            ));


    verify(bookClient).validateOrderItems(eq(sampleOrderItemListDto));
}

@Test
@DisplayName("주문 생성 - 회원")
void processOrder_member_success() throws Exception {
    Long userId = 1L;

    OrderInfoForPayment mockResponse = new OrderInfoForPayment();
    mockResponse.setOrderId(123L);
    mockResponse.setOrderName("테스트 주문");
    mockResponse.setPayAmount(50000);


    when(orderService.createOrder(any(OrderSubmitDto.class), eq(userId)))
        .thenReturn(mockResponse);

    mockMvc.perform(post("/api/orders/new")
                    .header("X-USER-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleOrderSubmitDto)))
            .andDo(result -> System.out.println("Response JSON: " + result.getResponse().getContentAsString())) // 응답 출력
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(mockResponse.getOrderId()))
            .andExpect(jsonPath("$.orderName").value(mockResponse.getOrderName()))
            .andExpect(jsonPath("$.payAmount").value(mockResponse.getPayAmount()))
            .andDo(document("orders-create-member",
                    requestHeaders(
                            headerWithName("X-USER-ID").description("회원 ID")
                    ),
                    requestFields(
                            fieldWithPath("bookOrderSubmitDtos").description("주문할 도서 목록"),
                            fieldWithPath("bookOrderSubmitDtos[].bookId").description("도서 ID"),
                            fieldWithPath("bookOrderSubmitDtos[].bookQuantity").description("주문 수량 (최소 1)"),
                            fieldWithPath("bookOrderSubmitDtos[].pavingId").optional().description("포장 옵션 ID"),
                            fieldWithPath("bookOrderSubmitDtos[].appliedCouponId").optional().description("도서별 적용 쿠폰 ID"),
                            fieldWithPath("userId").optional().description("회원 ID (백엔드 식별용)"),
                            fieldWithPath("usedPoints").optional().description("사용한 포인트"),
                            fieldWithPath("deliveryRequestAt").optional().description("희망 배송일 (yyyy-MM-dd)"),
                            fieldWithPath("email").optional().description("주문자 이메일"),
                            fieldWithPath("recipientPhoneNumber").description("수령인 전화번호"),
                            fieldWithPath("recipientName").description("수령인 이름"),
                            fieldWithPath("recipientAddress").description("수령인 주소"),
                            fieldWithPath("guestPassword").description("비회원용 비밀번호 (회원일 경우도 전달됨)"),
                            fieldWithPath("appliedOrderCouponId").optional().description("적용된 주문 쿠폰 ID")
                    ),
                    responseFields(
                            fieldWithPath("orderId").description("주문 ID"),
                            fieldWithPath("orderName").description("주문 이름"),
                            fieldWithPath("payAmount").description("결제 금액")
                    )
            ));

    verify(orderService).createOrder(eq(sampleOrderSubmitDto), eq(userId));
}




@Test
@DisplayName("주문 상세 조회 - 회원")
void getOrderDetail_member_success() throws Exception {
    Long userId = 1L;
    Long orderId = 123L;

    List<OrderBookDetailResponse> bookDetails = List.of(
        new OrderBookDetailResponse(1L, "자바의 정석", 2, 30000, "https://example.com/thumbnail1.jpg"),
        new OrderBookDetailResponse(2L, "스프링 인 액션", 1, 20000, "https://example.com/thumbnail2.jpg")
    );

    OrderDetailResponse detailResponse = new OrderDetailResponse(
        bookDetails,
        123L,
        LocalDateTime.of(2025, 7, 20, 15, 30),
        OrderStatus.COMPLETED,
        50000,
        3000,
        7000,
        1000,
        "홍길동"
    );

    when(orderService.getOrder(eq(orderId), eq(userId))).thenReturn(detailResponse);

    mockMvc.perform(get("/api/orders/{orderId}", orderId)
                    .header("X-USER-ID", userId))
            .andExpect(status().isOk())
            .andDo(document("orders-get-detail-member",
                requestHeaders(
                    headerWithName("X-USER-ID").description("회원 ID")
                ),
                pathParameters(
                    parameterWithName("orderId").description("주문 ID")
                ),
                responseFields(
                    fieldWithPath("books[].bookId").description("도서 ID"),
                    fieldWithPath("books[].title").description("도서 제목"),
                    fieldWithPath("books[].quantity").description("주문 수량"),
                    fieldWithPath("books[].bookTotalPrice").description("도서 가격"),
                    fieldWithPath("books[].thumbnailUrl").description("도서 썸네일 URL"),
                    fieldWithPath("orderId").description("주문 ID"),
                    fieldWithPath("orderAt").description("주문 일시"),
                    fieldWithPath("orderStatus").description("주문 상태"),
                    fieldWithPath("totalPrice").description("총 주문 금액"),
                    fieldWithPath("shippingFee").description("배송비"),
                    fieldWithPath("totalDiscount").description("할인 금액"),
                    fieldWithPath("totalPavingPrice").description("포장비용 총합"),
                    fieldWithPath("name").description("주문자 이름")
                )
            ));

    verify(orderService).getOrder(eq(orderId), eq(userId));
}


@Test
@DisplayName("주문 상세 조회 - 비회원 성공")
void getOrderDetail_guest_success() throws Exception {
    // given
    sampleGuestOrderRequest.setOrderId(123L);
    sampleGuestOrderRequest.setPassword("123456");

    List<OrderBookDetailResponse> bookDetails = List.of(
            new OrderBookDetailResponse(1L, "자바의 정석", 2, 30000, "https://example.com/thumbnail1.jpg"),
            new OrderBookDetailResponse(2L, "스프링 인 액션", 1, 20000, "https://example.com/thumbnail2.jpg")
    );

    OrderDetailResponse detailResponse = new OrderDetailResponse(
            bookDetails,
            123L,
            LocalDateTime.of(2025, 7, 20, 15, 30),
            OrderStatus.COMPLETED,
            50000,
            3000,
            7000,
            1000,
            "홍길동"
    );

    when(orderService.getOrderForGuest(eq(123L), eq("123456"))).thenReturn(detailResponse);

    // when & then
    mockMvc.perform(post("/api/orders/guest")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleGuestOrderRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(123))
            .andExpect(jsonPath("$.orderStatus").value("COMPLETED"))
            .andExpect(jsonPath("$.totalPrice").value(50000))
            .andExpect(jsonPath("$.shippingFee").value(3000))
            .andExpect(jsonPath("$.totalDiscount").value(7000))
            .andExpect(jsonPath("$.totalPavingPrice").value(1000))
            .andExpect(jsonPath("$.name").value("홍길동"))
            .andExpect(jsonPath("$.books[0].bookId").value(1))
            .andExpect(jsonPath("$.books[0].title").value("자바의 정석"))
            .andExpect(jsonPath("$.books[0].quantity").value(2))
            .andExpect(jsonPath("$.books[0].bookTotalPrice").value(30000))
            .andExpect(jsonPath("$.books[0].thumbnailUrl").value("https://example.com/thumbnail1.jpg"))
            .andExpect(jsonPath("$.books[1].bookId").value(2))
            .andExpect(jsonPath("$.books[1].title").value("스프링 인 액션"))
            .andDo(document("orders-get-detail-guest",
                    requestFields(
                            fieldWithPath("orderId").description("주문 ID"),
                            fieldWithPath("password").description("비회원 주문 비밀번호")
                    ),
                    responseFields(
                            fieldWithPath("books[].bookId").description("도서 ID"),
                            fieldWithPath("books[].title").description("도서 제목"),
                            fieldWithPath("books[].quantity").description("주문 수량"),
                            fieldWithPath("books[].bookTotalPrice").description("도서 가격"),
                            fieldWithPath("books[].thumbnailUrl").description("도서 썸네일 URL"),
                            fieldWithPath("orderId").description("주문 ID"),
                            fieldWithPath("orderAt").description("주문 일시"),
                            fieldWithPath("orderStatus").description("주문 상태"),
                            fieldWithPath("totalPrice").description("총 주문 금액"),
                            fieldWithPath("shippingFee").description("배송비"),
                            fieldWithPath("totalDiscount").description("할인 금액"),
                            fieldWithPath("totalPavingPrice").description("포장비용 총합"),
                            fieldWithPath("name").description("주문자 이름")
                    )
            ));

    verify(orderService).getOrderForGuest(eq(123L), eq("123456"));
}


}