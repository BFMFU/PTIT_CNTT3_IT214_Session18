package ra.edu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.api.entity.Staff;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByEmail(String email);
    Optional<Staff> findByEmail(String email);
}
