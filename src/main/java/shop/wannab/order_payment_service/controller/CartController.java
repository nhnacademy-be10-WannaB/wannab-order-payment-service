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
    public OrderBookInfoListDto getCartItems(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestBody(required = false) Long guestId) {
        if (Objects.nonNull(userId)) {
            return cartService.getCartItemInfos(userId);
        }
        return cartService.getCartItemInfos(guestId);
    }

    @PostMapping("/books")
    public void addProductToCart(@RequestHeader(value = "X-USER-ID", required = false) Long userId, @RequestBody(required = false) Long guestId, @RequestParam Long bookId) {
        if (Objects.nonNull(userId)) {
            cartService.addCartItem(userId, bookId);
        } else {
            cartService.addCartItem(guestId, bookId);
        }
    }

    @PutMapping("/books/{book-id}")
    public void updateCartItemQuantity(@RequestHeader(value = "X-USER-ID", required = false) Long userId,
                                                       @RequestBody(required = false) Long guestId,
                                                       @PathVariable(name = "book-id") Long bookId,
                                                       @RequestParam int quantity) {
        if (Objects.nonNull(userId)) {
            cartService.updateItemQuantity(userId, bookId, quantity);
        } else {
            cartService.updateItemQuantity(guestId, bookId, quantity);
        }

    }

    @DeleteMapping("/books/{book-id}")
    public void removeProductFromCart(@RequestHeader("X-USER-ID") Long userId, @RequestBody(required = false) Long guestId, @PathVariable(name = "book-id") Long bookId) {
        if (Objects.nonNull(userId)) {
            cartService.removeProductFromCart(userId, bookId);
        } else {
            cartService.removeProductFromCart(guestId, bookId);
        }
    }


}
