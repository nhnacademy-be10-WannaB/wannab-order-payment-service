package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import shop.wannab.order_payment_service.entity.CartItem;
import shop.wannab.order_payment_service.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("/api/cart")
@RequiredArgsConstructor
public class CartController {

    public static final long CART_DUMMY_KEY = -1;
    public static final long CART_DUMMY_VAL = -1;

    private final RedisTemplate<String, Object> redisTemplate;
    private final CartService cartService;

    @PostMapping
    public void createCart(@RequestBody String userIdentifier) {
        String cartKey = "cart:" + userIdentifier;
        //비회원일 경우 ttl 걸어줘야함
        redisTemplate.opsForHash().put(cartKey, CART_DUMMY_KEY, CART_DUMMY_VAL);
    }

    @GetMapping
    public List<CartItem> getCartItems(@RequestHeader("X-User-Id") long userId) {//String userIdentifier
        String cartKey = "cart:" + userId;
        return cartService.getCartItems(cartKey);
    }

    @PostMapping("/books")
    public List<CartItem> addProductToCart(@RequestHeader("X-User-Id") Long userId, @RequestParam long bookId) {
        String cartKey = "cart:" + userId;
        redisTemplate.opsForHash().increment(cartKey, bookId, 1);
        return cartService.getCartItems(cartKey);
    }

    @PutMapping("/books/{bookId}")
    public List<CartItem> updateCartItemQuantity(@RequestHeader("X-User-Id") Long userId, @PathVariable long bookId, @RequestParam int quantity) {
        String cartKey = "cart:" + userId;
        redisTemplate.opsForHash().put(cartKey, bookId, quantity);
        return cartService.getCartItems(cartKey);
    }



}
