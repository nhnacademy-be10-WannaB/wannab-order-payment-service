package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointProcessRequest {
    private final Long userId;
    private final Long orderId;
    private final int usedPoints;
    private final int orderTotalPrice;
}

