package ra.edu.api.kafka;

public final class KafkaTopics {
    public static final String SAGA_TRANSFER_INITIATE = "saga.transfer.initiate";
    public static final String SAGA_DEBIT_COMPLETED = "saga.debit.completed";
    public static final String SAGA_DEBIT_FAILED = "saga.debit.failed";
    public static final String SAGA_CREDIT_COMPLETED = "saga.credit.completed";
    public static final String SAGA_CREDIT_FAILED = "saga.credit.failed";
    public static final String SAGA_COMPENSATE_DEBIT = "saga.compensate.debit";
    public static final String NOTIFICATION_TRANSFER_SUCCESS = "notification.transfer.success";
    public static final String NOTIFICATION_TRANSFER_FAILED = "notification.transfer.failed";
    private KafkaTopics() {}
}
