package ra.edu.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type ID is required")
    private Long accountTypeId;

    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    private Long createdByStaffId;
    private String status;  // For update operations
}
