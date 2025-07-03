package shop.wannab.order_payment_service.repository.query;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;

public interface OrderQueryRepository {

    //주문검색
    Page<OrderLookupResponse> searchOrders(
            OrderSearchDto orderSearchDto,
            Pageable pageable
    );
}
