package shop.wannab.order_payment_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.wannab.order_payment_service.entity.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

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
}
