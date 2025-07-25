package shop.wannab.order_payment_service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    private RedisConfig redisConfig;
    private RedisConnectionFactory mockConnectionFactory;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
        mockConnectionFactory = Mockito.mock(RedisConnectionFactory.class);
    }

    @Test
    void redisTemplate_shouldBeConfiguredCorrectly() {
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(mockConnectionFactory);

        assertThat(redisTemplate).isNotNull();
        assertThat(redisTemplate.getConnectionFactory()).isEqualTo(mockConnectionFactory);

        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(redisTemplate.getHashKeySerializer()).isInstanceOf(GenericToStringSerializer.class);
        assertThat(redisTemplate.getHashValueSerializer()).isInstanceOf(GenericToStringSerializer.class);
    }
}