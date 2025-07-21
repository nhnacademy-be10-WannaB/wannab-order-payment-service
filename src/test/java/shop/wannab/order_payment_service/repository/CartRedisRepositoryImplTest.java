package shop.wannab.order_payment_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import shop.wannab.order_payment_service.entity.dto.CartItem;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartRedisRepositoryImplTest {

     @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    private CartRedisRepositoryImpl cartRedisRepository;

    @BeforeEach
    void setUp() {
        cartRedisRepository = new CartRedisRepositoryImpl(redisTemplate);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void getCartItems_returnsItems_whenUserExists() {
        Long userId = 1L;
        String cartKey = "cart:" + userId;

        Map<Object, Object> redisData = new HashMap<>();
        redisData.put(101L, 2);
        redisData.put(102L, 3);

        when(hashOperations.entries(cartKey)).thenReturn(redisData);

        List<CartItem> items = cartRedisRepository.getCartItems(userId);

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getBookId() == 101L && i.getQuantity() == 2));
    }

    @Test
    void addItemToCart_incrementsBookQuantity() {
        Long userId = 1L;
        long bookId = 100L;
        String cartKey = "cart:" + userId;

        cartRedisRepository.addItemToCart(userId, bookId);

        verify(hashOperations).increment(cartKey, bookId, 1);
    }

    @Test
    void updateItemQuantity_putsNewQuantity() {
        Long userId = 1L;
        long bookId = 100L;
        int quantity = 3;
        String cartKey = "cart:" + userId;

        cartRedisRepository.updateItemQuantity(userId, bookId, quantity);

        verify(hashOperations).put(cartKey, bookId, quantity);
    }

    @Test
    void removeItemFromCart_deletesBookFromCart() {
        Long userId = 1L;
        long bookId = 100L;
        String cartKey = "cart:" + userId;

        cartRedisRepository.removeItemFromCart(userId, bookId);

        verify(hashOperations).delete(cartKey, bookId);
    }

        @Test
    void createCart_createsGuestCart_withTTL() {
        Long guestId = -42L;
        String cartKey = "cart:" + guestId;

        cartRedisRepository.createCart(guestId);

        verify(redisTemplate).expire(eq(cartKey), any(Duration.class));
    }

    @Test
    void createCart_createsUserCart_withoutTTL() {
        Long userId = 42L;
        String cartKey = "cart:" + userId;

        cartRedisRepository.createCart(userId);

        verify(redisTemplate, never()).expire(anyString(), any());
    }


}