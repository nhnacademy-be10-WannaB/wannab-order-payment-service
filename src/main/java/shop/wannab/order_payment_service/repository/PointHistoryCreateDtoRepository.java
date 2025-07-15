package shop.wannab.order_payment_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;

@Repository
@RequiredArgsConstructor
public class PointHistoryCreateDtoRepository {
    private static final String PREFIX = "order-id-point-history:";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void save(PointHistoryCreateDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            stringRedisTemplate.opsForValue().set(PREFIX + dto.orderId(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("DTO 직렬화 실패", e);
        }
    }

    public PointHistoryCreateDTO consumeByOrderId(long orderId) {
        String key = PREFIX + orderId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) return null;

        try {
            PointHistoryCreateDTO dto = objectMapper.readValue(json, PointHistoryCreateDTO.class);
            stringRedisTemplate.delete(key);
            return dto;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("DTO 역직렬화 실패", e);
        }
    }
}
