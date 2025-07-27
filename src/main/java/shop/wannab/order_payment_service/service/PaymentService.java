package shop.wannab.order_payment_service.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.TossPaymentsApiClient;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.PaymentProvider;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.CouponUsageRequestDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.entity.dto.PointHistoryCreateDTO;
import shop.wannab.order_payment_service.entity.payment.Cancel;
import shop.wannab.order_payment_service.entity.payment.Failure;
import shop.wannab.order_payment_service.entity.payment.Payment;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;
import shop.wannab.order_payment_service.event.OrderCreatedEvent;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.exception.PaymentProcessingException;
import shop.wannab.order_payment_service.properties.TossPaymentsProperties;
import shop.wannab.order_payment_service.repository.CancelRepository;
import shop.wannab.order_payment_service.repository.CouponUsageTempRedisRepository;
import shop.wannab.order_payment_service.repository.FailureRepository;
import shop.wannab.order_payment_service.repository.OrderItemTempRedisRepository;
import shop.wannab.order_payment_service.repository.OrderRepository;
import shop.wannab.order_payment_service.repository.PaymentRepository;
import shop.wannab.order_payment_service.repository.PointHistoryCreateDtoRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentsApiClient tossPaymentsApiClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CancelRepository cancelRepository;
    private final FailureRepository failureRepository;
    private final OrderItemTempRedisRepository itemTempRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PointHistoryCreateDtoRepository pointHistoryCreateDtoRepository;
    private final CouponUsageTempRedisRepository couponUsageTempRedisRepository;
    private final TossPaymentsProperties tossPaymentsProperties;

    @Transactional
    public FinalOrderResultDto confirmAndProcessTossPayment(TossConfirmRequestDto requestDto) {
        log.info("action=confirmAndProcessTossPayment,orderId = {},paymentKey={},amount={}, message=\"결제 승인 로직 시작\"",requestDto.getOrderId(),requestDto.getPaymentKey(),requestDto.getAmount());

        String encodedAuth = Base64.getEncoder().encodeToString((tossPaymentsProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + encodedAuth;

//      TODO: 주문 정보 조회 및 상태 변경
//      왜 이렇게 파싱하냐면 토스페이먼츠 API에 orderId를 보낼때 최소 6자이상 String 타입을 보내야해서 붙였습니다
//      그래서 아래에 파싱한 orderId로 OrderRepository에서 값 변경하시거나 활용할때 사용하시면 될거같습니다.
        Long orderId = Long.parseLong(requestDto.getOrderId().replace(tossPaymentsProperties.getPrefix(), ""));
        log.debug("Prefix 파싱 완료 orderId : {}",orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderPaymentServiceException(OrderPaymentErrorCode.NOT_FOUND_ORDER_INFO));
        log.debug("orderId로 order 찾기 완료 orderId : {}",orderId);

        Payment payment = new Payment();
        payment.setPaymentKey(requestDto.getPaymentKey());
        payment.setOrder(order);
        payment.setPaymentProvider(PaymentProvider.TOSS);
        payment.setTotalAmount(requestDto.getAmount());
        payment.setRequestAt(LocalDateTime.now());
        payment.setStatus(OrderStatus.PENDING.name());
        log.debug("Payment 객체 생성 완료 PaymentKey : {}",payment.getPaymentKey());

        try {
            TossConfirmResponseDto tossResponse = tossPaymentsApiClient.confirmPayment(authHeader, requestDto);
            log.debug("토스페이먼츠 통신 및 승인 완료 PaymentKey : {}",payment.getPaymentKey());

            payment.setType(tossResponse.getType());
            payment.setMethod(tossResponse.getMethod());
            payment.setBalanceAmount(tossResponse.getBalanceAmount());
            payment.setStatus(tossResponse.getStatus());
            log.debug("Payment setStatus 완료 PaymentKey: {}",payment.getPaymentKey());

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
                log.debug("결제 실패 FAILED PaymentKey : {}",payment.getPaymentKey());

                String errorCode = tossResponse.getFailure().getCode();
                String errorMessage = tossResponse.getFailure().getMessage();
                savePaymentFailure(payment, errorCode, errorMessage);
                log.debug("결제 실패 Failure 객체 생성 PaymentKey : {}",payment.getPaymentKey());

                throw new PaymentProcessingException(errorCode, errorMessage, requestDto.getOrderId(), requestDto.getPaymentKey(), HttpStatus.BAD_REQUEST);

            }

            order.setOrderStatus(OrderStatus.PAID);
            paymentRepository.save(payment);
            log.debug("결제 성공 Payment 객체 생성 PaymentKey : {}",payment.getPaymentKey());

            List<CartItem> orderItems = itemTempRedisRepository.getOrderItems(order.getUserId());
            OrderItemListDto itemListDto = new OrderItemListDto(orderItems);

            PointHistoryCreateDTO pointHistoryCreateDTO = null;

            List<CouponUsageRequestDto.UsedCouponInfo> usedCouponInfos = null;
            if (Objects.nonNull(order.getUserId())) {
                usedCouponInfos = couponUsageTempRedisRepository.consumeUsedCouponInfos(order.getUserId());
                pointHistoryCreateDTO = pointHistoryCreateDtoRepository.consumeByOrderId(orderId);
                log.debug("포인트 생성 orderId : {}",orderId);
            }

            CouponUsageRequestDto couponUsageRequestDto = new CouponUsageRequestDto();
            couponUsageRequestDto.setOrderId(orderId);
            couponUsageRequestDto.setUsedCoupons(usedCouponInfos);
            couponUsageRequestDto.setUserId(order.getUserId());
            log.debug("쿠폰 사용 처리 orderId : {}",orderId);

            eventPublisher.publishEvent(new OrderCreatedEvent(order, order.getUserId(), itemListDto, pointHistoryCreateDTO, couponUsageRequestDto));
            log.debug("eventPublisher 성공");

            itemTempRedisRepository.deleteOrderItems(order.getUserId());
            log.debug("재고 감소 orderId : {}",orderId);
            log.info("action=confirmAndProcessTossPayment,orderId = {},paymentKey={},amount={}, message=\"결제 승인 로직 완료\"",requestDto.getOrderId(),requestDto.getPaymentKey(),requestDto.getAmount());
            return new FinalOrderResultDto(
                    tossResponse.getPaymentKey(),
                    tossResponse.getOrderId(),
                    tossResponse.getTotalAmount()
            );
        } catch (PaymentProcessingException e) {
            throw e;
        } catch (Exception e) {
            order.setOrderStatus(OrderStatus.FAILED);
            payment.setStatus("API_CALL_FAILED");
            paymentRepository.save(payment);
            String errorCode = "API_CALL_ERROR";
            String errorMessage = "토스페이먼츠 API 호출 중 예외 발생: " + e.getMessage();
            savePaymentFailure(payment, errorCode, errorMessage);
            log.debug("토스페이먼츠 API 호출 중 예외 발생 : {}",payment.getPaymentKey());
            throw new PaymentProcessingException(errorCode, errorMessage, requestDto.getOrderId(), requestDto.getPaymentKey(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }

    }

    private void savePaymentFailure(Payment payment, String errorCode, String message) {
        Failure failure = new Failure(
                payment,
                errorCode,
                message
        );
        failureRepository.save(failure);
    }

    @Transactional
    public void paymentCancel(Long orderId, Integer cancelAmount) {
        log.info("action=paymentCancel,orderId = {},cancelAmount = {}, message=\"결제 취소 로직 시작\"",orderId,cancelAmount);
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new IllegalStateException("결제 내역이 존재하지 않습니다."));

        payment.setStatus("CANCELLED");

        Cancel cancel = new Cancel(
                payment,
                cancelAmount,
                LocalDateTime.now()
        );
        log.info("action=paymentCancel,orderId = {},cancelAmount = {}, message=\"결제 취소 로직 완료\"",orderId,cancelAmount);

        cancelRepository.save(cancel);
    }
}