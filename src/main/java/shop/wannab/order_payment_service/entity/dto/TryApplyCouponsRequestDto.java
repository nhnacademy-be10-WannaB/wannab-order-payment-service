package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class TryApplyCouponsRequestDto {
    private Map<Long,Long> couponAndBookIds;
}
