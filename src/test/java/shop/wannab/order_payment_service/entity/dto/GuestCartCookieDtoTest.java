package shop.wannab.order_payment_service.entity.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuestCartCookieDtoTest {

    @Test
    @DisplayName("생성자 확인")
    void testAllArgsConstructor() {
        String keyName = "guestId";
        long value = 12345L;
        int cookieMaxAge = 3600;

        GuestCartCookieDto dto = new GuestCartCookieDto(keyName, value, cookieMaxAge);

        assertThat(dto.getKeyName()).isEqualTo("guestId");
        assertThat(dto.getValue()).isEqualTo(12345L);
        assertThat(dto.getCookieMaxAge()).isEqualTo(3600);
    }

    @Test
    @DisplayName("value와 cookieMaxAge 생성자 확인")
    void testPartialArgsConstructor() {
        long value = 999L;
        int cookieMaxAge = 1800;

        GuestCartCookieDto dto = new GuestCartCookieDto(value, cookieMaxAge);

        assertThat(dto.getKeyName()).isEqualTo("guestId");
        assertThat(dto.getValue()).isEqualTo(999L);
        assertThat(dto.getCookieMaxAge()).isEqualTo(1800);
    }

    @Test
    @DisplayName("Setter 및 Getter 동작 확인")
    void testSettersAndGetters() {
        GuestCartCookieDto dto = new GuestCartCookieDto();
        dto.setKeyName("guestId");
        dto.setValue(777L);
        dto.setCookieMaxAge(600);

        assertThat(dto.getKeyName()).isEqualTo("guestId");
        assertThat(dto.getValue()).isEqualTo(777L);
        assertThat(dto.getCookieMaxAge()).isEqualTo(600);
    }
}