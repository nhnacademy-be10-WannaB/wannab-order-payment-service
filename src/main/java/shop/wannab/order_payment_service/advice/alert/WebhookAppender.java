package shop.wannab.order_payment_service.advice.alert;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.Setter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


@Setter
public class WebhookAppender extends AppenderBase<ILoggingEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String webhookUrl;
    private String minLevel = "ERROR";

    @Override
    public void start() {
        if (this.webhookUrl == null || this.webhookUrl.isEmpty()) {
            addError("Webhook URL not set for WebhookAppender.");
        }
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted() || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        Level currentLevel = eventObject.getLevel();
        Level configuredMinLevel = Level.valueOf(minLevel.toUpperCase());

        if (currentLevel.isGreaterOrEqual(configuredMinLevel)) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(new URI(webhookUrl));
                post.setHeader("Content-Type", "application/json");

                String appName = "UNKNOWN_SERVICE";
                if (this.context != null && this.context.getProperty("APP_NAME") != null) {
                    appName = this.context.getProperty("APP_NAME");
                }

                String formattedTimestamp = Instant.ofEpochMilli(eventObject.getTimeStamp())
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

                SendMessageRequest sendMessageRequest = SendMessageRequest.errorLogMessage(
                        appName,
                        String.format("메시지: %s\n로거: %s\n시간: %s",
                                eventObject.getFormattedMessage(),
                                eventObject.getLoggerName(),
                                formattedTimestamp),
                        eventObject.getLevel().toString()
                );

                String jsonPayload = objectMapper.writeValueAsString(sendMessageRequest);

                StringEntity entity = new StringEntity(jsonPayload, StandardCharsets.UTF_8);
                post.setEntity(entity);

                httpClient.execute(post).close();
            } catch (Exception e) {
                addError("Failed to send webhook notification for event: " + eventObject.getFormattedMessage(), e);
            }
        }
    }
}

