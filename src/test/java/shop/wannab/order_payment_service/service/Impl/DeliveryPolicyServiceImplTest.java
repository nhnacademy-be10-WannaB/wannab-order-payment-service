package shop.wannab.order_payment_service.service.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyRequest;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyResponse;
import shop.wannab.order_payment_service.exception.DeliveryPolicyAlreadyExistsException;
import shop.wannab.order_payment_service.exception.DeliveryPolicyNotFoundException;
import shop.wannab.order_payment_service.repository.DeliveryPolicyRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryPolicyServiceImplTest {

    @Mock
    private DeliveryPolicyRepository deliveryPolicyRepository;

    @InjectMocks
    private DeliveryPolicyServiceImpl deliveryPolicyService;

    @Test
    @DisplayName("배송 정책 생성 성공")
    void createDeliveryPolicy_Success() {
        // Given
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("새 정책", 3000, 50000);

        when(deliveryPolicyRepository.existsByName(request.getName())).thenReturn(false);

        DeliveryPolicy savedPolicy = new DeliveryPolicy(1L, "새 정책", 3000, 50000);
        when(deliveryPolicyRepository.save(any(DeliveryPolicy.class))).thenReturn(savedPolicy);

        // When
        DeliveryPolicy result = deliveryPolicyService.createDeliveryPolicy(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("새 정책");
        assertThat(result.getId()).isEqualTo(1L);
        verify(deliveryPolicyRepository, times(1)).existsByName("새 정책");
        verify(deliveryPolicyRepository, times(1)).save(any(DeliveryPolicy.class));
    }

    @Test
    @DisplayName("배송 정책 생성 실패 - 이름 중복")
    void createDeliveryPolicy_AlreadyExistsException() {
        // Given
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("기존 정책", 3000, 50000);

        when(deliveryPolicyRepository.existsByName(request.getName())).thenReturn(true);

        // When & Then
        assertThrows(DeliveryPolicyAlreadyExistsException.class,
                () -> deliveryPolicyService.createDeliveryPolicy(request));

        verify(deliveryPolicyRepository, times(1)).existsByName("기존 정책");
        verify(deliveryPolicyRepository, never()).save(any(DeliveryPolicy.class));
    }

    @Test
    @DisplayName("배송 정책 수정 성공")
    void updateDeliveryPolicy_Success() {
        // Given
        Long policyId = 1L;
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("수정된 정책", 4000, 60000);

        DeliveryPolicy existingPolicy = new DeliveryPolicy(policyId, "원래 정책", 3000, 50000);

        when(deliveryPolicyRepository.findById(policyId)).thenReturn(Optional.of(existingPolicy));
        when(deliveryPolicyRepository.findByName(request.getName())).thenReturn(Optional.empty()); // No name conflict

        // When
        DeliveryPolicy result = deliveryPolicyService.updateDeliveryPolicy(policyId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(policyId);
        assertThat(result.getName()).isEqualTo(request.getName());
        assertThat(result.getFee()).isEqualTo(request.getFee());
        assertThat(result.getMinPrice()).isEqualTo(request.getMinPrice());

        verify(deliveryPolicyRepository, times(1)).findById(policyId);
        verify(deliveryPolicyRepository, times(1)).findByName(request.getName());
    }

    @Test
    @DisplayName("배송 정책 수정 실패 - 정책을 찾을 수 없음")
    void updateDeliveryPolicy_NotFoundException() {
        // Given
        Long policyId = 99L; // 존재하지 않는 ID
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("수정된 정책", 4000, 60000);

        when(deliveryPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DeliveryPolicyNotFoundException.class,
                () -> deliveryPolicyService.updateDeliveryPolicy(policyId, request));

        verify(deliveryPolicyRepository, times(1)).findById(policyId);
        verify(deliveryPolicyRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("배송 정책 수정 실패 - 다른 정책과 이름 중복")
    void updateDeliveryPolicy_AlreadyExistsException() {
        // Given
        Long policyId = 1L;
        DeliveryPolicyRequest request = new DeliveryPolicyRequest("중복 이름", 4000, 60000);

        // @AllArgsConstructor 생성자로 기존 정책 객체와 다른 중복 정책 객체를 만듭니다.
        DeliveryPolicy existingPolicy = new DeliveryPolicy(policyId, "원래 정책", 3000, 50000);
        DeliveryPolicy otherPolicyWithSameName = new DeliveryPolicy(2L, "중복 이름", 5000, 70000);

        when(deliveryPolicyRepository.findById(policyId)).thenReturn(Optional.of(existingPolicy));
        when(deliveryPolicyRepository.findByName(request.getName())).thenReturn(Optional.of(otherPolicyWithSameName));

        // When & Then
        assertThrows(DeliveryPolicyAlreadyExistsException.class,
                () -> deliveryPolicyService.updateDeliveryPolicy(policyId, request));

        verify(deliveryPolicyRepository, times(1)).findById(policyId);
        verify(deliveryPolicyRepository, times(1)).findByName("중복 이름");
    }

    @Test
    @DisplayName("배송 정책 삭제 성공")
    void deleteDeliveryPolicy_Success() {
        // Given
        Long policyId = 1L;
        DeliveryPolicy existingPolicy = new DeliveryPolicy(policyId, "삭제 대상", 1000, 20000);

        when(deliveryPolicyRepository.findById(policyId)).thenReturn(Optional.of(existingPolicy));
        doNothing().when(deliveryPolicyRepository).deleteById(policyId);

        // When
        deliveryPolicyService.deleteDeliveryPolicy(policyId);

        // Then
        verify(deliveryPolicyRepository, times(1)).findById(policyId);
        verify(deliveryPolicyRepository, times(1)).deleteById(policyId);
    }

    @Test
    @DisplayName("배송 정책 삭제 실패 - 정책을 찾을 수 없음")
    void deleteDeliveryPolicy_NotFoundException() {
        // Given
        Long policyId = 99L;

        when(deliveryPolicyRepository.findById(policyId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DeliveryPolicyNotFoundException.class,
                () -> deliveryPolicyService.deleteDeliveryPolicy(policyId));

        verify(deliveryPolicyRepository, times(1)).findById(policyId);
        verify(deliveryPolicyRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("배송 정책 목록 조회 성공")
    void getDeliveryPolicyList_Success() {
        // Given
        DeliveryPolicy policy1 = new DeliveryPolicy(1L, "정책1", 2500, 0);
        DeliveryPolicy policy2 = new DeliveryPolicy(2L, "정책2", 0, 50000);

        List<DeliveryPolicy> policies = Arrays.asList(policy1, policy2);

        when(deliveryPolicyRepository.findAll()).thenReturn(policies);

        // When
        List<DeliveryPolicyResponse> result = deliveryPolicyService.getDeliveryPolicyList();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("정책1");
        assertThat(result.get(0).getMinPrice()).isEqualTo(0);
        assertThat(result.get(0).getFee()).isEqualTo(2500);

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("정책2");
        assertThat(result.get(1).getMinPrice()).isEqualTo(50000);
        assertThat(result.get(1).getFee()).isEqualTo(0);

        verify(deliveryPolicyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("배송 정책 목록 조회 - 빈 목록 반환")
    void getDeliveryPolicyList_EmptyList() {
        // Given
        when(deliveryPolicyRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<DeliveryPolicyResponse> result = deliveryPolicyService.getDeliveryPolicyList();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(deliveryPolicyRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("적용 가능한 배송 정책 찾기 성공 - 가장 높은 최소 주문 금액 정책 반환")
    void findApplicablePolicy_Success_MaxMinPrice() {
        // Given
        DeliveryPolicy policy1 = new DeliveryPolicy(1L, "2만원 이상", 2500, 20000);
        DeliveryPolicy policy2 = new DeliveryPolicy(2L, "5만원 이상", 0, 50000);
        DeliveryPolicy policy3 = new DeliveryPolicy(3L, "기본배송비", 3000, 0);

        List<DeliveryPolicy> policies = Arrays.asList(policy1, policy2, policy3);

        when(deliveryPolicyRepository.findAll()).thenReturn(policies);

        // When
        DeliveryPolicy result = deliveryPolicyService.findApplicablePolicy(60000);

        // Then
        assertThat(result).isEqualTo(policy2);
        verify(deliveryPolicyRepository, times(1)).findAll();
        verify(deliveryPolicyRepository, never()).findByName("기본배송비");
    }

    @Test
    @DisplayName("적용 가능한 배송 정책 찾기 - 기본 정책 반환 (적용 가능한 정책이 없을 경우)")
    void findApplicablePolicy_FallbackToDefault() {
        // Given
        DeliveryPolicy policy1 = new DeliveryPolicy(1L, "2만원 이상", 2500, 20000);
        DeliveryPolicy policy2 = new DeliveryPolicy(2L, "5만원 이상", 0, 50000);

        List<DeliveryPolicy> policies = Arrays.asList(policy1, policy2);
        when(deliveryPolicyRepository.findAll()).thenReturn(policies);

        DeliveryPolicy defaultPolicy = new DeliveryPolicy(3L, "기본배송비", 3000, 0);
        when(deliveryPolicyRepository.findByName("기본배송비")).thenReturn(Optional.of(defaultPolicy));

        // When
        DeliveryPolicy result = deliveryPolicyService.findApplicablePolicy(10000); // 10000원은 어떤 정책에도 적용 안 됨

        // Then
        assertThat(result).isEqualTo(defaultPolicy);
        verify(deliveryPolicyRepository, times(1)).findAll();
        verify(deliveryPolicyRepository, times(1)).findByName("기본배송비"); // 기본 정책이 호출되었는지 확인
    }

    @Test
    @DisplayName("기본 배송 정책 찾기 성공")
    void getDefaultPolicy_Success() {
        // Given
        DeliveryPolicy defaultPolicy = new DeliveryPolicy(1L, "기본배송비", 3000, 0);

        when(deliveryPolicyRepository.findByName("기본배송비")).thenReturn(Optional.of(defaultPolicy));

        // When
        DeliveryPolicy result = deliveryPolicyService.getDefaultPolicy();

        // Then
        assertThat(result).isEqualTo(defaultPolicy);
        verify(deliveryPolicyRepository, times(1)).findByName("기본배송비");
    }

    @Test
    @DisplayName("기본 배송 정책 찾기 실패 - 기본 정책이 설정되지 않음")
    void getDefaultPolicy_IllegalArgumentException() {
        // Given
        when(deliveryPolicyRepository.findByName("기본배송비")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> deliveryPolicyService.getDefaultPolicy());

        verify(deliveryPolicyRepository, times(1)).findByName("기본배송비");
    }
}