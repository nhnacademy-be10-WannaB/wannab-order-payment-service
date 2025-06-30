package shop.wannab.order_payment_service.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;

public interface OrderReopsitory extends JpaRepository<Order, Long> {

    //주문목록조회 (회원)
    Page<Order> findAllByUserId(Long userId, Pageable pageable);


    //배송중으로 변경뒤 일정시간 지난후 배송완료로 변경
    List<Order> findByOrderStatusAndShippedAtBefore(OrderStatus status, LocalDateTime shippedAtBefore);
}