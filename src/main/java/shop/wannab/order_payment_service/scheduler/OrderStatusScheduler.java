package shop.wannab.order_payment_service.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.repository.OrderRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;

    // 매 1분마다 실행 (배송 시작 1일 후 자동 완료)
    @Scheduled(fixedDelay = 60 * 1000) // TODO: 테스트하려고 1분으로 설정해둠 (추후에 변경필요)
    @Transactional
    public void completeShippedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<Order> shippingOrders = orderRepository.findByOrderStatusAndShippedAtBefore(OrderStatus.SHIPPING, threshold);

        for (Order order : shippingOrders) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        }
    }


    @Scheduled(fixedDelay = 60 * 1000) // 1분마다 실행
    @Transactional
    public void markFailedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<Order> expiredOrders = orderRepository.findByOrderStatusAndOrderAtBefore(OrderStatus.PENDING, threshold);

        for (Order order : expiredOrders) {
            order.setOrderStatus(OrderStatus.FAILED);
            log.info("결제 실패로 전환된 주문: id={}, 생성시각={}", order.getId(), order.getOrderAt());
        }
    }
}