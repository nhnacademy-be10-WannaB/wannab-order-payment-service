package shop.wannab.order_payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.payment.Failure;

public interface FailureRepository extends JpaRepository<Failure, Long> {
}
