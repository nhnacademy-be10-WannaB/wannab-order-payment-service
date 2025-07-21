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
import shop.wannab.order_payment_service.entity.Paving;
import shop.wannab.order_payment_service.entity.dto.PavingRequest;
import shop.wannab.order_payment_service.entity.dto.PavingResponse;
import shop.wannab.order_payment_service.service.PavingService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PavingController.class)
@ActiveProfiles("ci")
class PavingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PavingService pavingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포장 생성 성공")
    void createPaving_success() throws Exception {
        PavingRequest request = new PavingRequest("Premium", 1000);
        Paving paving = new Paving(1L, "Premium", 1000);

        when(pavingService.createPaving(any(PavingRequest.class))).thenReturn(paving);

        mockMvc.perform(post("/api/admin/paving")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paving.getId()))
                .andExpect(jsonPath("$.name").value(paving.getName()))
                .andExpect(jsonPath("$.price").value(paving.getPrice()));
    }

    @Test
    @DisplayName("포장 수정 성공")
    void updatePaving_success() throws Exception {
        Long id = 1L;
        PavingRequest request = new PavingRequest("Updated", 2000);
        Paving paving = new Paving(id, "Updated", 2000);

        when(pavingService.updatePaving(eq(id), any(PavingRequest.class))).thenReturn(paving);

        mockMvc.perform(post("/api/admin/paving/{paving-id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paving.getId()))
                .andExpect(jsonPath("$.name").value(paving.getName()))
                .andExpect(jsonPath("$.price").value(paving.getPrice()));
    }

    @Test
    @DisplayName("포장 삭제 성공")
    void deletePaving_success() throws Exception {
        Long id = 1L;
        doNothing().when(pavingService).deletePaving(id);

        mockMvc.perform(delete("/api/admin/paving/{paving-id}", id))
                .andExpect(status().isOk());

        verify(pavingService, times(1)).deletePaving(id);
    }

    @Test
    @DisplayName("포장 목록 조회 성공")
    void getPavingList_success() throws Exception {
        List<PavingResponse> list = Arrays.asList(
                new PavingResponse(1L, "Standard", 1000),
                new PavingResponse(2L, "Deluxe", 2000)
        );

        when(pavingService.getPavingList()).thenReturn(list);

        mockMvc.perform(get("/api/admin/paving"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(list.size()))
                .andExpect(jsonPath("$[0].id").value(list.get(0).getId()))
                .andExpect(jsonPath("$[1].name").value(list.get(1).getName()));
    }
}