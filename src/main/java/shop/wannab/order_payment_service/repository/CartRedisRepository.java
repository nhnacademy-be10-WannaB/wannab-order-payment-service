package shop.wannab.order_payment_service.repository;

import shop.wannab.order_payment_service.entity.dto.CartItem;

import java.util.List;

public interface CartRedisRepository {
    List<CartItem> getCartItems(Long userIdentifier);

    void addItemToCart(Long userIdentifier, long bookId);

    void updateItemQuantity(Long userIdentifier, long bookId, int quantity);

    void removeItemFromCart(Long userIdentifier, long bookId);

    void createCart(Long userIdentifier);

}
