package shop.wannab.order_payment_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderItemTempRedisRepositoryTest {
     @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private OrderItemTempRedisRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void saveTemporaryOrderInfo_savesAllItemsToRedis() {
        long customerId = 1L;
        String key = "TMP: " + customerId;

        CartItem item1 = new CartItem(101L, 2);
        CartItem item2 = new CartItem(202L, 5);
        List<CartItem> cartItems = Arrays.asList(item1, item2);

        OrderItemListDto dto = new OrderItemListDto();
        dto.setOrderItems(cartItems);

        repository.saveTemporaryOrderInfo(customerId, dto);

        verify(hashOperations).put(key, 101L, 2);
        verify(hashOperations).put(key, 202L, 5);
    }

    @Test
    void getOrderItems_returnsCorrectCartItems() {
        Long customerId = 1L;
        String key = "TMP: " + customerId;

        Map<Object, Object> redisData = new HashMap<>();
        redisData.put(101L, 2);
        redisData.put(202L, 5);

        when(hashOperations.entries(key)).thenReturn(redisData);

        List<CartItem> result = repository.getOrderItems(customerId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(i -> i.getBookId() == 101L && i.getQuantity() == 2));
        assertTrue(result.stream().anyMatch(i -> i.getBookId() == 202L && i.getQuantity() == 5));
    }

    @Test
    void getOrderItems_returnsEmptyList_whenCustomerIdIsNull() {
        List<CartItem> result = repository.getOrderItems(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteOrderItems_deletesAllItemsFromRedis() {
        Long customerId = 1L;
        String key = "TMP: " + customerId;

        Map<Object, Object> itemMap = new HashMap<>();
        itemMap.put(101L, 2);
        itemMap.put(202L, 5);

        when(hashOperations.entries(key)).thenReturn(itemMap);

        repository.deleteOrderItems(customerId);

        verify(hashOperations).delete(key, 101L);
        verify(hashOperations).delete(key, 202L);
    }


}