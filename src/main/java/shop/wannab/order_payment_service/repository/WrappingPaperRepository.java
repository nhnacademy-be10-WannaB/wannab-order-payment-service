package shop.wannab.order_payment_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.WrappingPaper;

public interface WrappingPaperRepository extends JpaRepository<WrappingPaper, Long> {
    boolean existsByName(String name);

    Optional<WrappingPaper> findByName(String name);
}
