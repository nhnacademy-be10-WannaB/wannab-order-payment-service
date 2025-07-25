package shop.wannab.order_payment_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.order_payment_service.entity.Paving;
import shop.wannab.order_payment_service.entity.dto.PavingRequest;
import shop.wannab.order_payment_service.entity.dto.PavingResponse;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.repository.PavingRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PavingServiceTest {

    @Mock
    private PavingRepository pavingRepository;

    @InjectMocks
    private PavingService pavingService;

    private Paving initialPaving;
    private PavingRequest newPavingRequest;
    private PavingRequest existingPavingRequest;
    private Paving existingPavingWithSameId;
    private Paving existingPavingWithDifferentId;

    @BeforeEach
    void setUp() {
        initialPaving = new Paving(1L, "기존 포장지", 1000);
        newPavingRequest = new PavingRequest("새로운 포장지", 2000);
        existingPavingRequest = new PavingRequest("기존 포장지", 1500);

        existingPavingWithSameId = new Paving(1L, "수정될 포장지", 1000);
        existingPavingWithDifferentId = new Paving(2L, "다른 포장지", 3000);
    }

    @Test
    @DisplayName("포장지 생성 성공")
    void createPaving_Success() {
        // Given
        Long expectedGeneratedId = 10L;

        when(pavingRepository.existsByName(newPavingRequest.getName())).thenReturn(false);

        when(pavingRepository.save(any(Paving.class))).thenAnswer(invocation -> {
            Paving receivedPaving = invocation.getArgument(0);

            return new Paving(expectedGeneratedId, receivedPaving.getName(), receivedPaving.getPrice());
        });

        // When
        Paving createdPaving = pavingService.createPaving(newPavingRequest);

        // Then
        assertThat(createdPaving).isNotNull();
        assertThat(createdPaving.getName()).isEqualTo(newPavingRequest.getName());
        assertThat(createdPaving.getPrice()).isEqualTo(newPavingRequest.getPrice());
        assertThat(createdPaving.getId()).isEqualTo(expectedGeneratedId);

        verify(pavingRepository, times(1)).existsByName(newPavingRequest.getName());
        verify(pavingRepository, times(1)).save(any(Paving.class));
    }

    @Test
    @DisplayName("포장지 생성 실패 - 이름 중복")
    void createPaving_Failure_NameExists() {
        // Given
        when(pavingRepository.existsByName(existingPavingRequest.getName())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> pavingService.createPaving(existingPavingRequest))
                .isInstanceOf(OrderPaymentServiceException.class)
                .hasMessage("이미 존재하는 포장지 이름입니다.");

        verify(pavingRepository, times(1)).existsByName(existingPavingRequest.getName());
        verify(pavingRepository, never()).save(any(Paving.class));
    }

    @Test
    @DisplayName("포장지 수정 성공 - 이름 변경 및 가격 변경")
    void updatePaving_Success_NameAndPriceChange() {
        // Given
        Long idToUpdate = 1L;
        when(pavingRepository.findById(idToUpdate)).thenReturn(Optional.of(initialPaving));
        when(pavingRepository.findByName(newPavingRequest.getName())).thenReturn(Optional.empty());

        // When
        Paving updatedPaving = pavingService.updatePaving(idToUpdate, newPavingRequest);

        // Then
        assertThat(updatedPaving).isNotNull();
        assertThat(updatedPaving.getId()).isEqualTo(idToUpdate);
        assertThat(updatedPaving.getName()).isEqualTo(newPavingRequest.getName());
        assertThat(updatedPaving.getPrice()).isEqualTo(newPavingRequest.getPrice());

        verify(pavingRepository, times(1)).findById(idToUpdate);
        verify(pavingRepository, times(1)).findByName(newPavingRequest.getName());
    }

    @Test
    @DisplayName("포장지 수정 성공 - 같은 ID에 같은 이름 유지")
    void updatePaving_Success_SameNameSameId() {
        // Given
        Long idToUpdate = 1L;
        PavingRequest requestWithSameName = new PavingRequest("기존 포장지", 2000);

        when(pavingRepository.findById(idToUpdate)).thenReturn(Optional.of(initialPaving));
        when(pavingRepository.findByName(requestWithSameName.getName())).thenReturn(Optional.of(initialPaving));

        // When
        Paving updatedPaving = pavingService.updatePaving(idToUpdate, requestWithSameName);

        // Then
        assertThat(updatedPaving).isNotNull();
        assertThat(updatedPaving.getId()).isEqualTo(idToUpdate);
        assertThat(updatedPaving.getName()).isEqualTo(requestWithSameName.getName());
        assertThat(updatedPaving.getPrice()).isEqualTo(requestWithSameName.getPrice());

        verify(pavingRepository, times(1)).findById(idToUpdate);
        verify(pavingRepository, times(1)).findByName(requestWithSameName.getName());
    }

    @Test
    @DisplayName("포장지 수정 실패 - 존재하지 않는 포장지 ID")
    void updatePaving_Failure_NotFound() {
        // Given
        Long nonExistentId = 99L;
        when(pavingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pavingService.updatePaving(nonExistentId, newPavingRequest))

                .isInstanceOf(OrderPaymentServiceException.class)
                .hasMessage("존재하지 않는 포장지입니다.");

        verify(pavingRepository, times(1)).findById(nonExistentId);
        verify(pavingRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("포장지 수정 실패 - 다른 ID의 포장지와 이름 중복")
    void updatePaving_Failure_NameConflictWithDifferentId() {
        // Given
        Long idToUpdate = 1L;
        PavingRequest requestWithConflictingName = new PavingRequest("다른 포장지", 1000);

        when(pavingRepository.findById(idToUpdate)).thenReturn(Optional.of(initialPaving));
        when(pavingRepository.findByName(requestWithConflictingName.getName()))
                .thenReturn(Optional.of(existingPavingWithDifferentId));

        // When & Then
        assertThatThrownBy(() -> pavingService.updatePaving(idToUpdate, requestWithConflictingName))
                .isInstanceOf(OrderPaymentServiceException.class)
                .hasMessage(OrderPaymentErrorCode.PAVING_ALREADY_EXISTS.getMessage());

        verify(pavingRepository, times(1)).findById(idToUpdate);
        verify(pavingRepository, times(1)).findByName(requestWithConflictingName.getName());
    }

    @Test
    @DisplayName("포장지 삭제 성공")
    void deletePaving_Success() {
        // Given
        Long idToDelete = 1L;
        when(pavingRepository.findById(idToDelete)).thenReturn(Optional.of(initialPaving));
        doNothing().when(pavingRepository).deleteById(idToDelete);

        // When
        pavingService.deletePaving(idToDelete);

        // Then
        verify(pavingRepository, times(1)).findById(idToDelete);
        verify(pavingRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    @DisplayName("포장지 삭제 실패 - 존재하지 않는 포장지 ID")
    void deletePaving_Failure_NotFound() {
        // Given
        Long nonExistentId = 99L;
        when(pavingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> pavingService.deletePaving(nonExistentId))
                .isInstanceOf(OrderPaymentServiceException.class)
                .hasMessage("존재하지 않는 포장지입니다.");

        verify(pavingRepository, times(1)).findById(nonExistentId);
        verify(pavingRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("포장지 목록 조회 성공")
    void getPavingList_Success() {
        // Given
        List<Paving> pavings = Arrays.asList(
                new Paving(1L, "포장지A", 1000),
                new Paving(2L, "포장지B", 2000)
        );
        when(pavingRepository.findAll()).thenReturn(pavings);

        // When
        List<PavingResponse> result = pavingService.getPavingList();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("포장지A");
        assertThat(result.get(0).getPrice()).isEqualTo(1000);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("포장지B");
        assertThat(result.get(1).getPrice()).isEqualTo(2000);

        verify(pavingRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("포장지 목록 조회 성공 - 빈 목록")
    void getPavingList_Empty() {
        // Given
        when(pavingRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<PavingResponse> result = pavingService.getPavingList();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(pavingRepository, times(1)).findAll();
    }
}