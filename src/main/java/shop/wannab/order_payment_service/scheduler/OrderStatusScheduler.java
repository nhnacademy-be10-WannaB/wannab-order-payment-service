package shop.wannab.order_payment_service.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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


    /**
     * 출고된 후 일정시간 후 배송완료처리(배송 시작 1분 후 자동 완료)
     */
    @Scheduled(fixedDelay = 60 * 1000) // TODO: 테스트하려고 1분으로 설정해둠 (추후에 변경필요)
    @Transactional
    public void completeShippedOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);
        List<Order> shippingOrders = orderRepository.findByOrderStatusAndShippedAtBefore(OrderStatus.SHIPPING, threshold);

        for (Order order : shippingOrders) {
            order.setOrderStatus(OrderStatus.COMPLETED);
        }
    }

    /**
     * 주문생성후 1분내로 결제하지 않으면 주문실패처리
     */
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

    /**
     * 출고정책
     */
    @Scheduled(cron = "0 0 15 * * MON-FRI")
    @Transactional
    public void updateOrdersToShipping() {
        LocalDate today = LocalDate.now();
        DayOfWeek day = today.getDayOfWeek();

        List<Order> ordersToShip = new ArrayList<>();

        if (day == DayOfWeek.FRIDAY) {
            // 배송희망일이 주말인경우 금요일에 전부 출고
            LocalDate saturday = today.plusDays(1);
            LocalDate sunday = today.plusDays(2);
            ordersToShip.addAll(orderRepository.findPaidOrdersWithDeliveryWantIn(today, saturday, sunday));
        } else {
            // 배송희망일이 오늘인 주문
            ordersToShip.addAll(orderRepository.findPaidOrdersWithTodayDelivery(today));
        }

        // 배송희망일이 null인 경우는 전부 월요일에 출고
        if (day == DayOfWeek.MONDAY) {
            ordersToShip.addAll(orderRepository.findPaidOrdersWithNoDeliveryWant());
        }

        for (Order order : ordersToShip) {
            order.setOrderStatus(OrderStatus.SHIPPING);
            order.setShippedAt(LocalDateTime.now());
            log.info("주문 {} 출고 처리됨 (배송희망일: {})", order.getId(), order.getDeliveryWant());
        }
    }
}