package shop.wannab.order_payment_service.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import shop.wannab.order_payment_service.config.QuerydslConfig;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.PaymentProvider;
import shop.wannab.order_payment_service.entity.payment.Payment;
import shop.wannab.order_payment_service.repository.CartRedisRepositoryImpl;
import shop.wannab.order_payment_service.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")
@Import(QuerydslConfig.class)
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private CartRedisRepositoryImpl cartRedisRepositoryImpl;

    private Order order;
    private Payment payment; // Payment 객체를 필드로 선언하여 다른 테스트에서도 접근 가능하게 함

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .orderAt(LocalDateTime.now())
                .orderName("테스트 주문")
                .totalBookPrice(10000)
                .totalDiscountAmount(0)
                .totalPavingPrice(0)
                .shippingFee(2500)
                .userId(1L)
                .recipientName("테스트 수신자")
                .recipientEmail("test@example.com")
                .recipientPhoneNumber("010-1234-5678")
                .recipientAddress("테스트 주소")
                .build();
        entityManager.persist(order);

        // Payment 엔티티의 모든 필드를 명확하게 설정 (NotNull 포함)
        payment = Payment.builder()
                .paymentKey("test_payment_key")
                .type("NORMAL")
                .method("CARD")
                .totalAmount(10000)
                .balanceAmount(10000)
                .status("DONE")
                .requestAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.now().plusHours(1))
                .paymentProvider(PaymentProvider.TOSS)
                .order(order)
                .build();
        entityManager.persist(payment);
        entityManager.flush(); // DB에 즉시 반영
        entityManager.clear(); // 영속성 컨텍스트 초기화하여 DB에서 새로 읽어오도록 강제
    }

    @Test
    @DisplayName("주문 ID로 결제 정보 조회 성공 및 필드 검증")
    void findByOrder_Id_Success() {
        // when
        Optional<Payment> foundPaymentOptional = paymentRepository.findByOrder_Id(order.getId());

        // then
        assertThat(foundPaymentOptional).isPresent();
        Payment foundPayment = foundPaymentOptional.get();

        // 저장된 Payment 객체의 모든 필드를 검증하여 커버리지 확보
        assertThat(foundPayment.getPaymentKey()).isEqualTo(payment.getPaymentKey());
        assertThat(foundPayment.getType()).isEqualTo(payment.getType());
        assertThat(foundPayment.getMethod()).isEqualTo(payment.getMethod());
        assertThat(foundPayment.getTotalAmount()).isEqualTo(payment.getTotalAmount());
        assertThat(foundPayment.getBalanceAmount()).isEqualTo(payment.getBalanceAmount());
        assertThat(foundPayment.getStatus()).isEqualTo(payment.getStatus());
        assertThat(foundPayment.getRequestAt()).isEqualToIgnoringNanos(payment.getRequestAt()); // Nanos 무시 비교
        assertThat(foundPayment.getApprovedAt()).isEqualToIgnoringNanos(payment.getApprovedAt()); // Nanos 무시 비교
        assertThat(foundPayment.getPaymentProvider()).isEqualTo(payment.getPaymentProvider());
        assertThat(foundPayment.getOrder().getId()).isEqualTo(order.getId()); // Order 관계 확인
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 결제 정보 조회 시 빈 Optional 반환")
    void findByOrder_Id_NotFound() {
        // given
        Long nonExistentOrderId = 999L;

        // when
        Optional<Payment> foundPayment = paymentRepository.findByOrder_Id(nonExistentOrderId);

        // then
        assertThat(foundPayment).isNotPresent();
    }

    // 새로운 테스트: Payment 상태 업데이트 테스트 (Setter 커버리지)
    @Test
    @DisplayName("결제 상태 업데이트 성공")
    void updatePaymentStatus_Success() {
        // given
        String newStatus = "CANCELED";
        Payment existingPayment = paymentRepository.findByOrder_Id(order.getId()).orElseThrow();
        assertThat(existingPayment.getStatus()).isNotEqualTo(newStatus); // 초기 상태 확인

        // when
        existingPayment.setStatus(newStatus); // Setter 사용
        paymentRepository.save(existingPayment); // 변경사항 저장
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화

        // then
        Optional<Payment> updatedPaymentOptional = paymentRepository.findByOrder_Id(order.getId());
        assertThat(updatedPaymentOptional).isPresent();
        assertThat(updatedPaymentOptional.get().getStatus()).isEqualTo(newStatus);
    }

}