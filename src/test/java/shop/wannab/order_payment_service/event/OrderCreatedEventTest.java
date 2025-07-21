package shop.wannab.order_payment_service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;

class OrderCreatedEventTest {

    @Test
    void lombokAllArgsConstructor_andGetters_shouldWork() {
        Order order = new Order();
        Long userId = 123L;
        OrderItemListDto itemListDto = new OrderItemListDto();
        PointHistoryCreateDTO pointHistoryCreateDTO = new PointHistoryCreateDTO(1L, 100, 2000, 123L);
        CouponUsageRequestDto couponUsageRequestDto = new CouponUsageRequestDto();

        OrderCreatedEvent event = new OrderCreatedEvent(order, userId, itemListDto, pointHistoryCreateDTO, couponUsageRequestDto);

        assertThat(event.getOrder()).isEqualTo(order);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getItemListDto()).isEqualTo(itemListDto);
        assertThat(event.getPointHistoryCreateDTO()).isEqualTo(pointHistoryCreateDTO);
        assertThat(event.getCouponUsageRequestDto()).isEqualTo(couponUsageRequestDto);
    }

    @Test
    void lombokNoArgsConstructor_andSetters_shouldWork() {
        OrderCreatedEvent event = new OrderCreatedEvent();

        Order order = new Order();
        Long userId = 456L;
        OrderItemListDto itemListDto = new OrderItemListDto();
        PointHistoryCreateDTO pointHistoryCreateDTO = new PointHistoryCreateDTO(1L, 100, 2000, 123L);
        CouponUsageRequestDto couponUsageRequestDto = new CouponUsageRequestDto();

        event.setOrder(order);
        event.setUserId(userId);
        event.setItemListDto(itemListDto);
        event.setPointHistoryCreateDTO(pointHistoryCreateDTO);
        event.setCouponUsageRequestDto(couponUsageRequestDto);

        assertThat(event.getOrder()).isEqualTo(order);
        assertThat(event.getUserId()).isEqualTo(userId);
        assertThat(event.getItemListDto()).isEqualTo(itemListDto);
        assertThat(event.getPointHistoryCreateDTO()).isEqualTo(pointHistoryCreateDTO);
        assertThat(event.getCouponUsageRequestDto()).isEqualTo(couponUsageRequestDto);
    }
}