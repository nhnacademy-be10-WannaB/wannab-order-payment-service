package shop.wannab.order_payment_service.service.Impl;

import io.micrometer.common.util.StringUtils;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.Guest;
import shop.wannab.order_payment_service.entity.Order;
import shop.wannab.order_payment_service.entity.OrderBook;
import shop.wannab.order_payment_service.entity.OrderStatus;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.OrderBookRequest;
import shop.wannab.order_payment_service.entity.dto.OrderRequest;
import shop.wannab.order_payment_service.entity.dto.OrderResponse;
import shop.wannab.order_payment_service.hardcoding.Address;
import shop.wannab.order_payment_service.hardcoding.AddressRepository;
import shop.wannab.order_payment_service.hardcoding.Book;
import shop.wannab.order_payment_service.hardcoding.BookRepository;
import shop.wannab.order_payment_service.hardcoding.User;
import shop.wannab.order_payment_service.hardcoding.UserRepository;
import shop.wannab.order_payment_service.repository.GuestRepository;
import shop.wannab.order_payment_service.repository.OrderBookRepository;
import shop.wannab.order_payment_service.repository.OrderReopsitory;
import shop.wannab.order_payment_service.repository.WrappingPaperRepository;
import shop.wannab.order_payment_service.service.DeliveryPolicyService;
import shop.wannab.order_payment_service.service.OrderService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderReopsitory orderReopsitory;
    private final WrappingPaperRepository wrappingPaperRepository;
    private final GuestRepository guestRepository;
    private final OrderBookRepository orderBookRepository;
    private final DeliveryPolicyService deliveryPolicyService;

    private final EmailService emailService;

    //TODO: API 연동시 수정필요
//    private final BookFeignClient bookFeignClient;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;





    // 주문생성
    @Transactional
    @Override
    public OrderResponse createOrder(OrderRequest request, Long userId) {
        Order order = new Order();
        order.setOrderAt(ZonedDateTime.now());
        order.setDeliveryWant(request.getDeliveryWant());
        order.setOrderStatus(OrderStatus.PENDING);
        order = orderReopsitory.save(order);



        //TODO: API 연동시 수정필요
        User user = null;
        Address address = null;
        if (userId > 0) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저아이디 x"));
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("주소아이디 x"));
        }



        // 도서 합계
        int totalBookPrice = 0;
        // 선택한 포장지 합계
        int totalWrappingPrice = 0;

        // 할인 총액 (회원만 사용)
        int totalDiscount = 0;


        //장바구니에서 선택한 도서에 대한 로직처리
        for(OrderBookRequest req : request.getBookList()){
            Book book = bookRepository.findById(req.getBookId()).orElseThrow(()-> new IllegalArgumentException("도서를 찾을 수 없습니다."));
//            BookDto book = bookFeignClient.getBook(req.getBookId());

            WrappingPaper wp = null;
            int wpPrice = 0;
            if(req.getSelectedWrappingId() != null){
                wp = wrappingPaperRepository.findById(req.getSelectedWrappingId()).orElseThrow(()-> new IllegalArgumentException("포장지를 찾을수 없습니다."));
                wpPrice = wp.getPrice();
            }

            OrderBook orderBook = new OrderBook();
            //TODO: API 연동시 수정필요
            orderBook.setOrder(order);
//            orderBook.setBookId(book.getId());
//            orderBook.setBookPrice(book.getPrice());
            orderBook.setBook(book);
            orderBook.setBookPrice(book.getPrice());

            orderBook.setQuantity(req.getQuantity());
            orderBook.setWrappingPaper(wp);
            orderBook.setWrappingPrice(wpPrice);
            //회원일때 쿠폰적용

            orderBookRepository.save(orderBook);

            totalBookPrice += book.getPrice() * req.getQuantity(); //도서가격누적
            totalWrappingPrice += wpPrice; //포장가격누적 (수량당 포장지가격을 먹일것인지?)
        }

        //배송정책 적용
        DeliveryPolicy deliveryPolicy = deliveryPolicyService.findApplicablePolicy(totalBookPrice);

        order.setTotalBookPrice(totalBookPrice);
        order.setTotalWrappingPrice(totalWrappingPrice);
        order.setDeliveryFee(deliveryPolicy.getFee());
        totalDiscount += request.getUsedPoint();

        //비회원 회원 구분로직
        if(userId > 0){ //회원일때

//            order.setUserId(userId);
//            order.setAddressId(request.getAddressId());


            order.setUser(user); //TODO: API 연동시 수정필요
            order.setTotalDiscount(totalDiscount); //TODO: + 쿠폰할인 추가

        }else { //비회원일때
            order.setTotalDiscount(0); //비회원은 쿠폰,포인트 사용불가
        }

        orderReopsitory.save(order);

        if(userId < 0){
            if (StringUtils.isBlank(request.getName()) ||
                    StringUtils.isBlank(request.getEmail()) ||
                    StringUtils.isBlank(request.getPhone()) ||
                    StringUtils.isBlank(request.getPassword()) ||
                    StringUtils.isBlank(request.getAddress())) {
                throw new IllegalArgumentException("비회원 주문 시 이름, 이메일, 비밀번호, 주소, 전화번호는 필수입니다.");
            }
            //비회원 정보저장
            Guest guest = new Guest();
            guest.setName(request.getName());
            guest.setEmail(request.getEmail());
            guest.setPhone(request.getPhone());
            guest.setAddress(request.getAddress());
            guest.setPassword(request.getPassword()); //인코딩처리 고려
            guest.setOrder(order);

            guestRepository.save(guest);

            //이메일 발송시스템
            String emailText = String.format("""
            %s님, 주문이 완료되었습니다.
            
            ▷ 주문번호: %d
            ▷ 결제금액: %,d원
            ▷ 배송주소: %s
            ▷ 배송희망일: %s
            
            감사합니다.
        """, guest.getName(), order.getId(), order.getTotalPrice(), guest.getAddress(), order.getDeliveryWant().toLocalDate());

            emailService.sendOrderEmail(guest.getEmail(), "[WannaB] 비회원 주문확인서", emailText);


        }else {

            // TODO: 회원 주문 시 포인트 차감,쿠폰 등의 로직 추가

            String emailText = String.format("""
                    %s 회원님, 주문이 완료되었습니다.
                    
                    ▷ 주문번호: %d
                    ▷ 할인금액: %,d원
                    ▷ 결제금액: %,d원
                    ▷ 배송주소: %s
                    ▷ 배송희망일: %s
                    
                    감사합니다.
                    """, user.getName(), order.getId(), order.getTotalDiscount(), order.getTotalPrice(), address.getAddress(), order.getDeliveryWant().toLocalDate());

            emailService.sendOrderEmail(user.getEmail(), "[WannaB] 회원 주문확인서", emailText);

        }





        return new OrderResponse(order.getId(), order.getOrderAt(), order.getTotalPrice());
    }
}
