package shop.wannab.order_payment_service.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import shop.wannab.order_payment_service.client.TossPaymentsApiClient;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.payment.Failure;
import shop.wannab.order_payment_service.entity.payment.Payment;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;
import shop.wannab.order_payment_service.exception.PaymentProcessingException;
import shop.wannab.order_payment_service.properties.TossPaymentsProperties;
import shop.wannab.order_payment_service.repository.*;
import shop.wannab.order_payment_service.service.PaymentService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private TossPaymentsApiClient tossPaymentsApiClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CancelRepository cancelRepository;

    @Mock
    private FailureRepository failureRepository;

    @Mock
    private OrderItemTempRedisRepository itemTempRedisRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PointHistoryCreateDtoRepository pointHistoryCreateDtoRepository;

    @Mock
    private CouponUsageTempRedisRepository couponUsageTempRedisRepository;

    @Mock
    private TossPaymentsProperties tossPaymentsProperties;

    @Test
    @DisplayName("토스 결제 승인 및 처리 성공")
    void confirmAndProcessTossPayment_Success() {
        // given
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);
        Order order = Order.builder().id(1L).build();
        TossConfirmResponseDto tossResponse = new TossConfirmResponseDto();
        tossResponse.setStatus("DONE");
        tossResponse.setTotalAmount(10000);

        when(tossPaymentsProperties.getPrefix()).thenReturn("orderId-");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(tossPaymentsApiClient.confirmPayment(any(), any(TossConfirmRequestDto.class))).thenReturn(tossResponse);

        // when
        paymentService.confirmAndProcessTossPayment(requestDto);

        // then
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 금액 불일치")
    void confirmAndProcessTossPayment_AmountMismatch() {
        // given
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);
        Order order = Order.builder().id(1L).build();
        TossConfirmResponseDto tossResponse = new TossConfirmResponseDto();
        tossResponse.setStatus("DONE");
        tossResponse.setTotalAmount(9000); // Different amount

        when(tossPaymentsProperties.getPrefix()).thenReturn("orderId-");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(tossPaymentsApiClient.confirmPayment(any(), any(TossConfirmRequestDto.class))).thenReturn(tossResponse);

        // when & then
        assertThrows(PaymentProcessingException.class, () -> paymentService.confirmAndProcessTossPayment(requestDto));
    }

    @Test
    @DisplayName("결제 실패")
    void confirmAndProcessTossPayment_Failed() {
        // given
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);
        Order order = Order.builder().id(1L).build();
        TossConfirmResponseDto tossResponse = new TossConfirmResponseDto();
        tossResponse.setStatus("FAILED");
        TossConfirmResponseDto.TossFailureDto failure = new TossConfirmResponseDto.TossFailureDto();
        failure.setCode("ERROR_CODE");
        failure.setMessage("ERROR_MESSAGE");
        tossResponse.setFailure(failure);
        tossResponse.setTotalAmount(10000);

        when(tossPaymentsProperties.getPrefix()).thenReturn("orderId-");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(tossPaymentsApiClient.confirmPayment(any(), any(TossConfirmRequestDto.class))).thenReturn(tossResponse);

        // when & then
        assertThrows(PaymentProcessingException.class, () -> paymentService.confirmAndProcessTossPayment(requestDto));
        verify(failureRepository).save(any(Failure.class));
    }

    @Test
    @DisplayName("API 호출 예외")
    void confirmAndProcessTossPayment_ApiCallException() {
        // given
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);
        Order order = Order.builder().id(1L).build();

        when(tossPaymentsProperties.getPrefix()).thenReturn("orderId-");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(tossPaymentsApiClient.confirmPayment(any(), any(TossConfirmRequestDto.class))).thenThrow(new RuntimeException("API Error"));

        // when & then
        assertThrows(PaymentProcessingException.class, () -> paymentService.confirmAndProcessTossPayment(requestDto));
        verify(failureRepository).save(any(Failure.class));
    }


    @Test
    @DisplayName("결제 취소 성공")
    void paymentCancel_Success() {
        // given
        Long orderId = 1L;
        Integer cancelAmount = 5000;
        Order order = Order.builder().id(orderId).build();
        Payment payment = Payment.builder().order(order).build();

        when(paymentRepository.findByOrder_Id(orderId)).thenReturn(Optional.of(payment));

        // when
        paymentService.paymentCancel(orderId, cancelAmount);

        // then
        verify(cancelRepository).save(any());
    }
}
