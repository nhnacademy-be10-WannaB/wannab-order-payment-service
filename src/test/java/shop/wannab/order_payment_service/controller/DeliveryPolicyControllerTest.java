package shop.wannab.order_payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyRequest;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyResponse;
import shop.wannab.order_payment_service.repository.DeliveryPolicyRepository;
import shop.wannab.order_payment_service.service.DeliveryPolicyService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryPolicyController.class)
@ActiveProfiles("ci")
class DeliveryPolicyControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DeliveryPolicyService deliveryPolicyService;

    @MockBean
    DeliveryPolicyRepository deliveryPolicyRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("배송정책 생성 성공")
    void createDeliveryPolicy_success() throws Exception {
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("기본 배송", 30000, 2500);
        DeliveryPolicy saved = new DeliveryPolicy(1L, "기본 배송", 2500, 30000);

        when(deliveryPolicyService.createDeliveryPolicy(any())).thenReturn(saved);

        mockMvc.perform(post("/api/admin/delivery-policy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("기본 배송"))
                .andExpect(jsonPath("$.minPrice").value(30000))
                .andExpect(jsonPath("$.fee").value(2500));
    }

    @Test
    @DisplayName("배송정책 수정 성공")
    void updateDeliveryPolicy_success() throws Exception {
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("새 배송정책", 40000, 2000);
        DeliveryPolicy updated = new DeliveryPolicy(2L, "새 배송정책", 2000, 40000);

        when(deliveryPolicyService.updateDeliveryPolicy(eq(2L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/admin/delivery-policy/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("새 배송정책"))
                .andExpect(jsonPath("$.minPrice").value(40000))
                .andExpect(jsonPath("$.fee").value(2000));
    }

    @Test
    @DisplayName("배송정책 삭제 성공")
    void deleteDeliveryPolicy_success() throws Exception {
        doNothing().when(deliveryPolicyService).deleteDeliveryPolicy(3L);

        mockMvc.perform(delete("/api/admin/delivery-policy/3"))
                .andExpect(status().isOk());

        verify(deliveryPolicyService).deleteDeliveryPolicy(3L);
    }

    @Test
    @DisplayName("배송정책 목록 조회")
    void getDeliveryPolicyList_success() throws Exception {
        List<DeliveryPolicyResponse> mockList = List.of(
                new DeliveryPolicyResponse(1L, "기본", 30000, 2500),
                new DeliveryPolicyResponse(2L, "할인", 50000, 1000)
        );

        when(deliveryPolicyService.getDeliveryPolicyList()).thenReturn(mockList);

        mockMvc.perform(get("/api/admin/delivery-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("기본"))
                .andExpect(jsonPath("$[1].fee").value(1000));
    }
}