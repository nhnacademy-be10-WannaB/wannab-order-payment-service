package shop.wannab.order_payment_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Guest;
import shop.wannab.order_payment_service.entity.Order;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByOrder_Id(Long orderId);
}