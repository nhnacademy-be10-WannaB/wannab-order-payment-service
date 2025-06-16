package shop.wannab.order_payment_service.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;
import shop.wannab.order_payment_service.exception.WrappingPaperAlreadyExistsException;
import shop.wannab.order_payment_service.exception.WrappingPaperNotFoundException;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
import shop.wannab.order_payment_service.service.WrappingPaperService;


@Service
@RequiredArgsConstructor
public class WrappingPaperServiceImpl implements WrappingPaperService {

    private final WrappingPaperRepository wrappingPaperRepository;


    //포장지 생성
    @Transactional
    @Override
    public WrappingPaper creatWrappingPaper(WrappingPaperRequest request) {

        //이름 중복검사
        if(wrappingPaperRepository.existsByName(request.getName())){
            throw new WrappingPaperAlreadyExistsException(request.getName());
        }

        WrappingPaper wrappingPaper = new WrappingPaper();
        wrappingPaper.setName(request.getName());
        wrappingPaper.setPrice(request.getPrice());

        return wrappingPaperRepository.save(wrappingPaper);
    }

    //포장지 수정
    @Transactional
    @Override
    public WrappingPaper updateWrappingPaper(Long wpId, WrappingPaperRequest request) {

        WrappingPaper wrappingPaper = wrappingPaperRepository.findById(wpId).orElseThrow(()-> new WrappingPaperNotFoundException(wpId));

        //이름 중복검사
        if(wrappingPaperRepository.existsByName(request.getName())){
            throw new WrappingPaperAlreadyExistsException(request.getName());
        }

        wrappingPaper.setName(request.getName());
        wrappingPaper.setPrice(request.getPrice());

        return wrappingPaperRepository.save(wrappingPaper);
    }

    //포장지 삭제
    @Transactional
    @Override
    public void deleteWrappingPaper(Long wpId) {
        WrappingPaper wrappingPaper = wrappingPaperRepository.findById(wpId).orElseThrow(()-> new WrappingPaperNotFoundException(wpId));

        wrappingPaperRepository.delete(wrappingPaper);
    }
}
