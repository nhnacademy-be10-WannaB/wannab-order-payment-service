package shop.wannab.order_payment_service.entity.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.OrderStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLookupResponseTest {

    @Test
    @DisplayName("전체 필드 생성자 확인")
    void testAllArgsConstructor() {
        Long orderId = 1L;
        String orderName = "테스트 주문";
        LocalDateTime orderAt = LocalDateTime.of(2025, 7, 20, 12, 0);
        OrderStatus status = OrderStatus.COMPLETED;
        LocalDateTime shippedAt = LocalDateTime.of(2025, 7, 21, 14, 0);
        int totalPrice = 15000;
        String thumbnailUrl = "http://test.com/image.jpg";

        OrderLookupResponse dto = new OrderLookupResponse(
                orderId, orderName, orderAt, status, shippedAt, totalPrice, thumbnailUrl
        );

        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getOrderName()).isEqualTo(orderName);
        assertThat(dto.getOrderAt()).isEqualTo(orderAt);
        assertThat(dto.getOrderStatus()).isEqualTo(status);
        assertThat(dto.getShippedAt()).isEqualTo(shippedAt);
        assertThat(dto.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(dto.getThumbnailUrl()).isEqualTo(thumbnailUrl);
    }

    @Test
    @DisplayName("썸네일 x 생성자 사용 값 확인")
    void testConstructorWithoutThumbnailUrl() {
        Long orderId = 2L;
        String orderName = "썸네일 없음 주문";
        LocalDateTime orderAt = LocalDateTime.of(2025, 7, 18, 10, 30);
        OrderStatus status = OrderStatus.PENDING;
        LocalDateTime shippedAt = LocalDateTime.of(2025, 7, 19, 9, 0);
        int totalPrice = 20000;

        OrderLookupResponse dto = new OrderLookupResponse(
                orderId, orderName, orderAt, status, shippedAt, totalPrice
        );

        assertThat(dto.getOrderId()).isEqualTo(orderId);
        assertThat(dto.getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("기본 생성자 및 setter 확인")
    void testNoArgsConstructorAndSetters() {
        OrderLookupResponse dto = new OrderLookupResponse();
        LocalDateTime now = LocalDateTime.now();

        dto.setOrderId(3L);
        dto.setOrderName("기본 생성자 주문");
        dto.setOrderAt(now);
        dto.setOrderStatus(OrderStatus.COMPLETED);
        dto.setShippedAt(now.plusDays(1));
        dto.setTotalPrice(30000);
        dto.setThumbnailUrl("http://image.com/sample.jpg");

        assertThat(dto.getOrderId()).isEqualTo(3L);
        assertThat(dto.getOrderName()).isEqualTo("기본 생성자 주문");
        assertThat(dto.getOrderAt()).isEqualTo(now);
        assertThat(dto.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(dto.getShippedAt()).isEqualTo(now.plusDays(1));
        assertThat(dto.getTotalPrice()).isEqualTo(30000);
        assertThat(dto.getThumbnailUrl()).isEqualTo("http://image.com/sample.jpg");
    }
}