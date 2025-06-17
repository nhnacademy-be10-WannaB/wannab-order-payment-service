package shop.wannab.order_payment_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;


public interface DeliveryPolicyRepository extends JpaRepository<DeliveryPolicy, Long> {
    boolean existsByName(String name);
    Optional<DeliveryPolicy> findByName(String name);
}
