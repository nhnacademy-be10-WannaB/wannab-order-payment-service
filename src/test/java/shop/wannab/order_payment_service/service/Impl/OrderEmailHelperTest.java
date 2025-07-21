package shop.wannab.order_payment_service.service.Impl;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import shop.wannab.order_payment_service.entity.Order;

class OrderEmailHelperTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderEmailHelper orderEmailHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("주문 이메일 전송 성공")
    void sendOrderEmail_success() {
        // given
        Long orderId = 11L;
        String orderName = "테스트 주문";
        LocalDateTime shippedAt = null;
        LocalDate deliveryWant = LocalDate.of(2025, 7, 20);
        int totalBookPrice = 20000;
        int totalDiscountAmount = 3000;
        int shippingFee = 2500;
        int totalPavingPrice = 1000;
        String recipientName = "정민수수수수";
        String recipientEmail = "test@tttt.com";
        String recipientPhone = "01011111111";
        String recipientAddress = "광주어딘가";

        Order order = new Order(
                1L,
                orderName,
                shippedAt,
                deliveryWant,
                totalBookPrice,
                totalDiscountAmount,
                shippingFee,
                totalPavingPrice,
                recipientName,
                recipientEmail,
                recipientPhone,
                recipientAddress
        );

        java.lang.reflect.Field idField = null;
        try {
            idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, orderId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // when
        orderEmailHelper.sendOrderEmail(order, recipientEmail, recipientAddress, recipientName);

        // then
        String expectedText = String.format("""
                %s님, 주문이 완료되었습니다.

                ▷ 주문번호: %d
                ▷ 결제금액: %,d원
                ▷ 배송주소: %s
                ▷ 배송희망일: %s

                감사합니다.
            """, recipientName, orderId, order.getTotalPrice(), recipientAddress, deliveryWant);

        verify(emailService, times(1)).sendOrderEmail(
                eq(recipientEmail),
                eq("[WannaB] 주문확인서"),
                eq(expectedText)
        );
    }
}