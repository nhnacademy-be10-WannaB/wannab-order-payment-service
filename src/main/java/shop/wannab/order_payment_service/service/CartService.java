package shop.wannab.order_payment_service.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.repository.CartRepository;
import static shop.wannab.order_payment_service.constants.Constants.GUEST_CART_TTL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public List<CartItem> getCartItems(long userIdentifier) {
        return cartRepository.getCartItems(userIdentifier);
    }

    public void addCartItem(Long userIdentifier, long bookId) {
        cartRepository.addItemToCart(userIdentifier, bookId);
    }

    public void updateItemQuantity(Long userIdentifier, long bookId, int quantity) {
        cartRepository.updateItemQuantity(userIdentifier, bookId, quantity);
    }

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
