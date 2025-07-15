package shop.wannab.order_payment_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "wannab.order.exchange";

    public static final String ORDER_CREATED_BOOK_QUEUE = "wannab.order.created.book.queue";
    public static final String ORDER_CREATED_USER_QUEUE = "wannab.order.created.user.queue";
    public static final String ORDER_CREATED_COUPON_QUEUE = "wannab.order.created.coupon.queue";

    public static final String BOOK_ROUTING_KEY = "order.created.book";
    public static final String USER_ROUTING_KEY = "order.created.user";
    public static final String COUPON_ROUTING_KEY = "order.created.coupon";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderCreatedBookQueue() {
        return new Queue(ORDER_CREATED_BOOK_QUEUE);
    }

    @Bean
    public Queue orderCreatedUserQueue() {
        return new Queue(ORDER_CREATED_USER_QUEUE);
    }

    @Bean
    public Queue orderCreatedCouponQueue() {
        return new Queue(ORDER_CREATED_COUPON_QUEUE);
    }

    @Bean
    public Binding bindingBook() {
        return BindingBuilder.bind(orderCreatedBookQueue()).to(exchange()).with(BOOK_ROUTING_KEY);
    }

    @Bean
    public Binding bindingUser() {
        return BindingBuilder.bind(orderCreatedUserQueue()).to(exchange()).with(USER_ROUTING_KEY);
    }

    @Bean
    public Binding bindingCoupon() {
        return BindingBuilder.bind(orderCreatedCouponQueue()).to(exchange()).with(COUPON_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
