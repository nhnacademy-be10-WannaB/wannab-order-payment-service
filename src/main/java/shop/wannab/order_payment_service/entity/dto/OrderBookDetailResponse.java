package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//주문상세페이지에서 보여줄 도서에대한 정보를 담는 dto
@Data
@AllArgsConstructor
public class OrderBookDetailResponse {
    private final Long bookId;
    private final String title;
    private final int quantity;
    private final int bookTotalPrice; //도서마다 총가격 (도서개당가격 * quantity)
    private final String thumbnailUrl;
}
