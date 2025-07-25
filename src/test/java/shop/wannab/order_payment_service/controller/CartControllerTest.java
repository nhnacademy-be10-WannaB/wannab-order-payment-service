package shop.wannab.order_payment_service.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.order_payment_service.advice.GlobalExceptionHandler;
import shop.wannab.order_payment_service.entity.dto.GuestCartCookieDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.service.CartService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;

@WebMvcTest(CartController.class)
@ActiveProfiles("ci")
@AutoConfigureRestDocs
@Import({GlobalExceptionHandler.class})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;


@Test
@DisplayName("비회원 장바구니 생성 성공")
void createGuestCart_success() throws Exception {
    GuestCartCookieDto dto = new GuestCartCookieDto(-123L, 3600);
    Mockito.when(cartService.createCart(isNull())).thenReturn(dto);

    mockMvc.perform(post("/api/cart"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.keyName").value("guestId"))
            .andExpect(jsonPath("$.value").value(-123L))
            .andExpect(jsonPath("$.cookieMaxAge").value(3600))
            .andDo(document("cart/create-guest-cart",
                responseFields(
                    fieldWithPath("keyName").description("쿠키 이름, 비회원 장바구니 식별자"),
                    fieldWithPath("value").description("비회원 장바구니 ID 값"),
                    fieldWithPath("cookieMaxAge").description("쿠키 유지 시간 (초)")
                )
            ));
}

@Test
@DisplayName("회원 장바구니 생성 - 반환값 없음")
void createMemberCart_success() throws Exception {
    Mockito.when(cartService.createCart(eq(1L))).thenReturn(null);

    mockMvc.perform(post("/api/cart")
                    .header("X-USER-ID", "1"))
            .andExpect(status().isOk())
            .andExpect(content().string(""))
            .andDo(document("cart/create-member-cart",
                requestHeaders(
                    headerWithName("X-USER-ID").description("회원 ID")
                )
                // 반환 바디가 없으므로 responseFields 는 생략
            ));
}


@Test
@DisplayName("장바구니 항목 조회 - 회원")
void getCartItems_member_success() throws Exception {
    OrderBookInfoListDto dto = new OrderBookInfoListDto(Collections.emptyList());
    Mockito.when(cartService.getCartItemInfos(eq(1L))).thenReturn(dto);

    mockMvc.perform(get("/api/cart")
            .header("X-USER-ID", "1"))
        .andExpect(status().isOk())
        .andDo(document("cart/get-cart-items-member",
            requestHeaders(
                headerWithName("X-USER-ID").description("회원 ID (비회원일 경우 생략 가능)").optional()
            ),
            responseFields(
                fieldWithPath("orderBookInfos").description("장바구니에 담긴 도서 목록")
            )
        ));
}

@Test
@DisplayName("장바구니 항목 조회 - 비회원")
void getCartItems_guest_success() throws Exception {
    OrderBookInfoListDto dto = new OrderBookInfoListDto(Collections.emptyList());
    Mockito.when(cartService.getCartItemInfos(eq(-999L))).thenReturn(dto);

    mockMvc.perform(get("/api/cart")
            .param("guestId", "-999"))
        .andExpect(status().isOk())
        .andDo(document("cart/get-cart-items-guest",
            queryParameters(
                parameterWithName("guestId").description("비회원 식별자")
            ),
            responseFields(
                fieldWithPath("orderBookInfos").description("장바구니에 담긴 도서 목록")
            )
        ));
}

@Test
@DisplayName("도서 장바구니 추가 - 회원")
void addProductToCart_member_success() throws Exception {
    doNothing().when(cartService).addCartItem(eq(1L), eq(10L));

    mockMvc.perform(post("/api/cart/books?bookId=10")
                .header("X-USER-ID", "1"))
        .andExpect(status().isOk())
        .andDo(document("cart/add-product-member",
            requestHeaders(
                headerWithName("X-USER-ID").description("로그인한 유저의 ID (비로그인 시 null)").optional()
            ),
            queryParameters(
                parameterWithName("guestId").optional().description("비회원 식별자"),
                parameterWithName("bookId").description("장바구니에 추가할 도서 ID")
            )
        ));
}

@Test
@DisplayName("도서 장바구니 추가 - 비회원")
void addProductToCart_guest_success() throws Exception {
    doNothing().when(cartService).addCartItem(eq(999L), eq(10L));

    mockMvc.perform(post("/api/cart/books")
                    .param("guestId", "999")
                    .param("bookId", "10"))
            .andExpect(status().isOk())
            .andDo(document("cart/add-product-guest",
                queryParameters(
                    parameterWithName("guestId").optional().description("비회원 식별자"),
                    parameterWithName("bookId").optional().description("장바구니에 추가할 도서 ID")
                )
            ));
}


@Test
@DisplayName("도서 장바구니 추가 - 음수 bookId 예외 발생")
void addProductToCart_invalidBookId_shouldReturnBadRequest() throws Exception {
    // Given
    long invalidBookId = -10L;
    long userId = 1L;

    doThrow(new OrderPaymentServiceException(OrderPaymentErrorCode.WRONG_BOOK_ID))
            .when(cartService).addCartItem(userId, invalidBookId);

    // When & Then
    mockMvc.perform(post("/api/cart/books?bookId=" + invalidBookId)
                    .header("X-USER-ID", String.valueOf(userId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER-PAYMENT-8001")) // 실제 코드명으로 수정
            .andExpect(jsonPath("$.message").value("잘못된 도서정보입니다.")) // 실제 메시지로 수정
            .andDo(document("cart/add-product-invalid-book-id",
                requestHeaders(
                    headerWithName("X-USER-ID").description("로그인한 유저의 ID (비로그인 시 null)").optional()
                ),
                queryParameters(
                    parameterWithName("guestId").optional().description("비회원 식별자"),
                    parameterWithName("bookId").description("장바구니에 추가할 도서 ID (음수 불가)")
                ),
                responseFields(
                    fieldWithPath("code").description("에러 코드"),
                    fieldWithPath("message").description("에러 메시지")
                )
            ));
}



@Test
@DisplayName("수량 변경 - 회원")
void updateQuantity_member_success() throws Exception {
    doNothing().when(cartService).updateItemQuantity(eq(1L), eq(10L), eq(3));

    mockMvc.perform(put("/api/cart/books/{book-id}", 10L)
                   .header("X-USER-ID", "1")
                   .param("quantity", "3"))
           .andExpect(status().isOk())
           .andDo(document("cart/update-cart-item-quantity-member",
                   pathParameters(
                       parameterWithName("book-id").description("도서 ID")
                   ),
                   requestHeaders(
                       headerWithName("X-USER-ID").optional().description("회원 ID, 로그인 시 사용")
                   ),
                   queryParameters(
                       parameterWithName("guestId").optional().description("비회원 ID"),
                       parameterWithName("quantity").optional().description("수정할 수량")
                   )
           ));
}

@Test
@DisplayName("수량 변경 - 비회원")
void updateQuantity_guest_success() throws Exception {
    doNothing().when(cartService).updateItemQuantity(eq(999L), eq(10L), eq(3));

    mockMvc.perform(RestDocumentationRequestBuilders.put("/api/cart/books/{book-id}", 10L)
                    .param("guestId", "999")
                    .param("quantity", "3"))
            .andExpect(status().isOk())
            .andDo(document("cart/update-cart-item-quantity-guest",
                    pathParameters(
                        parameterWithName("book-id").description("도서 ID")
                    ),
                    queryParameters(
                        parameterWithName("guestId").optional().description("비회원 ID"),
                        parameterWithName("quantity").optional().description("수정할 수량")
                    )
            ));
}


@Test
@DisplayName("장바구니 삭제 - 회원")
void removeProductFromCart_member_success() throws Exception {
    doNothing().when(cartService).removeProductFromCart(eq(1L), eq(10L));

    mockMvc.perform(delete("/api/cart/books/{book-id}", 10L)
            .header("X-USER-ID", "1"))
        .andExpect(status().isOk())
        .andDo(document("cart/remove-product-member",
            requestHeaders(
                headerWithName("X-USER-ID").description("회원일 경우 유저 ID (비회원은 null 가능)").optional()
            ),
            queryParameters(
                parameterWithName("guestId").optional().description("비회원일 경우 게스트 ID")
            ),
            pathParameters(
                parameterWithName("book-id").description("장바구니에서 삭제할 책 ID")
            )
        ));
}

@Test
@DisplayName("장바구니 삭제 - 비회원")
void removeProductFromCart_guest_success() throws Exception {
    doNothing().when(cartService).removeProductFromCart(eq(999L), eq(10L));

    mockMvc.perform(delete("/api/cart/books/{book-id}", 10L)
                    .param("guestId", "999"))
            .andExpect(status().isOk())
            .andDo(document("cart/remove-product-guest",
                queryParameters(
                    parameterWithName("guestId").optional().description("비회원 게스트 ID")
                ),
                pathParameters(
                    parameterWithName("book-id").description("장바구니에서 삭제할 책 ID")
                )
            ));
}

}