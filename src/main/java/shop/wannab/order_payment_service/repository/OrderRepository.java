package shop.wannab.order_payment_service.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    //원하는 배송희망날짜에 출고
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PAID' AND o.deliveryWant = :today")
    List<Order> findPaidOrdersWithTodayDelivery(@Param("today") LocalDate today);
    //원하는 출고날이 주말이라면 금요일에 처리
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PAID' AND o.deliveryWant IN (:today, :saturday, :sunday)")
    List<Order> findPaidOrdersWithDeliveryWantIn(@Param("today") LocalDate today,
                                                 @Param("saturday") LocalDate saturday,
                                                 @Param("sunday") LocalDate sunday);
    //원하는 배송희망날짜가 Null일시
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PAID' AND o.deliveryWant IS NULL")
    List<Order> findPaidOrdersWithNoDeliveryWant();
}