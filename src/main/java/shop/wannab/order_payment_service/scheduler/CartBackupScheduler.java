package shop.wannab.order_payment_service.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shop.wannab.order_payment_service.entity.Cart;
import shop.wannab.order_payment_service.entity.CartItemEntity;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Component
@RequiredArgsConstructor
public class CartBackupScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, Boolean> userScheduledMap = new ConcurrentHashMap<>();
    private final CartRepository cartRepository;

    public void onCartChanged(Long userid) {
        if (userScheduledMap.putIfAbsent(userid, true) != null) {
            return;
        }

        scheduler.schedule(()-> {
            try {
                writeBackCart(userid);
            } finally {
                userScheduledMap.remove(userid);
            }
        }, 5, TimeUnit.MINUTES);
    }

    private void writeBackCart(Long userId) {
        List<CartItem> redisCartItems = cartRepository.getCartItems(userId);
        if (redisCartItems.isEmpty()) {
            return;
        }
        Cart cart = new Cart();
        cart.setUserId(userId);

        for (CartItem cartItem : redisCartItems) {
            CartItemEntity entity = new CartItemEntity(cartItem.getBookId(), cartItem.getQuantity());
            cart.addItem(entity);
        }

        cartRepository.save(cart);
    }

}
