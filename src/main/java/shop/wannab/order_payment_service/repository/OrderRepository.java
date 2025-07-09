package shop.wannab.order_payment_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.repository.query.OrderQueryRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderQueryRepository {

    //주문목록조회 (회원)
    Page<Order> findAllByUserId(Long userId, Pageable pageable);


    //배송중으로 변경뒤 일정시간 지난후 배송완료로 변경
    List<Order> findByOrderStatusAndShippedAtBefore(OrderStatus status, LocalDateTime shippedAtBefore);

    //failed 필터후 주문목록조회
    Page<Order> findAllByUserIdAndOrderStatusNot(Long userId, OrderStatus status, Pageable pageable);

    //대기후 결제가 되지않으면 주문상태를 FAILED로 변환
    List<Order> findByOrderStatusAndOrderAtBefore(OrderStatus status, LocalDateTime threshold);
}