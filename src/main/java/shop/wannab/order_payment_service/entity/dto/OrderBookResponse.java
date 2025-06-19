package shop.wannab.order_payment_service.entity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;


//TODO:클라이언트에게 보여줄 주문페이지 (임시)
@Data
@AllArgsConstructor
public class OrderBookResponse {
    private Long bookId;
    private String title;
    private String author;
    private String thumbnailUrl;
    private int quantity;
    private int bookPrice;
    private List<WrappingPaperResponse> wrappingPapers;


    //회원
    //쿠폰 list
}
