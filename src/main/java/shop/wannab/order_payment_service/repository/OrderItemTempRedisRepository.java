package shop.wannab.order_payment_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class OrderItemTempRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "TMP: ";

    public void saveTemporaryOrderInfo(long customerId, OrderItemListDto dto) {
        String key = PREFIX + customerId;
        List<CartItem> orderItems = dto.getOrderItems();
        for (CartItem item : orderItems) {
            redisTemplate.opsForHash().put(key, item.getBookId(), item.getQuantity());
        }
    }

    public List<CartItem> consumeOrderItems(Long customerId) {
        List<CartItem> cartItems = new ArrayList<>();
        if (Objects.isNull(customerId)) {
            return cartItems;
        }
        String key = PREFIX + customerId;
        Map<Long, Integer> itemMap = redisTemplate.<Long, Integer>opsForHash().entries(key); //key: bookId, value: quantity

        for (Map.Entry<Long, Integer> entry : itemMap.entrySet()) {
            CartItem item = new CartItem(entry.getKey(), entry.getValue());
            cartItems.add(item);
            redisTemplate.opsForHash().delete(key, item.getBookId());
        }

        return cartItems;
    }


}
