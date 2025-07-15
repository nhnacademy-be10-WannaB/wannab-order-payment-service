package shop.wannab.order_payment_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.service.Impl.OrderEmailHelper;

import static shop.wannab.order_payment_service.config.RabbitConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventHandler {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OrderEmailHelper orderEmailHelper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) throws JsonProcessingException {
        String bookIdDecrementQuantities = objectMapper.writeValueAsString(event.getItemListDto());
        rabbitTemplate.convertAndSend(EXCHANGE, BOOK_ROUTING_KEY, bookIdDecrementQuantities);

        if (event.getUserId() > 0) {
            String pointHistoryCreationDtoPayload = objectMapper.writeValueAsString(event.getPointHistoryCreateDTO());
            rabbitTemplate.convertAndSend(EXCHANGE, USER_ROUTING_KEY, pointHistoryCreationDtoPayload);
        }

        rabbitTemplate.convertAndSend(EXCHANGE, COUPON_ROUTING_KEY, event.getCouponUsageRequestDto());

        try {
            Order order = event.getOrder();
            orderEmailHelper.sendOrderEmail(order, order.getRecipientEmail(), order.getRecipientAddress(), order.getRecipientName());
        } catch (RuntimeException e) {
            log.info("이메일 전송실패");
        }
    }
}
