package shop.wannab.order_payment_service.service;

import static shop.wannab.order_payment_service.constants.Constants.GUEST_CART_TTL;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.repository.CartRepository;

@Service
@RequiredArgsConstructor
public class CartService {
    private final BookClient bookClient;
    private final CartRepository cartRepository;

    @Transactional(readOnly = true)
    public OrderBookInfoListDto getCartItemInfos(long userIdentifier) {
        List<CartItem> cartItems = cartRepository.getCartItems(userIdentifier);
        return bookClient.getOrderBookInfos(new OrderItemListDto(cartItems));
    }

    @Transactional
    public void addCartItem(Long userIdentifier, long bookId) {
        cartRepository.addItemToCart(userIdentifier, bookId);
    }

    @Transactional
    public void updateItemQuantity(Long userIdentifier, long bookId, int quantity) {
        cartRepository.updateItemQuantity(userIdentifier, bookId, quantity);
    }

    @Transactional
    public void removeProductFromCart(Long userIdentifier, long bookId) {
        cartRepository.removeItemFromCart(userIdentifier, bookId);
    }

    public Cookie createCart(Long userIdentifier) {
        if (Objects.isNull(userIdentifier)) {
            long guestId = createGuestId();
            cartRepository.createCart(guestId);

            Cookie cookie = new Cookie("X-User-Id", String.valueOf(guestId));
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * GUEST_CART_TTL);
            return cookie;
        }
        cartRepository.createCart(userIdentifier);
        return null;
    }

    private long createGuestId() {
        Random random = new Random();
        return -random.nextLong(9000000) - 1000000;
    }

}
