package shop.wannab.order_payment_service.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.service.CartService;

import java.util.List;
import java.util.Objects;

@RestController("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public void createCart(@RequestHeader(value = "X-User-Id", required = false) Long userIdentifier, HttpServletResponse response) {
        Cookie guestCookieOrNull = cartService.createCart(userIdentifier);
        if (Objects.nonNull(guestCookieOrNull)) {
            response.addCookie(guestCookieOrNull);
        }
    }

    @GetMapping
    public List<CartItem> getCartItems(@RequestHeader("X-User-Id") Long userId) {
        return cartService.getCartItems(userId);
    }

    @PostMapping("/books")
    public List<CartItem> addProductToCart(@RequestHeader("X-User-Id") Long userId, @RequestParam Long bookId) {
        cartService.addCartItem(userId, bookId);
        return cartService.getCartItems(userId);
    }

    @PutMapping("/books/{book-id}")
    public List<CartItem> updateCartItemQuantity(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") Long bookId, @RequestParam int quantity) {
        cartService.updateItemQuantity(userId, bookId, quantity);
        return cartService.getCartItems(userId);
    }

    @DeleteMapping("/books/{book-id}")
    public List<CartItem> removeProductFromCart(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") Long bookId) {
        cartService.removeProductFromCart(userId, bookId);
        return cartService.getCartItems(userId);
    }


}
