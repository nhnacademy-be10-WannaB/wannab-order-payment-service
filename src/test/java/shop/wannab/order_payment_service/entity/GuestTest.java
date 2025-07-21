package shop.wannab.order_payment_service.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GuestTest {

    @Test
    public void testGuestConstructorWithPasswordAndOrder() {
        Order mockOrder = new Order();
        String password = "1234";

        Guest guest = new Guest(password, mockOrder);

        assertEquals(password, guest.getPassword());
        assertEquals(mockOrder, guest.getOrder());
        assertNull(guest.getId());
    }

    @Test
    public void testSettersAndGetters() {
        Guest guest = new Guest();
        String password = "5678";
        Order order = new Order();

        guest.setPassword(password);
        guest.setOrder(order);

        assertEquals(password, guest.getPassword());
        assertEquals(order, guest.getOrder());
    }
}