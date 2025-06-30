package shop.wannab.order_payment_service.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.repository.OrderReopsitory;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderReopsitory orderReopsitory;

    // 매 1분마다 실행 (배송 시작 1일 후 자동 완료)
    @Scheduled(fixedDelay = 60 * 1000) // TODO: 테스트하려고 1분으로 설정해둠 (추후에 변경필요)
    @Transactional
    public void completeShippedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<Order> shippingOrders = orderReopsitory.findByOrderStatusAndShippedAtBefore(OrderStatus.SHIPPING, threshold);

        for (Order order : shippingOrders) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        }
    }
}