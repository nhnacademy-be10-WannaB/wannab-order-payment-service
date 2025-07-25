package shop.wannab.order_payment_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemEntityTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        CartItemEntity item = new CartItemEntity();

        item.setCart(new Cart());
        assertThat(item.getCart()).isNotNull();
    }

    @Test
    void testAllArgsConstructor() {
        Cart cart = new Cart();
        CartItemEntity item = new CartItemEntity(1L, 100L, 3, cart);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getBookId()).isEqualTo(100L);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getCart()).isEqualTo(cart);
    }

    @Test
    void testCustomConstructor() {
        CartItemEntity item = new CartItemEntity(200L, 5);

        assertThat(item.getBookId()).isEqualTo(200L);
        assertThat(item.getQuantity()).isEqualTo(5);
        assertThat(item.getCart()).isNull();
    }

    @Test
    void testSetCart() {
        CartItemEntity item = new CartItemEntity(300L, 2);
        Cart cart = new Cart();

        item.setCart(cart);

        assertThat(item.getCart()).isEqualTo(cart);
    }
}