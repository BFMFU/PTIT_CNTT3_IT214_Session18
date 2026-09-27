package ra.edu.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ra.edu.api.dto.AccountTypeRequest;
import ra.edu.api.dto.ApiResponse;
import ra.edu.api.entity.AccountType;
import ra.edu.api.service.AccountTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<List<AccountType>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(accountTypeService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER')")
    public ResponseEntity<ApiResponse<AccountType>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(accountTypeService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountType>> create(@Valid @RequestBody AccountTypeRequest request) {
        AccountType at = accountTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account type created", at));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AccountType>> update(@PathVariable Long id,
                                                            @Valid @RequestBody AccountTypeRequest request) {
        AccountType updated = accountTypeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Account type updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        accountTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Account type deleted", null));
    }
}
