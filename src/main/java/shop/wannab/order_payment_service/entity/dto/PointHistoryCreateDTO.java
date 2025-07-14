package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Positive;

public record PointHistoryCreateDTO(Long userId,
                                    int usedPoints,
                                    @Positive int orderTotalPrice,
                                    @Positive Long orderId) {
}
