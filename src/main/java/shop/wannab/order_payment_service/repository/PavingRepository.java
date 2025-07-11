package shop.wannab.order_payment_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.Paving;

public interface PavingRepository extends JpaRepository<Paving, Long> {

    boolean existsByName(String name);
    Optional<Paving> findByName(String name);

}
