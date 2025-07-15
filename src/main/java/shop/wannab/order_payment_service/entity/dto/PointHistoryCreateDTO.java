package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record PointHistoryCreateDTO(Long userId,
                                    int usedPoints,
                                    @Positive int orderTotalPrice,
                                    @Positive Long orderId) implements Serializable {
}
