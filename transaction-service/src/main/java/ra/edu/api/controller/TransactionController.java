package ra.edu.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ra.edu.api.dto.ApiResponse;
import ra.edu.api.dto.TransferRequest;
import ra.edu.api.entity.Transaction;
import ra.edu.api.security.JwtUtil;
import ra.edu.api.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    /**
     * CUSTOMER initiates a transfer - Saga starts here
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Transaction>> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long customerId = jwtUtil.extractCustomerId(token);
        Transaction tx = transactionService.initiateTransfer(request, customerId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Transfer initiated. Processing asynchronously. Saga ID: " + tx.getSagaId(), tx));
    }

    /**
     * TELLER or ADMIN views today's transactions
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTodayTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findTodayTransactions()));
    }

    /**
     * ADMIN views all transactions
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findAll()));
    }

    /**
     * CUSTOMER views their own transactions
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMyTransactions(
            @RequestParam List<Long> accountIds) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.findMyTransactions(null, accountIds)));
    }

    /**
     * Get transaction details by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<Transaction>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.findById(id)));
    }
}
