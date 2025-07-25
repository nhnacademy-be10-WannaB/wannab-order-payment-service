package shop.wannab.order_payment_service.entity.dto;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PointHistoryCreateDtoTest {

    @Test
    @DisplayName("PointHistoryCreateDTO 생성자 및 필드 값 확인")
    void testPointHistoryCreateDTOCreation() {
        Long userId = 1L;
        int usedPoints = 500;
        int orderTotalPrice = 10000;
        Long orderId = 99L;

        PointHistoryCreateDTO dto = new PointHistoryCreateDTO(userId, usedPoints, orderTotalPrice, orderId);

        assertThat(dto.userId()).isEqualTo(userId);
        assertThat(dto.usedPoints()).isEqualTo(usedPoints);
        assertThat(dto.orderTotalPrice()).isEqualTo(orderTotalPrice);
        assertThat(dto.orderId()).isEqualTo(orderId);
    }
}