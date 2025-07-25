package shop.wannab.order_payment_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static shop.wannab.order_payment_service.constants.Constants.GUEST_CART_TTL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.GuestCartCookieDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.repository.CartRepository;
import shop.wannab.order_payment_service.scheduler.CartBackupScheduler;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private BookClient bookClient;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartBackupScheduler cartBackupScheduler;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
    }


    @Test
    @DisplayName("장바구니 아이템 정보 조회 - 빈 장바구니")
    void getCartItemInfos_EmptyCart() {
        // Given
        Long userIdentifier = 1L;
        List<CartItem> emptyCartItems = Collections.emptyList();
        OrderBookInfoListDto expectedEmptyBookInfos = new OrderBookInfoListDto(Collections.emptyList());

        when(cartRepository.getCartItems(userIdentifier)).thenReturn(emptyCartItems);
        when(bookClient.getOrderBookInfos(any(OrderItemListDto.class))).thenReturn(expectedEmptyBookInfos);

        // When
        OrderBookInfoListDto result = cartService.getCartItemInfos(userIdentifier);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderBookInfos()).isEmpty();

        verify(cartRepository, times(1)).getCartItems(userIdentifier);
        verify(bookClient, times(1)).getOrderBookInfos(any(OrderItemListDto.class));
    }

    @Test
    @DisplayName("장바구니 아이템 추가 성공")
    void addCartItem_Success() {
        // Given
        Long userIdentifier = 1L;
        long bookId = 103L;

        doNothing().when(cartRepository).addItemToCart(userIdentifier, bookId);
        doNothing().when(cartBackupScheduler).onCartChanged(userIdentifier);

        // When
        cartService.addCartItem(userIdentifier, bookId);

        // Then
        verify(cartRepository, times(1)).addItemToCart(userIdentifier, bookId);
        verify(cartBackupScheduler, times(1)).onCartChanged(userIdentifier);
    }

    @Test
    @DisplayName("장바구니 아이템 수량 업데이트 성공")
    void updateItemQuantity_Success() {
        // Given
        Long userIdentifier = 1L;
        long bookId = 104L;
        int quantity = 5;

        doNothing().when(cartRepository).updateItemQuantity(userIdentifier, bookId, quantity);
        doNothing().when(cartBackupScheduler).onCartChanged(userIdentifier);

        // When
        cartService.updateItemQuantity(userIdentifier, bookId, quantity);

        // Then
        verify(cartRepository, times(1)).updateItemQuantity(userIdentifier, bookId, quantity);
        verify(cartBackupScheduler, times(1)).onCartChanged(userIdentifier);
    }

    @Test
    @DisplayName("장바구니 아이템 삭제 성공")
    void removeProductFromCart_Success() {
        // Given
        Long userIdentifier = 1L;
        long bookId = 105L;

        doNothing().when(cartRepository).removeItemFromCart(userIdentifier, bookId);
        doNothing().when(cartBackupScheduler).onCartChanged(userIdentifier);

        // When
        cartService.removeProductFromCart(userIdentifier, bookId);

        // Then
        verify(cartRepository, times(1)).removeItemFromCart(userIdentifier, bookId);
        verify(cartBackupScheduler, times(1)).onCartChanged(userIdentifier);
    }

    @Test
    @DisplayName("회원 장바구니 생성 - userIdentifier가 존재하는 경우")
    void createCart_UserExists() {
        // Given
        Long userIdentifier = 1L;
        doNothing().when(cartRepository).createCart(userIdentifier);

        // When
        GuestCartCookieDto result = cartService.createCart(userIdentifier);

        // Then
        assertThat(result).isNull();
        verify(cartRepository, times(1)).createCart(userIdentifier);
    }

    @Test
@DisplayName("게스트 장바구니 생성 - userIdentifier가 null인 경우")
void createCart_GuestUser() {
    // Given
    Long userIdentifier = null;

    doNothing().when(cartRepository).createCart(anyLong());

    // When
    GuestCartCookieDto result = cartService.createCart(userIdentifier);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getValue()).isNegative();
    assertThat(result.getCookieMaxAge()).isEqualTo(60 * 60 * GUEST_CART_TTL);

    verify(cartRepository, times(1)).createCart(anyLong());
}

}