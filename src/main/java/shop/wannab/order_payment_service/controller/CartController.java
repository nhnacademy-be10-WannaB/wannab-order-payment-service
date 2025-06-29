package shop.wannab.order_payment_service.controller;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.service.CartService;

import java.util.Objects;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public Cookie createCart(@RequestHeader(value = "X-USER-ID", required = false) Long userIdentifier) {
        Cookie guestCookieOrNull = cartService.createCart(userIdentifier);
        if (Objects.nonNull(guestCookieOrNull)) {
            return guestCookieOrNull;
        }
        return null; //회원장바구니 생성의 경우, 회원api에서 이 메서드 호출하기만 하면 됨. 반환값으로 하는것 없다
    }

    @GetMapping
    public OrderBookInfoListDto getCartItems(@RequestHeader("X-USER-ID") Long userId) {
        return cartService.getCartItemInfos(userId);
    }

    @PostMapping("/books")
    public OrderBookInfoListDto addProductToCart(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestParam Long bookId) {
        cartService.addCartItem(userId, bookId);
        return cartService.getCartItemInfos(userId);
    }

    @PutMapping("/books/{book-id}")
    public OrderBookInfoListDto updateCartItemQuantity(@RequestHeader("X-USER-ID") Long userId, @PathVariable(name = "book-id") Long bookId, @RequestParam int quantity) {
        cartService.updateItemQuantity(userId, bookId, quantity);
        return cartService.getCartItemInfos(userId);
    }

    @DeleteMapping("/books/{book-id}")
    public OrderBookInfoListDto removeProductFromCart(@RequestHeader("X-USER-ID") Long userId, @PathVariable(name = "book-id") Long bookId) {
        cartService.removeProductFromCart(userId, bookId);
        return cartService.getCartItemInfos(userId);
    }


}
