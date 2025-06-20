package shop.wannab.order_payment_service.repository;

import shop.wannab.order_payment_service.entity.CartItem;

import java.util.List;

public interface CartRepository {
    List<CartItem> getCartItems(long userIdentifier);

    void addItemToCart(long userIdentifier, long bookId);

    void updateItemQuantity(long userIdentifier, long bookId, int quantity);

    void removeItemFromCart(long userIdentifier, long bookId);

    void createCart(long userIdentifier);

}
