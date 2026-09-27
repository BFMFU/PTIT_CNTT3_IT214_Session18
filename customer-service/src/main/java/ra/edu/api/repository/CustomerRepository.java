package ra.edu.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.edu.api.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    boolean existsByIdCard(String idCard);
    Optional<Customer> findByEmail(String email);
}
