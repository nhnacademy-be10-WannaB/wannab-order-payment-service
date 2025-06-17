package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WrappingPaperResponse {
    private final Long wpId; // 포장지 옵션 ID
    private final String name; // 포장지 이름
    private final int price; // 포장지 가격
}
