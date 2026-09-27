package ra.edu.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class SagaEvents {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransferInitiateEvent {
        private String sagaId;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DebitCompletedEvent {
        private String sagaId;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DebitFailedEvent {
        private String sagaId;
        private Long fromAccountId;
        private BigDecimal amount;
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreditCompletedEvent {
        private String sagaId;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreditFailedEvent {
        private String sagaId;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CompensateDebitEvent {
        private String sagaId;
        private Long fromAccountId;
        private BigDecimal amount;
        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class NotificationEvent {
        private String sagaId;
        private Long customerId;
        private String eventType;  // TRANSFER_SUCCESS, TRANSFER_FAILED
        private String message;
        private BigDecimal amount;
    }
}
