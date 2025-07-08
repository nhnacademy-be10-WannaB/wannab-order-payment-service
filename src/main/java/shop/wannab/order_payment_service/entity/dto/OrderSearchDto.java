package shop.wannab.order_payment_service.entity.dto;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import shop.wannab.order_payment_service.entity.OrderStatus;

/**
 * 관리자가 주문검색에 사용할 dto
 */

@Data
public class OrderSearchDto {
    private Long orderId;
    private String orderName;
    private OrderStatus orderStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;
}
