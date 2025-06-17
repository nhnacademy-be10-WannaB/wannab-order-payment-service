package shop.wannab.order_payment_service.service;


import java.util.List;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyRequest;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyResponse;

public interface DeliveryPolicyService {

    // 정책 생성
    DeliveryPolicy createDeliveryPolicy(DeliveryPolicyRequest request);

    // 정책 수정
    DeliveryPolicy updateDeliveryPolicy(Long id, DeliveryPolicyRequest request);

    // 정책 삭제
    void deleteDeliveryPolicy(Long id);

    // 정책 전체 목록 조회
    List<DeliveryPolicyResponse> getDeliveryPolicyList();



    //계산로직
    DeliveryPolicy findApplicablePolicy(int orderPrice);

    //기본배송비정책반환 -> 계산로직에서 필요
    DeliveryPolicy getDefaultPolicy();
}
