package shop.wannab.order_payment_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderBook;
import shop.wannab.order_payment_service.entity.dto.OrderBookIdQuantityProjection;

public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
    List<OrderBook> findAllByOrder_Id(Long orderId);

    List<OrderBookIdQuantityProjection> queryByOrder(Order order);
}