package shop.wannab.order_payment_service.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.service.CartService;

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
    public OrderBookInfoListDto getCartItems(@RequestHeader("X-User-Id") Long userId) {
        return cartService.getCartItemInfos(userId);
    }

    @PostMapping("/books")
    public OrderBookInfoListDto addProductToCart(@RequestHeader("X-User-Id") Long userId, @RequestParam Long bookId) {
        cartService.addCartItem(userId, bookId);
        return cartService.getCartItemInfos(userId);
    }

    @PutMapping("/books/{book-id}")
    public OrderBookInfoListDto updateCartItemQuantity(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") Long bookId, @RequestParam int quantity) {
        cartService.updateItemQuantity(userId, bookId, quantity);
        return cartService.getCartItemInfos(userId);
    }

    @DeleteMapping("/books/{book-id}")
    public OrderBookInfoListDto removeProductFromCart(@RequestHeader("X-User-Id") Long userId, @PathVariable(name = "book-id") Long bookId) {
        cartService.removeProductFromCart(userId, bookId);
        return cartService.getCartItemInfos(userId);
    }


}
