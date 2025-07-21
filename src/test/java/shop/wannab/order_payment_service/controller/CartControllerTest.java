package shop.wannab.order_payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.order_payment_service.entity.dto.GuestCartCookieDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.service.CartService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ActiveProfiles("ci")
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
                .andExpect(jsonPath("$.cookieMaxAge").value(3600));
    }

    @Test
    @DisplayName("회원 장바구니 생성 - 반환값 없음")
    void createMemberCart_success() throws Exception {
        Mockito.when(cartService.createCart(eq(1L))).thenReturn(null);

        mockMvc.perform(post("/api/cart")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("장바구니 항목 조회 - 회원")
    void getCartItems_member_success() throws Exception {
        OrderBookInfoListDto dto = new OrderBookInfoListDto(Collections.emptyList());
        Mockito.when(cartService.getCartItemInfos(eq(1L))).thenReturn(dto);

        mockMvc.perform(get("/api/cart")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 항목 조회 - 비회원")
    void getCartItems_guest_success() throws Exception {
        OrderBookInfoListDto dto = new OrderBookInfoListDto(Collections.emptyList());
        Mockito.when(cartService.getCartItemInfos(eq(999L))).thenReturn(dto);

        mockMvc.perform(get("/api/cart")
                        .param("guestId", "999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 장바구니 추가 - 회원")
    void addProductToCart_member_success() throws Exception {
        doNothing().when(cartService).addCartItem(eq(1L), eq(10L));

        mockMvc.perform(post("/api/cart/books")
                        .header("X-USER-ID", "1")
                        .param("bookId", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("도서 장바구니 추가 - 비회원")
    void addProductToCart_guest_success() throws Exception {
        doNothing().when(cartService).addCartItem(eq(999L), eq(10L));

        mockMvc.perform(post("/api/cart/books")
                        .param("guestId", "999")
                        .param("bookId", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("수량 변경 - 회원")
    void updateQuantity_member_success() throws Exception {
        doNothing().when(cartService).updateItemQuantity(eq(1L), eq(10L), eq(3));

        mockMvc.perform(put("/api/cart/books/10")
                        .header("X-USER-ID", "1")
                        .param("quantity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("수량 변경 - 비회원")
    void updateQuantity_guest_success() throws Exception {
        doNothing().when(cartService).updateItemQuantity(eq(999L), eq(10L), eq(3));

        mockMvc.perform(put("/api/cart/books/10")
                        .param("guestId", "999")
                        .param("quantity", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 삭제 - 회원")
    void removeProductFromCart_member_success() throws Exception {
        doNothing().when(cartService).removeProductFromCart(eq(1L), eq(10L));

        mockMvc.perform(delete("/api/cart/books/10")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("장바구니 삭제 - 비회원")
    void removeProductFromCart_guest_success() throws Exception {
        doNothing().when(cartService).removeProductFromCart(eq(999L), eq(10L));

        mockMvc.perform(delete("/api/cart/books/10")
                        .param("guestId", "999"))
                .andExpect(status().isOk());
    }
}