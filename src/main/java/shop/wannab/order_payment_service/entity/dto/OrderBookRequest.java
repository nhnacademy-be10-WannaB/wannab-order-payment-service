package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

//주문서에서 도서에 대한 내용을 입력받을 DTO (bookId, quantity는 장바구니에서 정보를 받아옴)
@Data
public class OrderBookRequest {
    @NotNull
    private Long bookId; //책id

    @Min(value = 1)
    private int quantity; //수량

    private Long selectedWrappingId; //포장지 선택 (없으면 null)


    //회원
    private Long selectedcouponId; //쿠폰선택 (없으면 null)
}
