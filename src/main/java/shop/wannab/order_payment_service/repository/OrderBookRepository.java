package shop.wannab.order_payment_service.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderBook;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.dto.BookIdQuantityProjection;

public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {
    List<OrderBook> findAllByOrder_Id(Long orderId);

    List<BookIdQuantityProjection> queryByOrder(Order order);

    //배송완료체크
    boolean existsByObIdAndOrder_OrderStatus(Long obId, OrderStatus orderStatus);

    //회원주문목록 썸네일 가져오기위해
    OrderBook findTop1ByOrder_IdOrderByObIdAsc(Long orderId);
}