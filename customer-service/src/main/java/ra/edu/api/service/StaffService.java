package ra.edu.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.api.dto.StaffRequest;
import ra.edu.api.entity.Staff;
import ra.edu.api.exception.DuplicateResourceException;
import ra.edu.api.exception.ResourceNotFoundException;
import ra.edu.api.repository.StaffRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;

    public List<Staff> findAll() {
        return staffRepository.findAll();
    }

    @Cacheable(value = "staffs", key = "#id")
    public Staff findById(Long id) {
        log.info("Cache MISS - loading staff {} from DB", id);
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + id));
    }

    @Transactional
    public Staff create(StaffRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        Staff staff = Staff.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .position(request.getPosition())
                .isActive(true)
                .build();
        return staffRepository.save(staff);
    }

    @Transactional
    @CachePut(value = "staffs", key = "#id")
    public Staff update(Long id, StaffRequest request) {
        Staff staff = findById(id);
        if (!staff.getEmail().equals(request.getEmail()) &&
                staffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }
        staff.setFullName(request.getFullName());
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setDepartment(request.getDepartment());
        staff.setPosition(request.getPosition());
        return staffRepository.save(staff);
    }

    @Transactional
    @CacheEvict(value = "staffs", key = "#id")
    public void delete(Long id) {
        Staff staff = findById(id);
        staff.setActive(false);  // Soft delete
        staffRepository.save(staff);
        log.info("Staff soft-deleted: {}", id);
    }
}
