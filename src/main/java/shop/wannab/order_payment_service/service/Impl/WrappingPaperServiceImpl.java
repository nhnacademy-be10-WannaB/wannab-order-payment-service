package shop.wannab.order_payment_service.service.Impl;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperResponse;
import shop.wannab.order_payment_service.exception.WrappingPaperAlreadyExistsException;
import shop.wannab.order_payment_service.exception.WrappingPaperNotFoundException;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
import shop.wannab.order_payment_service.service.WrappingPaperService;


@Slf4j
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
    public WrappingPaper updateWrappingPaper(Long id, WrappingPaperRequest request) {

        WrappingPaper wrappingPaper = wrappingPaperRepository.findById(id).orElseThrow(()-> new WrappingPaperNotFoundException(id));

        //이름 중복검사
        wrappingPaperRepository.findByName(request.getName()).ifPresent(wp -> {
            if(!wp.getId().equals(id)){
                throw new WrappingPaperAlreadyExistsException(request.getName());
            }
        });

        wrappingPaper.setName(request.getName());
        wrappingPaper.setPrice(request.getPrice());

        return wrappingPaper;
    }

    //포장지 삭제
    @Transactional
    @Override
    public void deleteWrappingPaper(Long id) {
        WrappingPaper wrappingPaper = wrappingPaperRepository.findById(id).orElseThrow(()-> new WrappingPaperNotFoundException(id));

        wrappingPaperRepository.delete(wrappingPaper);
    }

    //포장지목록 전체조회
    @Transactional(readOnly = true)
    @Override
    public List<WrappingPaperResponse> getWrappingPaperList() {
        log.debug("WrappingPaperService Impl : GetWrappingPaperList");

        //DTO반환을 위해 매핑처리
        return wrappingPaperRepository.findAll().stream()
                .map(wp -> new WrappingPaperResponse(wp.getId(), wp.getName(), wp.getPrice()))
                .collect(Collectors.toList());
    }
}
