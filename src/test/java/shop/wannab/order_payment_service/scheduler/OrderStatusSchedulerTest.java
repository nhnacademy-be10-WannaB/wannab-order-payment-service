package shop.wannab.order_payment_service.scheduler;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.repository.OrderRepository;

class OrderStatusSchedulerTest {

    private OrderRepository orderRepository;
    private OrderStatusScheduler scheduler;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        scheduler = new OrderStatusScheduler(orderRepository);
    }

    @Test
    void completeShippedOrders_shouldChangeStatusToCompleted() {
        Order order = createOrder(OrderStatus.SHIPPING);
        order.setShippedAt(LocalDateTime.now().minusMinutes(2));

        when(orderRepository.findByOrderStatusAndShippedAtBefore(eq(OrderStatus.SHIPPING), any()))
                .thenReturn(List.of(order));

        scheduler.completeShippedOrders();

        assert order.getOrderStatus() == OrderStatus.COMPLETED;
    }

    @Test
    void markFailedOrders_shouldChangeStatusToFailed() {
        Order order = createOrder(OrderStatus.PENDING);
        when(orderRepository.findByOrderStatusAndOrderAtBefore(eq(OrderStatus.PENDING), any()))
                .thenReturn(List.of(order));

        scheduler.markFailedOrders();

        assert order.getOrderStatus() == OrderStatus.FAILED;
    }

    @Test
    void updateOrdersToShipping_shouldSetStatusAndShippedAt() {
        Order order = createOrder(OrderStatus.PAID);

        when(orderRepository.findPaidOrdersWithTodayDelivery(any()))
                .thenReturn(List.of(order));
        when(orderRepository.findPaidOrdersWithNoDeliveryWant())
                .thenReturn(List.of());

        scheduler.updateOrdersToShipping();

        assert order.getOrderStatus() == OrderStatus.SHIPPING;
        assert order.getShippedAt() != null;
    }

    private Order createOrder(OrderStatus status) {
        return new Order(
                1L,
                "주문이름",
                null,
                LocalDate.now(),
                15000,
                2000,
                3000,
                16000,
                "정민수",
                "minsu@tttt.ttt",
                "010-0000-0000",
                "광주어딘가"
        ) {{
            setOrderStatus(status);
            setShippedAt(LocalDateTime.now().minusMinutes(5)); // 기본값 세팅
        }};
    }
}