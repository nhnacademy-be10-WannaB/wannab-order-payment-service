package shop.wannab.order_payment_service.service;

import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;

public interface WrappingPaperService {

    //포장지 생성
    WrappingPaper creatWrappingPaper(WrappingPaperRequest request);
}
