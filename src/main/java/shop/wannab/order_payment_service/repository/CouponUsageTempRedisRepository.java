package shop.wannab.order_payment_service.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class CouponUsageTempRedisRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "couponUsage :";

    public void saveUsedCouponInfos(long userId, List<CouponUsageRequestDto.UsedCouponInfo> usedCouponInfos) {
        String key = PREFIX + userId;
        for (CouponUsageRequestDto.UsedCouponInfo usedCouponInfo : usedCouponInfos) {
            Long bookIdOrNull = usedCouponInfo.getBookId();
            if (Objects.isNull(bookIdOrNull)) {
                bookIdOrNull = 0L;
            }
            redisTemplate.opsForHash().put(key, bookIdOrNull, usedCouponInfo.getCouponId());
        }
    }

    public List<CouponUsageRequestDto.UsedCouponInfo> consumeUsedCouponInfos(Long userId) {
        String key = PREFIX + userId;
        List<CouponUsageRequestDto.UsedCouponInfo> usedCouponInfos = new ArrayList<>();
        Map<Object, Object> bookIdCouponIdMap = redisTemplate.opsForHash().entries(key);

        for (Map.Entry<Object, Object> entry : bookIdCouponIdMap.entrySet()) {
            Long bookId = ((Number) entry.getKey()).longValue();
            Long couponId = ((Number) entry.getValue()).longValue();
            CouponUsageRequestDto.UsedCouponInfo couponInfo = new CouponUsageRequestDto.UsedCouponInfo();
            couponInfo.setBookId(bookId);
            couponInfo.setCouponId(couponId);
            usedCouponInfos.add(couponInfo);
        }
        redisTemplate.delete(PREFIX + userId);
        return usedCouponInfos;
    }
}
