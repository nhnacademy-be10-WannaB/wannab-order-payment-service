package shop.wannab.order_payment_service.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static shop.wannab.order_payment_service.config.RabbitConfig.EXCHANGE;
import static shop.wannab.order_payment_service.config.RabbitConfig.ROUTING_KEY;

@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(event);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, json);

    }
}
