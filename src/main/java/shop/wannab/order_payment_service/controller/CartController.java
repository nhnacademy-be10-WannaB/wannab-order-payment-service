package shop.wannab.order_payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.entity.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("/api")
@RequiredArgsConstructor
public class CartController {

    public static final long CART_DUMMY_KEY = -1;
    public static final long CART_DUMMY_VAL = -1;

    private final RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/cart")
    public void createCart(@RequestBody String userIdentifier) {
        String key = "cart:" + userIdentifier;
        //비회원일 경우 ttl 걸어줘야함
        redisTemplate.opsForHash().put(key, CART_DUMMY_KEY, CART_DUMMY_VAL);
    }

    @GetMapping("/cart")
    public List<CartItem> getCartItems(@RequestBody String userIdentifier) {//String userIdentifier
        String cartKey = "cart:" + userIdentifier;
        Map<Long, Integer> cartBooks = redisTemplate.<Long, Integer>opsForHash().entries(cartKey); //key: bookId, value: quantity

        List<CartItem> cartItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cartBooks.entrySet()) {
            CartItem item = new CartItem(entry.getKey(), entry.getValue());
            cartItems.add(item);
        }
        return cartItems;
    }

}
