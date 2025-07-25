package shop.wannab.order_payment_service.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.*;


@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubmitDto {
    @NotEmpty
    List<BookOrderSubmitDto> bookOrderSubmitDtos = new ArrayList<>();
    private String userId;
    private Integer usedPoints;
    @Future(message = "희망 배송일은 오늘 이후여야합니다")
    private LocalDate deliveryRequestAt;
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "올바른 번호형식이 아닙니다 ex: xxx-xxxx-xxxx")
    private String recipientPhoneNumber;
    @NotBlank(message = "수령인 이름은 필수입니다.")
    private String recipientName;
    @NotBlank(message = "수령인 주소는 필수입니다.")
    private String recipientAddress;
    @Pattern(regexp = "^\\d{4,6}$", message = "비밀번호는 숫자 4~6자 사이여야 합니다")
    private String guestPassword;
    private Long appliedOrderCouponId;

}