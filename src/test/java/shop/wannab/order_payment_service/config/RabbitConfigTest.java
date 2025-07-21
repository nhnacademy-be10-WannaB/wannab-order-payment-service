package shop.wannab.order_payment_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RabbitConfig.class)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "queue.order-created.book=test.order.created.book",
        "queue.order-created.user=test.order.created.user",
        "queue.order-created.coupon=test.order.created.coupon"
})
class RabbitConfigTest {

    @Autowired
    private RabbitConfig rabbitConfig;

    @Test
    void exchange_shouldBeCreated() {
        TopicExchange exchange = rabbitConfig.exchange();
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("wannab.order.exchange");
    }

    @Test
    void queues_shouldBeCreatedWithCorrectNames() {
        Queue bookQueue = rabbitConfig.orderCreatedBookQueue();
        Queue userQueue = rabbitConfig.orderCreatedUserQueue();
        Queue couponQueue = rabbitConfig.orderCreatedCouponQueue();

        assertThat(bookQueue.getName()).isEqualTo("test.order.created.book");
        assertThat(userQueue.getName()).isEqualTo("test.order.created.user");
        assertThat(couponQueue.getName()).isEqualTo("test.order.created.coupon");
    }

    @Test
    void bindings_shouldBeCreatedCorrectly() {
        Binding bookBinding = rabbitConfig.bindingBook();
        Binding userBinding = rabbitConfig.bindingUser();
        Binding couponBinding = rabbitConfig.bindingCoupon();

        assertThat(bookBinding.getRoutingKey()).isEqualTo("order.created.book");
        assertThat(userBinding.getRoutingKey()).isEqualTo("order.created.user");
        assertThat(couponBinding.getRoutingKey()).isEqualTo("order.created.coupon");
    }

    @Test
    void messageConverter_shouldBeJackson2Json() {
        MessageConverter converter = rabbitConfig.messageConverter();
        assertThat(converter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}