package ra.edu.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountTypeRequest {
    @NotBlank(message = "Type name is required")
    @Size(max = 100)
    private String typeName;

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Min balance cannot be negative")
    private BigDecimal minBalance = BigDecimal.ZERO;
}
