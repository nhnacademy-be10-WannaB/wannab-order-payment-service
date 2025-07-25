package shop.wannab.order_payment_service.entity;

import java.util.List;
import org.junit.jupiter.api.Test;
import shop.wannab.order_payment_service.entity.DiscountType;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountTypeTest {

    @Test
    public void testValueOf_FIXED() {
        DiscountType type = DiscountType.valueOf("FIXED");
        assertEquals(DiscountType.FIXED, type);
    }

    @Test
    public void testValueOf_PERCENT() {
        DiscountType type = DiscountType.valueOf("PERCENT");
        assertEquals(DiscountType.PERCENT, type);
    }

    @Test
    public void testValuesContainsAll() {
        DiscountType[] values = DiscountType.values();
        assertEquals(2, values.length);
        assertTrue(List.of(values).contains(DiscountType.FIXED));
        assertTrue(List.of(values).contains(DiscountType.PERCENT));
    }

    @Test
    public void testInvalidValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DiscountType.valueOf("INVALID");
        });
    }
}