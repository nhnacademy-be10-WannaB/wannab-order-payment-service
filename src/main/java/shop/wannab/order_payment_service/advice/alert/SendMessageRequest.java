package shop.wannab.order_payment_service.advice.alert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private String botName;
    private String text;
    private List<Attachment> attachments;

    public static SendMessageRequest errorLogMessage(String serviceName, String logMessage, String logLevel) {
        return new SendMessageRequest(
                serviceName + " 에러 로그 봇",
                null,
                List.of(new SendMessageRequest.Attachment(
                        "[" + serviceName + "] " + "OCCURRED " + logLevel + "!",
                        String.format("%s", logMessage),
                        null,
                        "https://static.dooray.com/static_images/dooray-bot.png",
                        "red"
                ))
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String title;
        private String text;
        private String titleLink;
        private String botIconImage;
        private String color;

    }
}

