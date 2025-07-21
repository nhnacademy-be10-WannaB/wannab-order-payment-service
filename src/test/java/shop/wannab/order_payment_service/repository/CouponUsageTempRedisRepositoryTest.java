package shop.wannab.order_payment_service.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponUsageTempRedisRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private CouponUsageTempRedisRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void saveUsedCouponInfos_handlesNullBookId_asZero() {
        long userId = 1L;
        String key = "couponUsage :" + userId;

        CouponUsageRequestDto.UsedCouponInfo nullBookId = new CouponUsageRequestDto.UsedCouponInfo();
        nullBookId.setCouponId(1111L);
        nullBookId.setBookId(null); // null bookId → should be treated as 0L

        CouponUsageRequestDto.UsedCouponInfo valid = new CouponUsageRequestDto.UsedCouponInfo();
        valid.setCouponId(2222L);
        valid.setBookId(200L);

        List<CouponUsageRequestDto.UsedCouponInfo> list = Arrays.asList(nullBookId, valid);

        repository.saveUsedCouponInfos(userId, list);

        verify(hashOperations).put(key, 0L, 1111L);
        verify(hashOperations).put(key, 200L, 2222L);
    }

    @Test
    void consumeUsedCouponInfos_readsFromRedis_andDeletesKey() {
        Long userId = 2L;
        String key = "couponUsage :" + userId;

        Map<Object, Object> redisData = new HashMap<>();
        redisData.put(101L, 1001L);
        redisData.put(0L, 9999L); // 0L means originally null bookId

        when(hashOperations.entries(key)).thenReturn(redisData);

        List<CouponUsageRequestDto.UsedCouponInfo> result = repository.consumeUsedCouponInfos(userId);

        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(info -> info.getBookId().equals(101L) && info.getCouponId().equals(1001L)));
        assertTrue(result.stream().anyMatch(info -> info.getBookId().equals(0L) && info.getCouponId().equals(9999L)));

        verify(redisTemplate).delete(key);
    }
}
