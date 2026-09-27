package ra.edu.api.kafka;

/**
 * Kafka topic constants for Saga choreography
 */
public final class KafkaTopics {
    // Saga Transfer topics
    public static final String SAGA_TRANSFER_INITIATE = "saga.transfer.initiate";
    public static final String SAGA_DEBIT_COMPLETED = "saga.debit.completed";
    public static final String SAGA_DEBIT_FAILED = "saga.debit.failed";
    public static final String SAGA_CREDIT_COMPLETED = "saga.credit.completed";
    public static final String SAGA_CREDIT_FAILED = "saga.credit.failed";
    public static final String SAGA_COMPENSATE_DEBIT = "saga.compensate.debit";

    // Notification topics
    public static final String NOTIFICATION_TRANSFER_SUCCESS = "notification.transfer.success";
    public static final String NOTIFICATION_TRANSFER_FAILED = "notification.transfer.failed";

    private KafkaTopics() {}
}
