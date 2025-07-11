package shop.wannab.order_payment_service.service;

import java.util.List;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperResponse;

public interface WrappingPaperService {

    //포장지 생성
    WrappingPaper creatWrappingPaper(WrappingPaperRequest request);

    //포장지 수정
    WrappingPaper updateWrappingPaper(Long id, WrappingPaperRequest request);

    //포장지 삭제
    void deleteWrappingPaper(Long id);

    //포장지 목록 조회 (전체조회) -> 나중에 필요하면 페이징 구현
    List<WrappingPaperResponse> getWrappingPaperList();
}
