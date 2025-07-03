package shop.wannab.order_payment_service.repository.query;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;

public interface OrderQueryRepository {

    //주문검색
    Page<OrderLookupResponse> searchOrders(
            Long orderId,
            String orderName,
            OrderStatus orderStatus,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}
