package shop.wannab.order_payment_service.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import shop.wannab.order_payment_service.entity.dto.OrderSubmitDto;

import java.util.Arrays;

@Component
public class OrderSubmitDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return OrderSubmitDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OrderSubmitDto dto = (OrderSubmitDto) target;

        boolean isUser = !isBlankOrNull(dto.getUserId());

        if (isUser) {
            if (!allNotBlank(dto.getUserId(), dto.getAddress())) {
                errors.reject("user.fields.missing", "회원 정보가 누락되었습니다.");
            }

            if (!allBlank(dto.getGuestName(), dto.getGuestPassword(), dto.getGuestEmail(),
                          dto.getGuestPhoneNumber(), dto.getGuestAddress(), dto.getGuestDetailAddress())) {
                errors.reject("guest.fields.should.be.empty", "회원 주문일 때 게스트 필드는 비워야 합니다.");
            }

        } else { // 게스트 주문
            if (!allNotBlank(dto.getGuestName(), dto.getGuestPassword(), dto.getGuestEmail(),
                             dto.getGuestPhoneNumber(), dto.getGuestAddress(), dto.getGuestDetailAddress())) {
                errors.reject("guest.fields.missing", "게스트 정보가 누락되었습니다.");
            }

            if (!allBlank(dto.getUserId(), dto.getAddress())) {
                errors.reject("user.fields.should.be.empty", "게스트 주문일 때 회원 필드는 비워야 합니다.");
            }
        }
    }

    private boolean isBlankOrNull(String s) {
        return s == null || s.isBlank();
    }

    private boolean allBlank(String... values) {
        return Arrays.stream(values).allMatch(this::isBlankOrNull);
    }

    private boolean allNotBlank(String... values) {
        return Arrays.stream(values).allMatch(s -> !isBlankOrNull(s));
    }
}

