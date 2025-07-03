package shop.wannab.order_payment_service.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderBookInfo {
    private long bookId;
    private String title;
    private int originPrice;
    private int salesPrice;
    private int quantity;
    //private String thumbnailUrl;
    private List<BookCouponDto> applicableCoupons = new ArrayList<>();
}
