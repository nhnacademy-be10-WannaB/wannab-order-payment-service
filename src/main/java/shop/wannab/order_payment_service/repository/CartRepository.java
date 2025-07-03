package shop.wannab.order_payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long>, CartRedisRepository {


}
