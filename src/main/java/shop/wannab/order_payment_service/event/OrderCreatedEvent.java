package shop.wannab.order_payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class OrderCreatedEvent {
    private Long orderId;
    private OrderItemListDto itemListDto;
}
