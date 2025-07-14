package shop.wannab.order_payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.TossPaymentsApiClient;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.PaymentProvider;
import shop.wannab.order_payment_service.entity.payment.Cancel;
import shop.wannab.order_payment_service.entity.payment.Failure;
import shop.wannab.order_payment_service.entity.payment.Payment;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;
import shop.wannab.order_payment_service.exception.PaymentProcessingException;
import shop.wannab.order_payment_service.repository.CancelRepository;
import shop.wannab.order_payment_service.repository.FailureRepository;
import shop.wannab.order_payment_service.repository.OrderRepository;
import shop.wannab.order_payment_service.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentsApiClient tossPaymentsApiClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CancelRepository cancelRepository;
    private final FailureRepository failureRepository;

    @Value("${toss.payments.secretKey}")
    private String secretKey;

    @Transactional
    public FinalOrderResultDto confirmAndProcessPayment(TossConfirmRequestDto requestDto) {
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

//      TODO: 주문 정보 조회 및 상태 변경
//      왜 이렇게 파싱하냐면 토스페이먼츠 API에 orderId를 보낼때 최소 6자이상 String 타입을 보내야해서 붙였습니다
//      그래서 아래에 파싱한 orderId로 OrderRepository에서 값 변경하시거나 활용할때 사용하시면 될거같습니다.
        Long orderId = Long.parseLong(requestDto.getOrderId().replace("testWannaBShop", ""));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));

        Payment payment = new Payment();
        payment.setPaymentKey(requestDto.getPaymentKey());
        payment.setOrder(order);
        payment.setPaymentProvider(PaymentProvider.TOSS);
        payment.setTotalAmount(requestDto.getAmount());
        payment.setRequestAt(LocalDateTime.now());
        payment.setStatus(OrderStatus.PENDING.name());

        try {
            TossConfirmResponseDto tossResponse = tossPaymentsApiClient.confirmPayment(authHeader, requestDto);

            payment.setType(tossResponse.getType());
            payment.setMethod(tossResponse.getMethod());
            payment.setBalanceAmount(tossResponse.getBalanceAmount());
            payment.setStatus(tossResponse.getStatus());

            if (tossResponse.getTotalAmount() != requestDto.getAmount()) {
                throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
            }

            if (tossResponse.getApprovedAt() != null && !tossResponse.getApprovedAt().isEmpty()) {
                payment.setApprovedAt(OffsetDateTime.parse(tossResponse.getApprovedAt()).toLocalDateTime());
            } else {
                payment.setApprovedAt(null);
            }
            if ("FAILED".equals(tossResponse.getStatus())) {
                order.setOrderStatus(OrderStatus.FAILED);
                paymentRepository.save(payment);

                String errorCode = tossResponse.getFailure().getCode();
                String errorMessage = tossResponse.getFailure().getMessage();
                savePaymentFailure(payment, errorCode, errorMessage);
                throw new PaymentProcessingException(errorCode, errorMessage, requestDto.getOrderId(), requestDto.getPaymentKey(), HttpStatus.BAD_REQUEST);

            }

            order.setOrderStatus(OrderStatus.PAID);
            paymentRepository.save(payment);

            return new FinalOrderResultDto(
                    tossResponse.getPaymentKey(),
                    tossResponse.getOrderId(),
                    tossResponse.getTotalAmount()
            );
        } catch (Exception e) {
            order.setOrderStatus(OrderStatus.FAILED);
            payment.setStatus("API_CALL_FAILED");
            paymentRepository.save(payment);
            String errorCode = "API_CALL_ERROR";
            String errorMessage = "토스페이먼츠 API 호출 중 예외 발생: " + e.getMessage();
            savePaymentFailure(payment, errorCode, errorMessage);
            throw new PaymentProcessingException(errorCode, errorMessage, requestDto.getOrderId(), requestDto.getPaymentKey(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

    }

    private void savePaymentFailure(Payment payment, String errorCode, String message) {
        Failure failure = new Failure(
                payment.getPaymentKey(),
                payment,
                errorCode,
                message
        );
        failureRepository.save(failure);
    }

    @Transactional
    public void paymentCancel(Long orderId, Integer cancelAmount) {
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalStateException("결제 내역이 존재하지 않습니다."));

        payment.setStatus("CANCELLED");

        Cancel cancel = new Cancel(
                payment,
                cancelAmount,
                LocalDateTime.now()
        );

        cancelRepository.save(cancel);
    }
}