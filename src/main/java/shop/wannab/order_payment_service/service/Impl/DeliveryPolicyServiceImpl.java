package shop.wannab.order_payment_service.service.Impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyRequest;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyResponse;
import shop.wannab.order_payment_service.exception.DeliveryPolicyAlreadyExistsException;
import shop.wannab.order_payment_service.exception.DeliveryPolicyNotFoundException;
import shop.wannab.order_payment_service.repository.DeliveryPolicyRepository;
import shop.wannab.order_payment_service.service.DeliveryPolicyService;

@Service
@RequiredArgsConstructor
public class DeliveryPolicyServiceImpl implements DeliveryPolicyService {

    private final DeliveryPolicyRepository deliveryPolicyRepository;

    //생성
    @Transactional
    @Override
    public DeliveryPolicy createDeliveryPolicy(DeliveryPolicyRequest request) {

        // 이름 중복검사
        if(deliveryPolicyRepository.existsByName(request.getName())){
            throw new DeliveryPolicyAlreadyExistsException(request.getName());
        }

        DeliveryPolicy deliveryPolicy = new DeliveryPolicy();
        deliveryPolicy.setName(request.getName());
        deliveryPolicy.setFee(request.getFee());
        deliveryPolicy.setMinPrice(request.getMinPrice());

        return deliveryPolicyRepository.save(deliveryPolicy);
    }

    //수정
    @Transactional
    @Override
    public DeliveryPolicy updateDeliveryPolicy(Long id, DeliveryPolicyRequest request) {

        //정책 유무 검사
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findById(id).orElseThrow(()-> new DeliveryPolicyNotFoundException(id));

        // 이름 중복검사 -> 같은 정책수정에 대해서는 이름중복예외처리 x
        deliveryPolicyRepository.findByName(request.getName()).ifPresent(dp -> {
            if (!dp.getId().equals(id)) {
                throw new DeliveryPolicyAlreadyExistsException(request.getName());
            }
        });

        deliveryPolicy.setName(request.getName());
        deliveryPolicy.setFee(request.getFee());
        deliveryPolicy.setMinPrice(request.getMinPrice());

        return deliveryPolicy;
    }

    //삭제
    @Transactional
    @Override
    public void deleteDeliveryPolicy(Long id) {
        //정책 유무 검사
        DeliveryPolicy deliveryPolicy = deliveryPolicyRepository.findById(id).orElseThrow(()-> new DeliveryPolicyNotFoundException(id));

        deliveryPolicyRepository.deleteById(id);
    }

    //전체목록조회
    @Transactional(readOnly = true)
    @Override
    public List<DeliveryPolicyResponse> getDeliveryPolicyList() {

        //DTO로 변환하기위해 매핑처리
        return deliveryPolicyRepository.findAll().stream()
                .map(dp -> new DeliveryPolicyResponse(dp.getId(), dp.getName(), dp.getMinPrice(), dp.getFee()))
                .collect(Collectors.toList());
    }


    //정책 계산 로직 -> 주문한 금액을 받고 그 금액에 알맞는 배송 정책을 반환
    @Override
    public DeliveryPolicy findApplicablePolicy(int totalBookPrice) {
        List<DeliveryPolicy> list = deliveryPolicyRepository.findAll();

        return list.stream().filter(dp -> dp.isApplicable(totalBookPrice))
                .max(Comparator.comparingInt(DeliveryPolicy::getMinPrice))
                .orElseGet(this::getDefaultPolicy);
    }

    //기본 배송비정책 로직
    @Override
    public DeliveryPolicy getDefaultPolicy() {
        return deliveryPolicyRepository.findByName("기본배송비").orElseThrow(()-> new IllegalArgumentException("배송비 기본정책이 설정되어있지 않습니다."));
    }
}
