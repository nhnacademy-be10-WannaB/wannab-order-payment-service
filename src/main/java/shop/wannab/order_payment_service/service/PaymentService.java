package shop.wannab.order_payment_service.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.TossPaymentsApiClient;
import shop.wannab.order_payment_service.entity.payment.Cancel;
import shop.wannab.order_payment_service.entity.payment.Payment;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;
import shop.wannab.order_payment_service.repository.CancelRepository;
import shop.wannab.order_payment_service.repository.OrderRepository;
import shop.wannab.order_payment_service.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentsApiClient tossPaymentsApiClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CancelRepository cancelRepository;

    @Value("${toss.payments.secretKey}")
    private String secretKey;

    @Transactional
    public FinalOrderResultDto confirmAndProcessPayment(TossConfirmRequestDto requestDto) {
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

        TossConfirmResponseDto tossResponse = tossPaymentsApiClient.confirmPayment(authHeader, requestDto);

        if (tossResponse.getTotalAmount() != requestDto.getAmount()) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

//      TODO: 주문 정보 조회 및 상태 변경
//      왜 이렇게 파싱하냐면 토스페이먼츠 API에 orderId를 보낼때 최소 6자이상 String 타입을 보내야해서 붙였습니다
//      그래서 아래에 파싱한 orderId로 OrderRepository에서 값 변경하시거나 활용할때 사용하시면 될거같습니다.
        Long orderId = Long.parseLong(requestDto.getOrderId().replace("testWannaB", ""));
        LocalDateTime approvedAt = null;
        if (tossResponse.getApprovedAt() != null && !tossResponse.getApprovedAt().isEmpty()) {
            approvedAt = OffsetDateTime.parse(tossResponse.getApprovedAt()).toLocalDateTime();
        }
        LocalDateTime requestedAt = OffsetDateTime.parse(tossResponse.getRequestedAt()).toLocalDateTime();

        // 4. Payment 객체 생성
        Payment payment = new Payment(
                tossResponse.getPaymentKey(),
                tossResponse.getType(),
                tossResponse.getMethod(),
                tossResponse.getTotalAmount(),
                tossResponse.getBalanceAmount(),
                tossResponse.getStatus(),
                requestedAt,
                approvedAt,
                orderRepository.findById(orderId).orElse(null)
        );

        // 5. DB에 Payment 정보 저장
        paymentRepository.save(payment);

        // 6. 프론트 서비스에 전달할 최종 결과 DTO 생성 및 반환
        return new FinalOrderResultDto(
                tossResponse.getPaymentKey(),
                tossResponse.getOrderId(),
                tossResponse.getTotalAmount()
        );
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