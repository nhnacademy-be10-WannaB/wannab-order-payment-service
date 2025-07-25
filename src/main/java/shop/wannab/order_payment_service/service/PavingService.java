package shop.wannab.order_payment_service.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.Paving;
import shop.wannab.order_payment_service.entity.dto.PavingRequest;
import shop.wannab.order_payment_service.entity.dto.PavingResponse;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.repository.PavingRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PavingService {

    private final PavingRepository pavingRepository;

    /**
     * 생성
     */
    public Paving createPaving(PavingRequest request){

        //이름 중복 검사
        if(pavingRepository.existsByName(request.getName())){
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.PAVING_ALREADY_EXISTS);
        }

        Paving paving = new Paving();
        paving.setName(request.getName());
        paving.setPrice(request.getPrice());

        return pavingRepository.save(paving);

    }

    /**
     * 수정
     */
    public Paving updatePaving(Long id, PavingRequest request){

        Paving paving = pavingRepository.findById(id).orElseThrow(()-> new OrderPaymentServiceException(OrderPaymentErrorCode.PAVING_NOT_EXISTS));

        // 이름 중복검사 -> 같은 정책수정에 대해서는 이름중복예외처리 x
        pavingRepository.findByName(request.getName()).ifPresent(pv -> {
            if (!pv.getId().equals(id)) {
                throw new IllegalArgumentException(request.getName());
            }
        });

        paving.setName(request.getName());
        paving.setPrice(request.getPrice());

        return paving;
    }

    /**
     * 삭제
     */
    public void deletePaving(Long id){
        Paving paving = pavingRepository.findById(id).orElseThrow(()-> new OrderPaymentServiceException(OrderPaymentErrorCode.PAVING_NOT_EXISTS));

        pavingRepository.deleteById(id);
    }

    /**
     * 조회
     */

    @Transactional(readOnly = true)
    public List<PavingResponse> getPavingList() {

        return pavingRepository.findAll().stream()
                .map(pv -> new PavingResponse(pv.getId(), pv.getName(), pv.getPrice()))
                .collect(Collectors.toList());
    }

}

