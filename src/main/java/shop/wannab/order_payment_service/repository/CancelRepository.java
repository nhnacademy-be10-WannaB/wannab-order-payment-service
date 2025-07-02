package shop.wannab.order_payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.payment.Cancel;

public interface CancelRepository extends JpaRepository<Cancel, Long> {
}
