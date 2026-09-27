package ra.edu.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class NotificationEvents {
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NotificationEvent {
        private String sagaId;
        private Long customerId;
        private String eventType;  // TRANSFER_SUCCESS, TRANSFER_FAILED
        private String message;
        private BigDecimal amount;
    }
}
