package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;


//비회원 주문서에서 입력받을 데이터 DTO
@Data
public class GuestOrderRequest {

    @NotEmpty
    private List<@Valid OrderBookRequest> bookList;

    @NotBlank(message = "이름을 입력해주세요")
    private String name;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "휴대폰 번호를 입력해주세요")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 번호형식이 아닙니다 ex: xxx-xxxx-xxxx")
    private String phone;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(regexp = "^\\d{4,6}$", message = "비밀번호는 숫자 4~6자 사이여야 합니다")
    private String password;

    @NotBlank(message = "주소를 입력해주세요")
    private String address;

    @Future(message = "희망 배송일은 오늘 이후여야합니다")
    private ZonedDateTime deliveryWant; //희망배송날짜



}
