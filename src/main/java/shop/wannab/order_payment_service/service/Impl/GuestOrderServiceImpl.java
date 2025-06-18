package shop.wannab.order_payment_service.service.Impl;

import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.order_payment_service.hardcoding.Book;
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.Guest;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderBook;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.GuestOrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderBookRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;
import shop.wannab.order_payment_service.hardcoding.BookRepository;
import shop.wannab.order_payment_service.repository.GuestRepository;
import shop.wannab.order_payment_service.repository.OrderBookRepository;
import shop.wannab.order_payment_service.repository.OrderReopsitory;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
import shop.wannab.order_payment_service.service.DeliveryPolicyService;
import shop.wannab.order_payment_service.service.GuestOrderService;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService {

    private final OrderReopsitory orderReopsitory;
    private final WrappingPaperRepository wrappingPaperRepository;
    private final GuestRepository guestRepository;
    private final OrderBookRepository orderBookRepository;
    private final DeliveryPolicyService deliveryPolicyService;

    //TODO: API 연동시 수정필요
    private final BookRepository bookRepository;



    //비회원 주문생성
    @Transactional
    @Override
    public OrderResponse createGuestOrder(GuestOrderRequest request) {
        Order guestOrder = new Order();
        guestOrder.setOrderAt(ZonedDateTime.now());
        guestOrder.setDeliveryWant(request.getDeliveryWant());
        guestOrder.setOrderStatus(OrderStatus.PENDING);
        guestOrder = orderReopsitory.save(guestOrder);

        // 도서 합계
        int totalBookPrice = 0;
        // 선택한 포장지 합계
        int totalWrappingPrice = 0;


        //장바구니에서 선택한 도서에 대한 로직처리
        for(OrderBookRequest req : request.getBookList()){
            Book book = bookRepository.findById(req.getBookId()).orElseThrow(()-> new IllegalArgumentException("도서를 찾을 수 없습니다."));

            WrappingPaper wp = null;
            int wpPrice = 0;
            if(req.getSelectedWrappingId() != null){
                wp = wrappingPaperRepository.findById(req.getSelectedWrappingId()).orElseThrow(()-> new IllegalArgumentException("포장지를 찾을수 없습니다."));
                wpPrice = wp.getPrice();
            }

            OrderBook orderBook = new OrderBook();
            orderBook.setOrder(guestOrder);
            orderBook.setBook(book);
            orderBook.setBookPrice(book.getPrice());
            orderBook.setQuantity(req.getQuantity());
            orderBook.setWrappingPaper(wp);
            orderBook.setWrappingPrice(wpPrice);

            orderBookRepository.save(orderBook);

            totalBookPrice += book.getPrice() * req.getQuantity(); //도서가격누적
            totalWrappingPrice += wpPrice; //포장가격누적 (수량당 포장지가격을 먹일것인지?)
        }

        //배송정책 적용
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);

        guestOrder.setTotalBookPrice(totalBookPrice);
        guestOrder.setTotalWrappingPrice(totalWrappingPrice);
        guestOrder.setDeliveryFee(deliveryPolicy.getFee());
        guestOrder.setTotalDiscount(0); //비회원은 쿠폰,포인트 사용불가

        orderReopsitory.save(guestOrder);

        //비회원 정보저장
        Guest guest = new Guest();
        guest.setName(request.getName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setAddress(request.getAddress());
        guest.setPassword(request.getPassword()); //인코딩처리 고려
        guest.setOrder(guestOrder);

        guestRepository.save(guest);

        return new OrderResponse(guestOrder.getId(), guestOrder.getOrderAt(), guestOrder.getTotalPrice());
    }
}
