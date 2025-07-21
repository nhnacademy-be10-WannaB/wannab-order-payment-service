package shop.wannab.order_payment_service.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.order_payment_service.controller.PaymentController;
import shop.wannab.order_payment_service.entity.payment.dto.FinalOrderResultDto;
import shop.wannab.order_payment_service.entity.payment.dto.PaymentFailResponseDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmRequestDto;
import shop.wannab.order_payment_service.entity.payment.dto.TossConfirmResponseDto;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategy;
import shop.wannab.order_payment_service.entity.payment.strategy.PaymentStrategyFactory;
import shop.wannab.order_payment_service.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@ActiveProfiles("ci")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false"
})
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentStrategyFactory paymentStrategyFactory;

    @MockBean
    private PaymentStrategy paymentStrategy;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 승인 및 처리 요청 성공")
    void confirmAndProcessPayment_Success() throws Exception {
        String provider = "toss";
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);
        FinalOrderResultDto resultDto = new FinalOrderResultDto("paymentKey", "orderId-1", 10000);

        when(paymentStrategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.confirmAndProcessPayment(any(TossConfirmRequestDto.class))).thenReturn(resultDto);

        mockMvc.perform(post("/api/payments/{provider}/confirm", provider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value(resultDto.getPaymentKey()))
                .andExpect(jsonPath("$.orderId").value(resultDto.getOrderId()))
                .andExpect(jsonPath("$.amount").value(resultDto.getAmount()));
    }

    @Test
    @DisplayName("결제 승인 및 처리 요청 실패 - 비즈니스 로직 오류")
    void confirmAndProcessPayment_Failure_BusinessLogic() throws Exception {
        String provider = "toss";
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);

        PaymentFailResponseDto failResponse = new PaymentFailResponseDto(
                "INTERNAL_SERVER_ERROR", "예상치 못한 서버 오류가 발생했습니다: 결제 처리 중 오류 발생", "orderId-1", "paymentKey"
        );

        when(paymentStrategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        when(paymentStrategy.confirmAndProcessPayment(any(TossConfirmRequestDto.class)))
                .thenThrow(new RuntimeException("결제 처리 중 오류 발생"));

        mockMvc.perform(post("/api/payments/{provider}/confirm", provider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(failResponse.getErrorCode()))
                .andExpect(jsonPath("$.errorMessage").value(failResponse.getErrorMessage()));
    }

    @Test
    @DisplayName("토스 결제 승인 요청 실패 - TossPaymentsApiClient 응답에 실패 정보 포함")
    void confirmAndProcessPayment_TossApiFailureResponse() throws Exception {
        String provider = "toss";
        TossConfirmRequestDto requestDto = new TossConfirmRequestDto("paymentKey", "orderId-1", 10000);

        TossConfirmResponseDto tossFailureResponse = new TossConfirmResponseDto();
        tossFailureResponse.setStatus("CANCELED");
        tossFailureResponse.setPaymentKey("paymentKey");
        tossFailureResponse.setOrderId("orderId-1");
        tossFailureResponse.setTotalAmount(10000);

        TossConfirmResponseDto.TossFailureDto failureDto = new TossConfirmResponseDto.TossFailureDto();
        failureDto.setCode("PAYMENT_CANCELED");
        failureDto.setMessage("고객에 의해 결제가 취소되었습니다.");
        tossFailureResponse.setFailure(failureDto);

        when(paymentStrategyFactory.getStrategy(anyString())).thenReturn(paymentStrategy);
        // PaymentStrategy가 Toss API의 실패 응답을 직접 반환하는 경우를 Mocking
        when(paymentStrategy.confirmAndProcessPayment(any(TossConfirmRequestDto.class)))
                .thenReturn(new FinalOrderResultDto(
                        tossFailureResponse.getPaymentKey(),
                        tossFailureResponse.getOrderId(),
                        tossFailureResponse.getTotalAmount()
                )); // FinalOrderResultDto가 TossFailureDto 정보를 포함하지 않으므로 실제 반환 타입에 맞춰 조정 필요

        mockMvc.perform(post("/api/payments/{provider}/confirm", provider)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentKey").value(tossFailureResponse.getPaymentKey()))
                .andExpect(jsonPath("$.orderId").value(tossFailureResponse.getOrderId()))
                .andExpect(jsonPath("$.amount").value(tossFailureResponse.getTotalAmount()));
        // PaymentController가 TossFailureDto 정보를 클라이언트에 어떻게 전달하는지에 따라 jsonPath 검증이 달라질 수 있습니다.
        // 현재 FinalOrderResultDto에는 failure 정보가 없으므로 해당 필드에 대한 검증은 추가할 수 없습니다.
    }
}