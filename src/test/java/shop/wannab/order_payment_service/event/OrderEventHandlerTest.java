package shop.wannab.order_payment_service.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static shop.wannab.order_payment_service.config.RabbitConfig.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;
import shop.wannab.order_payment_service.service.Impl.OrderEmailHelper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class OrderEventHandlerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderEmailHelper orderEmailHelper;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private OrderCreatedEvent createSampleEvent() throws JsonProcessingException {
        Order order = new Order(
                1L, // userId
                "orderNameTest",
                null, // shippedAt
                null, // deliveryWant
                10000, // totalBookPrice
                1000,  // totalDiscountAmount
                3000,  // shippingFee
                2000,  // totalPavingPrice
                "John Doe", // recipientName
                "test@example.com", // recipientEmail
                "010-1234-5678", // recipientPhoneNumber
                "123 Main St"    // recipientAddress
        );

        Long userId = 1L;
        OrderItemListDto itemListDto = new OrderItemListDto();
        PointHistoryCreateDTO pointHistoryCreateDTO = new PointHistoryCreateDTO(1L, 100, 2000, 123L);
        CouponUsageRequestDto couponUsageRequestDto = new CouponUsageRequestDto();

        when(objectMapper.writeValueAsString(itemListDto)).thenReturn("{\"dummy\":\"bookList\"}");
        when(objectMapper.writeValueAsString(pointHistoryCreateDTO)).thenReturn("{\"dummy\":\"pointHistory\"}");

        return new OrderCreatedEvent(order, userId, itemListDto, pointHistoryCreateDTO, couponUsageRequestDto);
    }

    @Test
    void handleOrderCreated_shouldSendMessagesAndSendEmail() throws Exception {
        OrderCreatedEvent event = createSampleEvent();

        orderEventHandler.handleOrderCreated(event);

        verify(objectMapper).writeValueAsString(event.getItemListDto());
        verify(rabbitTemplate).convertAndSend(EXCHANGE, BOOK_ROUTING_KEY, "{\"dummy\":\"bookList\"}");

        verify(objectMapper).writeValueAsString(event.getPointHistoryCreateDTO());
        verify(rabbitTemplate).convertAndSend(EXCHANGE, USER_ROUTING_KEY, "{\"dummy\":\"pointHistory\"}");

        verify(rabbitTemplate).convertAndSend(EXCHANGE, COUPON_ROUTING_KEY, event.getCouponUsageRequestDto());

        verify(orderEmailHelper).sendOrderEmail(
                event.getOrder(),
                event.getOrder().getRecipientEmail(),
                event.getOrder().getRecipientAddress(),
                event.getOrder().getRecipientName()
        );
    }

    @Test
    void handleOrderCreated_shouldLogOnEmailException() throws Exception {
        OrderCreatedEvent event = createSampleEvent();

        doThrow(new RuntimeException("이메일 전송실패")).when(orderEmailHelper).sendOrderEmail(any(), any(), any(), any());

        orderEventHandler.handleOrderCreated(event);

        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
        verify(orderEmailHelper).sendOrderEmail(any(), any(), any(), any());
    }
}