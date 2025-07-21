package shop.wannab.order_payment_service.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import shop.wannab.order_payment_service.entity.Cart;
import shop.wannab.order_payment_service.entity.CartItemEntity;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.repository.CartRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CartBackupSchedulerTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartBackupScheduler cartBackupScheduler;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        cartBackupScheduler = new CartBackupScheduler(cartRepository);
    }

    @Test
    @DisplayName("redis에서 데이터를 가져와 cart로 변환 후 저장")
    void testWriteBackCart_savesCart() throws Exception {
        // given
        Long userId = 1L;
        List<CartItem> redisItems = List.of(
                new CartItem(101L, 2),
                new CartItem(102L, 3)
        );

        when(cartRepository.getCartItems(userId)).thenReturn(redisItems);

        var method = CartBackupScheduler.class.getDeclaredMethod("writeBackCart", Long.class);
        method.setAccessible(true);
        method.invoke(cartBackupScheduler, userId);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(1)).save(cartCaptor.capture());

        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getUserId()).isEqualTo(userId);
        assertThat(savedCart.getItems())
                .extracting(CartItemEntity::getBookId)
                .containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    @DisplayName("onCartChanged 중복 호출 시 5분 후 실행")
    void testOnCartChanged_schedulesOnce() throws Exception {
        Long userId = 2L;

        when(cartRepository.getCartItems(userId)).thenReturn(List.of());

        cartBackupScheduler.onCartChanged(userId);
        cartBackupScheduler.onCartChanged(userId);

        TimeUnit.MILLISECONDS.sleep(100);
        assertThat(Thread.getAllStackTraces().keySet()).anyMatch(thread ->
                thread.getName().contains("pool-") && thread.isAlive());
    }
}