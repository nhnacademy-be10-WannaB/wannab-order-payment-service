package shop.wannab.order_payment_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointHistoryCreateDtoRepositoryTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PointHistoryCreateDtoRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void save_shouldSerializeDtoAndSaveToRedis() throws Exception {
        PointHistoryCreateDTO dto = mock(PointHistoryCreateDTO.class);
        when(dto.orderId()).thenReturn(123L);

        String json = "{\"orderId\":123}";
        when(objectMapper.writeValueAsString(dto)).thenReturn(json);

        repository.save(dto);

        verify(objectMapper).writeValueAsString(dto);
        verify(valueOperations).set("order-id-point-history:123", json);
    }

    @Test
    void save_shouldThrowIllegalStateException_whenSerializationFails() throws Exception {
        PointHistoryCreateDTO dto = mock(PointHistoryCreateDTO.class);
        when(dto.orderId()).thenReturn(123L);
        when(objectMapper.writeValueAsString(dto)).thenThrow(JsonProcessingException.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repository.save(dto);
        });

        assertTrue(ex.getMessage().contains("DTO 직렬화 실패"));
    }

    @Test
    void consumeByOrderId_shouldReturnDtoAndDeleteKey() throws Exception {
        long orderId = 123L;
        String key = "order-id-point-history:" + orderId;
        String json = "{\"orderId\":123}";

        when(valueOperations.get(key)).thenReturn(json);

        PointHistoryCreateDTO dto = mock(PointHistoryCreateDTO.class);
        when(objectMapper.readValue(json, PointHistoryCreateDTO.class)).thenReturn(dto);

        PointHistoryCreateDTO result = repository.consumeByOrderId(orderId);

        assertEquals(dto, result);
        verify(valueOperations).get(key);
        verify(objectMapper).readValue(json, PointHistoryCreateDTO.class);
        verify(stringRedisTemplate).delete(key);
    }

    @Test
    void consumeByOrderId_shouldReturnNullIfKeyNotExists() {
        long orderId = 123L;
        String key = "order-id-point-history:" + orderId;

        when(valueOperations.get(key)).thenReturn(null);

        PointHistoryCreateDTO result = repository.consumeByOrderId(orderId);

        assertNull(result);
        verify(valueOperations).get(key);
        verify(stringRedisTemplate, never()).delete(anyString());
    }

    @Test
    void consumeByOrderId_shouldThrowIllegalStateException_whenDeserializationFails() throws Exception {
        long orderId = 123L;
        String key = "order-id-point-history:" + orderId;
        String json = "{\"orderId\":123}";

        when(valueOperations.get(key)).thenReturn(json);
        when(objectMapper.readValue(json, PointHistoryCreateDTO.class)).thenThrow(JsonProcessingException.class);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            repository.consumeByOrderId(orderId);
        });

        assertTrue(ex.getMessage().contains("DTO 역직렬화 실패"));
        verify(stringRedisTemplate, never()).delete(key); // delete는 예외 발생 전이므로 호출 안 됨
    }
}
