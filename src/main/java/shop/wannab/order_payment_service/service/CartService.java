package shop.wannab.order_payment_service.service;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.entity.CartItem;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final long CART_DUMMY_KEY = -1;
    private static final long CART_DUMMY_VAL = -1;
    private static final int GUEST_CART_TTL = 24; //hour

    private final RedisTemplate<String, Object> redisTemplate;
    private final String cartKeyPrefix = "cart:";

    public List<CartItem> getCartItems(long userIdentifier) {
        String cartKey = cartKeyPrefix + userIdentifier;
        Map<Long, Integer> cartBooks = redisTemplate.<Long, Integer>opsForHash().entries(cartKey); //key: bookId, value: quantity

        List<CartItem> cartItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cartBooks.entrySet()) {
            CartItem item = new CartItem(entry.getKey(), entry.getValue());
            cartItems.add(item);
        }
        return cartItems;
    }

    public void addCartItem(Long userIdentifier, long bookId) {
        String cartKey = cartKeyPrefix + userIdentifier;
        redisTemplate.opsForHash().increment(cartKey, bookId, 1);
    }

    public void updateItemQuantity(Long userIdentifier, long bookId, int quantity) {
        String cartKey = cartKeyPrefix + userIdentifier;
        redisTemplate.opsForHash().put(cartKey, bookId, quantity);
    }

    public void removeProductFromCart(Long userIdentifier, long bookId) {
        String cartKey = cartKeyPrefix + userIdentifier;
        redisTemplate.opsForHash().delete(cartKey, bookId);
    }

    public Cookie createCart(Long userIdentifier) {
        String cartKey = null;
        String identifier = null;

        if (Objects.isNull(userIdentifier)) {
            identifier = "guest:" + UUID.randomUUID().toString().substring(0, 7);
            Cookie cookie = new Cookie("X-User-Id", identifier);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * GUEST_CART_TTL);
            return cookie;
        }
        cartKey = cartKeyPrefix + userIdentifier;
        redisTemplate.opsForHash().put(cartKey, CART_DUMMY_KEY, CART_DUMMY_VAL);

        if (cartKey.startsWith("cart:guest:")) {
        redisTemplate.expire(cartKey, Duration.ofHours(GUEST_CART_TTL));
        }
        return null;
    }
}
