package shop.wannab.order_payment_service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GuestCartCookieDto {
    private String keyName = "guestId";
    private long value;
    private int cookieMaxAge;

    public GuestCartCookieDto(long value, int cookieMaxAge) {
        this.value = value;
        this.cookieMaxAge = cookieMaxAge;
    }
}
