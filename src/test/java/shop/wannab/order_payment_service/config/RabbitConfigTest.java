package shop.wannab.order_payment_service.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitConfigTest {

    private RabbitConfig rabbitConfig;

    @BeforeEach
    void setUp() {
        rabbitConfig = new RabbitConfig();
    }

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

        assertThat(bookQueue.getName()).isEqualTo("test.book.queueueue");
        assertThat(userQueue.getName()).isEqualTo("test.user.queueueue");
        assertThat(couponQueue.getName()).isEqualTo("test.coupon.queueueue");
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