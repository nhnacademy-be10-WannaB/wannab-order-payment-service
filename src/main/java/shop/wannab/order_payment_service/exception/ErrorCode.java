package shop.wannab.order_payment_service.exception;

import java.io.Serializable;

public interface ErrorCode extends Serializable {
    int getStatus();

    String getCode();

    String getMessage();
}
