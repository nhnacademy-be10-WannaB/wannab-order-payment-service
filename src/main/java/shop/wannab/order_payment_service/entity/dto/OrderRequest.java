package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;


//주문서에서 입력받을 데이터 DTO
@Data
public class OrderRequest {

    //공통
    @NotEmpty
    private List<@Valid OrderBookRequest> bookList;

    @Future(message = "희망 배송일은 오늘 이후여야합니다")
    private LocalDate deliveryWant; //희망배송날짜




    //회원
    private Long couponId; //사용할 쿠폰
    private int usedPoint; //사용할 포인트
    private Long addressId; //배송할 주소


    //비회원
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 번호형식이 아닙니다 ex: xxx-xxxx-xxxx")
    private String phone;

    @Pattern(regexp = "^\\d{4,6}$", message = "비밀번호는 숫자 4~6자 사이여야 합니다")
    private String password;

    private String address;





}