package shop.wannab.order_payment_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import shop.wannab.order_payment_service.entity.CartItem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static shop.wannab.order_payment_service.constants.Constants.GUEST_CART_TTL;

@RequiredArgsConstructor
@Repository
public class CartRedisRepository implements CartRepository {
    private static final long CART_DUMMY_KEY = -1;
    private static final long CART_DUMMY_VAL = -1;

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";

    @Override
    public List<CartItem> getCartItems(long userIdentifier) {
        String cartKey = CART_KEY_PREFIX + userIdentifier;
        Map<Long, Integer> cartBooks = redisTemplate.<Long, Integer>opsForHash().entries(cartKey); //key: bookId, value: quantity

        List<CartItem> cartItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cartBooks.entrySet()) {
            CartItem item = new CartItem(entry.getKey(), entry.getValue());
            cartItems.add(item);
        }
        return cartItems;
    }

    @Override
    public void addItemToCart(long userIdentifier, long bookId) {
        String cartKey = CART_KEY_PREFIX + userIdentifier;
        redisTemplate.opsForHash().increment(cartKey, bookId, 1);
    }

    @Override
    public void updateItemQuantity(long userIdentifier, long bookId, int quantity) {
        String cartKey = CART_KEY_PREFIX + userIdentifier;
        redisTemplate.opsForHash().put(cartKey, bookId, quantity);
    }

    @Override
    public void removeItemFromCart(long userIdentifier, long bookId) {
        String cartKey = CART_KEY_PREFIX + userIdentifier;
        redisTemplate.opsForHash().delete(cartKey, bookId);
    }

    @Override
    public void createCart(long userIdentifier) {
        String cartKey;
        if (isGuest(userIdentifier)) {
            String guest = "guest:" + userIdentifier;
            cartKey = CART_KEY_PREFIX + guest; //cart:guest:-142435

            redisTemplate.opsForHash().put(cartKey, CART_DUMMY_KEY, CART_DUMMY_VAL);
            redisTemplate.expire(cartKey, Duration.ofHours(GUEST_CART_TTL));
            return;
        }
        cartKey = CART_KEY_PREFIX + userIdentifier;
        redisTemplate.opsForHash().put(cartKey, CART_DUMMY_KEY, CART_DUMMY_VAL);
    }

    private boolean isGuest(long userIdentifier) {
        return userIdentifier < 0;
    }

}
