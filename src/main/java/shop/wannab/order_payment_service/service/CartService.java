package shop.wannab.order_payment_service.service;

import static shop.wannab.order_payment_service.constants.Constants.GUEST_CART_TTL;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.client.BookClient;
import shop.wannab.order_payment_service.entity.dto.CartItem;
import shop.wannab.order_payment_service.entity.dto.GuestCartCookieDto;
import shop.wannab.order_payment_service.entity.dto.OrderBookInfoListDto;
import shop.wannab.order_payment_service.entity.dto.OrderItemListDto;
import shop.wannab.order_payment_service.exception.OrderPaymentErrorCode;
import shop.wannab.order_payment_service.exception.OrderPaymentServiceException;
import shop.wannab.order_payment_service.repository.CartRepository;
import shop.wannab.order_payment_service.scheduler.CartBackupScheduler;

@Service
@RequiredArgsConstructor
public class CartService {
    private final BookClient bookClient;
    private final CartRepository cartRepository;
    private final CartBackupScheduler cartBackupScheduler;

    @Transactional(readOnly = true)
    public OrderBookInfoListDto getCartItemInfos(Long userIdentifier) {
        if (Objects.isNull(userIdentifier)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.CART_BAD_REQUEST);
        }
        List<CartItem> cartItems = cartRepository.getCartItems(userIdentifier);
        return bookClient.getOrderBookInfos(new OrderItemListDto(cartItems));
    }

    @Transactional
    public void addCartItem(Long userIdentifier, long bookId) {
        if (Objects.isNull(userIdentifier)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.CART_BAD_REQUEST);
        } else if (bookId < 0) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.WRONG_BOOK_ID);
        }
        cartRepository.addItemToCart(userIdentifier, bookId);
        cartBackupScheduler.onCartChanged(userIdentifier);
    }

    @Transactional
    public void updateItemQuantity(Long userIdentifier, long bookId, int quantity) {
        if (Objects.isNull(userIdentifier)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.CART_BAD_REQUEST);
        } else if (bookId < 0) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.WRONG_BOOK_ID);
        }
        cartRepository.updateItemQuantity(userIdentifier, bookId, quantity);
        cartBackupScheduler.onCartChanged(userIdentifier);
    }

    @Transactional
    public void removeProductFromCart(Long userIdentifier, long bookId) {
        if (Objects.isNull(userIdentifier)) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.CART_BAD_REQUEST);
        } else if (bookId < 0) {
            throw new OrderPaymentServiceException(OrderPaymentErrorCode.WRONG_BOOK_ID);
        }
        cartRepository.removeItemFromCart(userIdentifier, bookId);
        cartBackupScheduler.onCartChanged(userIdentifier);
    }

    public GuestCartCookieDto createCart(Long userIdentifier) {
        if (Objects.isNull(userIdentifier)) {
            long guestId = createGuestId();
            cartRepository.createCart(guestId);
            return new GuestCartCookieDto(guestId, 60 * 60 * GUEST_CART_TTL);
        }
        cartRepository.createCart(userIdentifier);
        return null;
    }

    private long createGuestId() {
        Random random = new Random();
        return -random.nextLong(9000000) - 1000000;
    }

}
