package ra.edu.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ra.edu.api.dto.AccountRequest;
import ra.edu.api.dto.ApiResponse;
import ra.edu.api.entity.Account;
import ra.edu.api.security.JwtUtil;
import ra.edu.api.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<List<Account>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(accountService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Account>> findById(@PathVariable Long id,
                                                          @RequestHeader("Authorization") String authHeader) {
        Account account = accountService.findById(id);
        // CUSTOMER can only access their own accounts
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if ("CUSTOMER".equals(role)) {
            Long customerId = jwtUtil.extractCustomerId(token);
            if (!account.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Access denied: not your account"));
            }
        }
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Account>>> findMy(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Long customerId = jwtUtil.extractCustomerId(token);
        return ResponseEntity.ok(ApiResponse.success(accountService.findByCustomerId(customerId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<Account>> create(@Valid @RequestBody AccountRequest request) {
        Account account = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Account>> update(@PathVariable Long id,
                                                        @Valid @RequestBody AccountRequest request) {
        Account updated = accountService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Account updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable Long id) {
        accountService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Account closed", null));
    }
}
