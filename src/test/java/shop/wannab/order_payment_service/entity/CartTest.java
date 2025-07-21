package shop.wannab.order_payment_service.entity;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

    @Test
    void testDefaultConstructorAndSetters() {
        Cart cart = new Cart();
        cart.setUserId(1L);

        assertThat(cart.getUserId()).isEqualTo(1L);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void testAllArgsConstructor() {
        CartItemEntity item1 = new CartItemEntity();
        CartItemEntity item2 = new CartItemEntity();
        List<CartItemEntity> items = List.of(item1, item2);

        Cart cart = new Cart(2L, new ArrayList<>(items));

        assertThat(cart.getUserId()).isEqualTo(2L);
        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    void testAddItem() {
        Cart cart = new Cart();
        cart.setUserId(3L);

        CartItemEntity item = new CartItemEntity();
        cart.addItem(item);

        assertThat(cart.getItems()).contains(item);
        assertThat(item.getCart()).isEqualTo(cart);
    }
}