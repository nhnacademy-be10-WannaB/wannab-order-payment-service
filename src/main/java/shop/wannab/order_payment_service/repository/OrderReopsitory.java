package shop.wannab.order_payment_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Order;

public interface OrderReopsitory extends JpaRepository<Order, Long> {

    //주문목록조회 (회원)
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
}