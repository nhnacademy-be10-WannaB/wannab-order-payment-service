package shop.wannab.order_payment_service.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;
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

        WrappingPaper wrappingPaper = new WrappingPaper();
        wrappingPaper.setName(request.getName());
        wrappingPaper.setPrice(request.getPrice());

        return wrappingPaperRepository.save(wrappingPaper);
    }
}
