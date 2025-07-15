package shop.wannab.order_payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class OrderCreatedEvent {
    private Order order;
    private Long userId;
    private OrderItemListDto itemListDto;
    private PointHistoryCreateDTO pointHistoryCreateDTO;
    private CouponUsageRequestDto couponUsageRequestDto;
}
