package shop.wannab.order_payment_service.entity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;


//TODO:클라이언트에게 보여줄 주문페이지 (임시)
@Data
@AllArgsConstructor
public class OrderBookResponse {
    private final Long bookId;
    private final String title;
    private final String author;
    private final String thumbnailUrl;
    private final int quantity;
    private final int bookPrice;
    private final List<PavingResponse> pavings;


    //회원
    //쿠폰 list
}