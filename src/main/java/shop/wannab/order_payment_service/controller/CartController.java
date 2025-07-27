package shop.wannab.order_payment_service.controller;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.dto.GuestCartCookieDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.service.CartService;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public GuestCartCookieDto createCart(@RequestHeader(value = "X-USER-ID", required = false) Long userIdentifier) {
        GuestCartCookieDto guestCartCookieDto = cartService.createCart(userIdentifier);
        if (Objects.nonNull(guestCartCookieDto)) {
            return guestCartCookieDto;
        }
        log.info("action=createCart, userIdentifier={}, message=\"장바구니 생성 요청 완료\"", userIdentifier);
        return null; //회원장바구니 생성의 경우, 회원api에서 이 메서드 호출하기만 하면 됨. 반환값으로 하는것 없다
    }

    @GetMapping
    public OrderBookInfoListDto getCartItems(@RequestHeader(value = "X-USER-ID", required = false) Long userId,
                                             @RequestParam(required = false) Long guestId) {
        OrderBookInfoListDto result;
        if (Objects.nonNull(userId)) {
            result = cartService.getCartItemInfos(userId);
        } else {
            result = cartService.getCartItemInfos(guestId);
        }
        log.info("action=getCartItems, userId={}, guestId={}, message=\"장바구니 품목 조회 요청 완료\"", userId, guestId);
        return result;
    }

    @PostMapping("/books")
    public void addProductToCart(@RequestHeader(value = "X-USER-ID", required = false) Long userId,
                                 @RequestParam(required = false) Long guestId, @RequestParam Long bookId) {
        if (Objects.nonNull(userId)) {
            cartService.addCartItem(userId, bookId);
        } else {
            cartService.addCartItem(guestId, bookId);
        }
        log.info("action=addProductToCart, userId={}, guestId={}, bookId={}, message=\"장바구니에 상품 추가 요청 완료\"", userId,
                guestId, bookId);
    }

    @PutMapping("/books/{book-id}")
    public void updateCartItemQuantity(@RequestHeader(value = "X-USER-ID", required = false) Long userId,
                                       @RequestParam(required = false) Long guestId,
                                       @PathVariable(name = "book-id") Long bookId,
                                       @RequestParam int quantity) {
        if (Objects.nonNull(userId)) {
            cartService.updateItemQuantity(userId, bookId, quantity);
        } else {
            cartService.updateItemQuantity(guestId, bookId, quantity);
        }
        log.info(
                "action=updateCartItemQuantity, userId={}, guestId={}, bookId={}, quantity={}, message=\"장바구니 품목 수량 업데이트 요청 완료\"",
                userId, guestId, bookId, quantity);
    }

    @DeleteMapping("/books/{book-id}")
    public void removeProductFromCart(@RequestHeader(value = "X-USER-ID", required = false) Long userId,
                                      @RequestParam(required = false) Long guestId,
                                      @PathVariable(name = "book-id") Long bookId) {
        if (Objects.nonNull(userId)) {
            cartService.removeProductFromCart(userId, bookId);
        } else {
            cartService.removeProductFromCart(guestId, bookId);
        }
        log.info("action=removeProductFromCart, userId={}, guestId={}, bookId={}, message=\"장바구니에서 상품 제거 요청 완료\"",
                userId, guestId, bookId);
    }
}