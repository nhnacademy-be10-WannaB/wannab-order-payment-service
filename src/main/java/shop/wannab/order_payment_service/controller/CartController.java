package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.service.CartService;

import java.util.List;

@RestController("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public void createCart(@RequestBody String userIdentifier) {
        //비회원일 경우 ttl 걸어줘야함
        cartService.createCart(userIdentifier);
    }

    @GetMapping
    public List<CartItem> getCartItems(@RequestHeader("X-User-Id") long userId) {
        return cartService.getCartItems(userId);
    }

    @PostMapping("/books")
    public List<CartItem> addProductToCart(@RequestHeader("X-User-Id") Long userId, @RequestParam long bookId) {
        cartService.addCartItem(userId, bookId);
        return cartService.getCartItems(userId);
    }

    @PutMapping("/books/{book-id}")
    public List<CartItem> updateCartItemQuantity(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") long bookId, @RequestParam int quantity) {
        cartService.updateItemQuantity(userId, bookId, quantity);
        return cartService.getCartItems(userId);
    }

    @DeleteMapping("/books/{book-id}")
    public List<CartItem> removeProductFromCart(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") long bookId) {
        cartService.removeProductFromCart(userId, bookId);
        return cartService.getCartItems(userId);
    }


}
