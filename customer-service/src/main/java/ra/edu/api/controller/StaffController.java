package ra.edu.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ra.edu.api.dto.ApiResponse;
import ra.edu.api.dto.StaffRequest;
import ra.edu.api.entity.Staff;
import ra.edu.api.service.StaffService;

import java.util.List;

@RestController
@RequestMapping("/api/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Staff>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success(staffService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Staff>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Staff>> create(@Valid @RequestBody StaffRequest request) {
        Staff staff = staffService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff created successfully", staff));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Staff>> update(@PathVariable Long id,
                                                      @Valid @RequestBody StaffRequest request) {
        Staff updated = staffService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Staff updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        staffService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Staff deactivated successfully", null));
    }
}
