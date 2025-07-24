package shop.wannab.order_payment_service.repository.query;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.dto.OrderLookupResponse;
import shop.wannab.order_payment_service.entity.dto.OrderSearchDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderQueryRepositoryImplTest {

    @Qualifier("orderQueryRepositoryImpl")
    @Autowired
    private OrderQueryRepository orderQueryRepository;

    @Autowired
    private EntityManager em;


    private final LocalDateTime now = LocalDateTime.of(2025, 7, 24, 10, 0);

    @BeforeEach
    void setUp() {
        Order order1 = Order.builder()
                .orderName("첫 주문")
                .orderAt(now.minusDays(3))
                .shippedAt(now.minusDays(2))
                .orderStatus(OrderStatus.PENDING)
                .deliveryWant(LocalDate.of(2025, 7, 30))
                .totalBookPrice(10_000)
                .totalPavingPrice(2_000)
                .shippingFee(1_000)
                .totalDiscountAmount(3_000)
                .userId(1L)
                .recipientName("홍길동")
                .recipientEmail("hong@test.com")
                .recipientPhoneNumber("010-1111-1111")
                .recipientAddress("서울시 강남구")
                .build();

        Order order2 = Order.builder()
                .orderName("테스트 주문")
                .orderAt(now.minusDays(1))
                .shippedAt(now)
                .orderStatus(OrderStatus.SHIPPING)
                .deliveryWant(LocalDate.of(2025, 7, 29))
                .totalBookPrice(20_000)
                .totalPavingPrice(5_000)
                .shippingFee(3_000)
                .totalDiscountAmount(4_000)
                .userId(2L)
                .recipientName("이몽룡")
                .recipientEmail("lee@test.com")
                .recipientPhoneNumber("010-2222-2222")
                .recipientAddress("부산시 해운대구")
                .build();

        em.persist(order1);
        em.persist(order2);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("주문명으로 검색")
    void searchOrders_byOrderName_success() {
        // given
        OrderSearchDto searchDto = new OrderSearchDto();
        searchDto.setOrderName("테스트");

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderLookupResponse> result = orderQueryRepository.searchOrders(searchDto, pageable);

        // then
        assertThat(result).hasSize(1);
        OrderLookupResponse found = result.getContent().get(0);
        assertThat(found.getOrderName()).isEqualTo("테스트 주문");
        assertThat(found.getTotalPrice()).isEqualTo(20_000 + 5_000 + 3_000 - 4_000);
    }

    @Test
    @DisplayName("주문 기간으로 검색")
    void searchOrders_byDateRange_success() {
        OrderSearchDto searchDto = new OrderSearchDto();
        searchDto.setFrom(LocalDate.now().minusDays(2));
        searchDto.setTo(LocalDate.now());

        Pageable pageable = PageRequest.of(0, 10);

        Page<OrderLookupResponse> result = orderQueryRepository.searchOrders(searchDto, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderName()).contains("테스트");
    }

    @Test
    @DisplayName("조건 없이 전체 조회")
    void searchOrders_noCondition_returnsAll() {
        OrderSearchDto searchDto = new OrderSearchDto();
        Pageable pageable = PageRequest.of(0, 10);

        Page<OrderLookupResponse> result = orderQueryRepository.searchOrders(searchDto, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
