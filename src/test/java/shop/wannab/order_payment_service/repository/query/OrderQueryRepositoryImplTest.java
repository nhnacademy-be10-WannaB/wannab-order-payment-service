package shop.wannab.order_payment_service.repository.query;


import static org.assertj.core.api.Assertions.assertThat;


import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;

import org.junit.jupiter.api.BeforeEach;


import jakarta.persistence.EntityManager;
import shop.wannab.order_payment_service.repository.OrderRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
    Order order1 = Order.builder()
            .orderName("홍길동 주문")
            .orderStatus(OrderStatus.COMPLETED)
            .orderAt(LocalDateTime.of(2024, 7, 1, 10, 0))
            .shippedAt(LocalDateTime.of(2024, 7, 2, 10, 0))
            .totalBookPrice(10_000)
            .totalPavingPrice(5_000)
            .shippingFee(2_500)
            .totalDiscountAmount(1_000)
            .recipientName("홍길동")
            .recipientPhoneNumber("010-1111-2222")
            .recipientEmail("hong@example.com")
            .recipientAddress("서울시 강남구 테헤란로 1")
            .build();

    Order order2 = Order.builder()
            .orderName("임꺽정 주문")
            .orderStatus(OrderStatus.SHIPPING)
            .orderAt(LocalDateTime.of(2024, 7, 5, 15, 30))
            .shippedAt(LocalDateTime.of(2024, 7, 6, 11, 0))
            .totalBookPrice(20_000)
            .totalPavingPrice(10_000)
            .shippingFee(3_000)
            .totalDiscountAmount(2_000)
            .recipientName("임꺽정")
            .recipientPhoneNumber("010-3333-4444")
            .recipientEmail("lim@example.com")
            .recipientAddress("서울시 중구 을지로 1")
            .build();

    em.persist(order1);
    em.persist(order2);
    em.flush();
    em.clear();
}


    @Test
    void searchOrders_조건없이전체조회() {
        // given
        OrderSearchDto dto = new OrderSearchDto();
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderLookupResponse> result = orderRepository.searchOrders(dto, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getOrderName()).contains("임꺽정");
    }

    @Test
    void searchOrders_이름으로검색() {
        // given
        OrderSearchDto dto = new OrderSearchDto();
        dto.setOrderName("홍길동");
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderLookupResponse> result = orderRepository.searchOrders(dto, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOrderName()).isEqualTo("홍길동 주문");
    }

    @Test
    void searchOrders_날짜범위조회() {
        // given
        OrderSearchDto dto = new OrderSearchDto();
        dto.setFrom(LocalDate.of(2024, 7, 2));
        dto.setTo(LocalDate.of(2024, 7, 6));
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderLookupResponse> result = orderRepository.searchOrders(dto, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getOrderName()).isEqualTo("임꺽정 주문");
    }

    @Test
    void searchOrders_정산금액계산검증() {
        // given
        OrderSearchDto dto = new OrderSearchDto();
        dto.setOrderName("홍길동");
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderLookupResponse> result = orderRepository.searchOrders(dto, pageable);

        // then
        OrderLookupResponse response = result.getContent().get(0);
        int expectedTotal = 10_000 + 5_000 + 2_500 - 1_000;
        assertThat(response.getTotalPrice()).isEqualTo(expectedTotal);
    }
}

